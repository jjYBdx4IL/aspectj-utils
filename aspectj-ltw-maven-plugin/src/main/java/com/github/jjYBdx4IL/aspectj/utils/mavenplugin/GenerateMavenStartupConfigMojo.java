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
import org.apache.maven.project.MavenProject;
import java.io.PrintWriter;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.artifact.DefaultArtifactCoordinate;
import org.apache.maven.shared.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.artifact.resolve.ArtifactResolverException;
import org.codehaus.plexus.util.FileUtils;

@Mojo(
        name = "generate-maven-startup-config",
        defaultPhase = LifecyclePhase.INITIALIZE
)
public class GenerateMavenStartupConfigMojo
        extends AbstractMojo {

    public static final String MVN_JVM_CONFIG = ".mvn/jvm.config";

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${localRepository}", readonly = true, required = true)
    private ArtifactRepository localRepository;

    @Parameter(defaultValue = "${aspectjWeaverVersion}", readonly = true, required = true)
    private String aspectjWeaverVersion;

    /**
     * Remote repositories which will be searched for artifacts.
     */
    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true)
    private List<ArtifactRepository> remoteRepositories;

    /**
     * The Maven session
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

    @Component
    private ArtifactHandlerManager artifactHandlerManager;
    @Component
    private ArtifactResolver artifactResolver;

    public void execute() throws MojoExecutionException {
        try {
            final File cfg = new File(project.getBasedir(), MVN_JVM_CONFIG);
            
            ProjectBuildingRequest buildingRequest
                    = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
            
            buildingRequest.setRemoteRepositories(remoteRepositories);
            
            // Map dependency to artifact coordinate
            DefaultArtifactCoordinate coordinate = new DefaultArtifactCoordinate();
            coordinate.setGroupId("org.aspectj");
            coordinate.setArtifactId("aspectjweaver");
            coordinate.setVersion(aspectjWeaverVersion);
            coordinate.setClassifier(null);
            
            final String extension;
            ArtifactHandler artifactHandler = artifactHandlerManager.getArtifactHandler("jar");
            if (artifactHandler != null) {
                extension = artifactHandler.getExtension();
            } else {
                extension = "jar";
            }
            coordinate.setExtension(extension);
            
            Artifact artifact = artifactResolver.resolveArtifact(buildingRequest, coordinate).getArtifact();
            
            //String aspectjWeaverLocation = new File(localRepository.getBasedir(), localRepository.pathOf(artifact)).getAbsolutePath();
            String aspectjWeaverLocation = artifact.getFile().getAbsolutePath();
                    
            if (aspectjWeaverLocation == null) {
                throw new MojoExecutionException("failed to retrieve artifact " + artifact);
            }
            
            project.getProperties().setProperty("aspectjWeaverLocation", aspectjWeaverLocation);
            
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
        } catch (ArtifactResolverException ex) {
            throw new MojoExecutionException("", ex);
        }
    }

}
