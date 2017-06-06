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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 *
 * @author jjYBdx4IL
 */
@ServletAnno
public class RemoteServiceServletChild extends RemoteServiceServlet implements TestCounter {

    private int test = 0;

    @Override
    public void inc() {
        test++;
    }

    @Override
    public int get() {
        return test;
    }

}
