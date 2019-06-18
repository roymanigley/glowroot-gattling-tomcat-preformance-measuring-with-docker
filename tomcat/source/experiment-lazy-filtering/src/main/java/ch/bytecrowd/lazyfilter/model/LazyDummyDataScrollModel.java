package ch.bytecrowd.lazyfilter.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public class LazyDummyDataScrollModel extends LazyDataModel<Dummy> {

	private static final long serialVersionUID = 789L;
	private static final Logger LOG = Logger.getLogger(LazyDummyDataScrollModel.class);

	private EntityManager em;

	public LazyDummyDataScrollModel(EntityManager em) {
		this.em = em;
	}

	@Override
	public Dummy getRowData(String rowKey) {
		return em.find(Dummy.class, Long.valueOf(rowKey));
	}

	@Override
	public Object getRowKey(Dummy car) {
		return car.getId();
	}

	@Override
	public List<Dummy> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {

		Query query = createQuery(first, pageSize, sortField, sortOrder, filters);
		this.setRowCount(query.getMaxResults());
		List<Dummy> resultList = query.setFirstResult(first).setMaxResults(pageSize).getResultList();
		
		LOG.info("resultList.size(): " + resultList.size());
		return resultList; 
	}

	private Query createQuery(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		Query query = null;
		String hql = "FROM Dummy d \n/*FILTER*/ \n/*ORDER_BY*/";
		// sort
		hql = appendOrderBy(sortField, sortOrder, hql);
		// filter
		hql = appendFilter(filters, hql);

		query = em.createQuery(hql);
		putParametersToQuery(filters, query);

		LOG.info(String.format("\nHQL: %s\nfirstResult: %s\nmaxResult: %s", hql, first, pageSize));
		return query;
	}

	private String appendFilter(Map<String, Object> filters, String hql) {
		if (filters != null && !filters.isEmpty()) {
			String hqlFilter = "";
			boolean first = true;
			for (Iterator<String> it = filters.keySet().iterator(); it.hasNext();) {
				String filterProperty = it.next();
				hqlFilter += first ? "WHERE " : "\n\tAND ";
//				if (filterProperty.equals("globalFilter"))
//					hqlFilter += "d.name LIKE :name";
//				else
				hqlFilter += String.format("d. %s LIKE :%s", filterProperty, filterProperty);
				first = false;
			}
			return hql.replaceAll("/\\*FILTER\\*/", hqlFilter);
		} else {
			return hql.replaceAll("\\s/\\*FILTER\\*/", "");
		}
	}

	private String appendOrderBy(String sortField, SortOrder sortOrder, String hql) {
		if (sortField != null && sortOrder != null) {
			return hql.replaceAll("/\\*ORDER_BY\\*/", String.format("ORDER BY d.%s %s", sortField,
					(sortOrder.equals(SortOrder.DESCENDING) ? "DESC" : "ASC")));
		} else {
			return hql.replaceAll("\\s/\\*ORDER_BY\\*/", "");
		}
	}

	private void putParametersToQuery(Map<String, Object> filters, Query query) {
		if (filters != null && !filters.isEmpty()) {
			for (Iterator<String> it = filters.keySet().iterator(); it.hasNext();) {
				String filterProperty = it.next();
				Object filterValue = filters.get(filterProperty);
				if (LOG.isDebugEnabled())
					LOG.debug(String.format("%s : %s", filterProperty, filterValue));
//				if (filterProperty.equals("globalFilter"))
//					query.setParameter("name", String.format("%%%s%%", filterValue));
				else
					query.setParameter(filterProperty, String.format("%%%s%%", filterValue));
			}
		}
	}

}