package io.github.sinri.sapphire.test.lab;

import com.azure.ai.openai.models.FunctionDefinition;
import io.github.sinri.sapphire.azure.tools.AbstractTargetFunction;
import io.github.sinri.sapphire.azure.tools.FunctionDefinitionBuilder;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class TargetFunctionA extends AbstractTargetFunction {

    public TargetFunctionA(JsonObject parameters) {
        super(parameters);
    }

    public Future<String> execute() {
        return Future.succeededFuture(
                "Weather for " + parameters.readString("city") + " on " + parameters.readString("date") + " is RAINY."
        );
    }

    @Override
    public FunctionDefinition getToolDefinition() {
        return new FunctionDefinitionBuilder("TargetFunctionA")
                .setFunctionDescription("Query recent weather for cities")
                .property("city", FunctionDefinitionBuilder.ParameterType.string, "City name to get the weather for.")
                .property("date", FunctionDefinitionBuilder.ParameterType.string, "The date to get the weather for. The format is YYYY-MM-DD.")
                .build();
    }

}
