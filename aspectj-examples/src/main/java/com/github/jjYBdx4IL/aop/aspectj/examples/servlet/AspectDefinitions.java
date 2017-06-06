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
package com.github.jjYBdx4IL.aop.aspectj.examples.servlet;

import com.github.jjYBdx4IL.aspectj.utils.AspectJWeaveConfig;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jjYBdx4IL
 */
@Aspect
@AspectJWeaveConfig(
        showWeaveInfo = true,
        verbose = true,
        // Beware! don't forget to allow weaving of superclasses if the weaved method is not directly declared in your
        // own class definition!
        includesWithin = {"javax.servlet.GenericServlet"},
        weaveJavaxPackages = true
)
public class AspectDefinitions {

    private static final Logger LOG = LoggerFactory.getLogger(AspectDefinitions.class);
    
    @After("@this(com.github.jjYBdx4IL.aop.aspectj.examples.servlet.ServletAnno) && execution(* javax.servlet.GenericServlet.init()) && this(foo)")
    public void testSuperMethodCall(Object foo) {
        LOG.info(">>> POINTCUT: @this(ServletAnno) && exec(* GenericServlet.init()) " + foo);
        ((TestCounter)foo).inc();
    }
    
}
