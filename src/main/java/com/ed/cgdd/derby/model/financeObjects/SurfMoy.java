package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class SurfMoy {
	private String Id;
	private BigDecimal SurfMoy;

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public BigDecimal getSurfMoy() {
		return SurfMoy;
	}

	public void setSurfMoy(BigDecimal surfMoy) {
		SurfMoy = surfMoy;
	}

}
