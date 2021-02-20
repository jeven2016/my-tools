package app;

import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.List;

import org.pf4j.PluginManager;
import org.pf4j.spring.SpringPluginManager;
import org.pf4j.update.DefaultUpdateRepository;
import org.pf4j.update.UpdateManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class SpringConfiguration {

//  public static final String LIB = "/root/Desktop/workspace/projects/my-tools/pf4j/pf4j-spring/lib/";

  //home
  public static final String LIB = "/home/jujucom/Desktop/workspace/projects/my-tools/pf4j/pf4j-spring/lib/";

  @Bean
  public PluginManager pluginManager() {
    return new SpringPluginManager(
        Paths.get(LIB));
  }

  @Bean
  @DependsOn("pluginManager")
  public Greetings greetings() {
    return new Greetings();
  }


  /**
   * Init a update manager with corresponding repository
   * <p>
   * update manager should work with local repository
   *
   * @param p PluginManager
   * @return UpdateManager
   */
  @Bean
  public UpdateManager updateManager(PluginManager p) throws MalformedURLException {
    var updateRepo = new DefaultUpdateRepository("repo", Paths.get(LIB).toUri().toURL());
    return new UpdateManager(p, List.of(updateRepo));
  }
}
