/**
 * 
 */
package com.nextlabs.rms.persistence;

/**
 * @author nnallagatla
 *
 */
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.nextlabs.rms.config.GlobalConfigManager;

public class EntityManagerHelper {

    private static final EntityManagerFactory emf;
    private static final ThreadLocal<EntityManager> threadLocal;
    private static final String PERSISTENCE_UNIT = "RMS";
    
    static {    		
        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, GlobalConfigManager.getInstance().getDatabaseProperties());
        threadLocal = new ThreadLocal<EntityManager>();
    }
    

    public static EntityManager getEntityManager() {
        EntityManager em = threadLocal.get();

        if (em == null) {
            em = emf.createEntityManager();
            threadLocal.set(em);
        }
        return em;
    }

    public static void closeEntityManager() {
        EntityManager em = threadLocal.get();
        if (em != null) {
            em.close();
            threadLocal.set(null);
        }
    }

    public static void createEntityManagerFactory() {
    	//do nothing
    }
    
    public static void closeEntityManagerFactory() {
    	if (emf != null){
        	emf.close();
    	}
    }

    public static void beginTransaction() {
        getEntityManager().getTransaction().begin();
    }

    public static void rollback() {
    	if (getEntityManager().getTransaction().isActive()) {
    		getEntityManager().getTransaction().rollback();
    	}
    }

    public static void commit() {
    	if (getEntityManager().getTransaction().isActive()) {
    		getEntityManager().getTransaction().commit();
    	}
    }
}