package io.github.sinri.sapphire.azure;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.OpenAIServiceVersion;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import io.github.sinri.keel.facade.configuration.KeelConfigElement;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Objects;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class AzureOpenAIKit {
    private final String apiKey;
    private final String resourceName;
    private final String deployment;
    private final String apiVersion;

    private final OpenAIAsyncClient openAIAsyncClient;

    public AzureOpenAIKit(@Nonnull String modelName) {
        KeelConfigElement kce = Keel.getConfiguration().extract("azure", "openai", modelName);
        Objects.requireNonNull(kce, "azure.open." + modelName + ".* are missing");
        this.apiKey = Objects.requireNonNull(kce.readString(Collections.singletonList("apiKey")));
        this.resourceName = Objects.requireNonNull(kce.readString(Collections.singletonList("resourceName")));
        this.deployment = Objects.requireNonNull(kce.readString(Collections.singletonList("deployment")));
        this.apiVersion = Objects.requireNonNull(kce.readString(Collections.singletonList("apiVersion")));
        var endpoint = "https://" + resourceName + ".openai.azure.com";

        System.out.println("endpoint: " + endpoint);

        this.openAIAsyncClient = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(apiKey))
                .serviceVersion(OpenAIServiceVersion.V2024_02_15_PREVIEW)
                .buildAsyncClient();
    }

    public static BinaryData JsonObjectToBinaryData(JsonObject jsonObject) {
        return BinaryData.fromObject(jsonObject, SapphireJsonSerializer.instance);
    }

    public OpenAIAsyncClient getOpenAIAsyncClient() {
        return openAIAsyncClient;
    }

    protected String getDeployment() {
        return deployment;
    }
}
