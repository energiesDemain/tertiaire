package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.model.parc.TypeRenovSysteme;

public class ResultatsFinancements {
	String branche;
	String sysChaud;
	TypeRenovBati typeRenovBati;
	TypeRenovSysteme typeRenovSys;
	String anneeRenovBat;
	String anneeRenovSys;
	String reglementation;
	BigDecimal surface;
	BigDecimal coutInvestissement;
	BigDecimal valeurPBC;
	BigDecimal valeurPretBonif;
	BigDecimal valeurAides;

	public String getBranche() {
		return branche;
	}

	public void setBranche(String branche) {
		this.branche = branche;
	}

	public String getSysChaud() {
		return sysChaud;
	}

	public void setSysChaud(String sysChaud) {
		this.sysChaud = sysChaud;
	}

	public TypeRenovBati getTypeRenovBati() {
		return typeRenovBati;
	}

	public void setTypeRenovBati(TypeRenovBati typeRenovBati) {
		this.typeRenovBati = typeRenovBati;
	}

	public TypeRenovSysteme getTypeRenovSys() {
		return typeRenovSys;
	}

	public void setTypeRenovSys(TypeRenovSysteme typeRenovSys) {
		this.typeRenovSys = typeRenovSys;
	}

	public String getAnneeRenovBat() {
		return anneeRenovBat;
	}

	public void setAnneeRenovBat(String anneeRenovBat) {
		this.anneeRenovBat = anneeRenovBat;
	}

	public String getAnneeRenovSys() {
		return anneeRenovSys;
	}

	public void setAnneeRenovSys(String anneeRenovSys) {
		this.anneeRenovSys = anneeRenovSys;
	}

	public BigDecimal getSurface() {
		return surface;
	}

	public void setSurface(BigDecimal surface) {
		this.surface = surface;
	}

	public BigDecimal getCoutInvestissement() {
		return coutInvestissement;
	}

	public void setCoutInvestissement(BigDecimal coutInvestissement) {
		this.coutInvestissement = coutInvestissement;
	}

	public BigDecimal getValeurPBC() {
		return valeurPBC;
	}

	public void setValeurPBC(BigDecimal valeurPBC) {
		this.valeurPBC = valeurPBC;
	}

	public BigDecimal getValeurPretBonif() {
		return valeurPretBonif;
	}

	public void setValeurPretBonif(BigDecimal valeurPretBonif) {
		this.valeurPretBonif = valeurPretBonif;
	}

	public BigDecimal getValeurAides() {
		return valeurAides;
	}

	public void setValeurAides(BigDecimal valeurAides) {
		this.valeurAides = valeurAides;
	}

	public String getReglementation() {
		return reglementation;
	}

	public void setReglementation(String reglementation) {
		this.reglementation = reglementation;
	}

}
