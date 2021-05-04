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
package tests;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(SimpleTest.class);
    
    @Test
    public void testGenerateLTWConfig() throws IOException {
        File generatedAopXml = new File(System.getProperty("basedir"), "target/classes/META-INF/aop.xml");
        
        assertTrue(generatedAopXml.exists());
        assertTrue(generatedAopXml.isFile());
        String xml = FileUtils.readFileToString(generatedAopXml, "UTF-8");
        assertNotNull(xml);
        
        assertTrue(xml.contains("simple.it.aspects.Aspect1"));
        assertTrue(xml.contains("simple.it.weaveroot..*"));
        
        LOG.info(xml);
    }
    
    @Test
    public void testGenerateMavenStartupConfig() throws IOException {
        File generatedJvmConfig = new File(System.getProperty("basedir"), ".mvn/jvm.config");
        
        assertTrue(generatedJvmConfig.exists());
        assertTrue(generatedJvmConfig.isFile());
        String contents = FileUtils.readFileToString(generatedJvmConfig, "ASCII");
        assertNotNull(contents);
        
        assertTrue(contents.contains("-javaagent:"));
        assertTrue(contents.contains("aspectjweaver"));
        
        LOG.info(contents);
    }
}