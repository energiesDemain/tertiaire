package com.ed.cgdd.derby.model.calcconso;

import java.math.BigDecimal;

public class ParamPartSysPerfEcs {

	private String id_energie;
	private BigDecimal[] part = new BigDecimal[6];

	public String getIdenergie() {
		return id_energie;
	}

	public void setIdenergie(String id_energie) {
		this.id_energie = id_energie;
	}

	public BigDecimal[] getPart() {
		return part;
	}

	public BigDecimal getPart(int index) {
		return part[index];
	}

	public void setPart(int index, BigDecimal valeurPart) {
		this.part[index] = valeurPart;
	}

}
