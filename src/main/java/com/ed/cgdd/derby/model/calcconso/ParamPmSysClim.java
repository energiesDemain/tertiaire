package com.ed.cgdd.derby.model.calcconso;

import java.math.BigDecimal;

public class ParamPmSysClim {
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIdSys() {
		return idSys;
	}

	public void setIdSys(String idSys) {
		this.idSys = idSys;
	}

	public BigDecimal getPart() {
		return part;
	}

	public void setPart(BigDecimal part) {
		this.part = part;
	}

	private String idSys;
	private BigDecimal part;

}
