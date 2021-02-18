package pf4j.first;

import java.nio.file.Paths;
import org.pf4j.CompoundPluginDescriptorFinder;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionFactory;
import org.pf4j.ManifestPluginDescriptorFinder;
import org.pf4j.PluginManager;
import org.pf4j.SingletonExtensionFactory;

public class PluginManagerHolder {

  public static PluginManager get() {
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

    return pluginManager;
  }

}
