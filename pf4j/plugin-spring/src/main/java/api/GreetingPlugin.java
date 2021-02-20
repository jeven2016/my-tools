package api;

import org.jsoup.Jsoup;
import org.pf4j.Extension;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

public class GreetingPlugin extends SpringPlugin {

  public GreetingPlugin(PluginWrapper wrapper) {
    super(wrapper);
  }

  @Override
  public void start() {
    System.out.println("GreetingPlugin.start()");
  }

  @Override
  public void stop() {
    System.out.println("GreetingPlugin.stop()");
    super.stop();
  }

  @Override
  protected ApplicationContext createApplicationContext() {
    var applicationContext = new AnnotationConfigApplicationContext();
    applicationContext.setClassLoader(getWrapper().getPluginClassLoader());
    applicationContext.register(PluginConfiguration.class);
    applicationContext.refresh();

    return applicationContext;
  }


  @Extension(ordinal = 1)
  public static class GreetingExtension implements Greeting {

    private final GreetingProvider provider;

    @Autowired
    public GreetingExtension(final GreetingProvider provider) {
      this.provider = provider;
    }

    @Override
    public String greeting() {
      try {
        var doc = Jsoup.connect("https://www.ali213.net/").timeout(3000).get();
        System.out.println(doc.body().toString());
      } catch (IOException e) {
        e.printStackTrace();
      }
      return provider.say();
    }
  }

}
