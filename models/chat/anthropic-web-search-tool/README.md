# Spring AI with Web Search Tool

A simple command-line chat application demonstrating Spring AI's ChatClient capabilities with AI models.

## Prerequisites
- Java 17 or higher
- Maven

This example uses Anthropic (Claude) as the model provider.

Before using the AI commands, make sure you have an API key from Anthropic.

Create an account at [Anthropic Console](https://console.anthropic.com/) and generate the key at [API Keys](https://console.anthropic.com/settings/keys).

The Spring AI project defines a configuration property named `spring.ai.anthropic.api-key` that you should set to the value of the API key obtained from Anthropic.

Exporting an environment variable is one way to set that configuration property:

```shell
export ANTHROPIC_API_KEY=<INSERT KEY HERE>
```



## Running the Application
1. Clone the repository
2. Navigate to the project directory
3. Run the application using Maven wrapper:
   `./mvnw spring-boot:run`

## Example Interaction
Once started, you'll see the Spring Boot banner and then:

```text
Let's chat!
USER: tell me a joke
ASSISTANT: Why don't skeletons fight each other?
They don't have the guts.
```

## Technical Details

The application uses Spring AI's ChatClient to interact with AI models. The following demo code showcases a basic setup for creating a chat interaction in a Spring Boot application:

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner cli(ChatClient.Builder builder) {
        return args -> {
            var chat = builder.build();
            var scanner = new Scanner(System.in);
            System.out.println("\nLet's chat!");
            while (true) {
                System.out.print("\nUSER: ");
                System.out.println("ASSISTANT: " +
                        chat.prompt(scanner.nextLine()).call().content());
            }
        };
    }
}
```

# Explanation
1. Application Entry Point:

* The `Application` class is annotated with `@SpringBootApplication`, marking it as the main entry point of the Spring Boot application.
* The `main` method launches the application by calling SpringApplication.run.

2. Command Line Runner:
* A `CommandLineRunner` bean is defined to initiate the chat functionality after the application starts.
* A `ChatClient.Builder` to create a ChatClient instance, which serves as the core interface for AI interactions.


3. User Interaction:
* A `Scanner` is used to capture user input from the command line.
* In a continuous loop, the application reads a line from the user (``USER``) and sends it as a prompt to the `ChatClient`.
* The assistant's response is fetched with `chat.prompt(...).call().content()` and displayed as the `ASSISTANT'`s reply.
* This simple loop continues until the user terminates the application.