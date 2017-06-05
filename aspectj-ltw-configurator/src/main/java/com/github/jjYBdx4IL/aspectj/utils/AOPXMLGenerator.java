/*
 * Copyright (C) 2017 jjYBdx4IL (https://github.com/jjYBdx4IL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jjYBdx4IL.aspectj.utils;

import com.github.jjYBdx4IL.aspectj.utils.jaxb.AOPXMLConfig;
import com.github.jjYBdx4IL.aspectj.utils.jaxb.AspectClass;
import com.github.jjYBdx4IL.aspectj.utils.jaxb.Include;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.MatchProcessorException;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.ClassAnnotationMatchProcessor;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.commons.io.FileUtils;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class generates load-time weaving configuration for AspectJ Weaver (enabled via -javaagent:aspectjweaver.jar jvm
 * command line option) that looks like this:
 *
 * <pre>{@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <aspectj>
 *     <aspects>
 *         <aspect name="com.github.jjYBdx4IL.maven.examples.aspectj.ContentGeneratorAspect"/>
 *         <aspect name="com.github.jjYBdx4IL.maven.examples.aspectj.fieldaccess.FieldAccessAspect"/>
 *     </aspects>
 *     <weaver options="-verbose -showWeaveInfo">
 *         <include within="com.github.jjYBdx4IL.maven.examples.aspectj..*"/>
 *     </weaver>
 * </aspectj>
 * }</pre>
 *
 * and is expected by AspectJ at the classpath location META-INF/aop.xml.
 *
 * The generator uses two annotations to achieve its goal: {@link AspectJWeaveRoot} and {@link Aspect}.
 *
 * The first one is a new annotation declared by this package and needs to be put on package info classes' package
 * declaration. It declares to the generator that the package should be used as a package root for weaving, ie. all
 * classes in its package and in sub-packages will be considered for weaving by AspectJ.
 *
 * Aspect configuration entries will be gathered by scanning for the {@link Aspect} annotation.
 *
 * This whole classpath scanning business can be limited to local classes directories, ie. to apply your generated
 * configuration only to your current project or module.
 *
 * @author jjYBdx4IL
 */
public class AOPXMLGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(AOPXMLGenerator.class);

    /**
     * aop.xml configuration generator for AspectJ load-time weaving (LTW).
     *
     * @param args first argument is output file path for aop.xml. Futher arguments are optional and may list allowed
     * classes directories. If none is given, no restriction is being made. If using inside a maven project, you
     * probably want to do something like
     *
     * <pre>..AOPXMLGenerator ${project.build.directory}/classes/META-INF/aop.xml ${project.build.directory}/classes</pre>
     *
     * using the exec-maven-plugin attached to the process-classes phase of your build.
     */
    public static void main(String[] args) {
        try {
            AOPXMLGenerator generator = new AOPXMLGenerator();
            for (int i = 1; i < args.length; i++) {
                generator.addAllowedClassesDir(args[i]);
            }
            generator.saveTo(args[0]);
        } catch (IOException | JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected List<String> allowedClasspathPrefixes = new ArrayList<>();
    protected String overrideClasspath = null;
    protected boolean enableVerbose = false;
    protected ClassLoader classLoader = null;
    protected List<ClassLoader> overrideClassLoaders = null;
    protected final AOPXMLConfig config = new AOPXMLConfig();

    public AOPXMLGenerator() {
    }

    /**
     *
     * @param classpathPrefix ie. ${project.build.directory}/classes if you only want to scan your current maven module
     * for {@link AspectJWeaveRoot} and {@link Aspect} annotations. Not needed if you use {@link AspectJWeaveConfig}
     * annotation because classpath entries containing it will be auto-selected for scanning.
     */
    public void addAllowedClassesDir(String classpathPrefix) {
        String moduleUriPrefix = new File(classpathPrefix).toURI().toString();
        LOG.debug("adding " + moduleUriPrefix + " to list of scanned class roots");
        allowedClasspathPrefixes.add(moduleUriPrefix);
    }

    /**
     *
     * @param outputFilename ie. ${project.build.directory}/classes/META-INF/aop.xml to put the resulting AspectJ LTW
     * configuration into your classes output directory and thereby into the resulting jar artifact.
     * @throws JAXBException
     * @throws IOException
     */
    public void saveTo(String outputFilename) throws JAXBException, IOException {
        if (outputFilename == null || outputFilename.isEmpty()) {
            throw new IllegalArgumentException("no output filename");
        }
        File outputFile = new File(outputFilename);
        String xml = createXML();
        if (LOG.isTraceEnabled()) {
            LOG.trace("writing to " + outputFile.getAbsolutePath() + ": " + xml);
        }
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }
        FileUtils.write(outputFile, xml, "UTF-8");
    }

    protected void processAspectJWeaveRootAnnotatedClass(Class<?> classRef) {
        if (!isAllowed(classRef)) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("skipping @AspectJWeaveRoot annotated class " + classRef.getName()
                        + " because of disallowed classpath location");
            }
            return;
        }

        String includeWithin = classRef.getName().replaceAll("\\.package-info$", "..*");
        if (LOG.isDebugEnabled()) {
            LOG.debug("adding weave include within: " + includeWithin
                    + " because package " + classRef.getName() + " is annotated with @AspectJWeaveRoot");
        }
        config.weaver.include.add(new Include(includeWithin));
    }

    protected void processAspectJWeaveConfigAnnotatedClass(Class<?> classRef) {
        String moduleUriPrefix = getClasspathPrefix(classRef);
        if (LOG.isDebugEnabled()) {
            LOG.debug("adding " + moduleUriPrefix + " to list of scanned class roots because @AspectJWeaveConfig"
                    + " was found in " + classRef.getName());
        }
        allowedClasspathPrefixes.add(moduleUriPrefix);

        AspectJWeaveConfig configAnno = classRef.getAnnotation(AspectJWeaveConfig.class);
        if (configAnno == null) {
            throw new RuntimeException("error while processing @AspectJWeaveConfig annotation in class "
                    + classRef.getName() + ": failed to read annotation");
        }
        for (String includeWithin : configAnno.includesWithin()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("adding weave include within: " + includeWithin + " because of @AspectJWeaveConfig"
                        + " parameter found in " + classRef.getName());
            }
            config.weaver.include.add(new Include(includeWithin));
        }
        if (configAnno.verbose()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("adding weave option: -verbose because of @AspectJWeaveConfig"
                        + " parameter found in " + classRef.getName());
            }
            config.weaver.appendOption("-verbose");
        }
        if (configAnno.showWeaveInfo()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("adding weave option: -showWeaveInfo because of @AspectJWeaveConfig"
                        + " parameter found in " + classRef.getName());
            }
            config.weaver.appendOption("-showWeaveInfo");
        }
    }

    protected void processAspectAnnotatedClass(Class<?> classRef) {
        if (!isAllowed(classRef)) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("skipping @Aspect annotated class " + classRef.getName()
                        + " because of disallowed classpath location");
            }
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("adding aspect class " + classRef.getName() + " because it is annotated with @Aspect");
        }
        config.aspects.aspect.add(new AspectClass(classRef.getName()));
    }

    public String createXML() throws JAXBException {

        ClassAnnotationMatchProcessor weaveRootProcessor = new ClassAnnotationMatchProcessor() {
            @Override
            public void processMatch(Class<?> classRef) {
                processAspectJWeaveRootAnnotatedClass(classRef);
            }
        };

        ClassAnnotationMatchProcessor weaveConfigProcessor = new ClassAnnotationMatchProcessor() {
            @Override
            public void processMatch(Class<?> classRef) {
                processAspectJWeaveConfigAnnotatedClass(classRef);
            }
        };

        ClassAnnotationMatchProcessor aspectProcessor = new ClassAnnotationMatchProcessor() {
            @Override
            public void processMatch(Class<?> classRef) {
                processAspectAnnotatedClass(classRef);
            }
        };

        FastClasspathScanner scanner = new FastClasspathScanner();
        scanner.verbose(enableVerbose);
        if (overrideClasspath != null) {
            scanner.overrideClasspath(overrideClasspath);
        }
        if (classLoader != null) {
            scanner.addClassLoader(classLoader);
        }
        if (overrideClassLoaders != null) {
            scanner.overrideClassLoaders(overrideClassLoaders.toArray(new ClassLoader[]{}));
        }
        scanner.matchClassesWithAnnotation(AspectJWeaveRoot.class, weaveRootProcessor);
        scanner.matchClassesWithAnnotation(AspectJWeaveConfig.class, weaveConfigProcessor);
        scanner.matchClassesWithAnnotation(Aspect.class, aspectProcessor);
        try {
            scanner.scan();
        } catch (MatchProcessorException ex) {
            throw new RuntimeException(ex.getExceptions().get(0));
        }

        // serialize config to XML
        JAXBContext jaxbContext = JAXBContext.newInstance(AOPXMLConfig.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jaxbMarshaller.marshal(config, baos);
        return baos.toString();
    }

    /**
     * Override the classpath to scan.
     *
     * @param overrideClasspath Use {@link File#pathSeparatorChar} to delimit the list of classpath entries.
     */
    public void setOverrideClasspath(String overrideClasspath) {
        this.overrideClasspath = overrideClasspath;
    }

    public void setEnableVerbose(boolean enableVerbose) {
        this.enableVerbose = enableVerbose;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setOverrideClassLoaders(ClassLoader... overrideClassLoaders) {
        this.overrideClassLoaders = Arrays.asList(overrideClassLoaders);
    }

    protected boolean isAllowed(Class<?> classRef) {
        String fullResourcePath = getFullResourcePath(classRef);
        boolean allowed = allowedClasspathPrefixes.isEmpty();
        for (String moduleUriPrefix : allowedClasspathPrefixes) {
            if (fullResourcePath.startsWith(moduleUriPrefix)) {
                allowed = true;
            }
        }
        return allowed;
    }

    protected String getClasspathPrefix(Class<?> classRef) {
        String fullResourcePath = getFullResourcePath(classRef);
        return fullResourcePath.substring(0, fullResourcePath.length() - classRef.getName().length() - 6);
    }

    protected String getFullResourcePath(Class<?> classRef) {
        ClassLoader cl = classRef.getClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        String classResourceFileName = classRef.getName().replace('.', '/') + ".class";
        return cl.getResource(classResourceFileName).toString();
    }
}
