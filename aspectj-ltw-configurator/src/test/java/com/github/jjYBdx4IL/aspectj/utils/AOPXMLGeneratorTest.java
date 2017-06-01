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

import com.github.jjYBdx4IL.aspectj.utils.testaspects.Aspect1;
import com.github.jjYBdx4IL.aspectj.utils.testroot.SomePotentiallyWeavedClass;
import org.junit.Test;
import com.github.jjYBdx4IL.utils.xml.XMLUtils;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jjYBdx4IL
 */
public class AOPXMLGeneratorTest {

    private static final Logger LOG = LoggerFactory.getLogger(AOPXMLGeneratorTest.class);
    
    /**
     * Test of createXML method, of class AOPXMLGenerator.
     */
    @Test
    public void test() throws Exception {
        AOPXMLGenerator generator = new AOPXMLGenerator();
//        generator.addAllowedClassesDir(new File(System.getProperty("basedir"), "target/test-classes").getAbsolutePath());
//        generator.setEnableVerbose(LOG.isDebugEnabled());
        String xml = generator.createXML();

        String expectedXml = String.format(Locale.ROOT, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<aspectj>\n"
                + "\n"
                + "    <aspects>\n"
                + "        <aspect name=\"%s\"/>\n"
                + "    </aspects>\n"
                + "\n"
                + "    <weaver options=\"-verbose -showWeaveInfo\">\n"
                + "        <include within=\"%s..*\"/>\n"
                + "        <include within=\"javax.servlet.GenericServlet\"/>\n"
                + "    </weaver>\n"
                + "\n"
                + "</aspectj>\n", Aspect1.class.getName(), SomePotentiallyWeavedClass.class.getPackage().getName());
        LOG.debug(xml);
        XMLUtils.assertEquals(expectedXml, xml);
    }

}
