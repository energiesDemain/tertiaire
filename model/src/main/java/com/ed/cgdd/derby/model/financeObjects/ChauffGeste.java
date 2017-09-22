package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

import com.ed.cgdd.derby.model.parc.TypeRenovBati;

public class ChauffGeste {

	private String idParcNew; // idParc du noveau segment
	private String idParcInit; // idParc du segment initial
	private BigDecimal gainEner; // gain obtenu sur le besoin de chauffage apres
	// renovation du bati
	private BigDecimal rdt; // rendement du systeme
	private BigDecimal part; // part de marche
	private TypeRenovBati typeRenovBat;

	public TypeRenovBati getTypeRenovBat() {
		return typeRenovBat;
	}

	public void setTypeRenovBat(TypeRenovBati typeRenovBat) {
		this.typeRenovBat = typeRenovBat;
	}

	public String getIdParcInit() {
		return idParcInit;
	}

	public void setIdParcInit(String idParcInit) {
		this.idParcInit = idParcInit;
	}

	public String getIdParcNew() {
		return idParcNew;
	}

	public void setIdParcNew(String idParcNew) {
		this.idParcNew = idParcNew;
	}

	public BigDecimal getGainEner() {
		return gainEner;
	}

	public void setGainEner(BigDecimal gainEner) {
		this.gainEner = gainEner;
	}

	public BigDecimal getRdt() {
		return rdt;
	}

	public void setRdt(BigDecimal rdt) {
		this.rdt = rdt;
	}

	public BigDecimal getPart() {
		return part;
	}

	public void setPart(BigDecimal part) {
		this.part = part;
	}

}
