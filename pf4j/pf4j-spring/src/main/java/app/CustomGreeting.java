package app;

import api.Greeting;
import org.pf4j.Extension;

@Extension
public class CustomGreeting implements Greeting {

  @Override
  public String greeting() {
    return "custom";
  }
}
