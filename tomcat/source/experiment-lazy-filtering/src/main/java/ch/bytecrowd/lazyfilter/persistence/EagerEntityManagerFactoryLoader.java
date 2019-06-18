package ch.bytecrowd.lazyfilter.persistence;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;

public class EagerEntityManagerFactoryLoader {

	void init(@Observes @Initialized(ApplicationScoped.class) Object event) {
		EntityManagerProducer.initEntityManagerFactory();
	}

	void destroy(@Observes @Destroyed(ApplicationScoped.class) Object event) {
		EntityManagerProducer.closeEntityManagerFactory();
	}
}
