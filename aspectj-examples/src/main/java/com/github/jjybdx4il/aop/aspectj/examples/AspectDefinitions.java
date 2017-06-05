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
package com.github.jjybdx4il.aop.aspectj.examples;

import com.github.jjYBdx4IL.aspectj.utils.AspectJWeaveConfig;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

/**
 *
 * @author jjYBdx4IL
 */
@Aspect
@AspectJWeaveConfig(
        showWeaveInfo = true,
        verbose = true,
        includesWithin = {"com.github.jjybdx4il.aop.aspectj.examples..*"}
)
public class AspectDefinitions {

    @After("execution(* Parent+.parentMethod(..)) && this(foo)")
    public void testSuperMethodCall(Parent foo) {
        foo.testSuperMethodCall++;
    }
    
    @After("@this(TypeAnno) && execution(* Parent+.parentMethod(..)) && this(foo)")
    public void testSuperMethodCallViaSubClassAnnotation(Parent foo) {
        foo.testSuperMethodCallViaSubClassAnnotation++;
    }
}
