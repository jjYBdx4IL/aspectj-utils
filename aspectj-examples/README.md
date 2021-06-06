# aspectj-examples

## A Little Warning

Make sure you understand what is described on https://eclipse.org/aspectj/doc/next/devguide/ltw-rules.html
very well. Here is a little example that drove me crazy when I didn't fully understand how AspectJ load-time weaving
(LTW) actually works:

consider the situation where you want to provide your own annotation-based transaction support to your
servlet classes. You annotate a class with @TxSupported and the methods to be wrapped with @TxRW and
@TxRO for read-write and read-only transaction respectively. Now you want to initialize global resources
like a global EntityManagerFactory singleton whenever such a servlet gets loaded and release the factory
when all of those servlets have been unloaded. For this, the javax.servlet.GenericServlet class
provides init() and destroy() methods that we want to weave. And here is the catch: although I
started my embedded Jetty instance with the jvm -javaagent:aspectjweaver.jar parameter, it simply
would refuse to weave those methods. In retrospect it's obvious: my aspect class was included within the
webapp, and the servlet infrastructure gets initialized by Jetty before loading the webapp stuff. So, while
aspectj weaver was active during all of Jetty operation, my aspect definition only got available and used
for weaving during webapp execution. And the way Jetty (and probably all other JavaEE servers) works,
is that the infrastructure classes get initialized only once, and definitely not by your webapp -
unless you do some deeply discouraged stuff.

## Conclusion

Don't try to weave external libraries unless it is for debugging purposes, or you are absolutely sure
how the classloading works for them. In my described case you could simply @Override the GenericServlet's
init() and destroy() methods in your own class and weave those. That's a little bit of overhead, but
it's definitely safer. Or you put your entire transaction handling into an independent module that you
can load with the Jetty instance.
