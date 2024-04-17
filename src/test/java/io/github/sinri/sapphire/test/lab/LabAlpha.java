package io.github.sinri.sapphire.test.lab;

import com.azure.ai.openai.models.*;
import io.github.sinri.drydock.naval.raider.Privateer;
import io.github.sinri.keel.tesuto.TestUnit;
import io.github.sinri.sapphire.azure.chat.ChatCompletionsKit;
import io.github.sinri.sapphire.azure.tools.AbstractTargetFunction;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LabAlpha extends Privateer {
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
        //System.getenv().put("AZURE_LOG_LEVEL", "2");
        //EnvironmentConfiguration.getGlobalConfiguration().put("AZURE_LOG_LEVEL", "2");
        return Future.succeededFuture();
    }

    @TestUnit(skip = true)
    public Future<Void> test1() {
//        AtomicInteger intRef = new AtomicInteger(0);
//        Keel.getVertx().setPeriodic(10_000L, x -> {
//            intRef.addAndGet(10);
//            System.out.println("+10 = " + intRef.get());
//        });

        ChatCompletionsKit chatCompletionsKit = new ChatCompletionsKit("Seventh-Tower-GPT4");

        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("你是一个很厉害的作家。"));
//        chatMessages.add(new ChatRequestUserMessage("Can you help me?"));
//        chatMessages.add(new ChatRequestAssistantMessage("Of course, me hearty! What can I do for ye?"));
        chatMessages.add(new ChatRequestUserMessage("请写一篇关于猫被恶龙捕获的超短篇小说。"));

        return chatCompletionsKit.getChatCompletions(new ChatCompletionsOptions(chatMessages)
                        .setMaxTokens(1024)
                )
                .compose(chatCompletions -> {
                    ChatChoice chatChoice = chatCompletions.getChoices().get(0);
                    ChatResponseMessage message = chatChoice.getMessage();
                    getLogger().info("> " + message.getRole() + ": " + message.getContent());
                    return Future.succeededFuture();
                });
    }

    @TestUnit(skip = true)
    public Future<Void> test2() {
//        AtomicInteger intRef = new AtomicInteger(0);
//        Keel.getVertx().setPeriodic(1000L, x -> {
//            intRef.addAndGet(1);
//            System.out.println("+1 = " + intRef.get());
//        });

        ChatCompletionsKit chatCompletionsKit = new ChatCompletionsKit("Seventh-Tower-GPT4");

        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage("你是一个很厉害的作家。"));
//        chatMessages.add(new ChatRequestUserMessage("Can you help me?"));
//        chatMessages.add(new ChatRequestAssistantMessage("Of course, me hearty! What can I do for ye?"));
        chatMessages.add(new ChatRequestUserMessage("请写一篇关于猫的超短篇小说。"));

        return chatCompletionsKit.getChatCompletionsStream(
                        new ChatCompletionsOptions(chatMessages)
                                .setMaxTokens(1024),
                        streamDelta -> {
                            getLogger().info("> " + streamDelta.role() + ": " + streamDelta.content());
                            return Future.succeededFuture();
                        }
                )
                .compose(completionsFinishReason -> {
                    getLogger().info("Finish Reason: " + completionsFinishReason.toString());
                    return Future.succeededFuture();
                });
    }

    @TestUnit
    public Future<Void> test3() {
        ChatCompletionsKit chatCompletionsKit = new ChatCompletionsKit("Seventh-Tower-GPT4");

        List<ChatRequestMessage> chatMessages = Arrays.asList(
                new ChatRequestSystemMessage("You are a helpful assistant."),
                new ChatRequestUserMessage("Will it rain in Berlin tomorrow?")
        );

        ChatCompletionsToolDefinition toolDefinition = new ChatCompletionsFunctionToolDefinition(
                new TargetFunctionA(null).getToolDefinition()
        );

        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);
        chatCompletionsOptions.setTools(List.of(toolDefinition));

        return chatCompletionsKit.getChatCompletions(chatCompletionsOptions)
                .compose(chatCompletions -> {
                    ChatChoice chatChoice = chatCompletions.getChoices().get(0);
                    if (chatChoice.getFinishReason() == CompletionsFinishReason.TOOL_CALLS) {
                        ChatCompletionsToolCall chatCompletionsToolCall = chatChoice.getMessage().getToolCalls().get(0);
                        getLogger().info("class of chatCompletionsToolCall is " + chatCompletionsToolCall.getClass());
                        ChatCompletionsFunctionToolCall chatCompletionsFunctionToolCall = (ChatCompletionsFunctionToolCall) chatCompletionsToolCall;
                        String functionName = chatCompletionsFunctionToolCall.getFunction().getName();
                        String functionArguments = chatCompletionsFunctionToolCall.getFunction().getArguments();
                        getLogger().info("functionName: " + functionName + " functionArguments: " + functionArguments);

                        try {
                            Class<?> aClass = Class.forName("io.github.sinri.sapphire.test.lab." + functionName);
                            Constructor<?> constructor = aClass.getConstructor(JsonObject.class);
                            var p = new JsonObject(functionArguments);
                            AbstractTargetFunction targetFunction = (AbstractTargetFunction) constructor.newInstance(p);
                            return targetFunction.execute()
                                    .compose(functionResult -> {
                                        getLogger().info("functionResult: " + functionResult);
                                        return Future.succeededFuture(new AIResp(ChatRole.TOOL, functionResult));
                                    })
                                    .compose(aiRespByTool -> {
                                        ChatRequestAssistantMessage assistantMessage = new ChatRequestAssistantMessage("");
                                        assistantMessage.setToolCalls(chatChoice.getMessage().getToolCalls());

                                        List<ChatRequestMessage> followUpMessages = Arrays.asList(
                                                chatMessages.get(0),
                                                chatMessages.get(1),
                                                assistantMessage,
                                                new ChatRequestToolMessage(aiRespByTool.content, chatCompletionsFunctionToolCall.getId())
                                        );
                                        ChatCompletionsOptions followUpChatCompletionsOptions = new ChatCompletionsOptions(followUpMessages);

                                        return chatCompletionsKit.getChatCompletions(followUpChatCompletionsOptions)
                                                .compose(x -> {
                                                    ChatResponseMessage message = x.getChoices().get(0).getMessage();
                                                    return Future.succeededFuture(new AIResp(message.getRole(), message.getContent()));
                                                });
                                    });
                        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                                 InstantiationException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }

                    } else {
                        ChatResponseMessage message = chatChoice.getMessage();
                        return Future.succeededFuture(new AIResp(message.getRole(), message.getContent()));
                    }
                })
                .compose(aiResp -> {
                    getLogger().info("Role: " + aiResp.role + " Content: " + aiResp.content);


                    return Future.succeededFuture();
                });
    }

    public record AIResp(ChatRole role, String content) {
    }
}
