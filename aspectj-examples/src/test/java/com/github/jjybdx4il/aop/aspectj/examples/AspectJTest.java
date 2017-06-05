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

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author jjYBdx4IL
 */
public class AspectJTest {

    @Test
    public void testSuperMethodCall() {
        SubClass sc = new SubClass();
        assertEquals(0, sc.testSuperMethodCall);
        assertEquals(0, sc.testSuperMethodCallViaSubClassAnnotation);
        sc.parentMethod();
        assertEquals(1, sc.testSuperMethodCall);
        assertEquals(1, sc.testSuperMethodCallViaSubClassAnnotation);
    }
    
}
