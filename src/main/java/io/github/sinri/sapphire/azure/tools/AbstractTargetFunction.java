package io.github.sinri.sapphire.azure.tools;

import com.azure.ai.openai.models.FunctionDefinition;
import io.github.sinri.keel.core.json.UnmodifiableJsonifiableEntity;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

abstract public class AbstractTargetFunction {
    protected final UnmodifiableJsonifiableEntity parameters;

    public AbstractTargetFunction(JsonObject parameters) {
        this.parameters = UnmodifiableJsonifiableEntity.wrap(parameters);
    }

    abstract public Future<String> execute();

    abstract public FunctionDefinition getToolDefinition();
}
