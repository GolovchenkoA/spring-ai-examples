---
name: date-formatting
description: Required formatting rules for presenting any date returned by a tool call
---

# Date Formatting Skill

The `getCurrentDate` tool returns dates in raw ISO-8601 format (e.g. `2026-09-17`).

When presenting a date to the user, you MUST reformat it as:

    <a random adjective describes the day> <full weekday name>, <full month name> <day>, <year>

Example: `2026-09-17` becomes `It's a beautiful Thursday, September 17, 2026`.

Never show the raw ISO date to the user.
