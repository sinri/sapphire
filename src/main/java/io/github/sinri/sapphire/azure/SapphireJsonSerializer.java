package io.github.sinri.sapphire.azure;

import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.TypeReference;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SapphireJsonSerializer implements JsonSerializer {
    public static final SapphireJsonSerializer instance = new SapphireJsonSerializer();

    private SapphireJsonSerializer() {
        super();
    }

    @Override
    public <T> T deserialize(InputStream inputStream, TypeReference<T> typeReference) {
        Class<T> javaClass = typeReference.getJavaClass();
        if (JsonObject.class.isAssignableFrom(javaClass)) {
            try {
                byte[] bytes = inputStream.readAllBytes();
                var x = new JsonObject(Buffer.buffer(bytes));
                return javaClass.cast(x);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new ClassCastException();
        }
    }

    @Override
    public <T> Mono<T> deserializeAsync(InputStream inputStream, TypeReference<T> typeReference) {
        return Mono.fromCallable(() -> {
            return deserialize(inputStream, typeReference);
        });
    }

    @Override
    public void serialize(OutputStream outputStream, Object o) {
        try {
            outputStream.write(o.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> serializeAsync(OutputStream outputStream, Object o) {
        return Mono.fromCallable(() -> {
            serialize(outputStream, o);
            return null;
        });
    }
}
