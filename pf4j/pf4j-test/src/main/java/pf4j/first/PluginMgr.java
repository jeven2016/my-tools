package pf4j.first;

import api.Greeting;
import org.pf4j.*;

import java.nio.file.Paths;

public class PluginMgr {

    public static void main(String[] args) {
        var path = Paths.get("/home/jujucom/Desktop/workspace/projects/my-tools/pf4j/pf4j-test/lib");

        // create the plugin manager
        final PluginManager pluginManager = new DefaultPluginManager(path) {
            @Override
            protected CompoundPluginDescriptorFinder createPluginDescriptorFinder() {
                return new CompoundPluginDescriptorFinder()
                        // Demo is using the Manifest file
                        // PropertiesPluginDescriptorFinder is commented out just to avoid error log
                        //.add(new PropertiesPluginDescriptorFinder())
                        .add(new ManifestPluginDescriptorFinder());
            }

            @Override
            protected ExtensionFactory createExtensionFactory() {
                return new SingletonExtensionFactory();
            }
        };

        pluginManager.loadPlugins();
//        pluginManager.startPlugins();

        pluginManager.startPlugin("plugin-1");
        pluginManager.getExtensions(Greeting.class).get(0).greeting();
        pluginManager.unloadPlugin("plugin-1");
        System.out.println("size=" + pluginManager.getExtensions(Greeting.class).size());
//        pluginManager.startPlugin("plugin-2");
//        System.out.println("current size=" + pluginManager.getExtensions(Greeting.class).size());
//        pluginManager.getExtensions(Greeting.class).get(0).greeting();
        System.out.println(pluginManager.getPlugins());
    }
}
