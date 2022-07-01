package nl.aurorion.blockregen.version;

import com.google.gson.InstanceCreator;
import nl.aurorion.blockregen.version.api.NodeData;

import java.lang.reflect.Type;

public class NodeDataInstanceCreator implements InstanceCreator<NodeData> {

    private final VersionManager.NodeDataProvider provider;

    public NodeDataInstanceCreator(VersionManager.NodeDataProvider provider) {
        this.provider = provider;
    }

    @Override
    public NodeData createInstance(Type type) {
        return this.provider.provide();
    }
}
