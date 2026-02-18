package org.price;

import com.openai.models.chat.completions.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Main {

    private static final String SYSTEM_MESSAGE =
            "You are an AI agent responsible for calculating the total cost of a solar EPC project using available tools. " +
                    "The project cost consists of panel, inverter, and BOM components. Each tool provides the cost per kW. " +
                    "You must compute the total project cost based on the capacity (kW) requested by the user. " +
                    "If any required quantity is missing, make a reasonable estimate before proceeding. " +
                    "Do not ask the user for clarification. Use the tools as needed and return only the final calculated result.";


    public static void main(String[] args) {
        Main main = new Main();
        main.boot();
    }

    private void boot() {
        log.info("=== AI Agent Loop ===");
        String question = "What is the total cost of a 100 kW solar EPC project?";

        ChatCompletionMessage assistantMessage = APIClient.call2(SYSTEM_MESSAGE, question);

        while (true) {

            if (assistantMessage.toolCalls().isPresent()) {
                List<ChatCompletionMessageToolCall> toolCallList = assistantMessage.toolCalls().get();

                List<ChatCompletionMessageParam> toolResults = new ArrayList<>();

                for (ChatCompletionMessageToolCall toolCall : toolCallList) {
                    ChatCompletionMessageFunctionToolCall.Function function = toolCall.function().get().function();
                    String toolName = function.name();
                    String toolArgs = function.arguments();
                    String toolCallId = toolCall.function().get().id();
                    log.info("Tool call: {} with ID : {} with args: {}", toolName, toolCallId, toolArgs);

                    ChatCompletionMessageParam result = toolCall(toolName, toolCallId);
                    if (result != null) {
                        toolResults.add(result);
                        log.info("Tool result for {}: {}", toolName, result.asTool().content());
                    }
                }

                ChatCompletion.Choice choice = APIClient.followUp(SYSTEM_MESSAGE, question, assistantMessage, toolResults);

                ChatCompletion.Choice.FinishReason finishReason = choice.finishReason();
                log.info("finishReason {}", finishReason);
                log.info("finishReason details {}", finishReason.getClass().getName());
                if(finishReason == ChatCompletion.Choice.FinishReason.STOP || finishReason.toString().equals("stop")) {
                    log.info("Final response: {}", choice.message().content().orElse("(empty or no content)"));
                    break;
                }
                if (assistantMessage == null) break;
                log.info("Model response: {}", assistantMessage.content().orElse("(tool_calls again)"));
            } else {
                log.info("Final response: {}", assistantMessage.content().orElse("(empty or no content)"));
                break;
            }
        }
    }

    private static ChatCompletionMessageParam toolCall(String toolName, String toolCallId) {
        if (toolName.equals("get_panel_price")) {
            ChatCompletionToolMessageParam price = panelPrice(toolCallId);
            return ChatCompletionMessageParam.ofTool(price);
        } else if (toolName.equals("get_inverter_price")) {
            // Implement inverter price retrieval and add to toolResults
            ChatCompletionToolMessageParam price = inverterPrice(toolCallId);
            return ChatCompletionMessageParam.ofTool(price);
        } else if (toolName.equals("get_bom_price")) {
            // Implement BOM price retrieval and add to toolResults
            ChatCompletionToolMessageParam price = bomPrice(toolCallId);
            return ChatCompletionMessageParam.ofTool(price);
        }
        return null;
    }

    @NotNull
    private static ChatCompletionToolMessageParam panelPrice(String toolCallId) {
        String price = "{\"price\": 15 per watt pick, \"currency\": \"USD\"}";

        ChatCompletionToolMessageParam build = ChatCompletionToolMessageParam.builder()
                .content(price)
                .toolCallId(toolCallId)
                .build();
        return build;
    }

    @NotNull
    private static ChatCompletionToolMessageParam inverterPrice(String toolCallId) {
        String price = "{\"price\": 2 per watt pick, \"currency\": \"USD\"}";

        ChatCompletionToolMessageParam build = ChatCompletionToolMessageParam.builder()
                .content(price)
                .toolCallId(toolCallId)
                .build();
        return build;
    }

    @NotNull
    private static ChatCompletionToolMessageParam bomPrice(String toolCallId) {
        String price = "{\"price\": 1 per watt pick, \"currency\": \"USD\"}";

        ChatCompletionToolMessageParam build = ChatCompletionToolMessageParam.builder()
                .content(price)
                .toolCallId(toolCallId)
                .build();
        return build;
    }


}
