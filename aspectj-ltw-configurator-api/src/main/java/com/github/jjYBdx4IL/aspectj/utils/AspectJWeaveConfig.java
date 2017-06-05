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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author jjYBdx4IL
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AspectJWeaveConfig {

    /**
     * Denote types to be weaved, ie. "a.b.c..*" for entire a.b.c package hierarchy.
     *
     * @return
     */
    String[] includesWithin() default {};

    boolean verbose() default false;
    boolean showWeaveInfo() default false;
    boolean weaveJavaxPackages() default false;
    boolean noInline() default false;
    boolean reweavable() default false;
    boolean debug() default false;
}
