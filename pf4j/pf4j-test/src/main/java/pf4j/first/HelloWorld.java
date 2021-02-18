package pf4j.first;

import api.Greeting;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloWorld {

  private static final Logger logger = LoggerFactory.getLogger(HelloWorld.class);

  public static void main(String[] args) {
    // print logo
    printLogo();

    // create the plugin manager
    final PluginManager pluginManager = PluginManagerHolder.get();

    // load the plugins
    pluginManager.loadPlugins();

    // enable a disabled plugin
//        pluginManager.enablePlugin("welcome-plugin");

    // start (active/resolved) the plugins
//        pluginManager.startPlugins();

    pluginManager.startPlugin("plugin-1");

    logger.info("Plugindirectory: ");
    logger.info("\t" + System.getProperty("pf4j.pluginsDir", "plugins") + "\n");

    // retrieves the extensions for Greeting extension point
    List<Greeting> greetings = pluginManager.getExtensions(Greeting.class);
    logger.info(String.format("Found %d extensions for extension point '%s'", greetings.size(),
        Greeting.class.getName()));
    for (Greeting greeting : greetings) {
      logger.info(">>> " + greeting.greeting());
    }

    // // print extensions from classpath (non plugin)
    // logger.info(String.format("Extensions added by classpath:"));
    // Set<String> extensionClassNames = pluginManager.getExtensionClassNames(null);
    // for (String extension : extensionClassNames) {
    //     logger.info("   " + extension);
    // }

    // print extensions for each started plugin
    List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();
    for (PluginWrapper plugin : startedPlugins) {
      String pluginId = plugin.getDescriptor().getPluginId();
      logger.info(String.format("Extensions added by plugin '%s':", pluginId));
      // extensionClassNames = pluginManager.getExtensionClassNames(pluginId);
      // for (String extension : extensionClassNames) {
      //     logger.info("   " + extension);
      // }
    }

    // stop the plugins
    pluginManager.stopPlugins();
        /*
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                pluginManager.stopPlugins();
            }
        });
        */
  }

  private static void printLogo() {
    logger.info(StringUtils.repeat("#", 40));
    logger.info(StringUtils.center("PF4J-DEMO", 40));
    logger.info(StringUtils.repeat("#", 40));
  }

}