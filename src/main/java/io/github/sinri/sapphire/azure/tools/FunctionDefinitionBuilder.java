package io.github.sinri.sapphire.azure.tools;

import com.azure.ai.openai.models.FunctionDefinition;
import com.azure.core.util.BinaryData;
import io.github.sinri.sapphire.azure.AzureOpenAIKit;
import io.vertx.json.schema.common.dsl.ObjectSchemaBuilder;
import io.vertx.json.schema.draft7.dsl.Schemas;

public class FunctionDefinitionBuilder {
    private final FunctionDefinition functionDefinition;
    private final ObjectSchemaBuilder propertiesSchemaJson = Schemas.objectSchema();

    public FunctionDefinitionBuilder(String functionName) {
        this.functionDefinition = new FunctionDefinition(functionName);
    }


    public FunctionDefinitionBuilder setFunctionDescription(String functionDescription) {
        this.functionDefinition.setDescription(functionDescription);
        return this;
    }

    public FunctionDefinitionBuilder property(String name, ParameterType type, String desc) {
        if (type == ParameterType.string) {
            propertiesSchemaJson.property(name, Schemas.stringSchema()
                    .withKeyword("description", desc));
        } else if (type == ParameterType.integer) {
            propertiesSchemaJson.property(name, Schemas.intSchema()
                    .withKeyword("description", desc));
        } else if (type == ParameterType.number) {
            propertiesSchemaJson.property(name, Schemas.numberSchema()
                    .withKeyword("description", desc));
        } else if (type == ParameterType.bool) {
            propertiesSchemaJson.property(name, Schemas.booleanSchema()
                    .withKeyword("description", desc));
        } else {
            throw new IllegalArgumentException();
        }
        return this;
    }

    public FunctionDefinition build() {
        BinaryData binaryData = AzureOpenAIKit.JsonObjectToBinaryData(this.propertiesSchemaJson.toJson());
        this.functionDefinition.setParameters(binaryData);
        return this.functionDefinition;
    }

    public enum ParameterType {
        string,
        integer,
        number,
        bool
    }
}
