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
package com.github.jjYBdx4IL.aop.tx;

import com.github.jjYBdx4IL.aspectj.utils.AspectJWeaveConfig;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * from: https://eclipse.org/aspectj/doc/next/adk15notebook/ataspectj-pcadvice.html
 *
 * @author jjYBdx4IL
 */
@Aspect
@AspectJWeaveConfig(
        includesWithin = {
            "com.google.gwt.user.server.rpc..*Servlet",
            "javax.servlet..*Servlet",
            "org.eclipse.jetty.websocket.servlet..*Servlet",
            "*..TxAspect"},
        verbose = true,
        showWeaveInfo = true,
        weaveJavaxPackages = true,
        debug = false,
        noInline = false,
        reweavable = false
)
public class TxAspect {

    private static final Logger LOG = LoggerFactory.getLogger(Aspect.class);

    @Around("get(@TxEM javax.persistence.EntityManager ((@Tx *)+).*) && within(@Tx *) && this(foo)")
    public Object injectEntityManager(ProceedingJoinPoint thisJoinPoint, Object foo) throws Throwable {
        thisJoinPoint.proceed();
        LOG.info("injecting entity manager");
        return TxManager.getSingleton().getEntityManager(foo);
    }

    // execution(public * ((@Transactional *)+).*(..)) && within(@Transactional *)
    
    @Before("@this(Tx) && execution(* javax.servlet.GenericServlet.init()) && this(foo)")
    public void beforeServletInit(Object foo) {
    	LOG.info("beforeServletInit " + foo);
      	TxManager.getSingleton().getEntityManagerFactory(foo);
    }

    @After("@this(Tx) && execution(* javax.servlet.GenericServlet.destroy()) && this(foo)")
    public void afterServletDestroy(Object foo) {
        LOG.info("afterServletDestroy " + foo);
        TxManager.getSingleton().releaseEntityManagerFactory(foo);
    }

    @Around("execution(@Tx * *..*(..)) && within(@Tx *) && this(foo)")
    public Object handleTx(ProceedingJoinPoint thisJoinPoint, Object foo) throws Throwable {
        LOG.info("handle tx");
        // start tx
        EntityManager em = TxManager.getSingleton().getEntityManager(foo);
        EntityTransaction transaction = em.getTransaction();
        if (!transaction.isActive()) {
            transaction.begin();
        }

        try {
            Object result = thisJoinPoint.proceed();
            transaction.commit();
            return result;
        } catch (Throwable ex) {
            transaction.rollback();
            throw ex;
        }
    }
  
    @Around("execution(@TxRO * *..*(..)) && within(@Tx *) && this(foo)")
    public Object handleTxRO(ProceedingJoinPoint thisJoinPoint, Object foo) throws Throwable {
        LOG.info("handle tx read-only");
        // start tx
        EntityManager em = TxManager.getSingleton().getEntityManager(foo);
        EntityTransaction transaction = em.getTransaction();
        transaction.setRollbackOnly();
        if (!transaction.isActive()) {
            transaction.begin();
        }

        try {
            return thisJoinPoint.proceed();
        } finally {
            transaction.rollback();
        }
    }
  
}