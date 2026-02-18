package org.price;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class APIClient {

    //.baseUrl("http://localhost:11434/v1")
    private static OpenAIClient client = OpenAIOkHttpClient.builder()
            .apiKey("")
            .build();

    private static ChatCompletionCreateParams params(String command, String question) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .addSystemMessage(command)
                .addUserMessage(question)
                .tools(Tool.getTools())
                .build();
        return params;
    }

    private static ChatCompletionCreateParams params(String command, String question, ChatCompletionMessage previousResponse, java.util.List<ChatCompletionMessageParam> toolResults) {
        // Build an explicit assistant message param that includes tool_calls.
        // Passing previousResponse directly can be serialized without tool_calls, causing:
        // "messages with role 'tool' must be a response to a preceeding message with 'tool_calls'"
        ChatCompletionAssistantMessageParam assistantParam = ChatCompletionAssistantMessageParam.builder()
                .toolCalls(previousResponse.toolCalls().orElseThrow())
                .build();

        ChatCompletionCreateParams.Builder builder = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .addSystemMessage(command)
                .addUserMessage(question)
                .addMessage(ChatCompletionMessageParam.ofAssistant(assistantParam));
        for (ChatCompletionMessageParam toolResult : toolResults) {
            builder.addMessage(toolResult);
        }
        return builder.tools(Tool.getTools()).build();
    }

    public static String call(String command, String question) {
        try {
            ChatCompletionCreateParams params = params(command, question);
            ChatCompletion response = client.chat().completions().create(params);

            if (response.choices() != null && !response.choices().isEmpty()) {
                ChatCompletionMessage msg = response.choices().get(0).message();
                String reply = msg.content().orElse("(empty or no content)");

                return reply;
            }
        } catch (Exception e) {
            log.error("OpenAI API error: {}", e.getMessage(), e);
        }
        return null;
    }

    public static ChatCompletionMessage call2(String command, String question) {
        try {
            ChatCompletionCreateParams params = params(command, question);
            ChatCompletion response = client.chat().completions().create(params);

            if (response.choices() != null && !response.choices().isEmpty()) {
                return response.choices().get(0).message();
            }
        } catch (Exception e) {
            log.error("OpenAI API error: {}", e.getMessage(), e);
        }
        return null;
    }

    public static ChatCompletion.Choice followUp(String command, String question, ChatCompletionMessage previousResponse, List<ChatCompletionMessageParam> toolResults) {
        try {
            ChatCompletionCreateParams params = params(command, question, previousResponse, toolResults);
            ChatCompletion response = client.chat().completions().create(params);

            if (response.choices() != null && !response.choices().isEmpty()) {
                return response.choices().get(0);
//                return response.choices().get(0).message();
            }
        } catch (Exception e) {
            log.error("OpenAI API error: {}", e.getMessage(), e);
        }
        return null;
    }
}
