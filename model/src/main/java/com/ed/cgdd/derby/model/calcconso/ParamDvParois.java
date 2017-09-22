package com.ed.cgdd.derby.model.calcconso;

import java.math.BigDecimal;

import com.ed.cgdd.derby.model.parc.TypeRenovBati;

public class ParamDvParois {

	private TypeRenovBati geste;

	public TypeRenovBati getGeste() {
		return geste;
	}

	public void setGeste(TypeRenovBati geste) {
		this.geste = geste;
	}

	public BigDecimal getDureeVie() {
		return dureeVie;
	}

	public void setDureeVie(BigDecimal dureeVie) {
		this.dureeVie = dureeVie;
	}

	private BigDecimal dureeVie;

}
