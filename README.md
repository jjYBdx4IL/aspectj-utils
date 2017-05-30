# aspectj-utils

Contains an AOPXMLGenerator for AspectJ load-time weaving.

The generator is controlled through a new annotation indicating the root
of the classes to be weaved, and aspects will be added by scanning
the classpath for @Aspect annotations. This scanning process can be limited
to specific classes directories.

This generator is intended as an alternative to aspectj-maven-plugin which
implements compile-time weaving and thus may provide more flexibility in
certain use cases.

See [class docs](aspectj-ltw-configurator/src/main/java/com/github/jjYBdx4IL/aspectj/utils/AOPXMLGenerator.java) for more details.

