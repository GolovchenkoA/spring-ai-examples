I have an excel form which should be filled out.  But I don't want user to fill the form themselves. Instead I want use AI models to ask them to provide info to fill the form. For that I need some process that can "scan" any excel document.  Understand  what fields in the document. Make a list of those fields (left-right, top-down). Ask user to provide information and then put those values in the excel document.
What should I use to effectively implement this task

## AI-assisted Excel form filling: what to use

Split the job into a deterministic part (reading and writing the file) and an AI part (understanding the layout and talking to the user). Don't let the model touch the file itself.

### Recommended stack (Spring Boot)

| Job | Tool |
|---|---|
| Read/write .xlsx and preserve formatting | **Apache POI** (XSSF) |
| Understand which cells are fields | Claude, with structured (JSON) output |
| Conversation with the customer | Spring AI `ChatClient` and Claude, with a server-side state machine |
| Optional: see the layout visually | LibreOffice headless (xlsx -> PDF -> PNG), sent to Claude as an image |

In Python you'd use `openpyxl`, and in .NET `ClosedXML` or `EPPlus`.

### Pipeline

**1. Extract the raw structure (deterministic, POI).** Walk the sheet row by row, left to right, and collect for each non-empty or "interesting" cell:

- address (`B7`), text value, and cell type
- merged regions (`sheet.getMergedRegions()`)
- data validations (dropdown lists, allowed dates or numbers). This is very valuable because it gives you the allowed values for free.
- number format (date, currency, percent), whether the cell is locked or unlocked, borders and fill, comments, and named ranges

Serialize this compactly, for example as JSON or a markdown grid, so the token cost stays low.

**2. Let the model build a form schema (one time per template).** Send the extracted structure, and optionally a rendered image of the sheet, and ask for a structured result:

```json
{
  "fields": [
    { "id": "f1", "label": "Company name", "cell": "C4", "type": "text", "required": true },
    { "id": "f2", "label": "Country", "cell": "C5", "type": "choice", "options": ["PL","DE","FR"] },
    { "id": "f3", "label": "Start date", "cell": "C6", "type": "date", "format": "dd.MM.yyyy" }
  ]
}
```

With Spring AI you can map this directly to a Java record via `.call().entity(FormSchema.class)`. Ask the model to order fields left-right, top-down, and to include `question` text if you want friendly phrasing. **Cache this schema per template**, and ideally have a human review it once. Then you don't pay for or risk re-analysis on every customer.

Useful heuristics to give the model: an input cell is usually empty and adjacent to a label (to its left or above), often has borders, fill, or validation, and is unlocked when the sheet is protected.

**3. Run the conversation from the schema.** Your server holds the state (which fields are filled, which is next). Two workable designs:

- **Server-driven:** your code walks the field list, and the model only phrases each question and extracts and normalizes the answer ("next Friday" -> a date). This is the most predictable.
- **Tool-driven:** give Claude a tool like `set_field(fieldId, value)` and the schema, and let it converse freely, asking things in any order and accepting several answers in one message. It's more natural but needs more validation.

In both designs, **validate on the server** against the schema (type, dropdown membership, required) before accepting a value. Keep all of this per customer, so conversations and filled values are never shared between customers.

**4. Write the values (deterministic, POI).**

```java
try (var wb = WorkbookFactory.create(templateStream)) {
    Sheet sheet = wb.getSheetAt(0);
    for (var f : filledFields) {
        CellReference ref = new CellReference(f.cell());
        Row row = Optional.ofNullable(sheet.getRow(ref.getRow()))
                          .orElseGet(() -> sheet.createRow(ref.getRow()));
        Cell cell = Optional.ofNullable(row.getCell(ref.getCol()))
                            .orElseGet(() -> row.createCell(ref.getCol()));
        switch (f.type()) {
            case "number" -> cell.setCellValue(Double.parseDouble(f.value()));
            case "date"   -> cell.setCellValue(LocalDate.parse(f.value())); // keep the cell's existing style
            default       -> cell.setCellValue(f.value());
        }
    }
    wb.setForceFormulaRecalculation(true);
    wb.write(outputStream);
}
```

Write to the cell's existing style so the template's formatting is kept. For merged cells, write to the top-left cell.

### Things that will bite you

- **Repeating sections** (e.g. invoice lines, a list of family members): the schema needs a "table" field type with columns and a variable number of rows.
- **Checkboxes and form controls:** POI can't handle ActiveX or legacy form controls well. They often appear as symbols like a checkbox character in cells, or you may need to edit the XML directly.
- **Formulas:** never overwrite formula cells. Mark them read-only in the schema.
- **Multiple sheets, hidden rows and columns, .xlsm macros, and the old .xls format:** decide upfront what you support. Don't silently ignore a hidden sheet.
- **Large or messy sheets:** send only the used range, and chunk by region if needed.
- **Sensitive data:** the template schema contains no customer data, so it's safe to share and cache. Only the filled values are sensitive.
- **Show a preview** of the filled form for the customer to confirm before generating the final file. Models and parsers make mistakes, and forms often have legal weight.

### A practical way to start

Build it in this order:

1. POI extractor
2. Schema generation with Claude on 3 or 4 of your real forms, and check how accurate it is
3. The writer
4. The conversation layer

If schema accuracy on your forms is poor, adding the rendered image alongside the cell data usually improves it noticeably, especially for forms with unusual layouts. If your forms are all variants of a few known templates, skip generic detection and hand-verify one schema per template.