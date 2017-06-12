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
package com.github.jjYBdx4IL.aop.tx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import org.h2.Driver;
import org.h2.engine.Constants;
import org.h2.tools.Server;
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
    private Server h2FrontendServer = null;
    public static final int H2_FRONTEND_PORT = 8083;

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

            try {
                startH2Frontend();
            } catch (IOException | SQLException ex) {
                LOG.error("failed to start h2 frontend", ex);
            }
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

            stopH2Frontend();
            LOG.info("stopped h2 frontend");
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

    public void releaseEntityManager() {
        EntityManager em = entityManagers.get();
        if (em == null) {
            LOG.error("trying to release unset entity manager");
            return;
        }
        try {
            if (em.isOpen()) {
                try {
                    EntityTransaction transaction = em.getTransaction();
                    if (transaction.isActive()) {
                        LOG.error("open transaction found, rolling back");
                        transaction.rollback();
                    }
                } finally {
                    em.close();
                }
            }
        } finally {
            entityManagers.set(null);
        }
    }

    private synchronized void startH2Frontend() throws IOException, SQLException {
        // inject connection settings into frontend config
        Properties webServerProps = new Properties();
        webServerProps.put("0", String.format(Locale.ROOT, "Generic H2 (Embedded)|%s|%s",
                Driver.class.getName(),
                props.get(AvailableSettings.JPA_JDBC_URL).replace("\\", "\\\\").replace(":", "\\:")));

        String dbLoc = props.get(AvailableSettings.JPA_JDBC_URL).replaceFirst("^[^:]*:[^:]*:", "");
        if (dbLoc.contains(";")) {
            dbLoc = dbLoc.substring(0, dbLoc.indexOf(";"));
        }
        if (dbLoc.contains("?")) {
            dbLoc = dbLoc.substring(0, dbLoc.indexOf("?"));
        }
        File dbDir = new File(dbLoc).getParentFile();
        // put h2 frontend config in same directory as the database:
        File h2FrontendConfigFile = new File(dbDir, Constants.SERVER_PROPERTIES_NAME);

        try (OutputStream os = new FileOutputStream(h2FrontendConfigFile)) {
            webServerProps.store(os, "");
        }

        // use -Dh2.bindAddress=localhost to force binding to your localhost interface!
        
        h2FrontendServer = new Server();
        h2FrontendServer.runTool(
                "-web",
                "-webPort", Integer.toString(H2_FRONTEND_PORT),
                "-ifExists",
                "-baseDir", dbDir.getAbsolutePath(),
                "-properties", dbDir.getAbsolutePath());

        LOG.info("H2 frontend available on localhost:" + H2_FRONTEND_PORT);
    }

    private void stopH2Frontend() {
        if (h2FrontendServer != null) {
            h2FrontendServer.stop();
        }
    }
}
