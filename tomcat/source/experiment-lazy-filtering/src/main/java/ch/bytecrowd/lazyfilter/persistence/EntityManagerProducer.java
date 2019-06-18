package ch.bytecrowd.lazyfilter.persistence;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;

@ApplicationScoped
public class EntityManagerProducer {

	private static final Logger LOG = Logger.getLogger(EntityManagerProducer.class);
	private static EntityManagerFactory EMF = null;

	public static void initEntityManagerFactory() {
		LOG.info("init EntityManagerFactory");
		EMF =  Persistence.createEntityManagerFactory("pu");
	}
	
	public static void closeEntityManagerFactory() {
		LOG.info("EMF.close()");
		if (EMF.isOpen())
			EMF.close();
	}
	
	@Produces
	@RequestScoped
	public EntityManager createEntityManager() {
		LOG.info("create EntityManager");
		if (EMF == null)
			initEntityManagerFactory();
		return EMF.createEntityManager();
	}

	public void closeEntityManager(@Disposes EntityManager em) {
		LOG.info("em.close()");
		if (em.isOpen())
			em.close();
	}
}
