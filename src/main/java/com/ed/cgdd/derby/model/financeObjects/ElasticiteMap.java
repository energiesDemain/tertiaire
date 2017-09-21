package com.ed.cgdd.derby.model.financeObjects;

import java.util.HashMap;

public class ElasticiteMap {

	private HashMap<String, Elasticite> elasticite = new HashMap<String, Elasticite>();

	public HashMap<String, Elasticite> getElasticite() {
		return elasticite;
	}

	public void putElasticite(Elasticite value) {
		this.elasticite.put(value.getIdBranche() + value.getUsage() + value.getIdEnergie(), value);
	}

}
