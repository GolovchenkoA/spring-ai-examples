# MCP Protocols and Transports

The Model Context Protocol (MCP) defines how a client (for example Claude Desktop or a Spring AI app) talks to an MCP server. Two layers are involved, and they are easy to mix up:

- **Protocol:** the messages the client and server exchange (tool calls, results, notifications). It is the same in every setup.
- **Transport:** the channel that carries those messages. The transport decides where the server can run and who can connect to it.

Spring AI's MCP server offers two transports. The HTTP transport can run in three protocol modes, selected with `spring.ai.mcp.server.protocol`. STDIO is switched on separately with `spring.ai.mcp.server.stdio`.

- **Transports:** STDIO, HTTP
- **HTTP protocol modes:** Streamable HTTP, Stateless, SSE

A server instance runs **one** HTTP protocol mode at a time. To offer several modes at once, run separate instances on different ports.

# Transports

## STDIO

The client launches the server as a local subprocess and exchanges messages over the process's standard input and output.

**Use when:**
- The server runs on the same machine as the client, for example a local tool registered in Claude Desktop's config file.
- You want the simplest setup, with no ports, no network exposure and no authentication.
- Each user or client gets their own private server process.

**Don't use when:**
- The server must be reachable over a network or shared by several clients.
- The server runs remotely or in a container that clients connect to.
- You can't keep stdout clean. The Spring banner and console logging must be disabled, because any extra output corrupts the message stream.

## HTTP

The server listens on a network port and clients connect to it over HTTP. It comes in three protocol modes, described in the next section.

**Use when:**
- The server is remote, shared, or reachable over a network.
- Several clients connect at once.
- The server runs in a container or on a separate machine from the client.

**Don't use when:**
- A local subprocess is enough. STDIO is simpler and needs no network setup.

# HTTP protocol modes

## Streamable HTTP

The current standard HTTP protocol in the MCP specification. It uses a single endpoint. Requests are HTTP POSTs, and the server can answer with a plain response or stream several messages back. It supports sessions, so the server can remember each client between calls.

**Use when:**
- You need server-to-client features such as notifications, progress updates, sampling (the server asking the client's model for a completion) or dynamic tool updates.
- You are starting a new HTTP server. This is the default choice.
- Several clients connect and the server benefits from tracking each one.

**Don't use when:**
- You run behind a load balancer without sticky sessions, or in a serverless setup where server-side session state can't be kept. Use Stateless instead.

## Stateless

Streamable HTTP without sessions. The server keeps no state between requests, and every request is handled independently.

**Use when:**
- The tools are simple request/response calls that don't depend on earlier calls.
- You need easy horizontal scaling, serverless deployment, or several replicas behind a load balancer.
- You want the smallest operational footprint.

**Don't use when:**
- You need the server to send messages to the client on its own, such as sampling, notifications or progress reporting.
- Tools depend on per-client state kept on the server.
- You need dynamic tool updates pushed to connected clients.

## SSE (HTTP + Server-Sent Events)

The older HTTP protocol. The client opens a long-lived event stream for server-to-client messages and sends its own messages through separate POST requests. The MCP specification has replaced it with Streamable HTTP.

**Use when:**
- You must support older MCP clients that only speak SSE.

**Don't use when:**
- You are building something new. Prefer Streamable HTTP.
- You run behind proxies or infrastructure that dislike long-lived connections.

# Comparison

**Transports**
- **STDIO:** local subprocess only, one client per process, no network exposure.
- **HTTP:** network-reachable, many clients, needs a port and (usually) authentication.

**HTTP protocol modes**
- **Streamable HTTP:** sessions, server-to-client messages, needs sticky sessions behind a load balancer. Current and the default.
- **Stateless:** no sessions, no server-to-client messages, scales freely behind a load balancer.
- **SSE:** sessions, server-to-client messages, needs sticky sessions. Legacy.

# Choosing quickly

- Local tool for one user, such as a Claude Desktop config entry: **STDIO**
- Remote or shared server with the full feature set: **HTTP with Streamable HTTP**
- Simple tools that must scale out or run serverless: **HTTP with Stateless**
- An older client that only supports SSE: **HTTP with SSE**
