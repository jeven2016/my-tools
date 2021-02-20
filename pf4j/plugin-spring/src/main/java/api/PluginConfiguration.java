package api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PluginConfiguration {

    @Bean
    public GreetingProvider greetingProvider() {
        return new GreetingProvider();
    }
}
