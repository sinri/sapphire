package io.github.sinri.sapphire.test.lab;

import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sinri.drydock.naval.raider.Privateer;
import io.github.sinri.keel.tesuto.TestUnit;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;

public class LabAlphaOri extends Privateer {
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
        FutureTemperatureParameters parameters = new FutureTemperatureParameters();
        BinaryData binaryData = BinaryData.fromObject(parameters);
        getLogger().info("binaryData: " + binaryData);
        return Future.succeededFuture();
    }

    @TestUnit
    public Future<Void> test2() {
        BinaryData binaryData = BinaryData.fromString(new JsonObject()
                .put("type", "object")
                .put("properties", new JsonObject()
                        .put("city", new JsonObject()
                                .put("type", "string")
                                .put("description", "City name to get the weather for.")
                        )
                        .put("date", new JsonObject()
                                .put("type", "string")
                                .put("description", "The date to get the weather for. The format is YYYY-MM-DD.")
                        )
                )
                .toString()
        );
        getLogger().info("binaryData: " + binaryData);
        return Future.succeededFuture();
    }

    private static class FutureTemperatureParameters {
        @JsonProperty(value = "type")
        private String type = "object";

        @JsonProperty(value = "properties")
        private FutureTemperatureProperties properties = new FutureTemperatureProperties();
    }

    private static class FutureTemperatureProperties {
        @JsonProperty(value = "unit")
        StringField unit = new StringField("Temperature unit. Can be either Celsius or Fahrenheit. Defaults to Celsius.");
        @JsonProperty(value = "location_name")
        StringField locationName = new StringField("The name of the location to get the future temperature for.");
        @JsonProperty(value = "date")
        StringField date = new StringField("The date to get the future temperature for. The format is YYYY-MM-DD.");
    }

    private static class StringField {
        @JsonProperty(value = "type")
        private final String type = "string";

        @JsonProperty(value = "description")
        private String description;

        @JsonCreator
        StringField(@JsonProperty(value = "description") String description) {
            this.description = description;
        }
    }
}
