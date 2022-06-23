package nl.aurorion.blockregen.version;

import com.google.gson.*;
import lombok.extern.java.Log;
import nl.aurorion.blockregen.version.api.NodeData;

import java.lang.reflect.Type;

@Log
public class NodeDataAdapter<T extends NodeData> implements JsonDeserializer<T>, JsonSerializer<T> {

    private final Gson simpleGson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public T deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        // https://stackoverflow.com/questions/38071530/gson-deserialize-interface-to-its-class-implementation

        final JsonObject jsonObject = jsonElement.getAsJsonObject();
        final JsonPrimitive prim = (JsonPrimitive) jsonObject.get("className");
        final String className = prim.getAsString();
        final Class<T> clazz = getClassInstance(className);

        log.fine(String.format("Deserializing %s (%s) into %s", jsonElement, type, clazz.getName()));

        return simpleGson.fromJson(jsonElement, clazz);
    }

    @Override
    public JsonElement serialize(T t, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonElement element = simpleGson.toJsonTree(t, type);

        // Add className to properly deserialize correctly later.
        element.getAsJsonObject().addProperty("className", t.getClass().getName());

        log.fine(String.format("Serializing %s (%s) into %s", t, type, element));
        return element;
    }

    @SuppressWarnings("unchecked")
    public Class<T> getClassInstance(String className) {
        try {
            return (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException cnfe) {
            throw new JsonParseException(cnfe.getMessage());
        }
    }
}
