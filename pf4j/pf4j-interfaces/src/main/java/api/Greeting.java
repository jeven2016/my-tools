package api;

import org.pf4j.ExtensionPoint;


public interface Greeting extends ExtensionPoint {

    public String greeting();
}
