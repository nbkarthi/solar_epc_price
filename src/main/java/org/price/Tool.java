package org.price;

import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.chat.completions.ChatCompletionFunctionTool;
import com.openai.models.chat.completions.ChatCompletionTool;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class Tool {


        public static List<ChatCompletionTool> getTools () {
            return List.of(ChatCompletionTool.ofFunction(panelCost()), ChatCompletionTool.ofFunction(inverterCost()), ChatCompletionTool.ofFunction(bomCost()));
        }

    @NotNull
    private static ChatCompletionFunctionTool panelCost() {
        FunctionParameters params = FunctionParameters.builder()
                .putAdditionalProperty("type", JsonValue.from("object"))
                .putAdditionalProperty("properties", JsonValue.from(Map.of())) // Empty properties
                .build();

        ChatCompletionFunctionTool getPrice = ChatCompletionFunctionTool.builder()
                .function(FunctionDefinition.builder()
                        .name("get_panel_price")
                        .description("Get the price of a solar panel")
                        .parameters(params)
                        .build())
                .build();
        return getPrice;
    }

    @NotNull
    private static ChatCompletionFunctionTool inverterCost() {
        FunctionParameters params = FunctionParameters.builder()
                .putAdditionalProperty("type", JsonValue.from("object"))
                .putAdditionalProperty("properties", JsonValue.from(Map.of())) // Empty properties
                .build();

        ChatCompletionFunctionTool getPrice = ChatCompletionFunctionTool.builder()
                .function(FunctionDefinition.builder()
                        .name("get_inverter_price")
                        .description("Get the price of a solar inverter")
                        .parameters(params)
                        .build())
                .build();
        return getPrice;
    }

    @NotNull
    private static ChatCompletionFunctionTool bomCost() {
        FunctionParameters params = FunctionParameters.builder()
                .putAdditionalProperty("type", JsonValue.from("object"))
                .putAdditionalProperty("properties", JsonValue.from(Map.of())) // Empty properties
                .build();

        ChatCompletionFunctionTool getPrice = ChatCompletionFunctionTool.builder()
                .function(FunctionDefinition.builder()
                        .name("get_bom_price")
                        .description("Get the price of the bill of materials (BOM) for a solar project")
                        .parameters(params)
                        .build())
                .build();
        return getPrice;
    }

}
