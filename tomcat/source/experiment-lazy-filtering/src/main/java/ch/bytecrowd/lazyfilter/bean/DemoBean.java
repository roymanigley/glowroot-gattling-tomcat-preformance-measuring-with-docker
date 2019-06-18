package ch.bytecrowd.lazyfilter.bean;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import ch.bytecrowd.lazyfilter.model.Dummy;
import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;

import ch.bytecrowd.lazyfilter.model.LazyDummyDataScrollModel;

@Named("dummyBean")
@ViewScoped
public class DemoBean implements Serializable {

	private static final long serialVersionUID = -5703244300384213407L;
	private static final Logger LOG = Logger.getLogger(DemoBean.class);

	@Inject
	private EntityManager em;
	
	private long resultSize = 0L;

	private Dummy selected = new Dummy();
	private LazyDataModel<Dummy> lazyScrollingModel;

	@PostConstruct
	public void init() {
		lazyScrollingModel = new LazyDummyDataScrollModel(em);
		
	}

	public void createTestDummies(int recordsToCreate) {
		try {
			em.getTransaction().begin();
			Dummy d = new Dummy();
			for (int i = 0; i < recordsToCreate; i++) {
				d.setName(new BigInteger(40, new SecureRandom()).toString(32));
				em.merge(d);
				if (i % 10 == 0)
					em.flush();
				LOG.info(String.format("merged %s records", i));
			}
			em.getTransaction().commit();
			LOG.info(String.format("created %s records", recordsToCreate));
		} catch (Exception e) {
			LOG.error("could not create records", e);
			if (em.getTransaction().isActive())
				em.getTransaction().rollback();
		}
		initResultSize();
	}
	
	private void initResultSize() {
		resultSize = (long) em.createQuery("SELECT COUNT(d) FROM Dummy d").getSingleResult();
	}
	
	public void onRowSelect(SelectEvent event) {
		Dummy d = (Dummy) event.getObject();
		LOG.info(d);
		FacesContext.getCurrentInstance().addMessage(
				"", 
				new FacesMessage("Selected", d.toString()));
	}

	public LazyDataModel<Dummy> getLazyScrollingModel() {
		return lazyScrollingModel;
	}

	public Dummy getSelected() {
		return selected;
	}

	public void setSelected(Dummy selected) {
		this.selected = selected;
	}

	public Long getResultSize() {
		if (resultSize == 0L)
			initResultSize();
		return resultSize;
	}

}
