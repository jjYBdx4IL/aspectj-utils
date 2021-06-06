# aspectj-utils

[![Build Status](https://travis-ci.org/jjYBdx4IL/aspectj-utils.png?branch=master)](https://travis-ci.org/jjYBdx4IL/aspectj-utils)

Contains:

* a META-INF/aop.xml generator (AopXmlGenerator) for AspectJ load-time weaving (LTW).
* an annotation-based transaction manager, similar to Spring but without pulling an entire framework into your
services. [Here is an example](https://github.com/jjYBdx4IL/example-maven-project-setups/tree/9bd2dd2e9dda97e2665404e608b56a8607cf307d/gwt-example)
using both.

The generator is controlled through annotations indicating the root
of the classes to be weaved, and aspects will be added by scanning
the classpath for @Aspect annotations. This scanning process can be limited
to specific classes directories.

This generator is intended as an alternative to aspectj-maven-plugin which
implements compile-time weaving and thus may provide more flexibility in
certain use cases.

See [class docs](aspectj-ltw-configurator/src/main/java/com/github/jjYBdx4IL/aspectj/utils/AopXmlGenerator.java) for more details.

## Note on AspectJ annotation-based pointcuts/runtime tests

If using a classloader hierarchy, make sure you don't load classes twice, ie. in parent and child
loaders. Example: Jetty WebApp. Add tx manager to Jetty parent, declare tx api as provided
in your webapp! Otherwise, due to Jetty's default handling of such cases, you will end up with
two annotation classes and AspectJ might not be able to match them. This is particularly painful
because AspectJ runtime tests don't seem to have any verbose option.

## Warning

There may be open CVEs as this project is largely unmaintained.



--
devel/java/github/aspectj-utils@7872
