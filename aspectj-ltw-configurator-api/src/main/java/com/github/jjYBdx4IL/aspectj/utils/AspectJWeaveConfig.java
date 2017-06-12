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
package com.github.jjYBdx4IL.aspectj.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AspectJWeaveConfig.
 * @author jjYBdx4IL
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AspectJWeaveConfig {

    /**
     * Denote types to be weaved, ie. "a.b.c..*" for entire a.b.c package hierarchy.
     * @return array of class selectors in the form "com..*Test" etc.
     */
    String[] includesWithin() default {};
    /**
     * Classes to dump to disk before and after weaving.
     * @return array of class selectors in the form "com..*Test" etc.
     */
    String[] dumpWithin() default {};

    /**
     * -verbose option.
     * @return true to enable -verbose weaver option
     */
    boolean verbose() default false;
    /**
     * -showWeaveInfo option.
     * @return true to enable -showWeaveInfo weaver option
     */
    boolean showWeaveInfo() default false;
    /**
     * Enable weaving of javax.* packages.
     * @return true to enable weaving classes in javax.* packages
     */
    boolean weaveJavaxPackages() default false;
    /**
     * -noInline option.
     * @return true to enable -noInline weaver option
     */
    boolean noInline() default false;
    /**
     * -reweavable option.
     * @return true to enable -reweavable weaver option
     */
    boolean reweavable() default false;
    /**
     * -debug option.
     * @return true to enable -debug weaver option
     */
    boolean debug() default false;
}
