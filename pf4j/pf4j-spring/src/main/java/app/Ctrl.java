package app;

import api.Greeting;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginManager;
import org.pf4j.update.PluginInfo;
import org.pf4j.update.UpdateManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("app")
public class Ctrl {

  @Autowired
  Greetings greetings;

  @Autowired
  PluginManager pluginManager;

  @Autowired
  UpdateManager updateManager;

  @GetMapping
  public Flux<String> hello() {
    return Flux.just("hello", "guest");
  }

  @GetMapping("greeting")
  public Flux<String> greeting() {
    var exts = pluginManager.getExtensions(Greeting.class);
    var exts2 = pluginManager.getExtensions(Greeting.class);
    System.out.println("result=" + (exts2 == exts));

    System.out.println(String
        .format("Found %d extensions for extension point '%s'", exts.size(),
            Greeting.class.getName()));
    for (Greeting greeting : exts) {
      System.out.println("updated >>> " + greeting.greeting());
    }

    return Flux.just("ok");
  }

  @GetMapping("uninstall/{pluginId}")
  public Flux<String> uninstall(@PathVariable String pluginId) {
//    updateManager.uninstallPlugin(pluginId);
    pluginManager.stopPlugin(pluginId);
    pluginManager.unloadPlugin(pluginId);
    return Flux.just("ok");
  }

  @GetMapping("load/{pluginId}")
  public Flux<String> loadAndStart(@PathVariable String pluginId) {
    pluginManager.loadPlugins();
    pluginManager.startPlugin(pluginId);

    var exts = pluginManager.getExtensions(Greeting.class);
    System.out.println(String
        .format("Found %d extensions for extension point '%s'", exts.size(),
            Greeting.class.getName()));
    for (Greeting greeting : exts) {
      System.out.println("updated >>> " + greeting.greeting());
    }

    return Flux.just("ok");
  }

  @GetMapping("updates")
  public Flux<String> getUpdates() {
    boolean systemUpToDate = true;
    if (updateManager.hasUpdates()) {
      List<PluginInfo> updates = updateManager.getUpdates();
      log.debug("Found {} updates", updates.size());
      for (PluginInfo plugin : updates) {
        log.debug("Found update for plugin '{}'", plugin.id);
        PluginInfo.PluginRelease lastRelease = updateManager.getLastPluginRelease(plugin.id);
        String lastVersion = lastRelease.version;
        String installedVersion = pluginManager.getPlugin(plugin.id).getDescriptor().getVersion();
        log.debug("Update plugin '{}' from version {} to version {}", plugin.id, installedVersion,
            lastVersion);
        boolean updated = updateManager.updatePlugin(plugin.id, lastVersion);
        if (updated) {
          log.debug("Updated plugin '{}'", plugin.id);
        } else {
          log.error("Cannot update plugin '{}'", plugin.id);
          systemUpToDate = false;
        }
      }
    }

    if (updateManager.hasAvailablePlugins()) {
      List<PluginInfo> availablePlugins = updateManager.getAvailablePlugins();
      log.debug("Found {} available plugins", availablePlugins.size());
      for (PluginInfo plugin : availablePlugins) {
        log.debug("Found available plugin '{}'", plugin.id);
        PluginInfo.PluginRelease lastRelease = updateManager.getLastPluginRelease(plugin.id);
        String lastVersion = lastRelease.version;
        log.debug("Install plugin '{}' with version {}", plugin.id, lastVersion);
        boolean installed = updateManager.installPlugin(plugin.id, lastVersion);
        if (installed) {
          log.debug("Installed plugin '{}'", plugin.id);
        } else {
          log.error("Cannot install plugin '{}'", plugin.id);
          systemUpToDate = false;
        }
      }
    }

    return Flux.just("ok");
  }
}
