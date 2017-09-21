package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class Maintenance {
	private String IdSysteme;
	private BigDecimal Part;

	public String getIdSysteme() {
		return IdSysteme;
	}

	public void setIdSysteme(String idSysteme) {
		IdSysteme = idSysteme;
	}

	public BigDecimal getPart() {
		return Part;
	}

	public void setPart(BigDecimal part) {
		Part = part;
	}

}
