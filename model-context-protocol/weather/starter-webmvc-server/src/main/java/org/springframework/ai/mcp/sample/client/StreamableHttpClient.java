/*
* Copyright 2024 - 2024 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.springframework.ai.mcp.sample.client;

import java.util.Map;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;

/**
 * @author Christian Tzolov
 */
public class StreamableHttpClient {

	public static void main(String[] args) {

		// The MCP server should be run manually
		HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder("http://localhost:8080")
			.build();

		var client = McpClient.sync(transport).build();

		client.initialize();

		client.ping();

		// List and demonstrate tools
		ListToolsResult toolsList = client.listTools();
//		System.out.println("Available Tools = " + toolsList);
		System.out.println("Available Tools = ");

		toolsList.tools().stream().forEach(tool -> {
			System.out.println("Tool: " + tool.name() + ", description: " + tool.description() + ", schema: "
					+ tool.inputSchema());
		});

		CallToolResult toUpperCaseResult = client
			.callTool(CallToolRequest.builder("toUpperCase").arguments(Map.of("input", "hellow")).build());
		System.out.println("\n toUpperCase Result = " + toUpperCaseResult);

		CallToolResult weatherForcastResult = client.callTool(CallToolRequest.builder("getWeatherForecastByLocation")
			.arguments(Map.of("latitude", "47.6062", "longitude", "-122.3321"))
			.build());
		System.out.println("\n Weather Forcast: " + weatherForcastResult);

		CallToolResult alertResult = client
			.callTool(CallToolRequest.builder("getAlerts").arguments(Map.of("state", "NY")).build());
		System.out.println("\n Alert Response = " + alertResult);

		client.closeGracefully();

	}

}
