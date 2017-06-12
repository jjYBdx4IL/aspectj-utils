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
package com.github.jjYBdx4IL.aop.aspectj.examples;

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
        // Beware! don't forget to allow weaving of superclasses if the weaved method is not directly declared in your
        // own class definition!
        includesWithin = {"com.github.jjYBdx4IL.aop.aspectj.examples..*"},
        debug = true
)
public class AspectDefinitions {

    @After("execution(* ParentsParent+.parentMethod(..)) && this(foo)")
    public void testSuperMethodCall(Parent foo) {
        foo.testSuperMethodCall++;
    }
    
    @After("@this(TypeAnno) && execution(* ParentsParent+.parentMethod(..)) && this(foo)")
    public void testSuperMethodCallViaSubClassAnnotation(Parent foo) {
        foo.testSuperMethodCallViaSubClassAnnotation++;
    }
}
