package nl.aurorion.blockregen.system;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.extern.java.Log;

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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Gson (json) save and load helper class.
 * <p>
 * Copied from DevportUtils by devport.space
 *
 * @author qwz
 */
@Log
public class GsonHelper {

    private final Gson gson;

    public GsonHelper(boolean prettyPrinting) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        if (prettyPrinting)
            gsonBuilder.setPrettyPrinting();
        this.gson = gsonBuilder.create();
    }

    public GsonHelper() {
        this(false);
    }

    public static <T> Type mapList(@NotNull Class<T> innerType) {
        return TypeToken.getParameterized(List.class, innerType).getType();
    }

    public static <T> Type map(@NotNull Class<T> clazz) {
        return new TypeToken<T>() {
        }.getType();
    }

    /**
     * Asynchronously read ByteBuffer from a file.
     */
    @NotNull
    public CompletableFuture<ByteBuffer> read(@NotNull final Path path) {

        AsynchronousFileChannel channel;
        long size;
        try {
            channel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
            size = channel.size();
        } catch (IOException e) {
            log.severe("Could not open an asynchronous file channel.");
            e.printStackTrace();
            return CompletableFuture.supplyAsync(() -> {
                throw new CompletionException(e);
            });
        }

        if (size > Integer.MAX_VALUE) {
            return CompletableFuture.supplyAsync(() -> {
                throw new CompletionException(new IllegalStateException("File is too big for the reader."));
            });
        }

        ByteBuffer buffer = ByteBuffer.allocate((int) size);

        CompletableFuture<ByteBuffer> future = new CompletableFuture<>();
        channel.read(buffer, 0, future, new CompletionHandler<Integer, CompletableFuture<ByteBuffer>>() {
            @Override
            public void completed(Integer result, CompletableFuture<ByteBuffer> attachment) {
                future.complete(buffer);
            }

            @Override
            public void failed(Throwable exc, CompletableFuture<ByteBuffer> attachment) {
                future.completeExceptionally(exc);
            }
        });
        return future;
    }

    /**
     * Load and parse json from a file.
     *
     * @return Parsed output or null.
     */
    @Nullable
    public <T> T load(@NotNull String dataPath, @NotNull Type type) {

        Path path = Paths.get(dataPath);

        if (!Files.exists(path))
            return null;

        String input;
        try {
            input = String.join("", Files.readAllLines(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (Strings.isNullOrEmpty(input))
            return null;

        return gson.fromJson(input, type);
    }

    /**
     * Asynchronously load a List<T> from a file.
     *
     * @return CompletableFuture with the resulting list or null.
     */
    @NotNull
    public <T> CompletableFuture<List<T>> loadListAsync(@NotNull final String dataPath, @NotNull Class<T> innerClazz) {
        Path path = Paths.get(dataPath);

        if (!Files.exists(path))
            return new CompletableFuture<>();

        final Type type = mapList(innerClazz);

        return read(path).thenApplyAsync(buffer -> {
            String output = new String(buffer.array(), StandardCharsets.UTF_8).trim();

            if (Strings.isNullOrEmpty(output))
                return null;

            return gson.fromJson(output, type);
        });
    }

    /**
     * Asynchronously save data to json.
     *
     * @return CompletableFuture with the number of bytes written
     */
    @NotNull
    public <T> CompletableFuture<Void> save(@NotNull final T input, @NotNull final String dataPath) {

        Path path = Paths.get(dataPath);

        AsynchronousFileChannel channel;
        try {
            channel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE);
        } catch (IOException e) {
            log.severe("Could not open an asynchronous file channel.");
            e.printStackTrace();
            return CompletableFuture.supplyAsync(() -> {
                throw new CompletionException(e);
            });
        }

        final Type type = map(input.getClass());

        CompletableFuture<Void> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            String jsonString = gson.toJson(input, type).trim();

            ByteBuffer buffer = ByteBuffer.allocate(jsonString.getBytes().length);
            buffer.put(jsonString.getBytes(StandardCharsets.UTF_8));
            buffer.flip();

            channel.write(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    future.complete(null);
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    future.completeExceptionally(exc);
                }
            });
        });
        return future;
    }
}