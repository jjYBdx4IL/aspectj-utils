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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import java.io.PrintWriter;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.util.FileUtils;

@Mojo(
        name = "generate-maven-startup-config",
        defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class GenerateMavenStartupConfigMojo
        extends AbstractMojo {

    public static final String MVN_JVM_CONFIG = ".mvn/jvm.config";

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${localRepository}", readonly = true, required = true)
    private ArtifactRepository localRepository;

    public void execute() throws MojoExecutionException {
        File cfg = new File(project.getBasedir(), MVN_JVM_CONFIG);

        // determine project's aspectj version by looking for the org.aspectj:aspectjweaver artifact on the runtime
        // classpath
        String aspectjWeaverLocation = null;
        for (Artifact artifact : project.getArtifacts()) {
            if (!"org.aspectj".equals(artifact.getGroupId())) {
                continue;
            }
            if (!"aspectjweaver".equals(artifact.getArtifactId())) {
                continue;
            }
            if (!"jar".equals(artifact.getType())) {
                continue;
            }
            if (!(artifact.getClassifier() == null || artifact.getClassifier().isEmpty())) {
                continue;
            }
            if (!(Artifact.SCOPE_RUNTIME.equals(artifact.getScope()) || Artifact.SCOPE_COMPILE.equals(artifact.getScope()))) {
                continue;
            }
            aspectjWeaverLocation = new File(localRepository.getBasedir(), localRepository.pathOf(artifact)).getAbsolutePath();
        }
        if (aspectjWeaverLocation == null) {
            throw new MojoExecutionException("failed to determine aspectjweaver artifact; "
                    + "you need to have org.aspectj:aspectjweaver artifact as a runtime dependency!");
        }

        String contents = null;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (PrintWriter pw = new PrintWriter(baos)) {
                pw.append("-javaagent:" + aspectjWeaverLocation + " ");
                pw.append("-Daj.weaving.verbose=true" + System.lineSeparator());
            }
            contents = baos.toString();
        } catch (IOException ex) {
            throw new MojoExecutionException("", ex);
        }

        boolean needsUpdate = false;
        if (!cfg.exists()) {
            needsUpdate = true;
        } else {
            try {
                String currentContents = FileUtils.fileRead(cfg, "ASCII");
                if (!contents.equals(currentContents)) {
                    needsUpdate = true;
                }
            } catch (IOException ex) {
                throw new MojoExecutionException("", ex);
            }
        }

        if (!needsUpdate) {
            getLog().debug("no update needed for " + cfg.getAbsolutePath());
            return;
        }

        try {
            if (!cfg.getParentFile().exists()) {
                cfg.getParentFile().mkdir();
            }
            FileUtils.fileWrite(cfg, "ASCII", contents);
        } catch (IOException ex) {
            throw new MojoExecutionException("", ex);
        }

        getLog().info("JVM startup options for Maven/AspectJ Weaver written to " + cfg.getAbsolutePath() + ", "
                + "settings will take effect on next maven start");
    }

}
