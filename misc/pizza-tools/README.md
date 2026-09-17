# Prompt Engineering — Pizza Order MCP Server

An MCP server exposing a single tool, `pizzaOrderTool`, meant to be connected from
**Claude Desktop** as a custom Connector (a remote MCP server) — no `ANTHROPIC_API_KEY`
needed here, since Claude Desktop's own model is the one calling this tool, not this app.

## What it exposes

`PizzaTools.pizzaOrderTool` — takes a pizza size and a list of ingredients, and
returns a mock order confirmation (order id, total price, estimated delivery time).
It's a deterministic backend tool call, not an LLM call — the parsing/understanding
of a natural-language request happens on Claude Desktop's side; this server just
handles the "place the order" step once Claude has figured out the structured
size/ingredients.

```java
@McpTool(name = "pizzaOrderTool",
        description = "Places a pizza order and returns an order confirmation with price and estimated delivery time.")
public PizzaOrderResponse pizzaOrderTool(String size, List<String> ingredients) { ... }
```

## Running

```bash
./mvnw spring-boot:run
```

Starts a Streamable HTTP MCP server on `http://localhost:8080/mcp`.

## Connecting from Claude Desktop

1. Start this server (`./mvnw spring-boot:run`) and leave it running.
2. In Claude Desktop: **Settings → Connectors → Add custom connector**.
3. Enter a name (e.g. "Pizza Order") and the URL `http://localhost:8080/mcp`.
4. Save, then enable the connector in a conversation. Claude can now call
   `pizzaOrderTool` when you ask it to order a pizza.

Try asking: *"Order me a large pizza with pepperoni, mushrooms, and extra cheese."*

## Configuration

```properties
spring.ai.mcp.server.name=pizza-order-server
spring.ai.mcp.server.version=0.0.1
spring.ai.mcp.server.protocol=STREAMABLE
```

See [model-context-protocol/weather/starter-webmvc-server](../../model-context-protocol/weather/starter-webmvc-server)
for a more elaborate example of this same pattern (`@McpTool`-annotated `@Service`
beans, auto-registered — no manual `ToolCallbackProvider` wiring needed).

## MCP configuration
To be able to connect to the MCP server locally from Claude update claude_desktop_config.json
And add the server using this example:
```
  "mcpServers": {
    "pizza-order-server": {
      "command": "java",
      "args": [
        "-Dspring.ai.mcp.server.stdio=true",
        "-Dspring.main.web-application-type=none",
        "-Dspring.main.banner-mode=off",
        "-Dlogging.pattern.console=",
        "-jar",
        "full-path-to-the-jar/pizza-tools-0.0.1-SNAPSHOT.jar"
      ]
    }
  },
```