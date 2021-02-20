package api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This class is required for spring boot plugin to build a fat jar,
 * but it should be excluded in plugin's zip file
 */
@SpringBootApplication
public class PluginSpringApp {

    public static void main(String[] args) {
        System.out.println("Run PluginSpringApp...");
        SpringApplication.run(PluginSpringApp.class);
    }
}
