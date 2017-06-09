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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.h2.Driver;
import org.hibernate.boot.SchemaAutoTooling;
import org.hibernate.cfg.AvailableSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jjYBdx4IL
 */
public class TxManager {

    private static final Logger LOG = LoggerFactory.getLogger(TxManager.class);

    protected static TxManager txManagerSingleton = null;
    private final String jdbcUrl;
    protected static final String PU_NAME = "default";

    public static synchronized TxManager getSingleton() {
        if (txManagerSingleton == null) {
            try {
                txManagerSingleton = new TxManager();
            } catch (NamingException ex) {
                throw new RuntimeException(ex);
            }
        }
        return txManagerSingleton;
    }

    protected final Map<String, String> props = new HashMap<>();
    protected EntityManagerFactory entityManagerFactory = null;
    // keep track of who is using the EMF:
    protected Set<Object> emfUsedByRefs = new HashSet<>();
    protected ThreadLocal<EntityManager> entityManagers = new ThreadLocal<>();

    public TxManager() throws NamingException {
        InitialContext ic = new InitialContext();
        jdbcUrl = (String) ic.lookup("java:comp/env/jdbc/url");

        LOG.info("new " + TxManager.class.getName() + ", db url: " + jdbcUrl);
        props.put(AvailableSettings.HBM2DDL_AUTO, SchemaAutoTooling.UPDATE.name().toLowerCase(Locale.ROOT));
        props.put(AvailableSettings.SHOW_SQL, "true");
        props.put(AvailableSettings.JPA_JDBC_DRIVER, Driver.class.getName());
        props.put(AvailableSettings.JPA_JDBC_URL, jdbcUrl);
    }

    public synchronized EntityManagerFactory getEntityManagerFactory(Object usedBy) {
        LOG.info("getEntityManagerFactory() for " + usedBy);
        if (entityManagerFactory == null) {
            LOG.info("creating EntityManagerFactory singleton for persistence unit " + PU_NAME);
            entityManagerFactory = Persistence.createEntityManagerFactory(PU_NAME, props);
        }
        emfUsedByRefs.add(usedBy);
        LOG.info("EntityManagerFactory for persistence unit " + PU_NAME + " now in use by " + emfUsedByRefs.size() + " objects");
        return entityManagerFactory;
    }

    public synchronized void releaseEntityManagerFactory(Object usedBy) {
        LOG.info("releaseEntityManagerFactory() for " + usedBy);
        emfUsedByRefs.remove(usedBy);
        LOG.info("EntityManagerFactory for persistence unit " + PU_NAME + " now in use by " + emfUsedByRefs.size() + " objects");
        if (emfUsedByRefs.isEmpty() && entityManagerFactory != null) {
            LOG.info("closing EntityManagerFactory singleton for persistence unit " + PU_NAME + " because it is not in use any more");
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }

    public EntityManager getNewEntityManager(Object usedBy) {
        if (entityManagers.get() != null && entityManagers.get().isOpen()) {
            LOG.error("replacing unclosed entity manager for " + usedBy);
            entityManagers.get().close();
        }
        entityManagers.set(getSingleton().getEntityManagerFactory(usedBy).createEntityManager());
        return entityManagers.get();
    }
    
    public EntityManager getExistingEntityManager(Object usedBy) {
        return entityManagers.get();
    }
}
