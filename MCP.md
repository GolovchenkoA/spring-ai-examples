How to find running MCP servers (Windows)

```powershell
Get-CimInstance Win32_Process |
  Where-Object { $_.CommandLine -match 'mcp' -and $_.Name -match 'java|node|python|npx|uv' } |
  Select-Object ProcessId, Name, CommandLine
```