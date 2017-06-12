/*
 * Copyright © 2017 jjYBdx4IL (https://github.com/jjYBdx4IL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jjYBdx4IL.aspectj.utils.mavenplugin;

import com.github.jjYBdx4IL.aspectj.utils.AopXmlGenerator;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import javax.xml.bind.JAXBException;

@Mojo(
        name = "generate-ltw-config",
        defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        requiresDependencyResolution = ResolutionScope.COMPILE
    )
public class GenerateLtwConfigMojo
        extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true, readonly = true)
    private File outputDirectory;

    /**
     * Execute() for goal "generate-ltw-config".
     * 
     * @throws MojoExecutionException on failure
     */
    public void execute() throws MojoExecutionException {
        AopXmlGenerator generator = new AopXmlGenerator();
        generator.setEnableVerbose(getLog().isDebugEnabled());
        try {
            ClassLoader cl = createClassLoader(project.getCompileClasspathElements());
            generator.setOverrideClassLoaders(cl);
        } catch (MalformedURLException | DependencyResolutionRequiredException ex) {
            throw new MojoExecutionException("class loader setup failed", ex);
        }
        generator.addAllowedClassesDir(outputDirectory.getAbsolutePath());
        String outputFilename = new File(outputDirectory, "META-INF/aop.xml").getAbsolutePath();
        try {
            getLog().info("creating " + outputFilename);
            generator.saveTo(outputFilename);
        } catch (IOException | JAXBException ex) {
            throw new MojoExecutionException("failed to save " + outputFilename, ex);
        }
    }

    protected URLClassLoader createClassLoader(List<String> resources) throws MalformedURLException {
        URL[] urls = new URL[resources.size()];
        for (int i = 0; i < resources.size(); i++) {
            URL url = new File(resources.get(i)).toURI().toURL();
            getLog().debug("adding to scan classpath: " + url.toExternalForm());
            urls[i] = url;
        }
        return new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
    }

}
