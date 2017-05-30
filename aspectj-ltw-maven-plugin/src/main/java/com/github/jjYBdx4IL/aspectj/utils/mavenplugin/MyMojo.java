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
package com.github.jjYBdx4IL.aspectj.utils.mavenplugin;

import com.github.jjYBdx4IL.aspectj.utils.AOPXMLGenerator;
import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(
        name = "generate-ltw-config",
        defaultPhase = LifecyclePhase.PROCESS_CLASSES
)
public class MyMojo
        extends AbstractMojo {

    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true, readonly = true)
    private File outputDirectory;

    public void execute() throws MojoExecutionException {
        AOPXMLGenerator generator = new AOPXMLGenerator();
        generator.addAllowedClassesDir(outputDirectory.getAbsolutePath());
        String outputFilename = new File(outputDirectory, "META-INF/aop.xml").getAbsolutePath();
        try {
            getLog().info("creating " + outputFilename);
            generator.saveTo(outputFilename);
        } catch (IOException | JAXBException ex) {
            throw new MojoExecutionException("failed to save " + outputFilename, ex);
        }
    }

}
