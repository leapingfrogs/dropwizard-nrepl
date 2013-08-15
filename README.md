# dropwizard-nrepl

A [Dropwizard](http://dropwizard.codahale.com) Bundle which registers an [nrepl](https://github.com/clojure/tools.nrepl)
server when added to the Bootstrap.

## Usage

In your Service implementation simply add the following to your initialize method:

```java
import com.leapingfrogs.dropwizard.NReplBundle;

@Override
public void initialize(Bootstrap<TestConfiguration> bootstrap) {
    bootstrap.addBundle(new NReplBundle(8082));
}
```

That will run the server at launch, however that's not much use unless all your state is global scope.  To be able to
access the state of your running application you need to go one step further.  Typically in a dropwizard application
most of the top level objects are defined within the run method of your service and injected into the resources from
there.

The following example gives a very simplistic example of how this can work:

```java
import com.leapingfrogs.dropwizard.NReplBundle;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

public class TestService extends Service<TestConfiguration> {
    private NReplBundle nReplBundle;

    @Override
    public void initialize(Bootstrap<TestConfiguration> bootstrap) {
        nReplBundle = new NReplBundle(8082);
        bootstrap.addBundle(nReplBundle);
    }

    @Override
    public void run(TestConfiguration configuration, Environment environment) throws Exception {
        Map<String, String> state = new Map<>();
        environment.addResource(new TestResource(state));

        // Register other objects we want access to from the repl:
        // NOTE: The string name used as a key will become a keyword in the repl's state map
        nReplBundle.put("configuration", configuration);
        nReplBundle.put("state", state);
    }

    public static void main(String[] args) throws Exception {
        new TestService().run(args);
    }
}
```

Once you have a server running you can connect to it simply from lein and jump into the namespace using:

```
#> lein repl :connect 8082
Connecting to nREPL at 127.0.0.1:8082
REPL-y 0.2.1
Clojure 1.5.1
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
    Exit: Control+D or (exit) or (quit)

user=> (in-ns 'dropwizard-nrepl.core)
```

You can now view the state of the application through the 'state' atom like this:

```
user=> (bean (:configuration @state))
{:loggingConfiguration #<LoggingConfiguration com.yammer.dropwizard.config.LoggingConfiguration@a39e50f>, :httpConfiguration #<HttpConfiguration com.yammer.dropwizard.config.HttpConfiguration@98b4587>, :class TestConfiguration}
```

Bean is a method in clojure which will give you a nicer print out than the default java toString, it also fits better in the clojure view we have.


## License

Copyright © 2013 Ian Davies

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
