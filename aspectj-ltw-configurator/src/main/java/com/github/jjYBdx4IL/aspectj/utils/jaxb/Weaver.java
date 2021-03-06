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
package com.github.jjYBdx4IL.aspectj.utils.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * DTO object for aop.xml generation via JAXB.
 * 
 * @author jjYBdx4IL
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Weaver {

    @XmlAttribute
    public String options = null;
    public List<Include> include = new ArrayList<>();
    public List<Dump> dump = new ArrayList<>();

    /**
     * Append a weaver config option.
     * 
     * @param option the weaver option, ie "-debug".
     */
    public void appendOption(String option) {
        if (option == null || option.isEmpty()) {
            throw new IllegalArgumentException("empty or null option string");
        }
        if (null == options) {
            options = "";
        }
        if (!options.isEmpty()) {
            options += " ";
        }
        options += option;
    }
}
