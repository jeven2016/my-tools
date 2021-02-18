package app;

import api.Greeting;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public class Greetings {

  @Autowired
  private List<Greeting> greetings;

  public void printGreetings() {
    System.out.println(String
        .format("Found %d extensions for extension point '%s'", greetings.size(),
            Greeting.class.getName()));
    for (Greeting greeting : greetings) {
      System.out.println(">>> " + greeting.greeting());
    }
  }

}