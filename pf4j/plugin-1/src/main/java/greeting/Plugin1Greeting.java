package greeting;

import api.Greeting;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.pf4j.util.StringUtils;


public class Plugin1Greeting extends Plugin {

    /**
     * Constructor to be used by plugin manager for plugin instantiation.
     * Your plugins have to provide constructor with this exact signature to
     * be successfully loaded by manager.
     *
     * @param wrapper
     */
    public Plugin1Greeting(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        System.out.println("Plugin1Greeting.start()");
    }

    @Override
    public void stop() {
        System.out.println("Plugin1Greeting.stop()");
    }


    @Extension
    public static class PluginGreeting implements Greeting {
        @Override
        public String greeting() {
            return "Plugin1Greeting";
        }
    }

}
