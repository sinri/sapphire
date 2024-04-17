package io.github.sinri.sapphire.test.lab;

import com.azure.ai.openai.models.*;
import com.azure.core.util.BinaryData;
import io.github.sinri.drydock.naval.raider.Privateer;
import io.github.sinri.keel.tesuto.TestUnit;
import io.github.sinri.sapphire.azure.chat.ChatCompletionsKit;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class LabBeta extends Privateer {
    @Nonnull
    @Override
    protected VertxOptions buildVertxOptions() {
        return super.buildVertxOptions()
                .setAddressResolverOptions(new AddressResolverOptions()
                        .addServer("223.5.5.5"));
    }

    /**
     * 本地配置已加载。
     * 准备数据库连接之类的东西。
     *
     * @since 1.2.0
     */
    @Nonnull
    @Override
    protected Future<Void> prepareEnvironment() {
        return Future.succeededFuture();
    }

    @TestUnit
    public Future<Void> test1() {
        Promise<Void> promise = Promise.promise();
        try {
            ChatCompletionsKit chatCompletionsKit = new ChatCompletionsKit("Seventh-Tower-GPT4");

            getLogger().info("Mark 1");

            List<ChatRequestMessage> chatMessages = Arrays.asList(
                    new ChatRequestSystemMessage("You are a helpful assistant."),
                    new ChatRequestUserMessage("Will it rain in Berlin tomorrow?")
            );

            ChatCompletionsToolDefinition toolDefinition = new ChatCompletionsFunctionToolDefinition(
                    new TargetFunctionA(null).getToolDefinition()
            );

            ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);
            chatCompletionsOptions.setTools(List.of(toolDefinition));
            chatCompletionsOptions.setStream(true);


            getLogger().info("Mark 2");

            AtomicInteger intRef = new AtomicInteger(0);
            Keel.getVertx().setPeriodic(10_000L, x -> {
                intRef.addAndGet(10);
                System.out.println("+10 = " + intRef.get());
            });

            chatCompletionsKit.getOpenAIAsyncClient()
                    .getChatCompletionsStream("Seventh-Tower-GPT4", chatCompletionsOptions)
                    .subscribe(
                            chatCompletions -> {
                                try {
                                    BinaryData binaryData = BinaryData.fromObject(chatCompletions);
                                    getLogger().info("received: " + binaryData);
                                } catch (Throwable throwable) {
                                    getLogger().exception(throwable, "Place 1");
                                }
                            },
                            throwable -> {
                                getLogger().exception(throwable, "Place 2");
                                promise.fail(throwable);
                            },
                            () -> {
                                getLogger().notice("finally");
                                promise.complete();
                            }
                    );
        } catch (Throwable throwable) {
            getLogger().exception(throwable, "Place 3");
            promise.fail(throwable);
        }
        return promise.future();
    }
}
