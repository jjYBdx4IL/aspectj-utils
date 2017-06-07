# aspectj-utils

Contains a META-INF/aop.xml generator (AOPXMLGenerator) for AspectJ load-time weaving (LTW).

The generator is controlled through annotations indicating the root
of the classes to be weaved, and aspects will be added by scanning
the classpath for @Aspect annotations. This scanning process can be limited
to specific classes directories.

This generator is intended as an alternative to aspectj-maven-plugin which
implements compile-time weaving and thus may provide more flexibility in
certain use cases.

See [class docs](aspectj-ltw-configurator/src/main/java/com/github/jjYBdx4IL/aspectj/utils/AOPXMLGenerator.java) for more details.

## Note on AspectJ annotation-based pointcuts/runtime tests

If using a classloader hierarchy, make sure you don't load classes twice, ie. in parent and child
loaders. Example: Jetty WebApp. Add tx manager to Jetty parent, declare tx api as provided
in your webapp! Otherwise, due to Jetty's default handling of such cases, you will end up with
two annotation classes and AspectJ might not be able to match them. This is particularly painful
because AspectJ runtime tests don't seem to have any verbose option.
