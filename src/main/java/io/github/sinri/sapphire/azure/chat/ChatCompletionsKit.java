package io.github.sinri.sapphire.azure.chat;

import com.azure.ai.openai.models.*;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.sapphire.azure.AzureOpenAIKit;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class ChatCompletionsKit extends AzureOpenAIKit {
    public ChatCompletionsKit(@Nonnull String modelName) {
        super(modelName);
    }

    /**
     * 经常Timeout 10min。
     */
    public Future<ChatCompletions> getChatCompletions(@Nonnull ChatCompletionsOptions chatCompletionsOptions) {
        return KeelAsyncKit.executeBlocking(promise -> {
            getOpenAIAsyncClient().getChatCompletions(getDeployment(), chatCompletionsOptions)
                    .subscribe(promise::complete, promise::fail, () -> {
                        System.out.println("FIN");
                    });
        });
    }

    public Future<CompletionsFinishReason> getChatCompletionsStream(
            @Nonnull ChatCompletionsOptions chatCompletionsOptions,
            Function<StreamDelta, Future<Void>> StreamDeltaAsyncConsumer
    ) {
        chatCompletionsOptions.setStream(true);
        return KeelAsyncKit.executeBlocking(promise -> {
            AtomicReference<ChatRole> roleRef = new AtomicReference<>();
            getOpenAIAsyncClient().getChatCompletionsStream(getDeployment(), chatCompletionsOptions)
                    .subscribe(
                            chatCompletions -> {
                                if (chatCompletions.getChoices() == null || chatCompletions.getChoices().isEmpty()) {
                                    //promise.fail(new NullPointerException());
                                    Keel.getLogger().info(
                                            "ID " + chatCompletions.getId()
                                                    + " | " + chatCompletions
                                    );
                                } else {
                                    ChatChoice chatChoice = chatCompletions.getChoices().get(0);
                                    ChatResponseMessage delta = chatChoice.getDelta();
                                    ChatRole role = delta.getRole();
                                    if (role != null) {
                                        roleRef.set(role);
                                    }
                                    String content = delta.getContent();
                                    StreamDeltaAsyncConsumer.apply(new StreamDelta(roleRef.get(), content));

                                    CompletionsFinishReason finishReason = chatChoice.getFinishReason();
                                    if (finishReason != null) {
                                        promise.complete(finishReason);
                                    }
                                }
                            },
                            throwable -> {
                                promise.fail(throwable);
                            },
                            () -> {
                                System.out.println("FIN");
                            }
                    );
        });
    }

    public record StreamDelta(ChatRole role, @Nullable String content) {

    }
}
