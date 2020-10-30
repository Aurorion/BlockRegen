package nl.aurorion.blockregen.system;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import nl.aurorion.blockregen.ConsoleOutput;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@RequiredArgsConstructor
public class GsonHelper {

    private final Gson gson = new GsonBuilder()
            // .setPrettyPrinting()
            .create();

    public <T> T load(String dataPath, Type type) {

        Path path = Paths.get(dataPath);

        if (!Files.exists(path)) return null;

        String input;
        try {
            input = String.join("", Files.readAllLines(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (Strings.isNullOrEmpty(input)) return null;

        ConsoleOutput.getInstance().debug("JSON: " + input);

        return gson.fromJson(input, type);
    }

    public <T> CompletableFuture<Integer> save(final T in, String dataPath) {

        String output = gson.toJson(in, new TypeToken<T>() {
        }.getType());

        ConsoleOutput.getInstance().debug("JSON: " + output);

        Path path = Paths.get(dataPath);

        AsynchronousFileChannel channel;
        try {
            channel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException e) {
            if (ConsoleOutput.getInstance().isDebug())
                e.printStackTrace();
            return CompletableFuture.supplyAsync(() -> {
                throw new CompletionException(e);
            });
        }

        ByteBuffer buffer = ByteBuffer.allocate(output.getBytes().length);

        buffer.put(output.getBytes(StandardCharsets.UTF_8));
        buffer.flip();

        CompletableFuture<Integer> future = new CompletableFuture<>();
        channel.write(buffer, 0, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                future.complete(result);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                future.completeExceptionally(exc);
            }
        });
        return future;
    }
}