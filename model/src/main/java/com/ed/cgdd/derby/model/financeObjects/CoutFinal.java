package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;
import java.util.List;

import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.model.parc.TypeRenovSysteme;

public class CoutFinal {
	private int dureeDeVie;
	private BigDecimal[] annuites;
	private BigDecimal coutIntangible;
	private BigDecimal rdtFin;
	private BigDecimal gainEner;
	private String energieFin;
	private String sysChaud;
	private String anneeRenovBat;
	private TypeRenovBati typeRenovBat;
	private String anneeRenovSys;
	private TypeRenovSysteme typeRenovSys;
	private String id;
	private BigDecimal coutGlobal;
	private List<ListeFinanceValeur> detailFinancement;
	private BigDecimal surfaceUnitaire;
	private String reglementation;

	public String getAnneeRenovBat() {
		return anneeRenovBat;
	}

	public void setAnneeRenovBat(String anneeRenovBat) {
		this.anneeRenovBat = anneeRenovBat;
	}

	public TypeRenovBati getTypeRenovBat() {
		return typeRenovBat;
	}

	public void setTypeRenovBat(TypeRenovBati typeRenovBat) {
		this.typeRenovBat = typeRenovBat;
	}

	public String getAnneeRenovSys() {
		return anneeRenovSys;
	}

	public void setAnneeRenovSys(String anneeRenovSys) {
		this.anneeRenovSys = anneeRenovSys;
	}

	public TypeRenovSysteme getTypeRenovSys() {
		return typeRenovSys;
	}

	public void setTypeRenovSys(TypeRenovSysteme typeRenovSys) {
		this.typeRenovSys = typeRenovSys;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getDureeDeVie() {
		return dureeDeVie;
	}

	public void setDureeDeVie(int dureeDeVie) {
		this.dureeDeVie = dureeDeVie;
	}

	public BigDecimal[] getAnnuites() {
		return annuites;
	}

	public void setAnnuites(BigDecimal[] annuites) {
		this.annuites = annuites;
	}

	public BigDecimal getCoutIntangible() {
		return coutIntangible;
	}

	public void setCoutIntangible(BigDecimal coutIntangible) {
		this.coutIntangible = coutIntangible;
	}

	public BigDecimal getRdtFin() {
		return rdtFin;
	}

	public void setRdtFin(BigDecimal rdtFin) {
		this.rdtFin = rdtFin;
	}

	public BigDecimal getGainEner() {
		return gainEner;
	}

	public void setGainEner(BigDecimal gainEner) {
		this.gainEner = gainEner;
	}

	public String getEnergieFin() {
		return energieFin;
	}

	public void setEnergieFin(String energieFin) {
		this.energieFin = energieFin;
	}

	public String getSysChaud() {
		return sysChaud;
	}

	public void setSysChaud(String sysChaud) {
		this.sysChaud = sysChaud;
	}

	public BigDecimal getCoutGlobal() {
		return coutGlobal;
	}

	public void setCoutGlobal(BigDecimal coutGlobal) {
		this.coutGlobal = coutGlobal;
	}

	public List<ListeFinanceValeur> getDetailFinancement() {
		return detailFinancement;
	}

	public void setDetailFinancement(List<ListeFinanceValeur> detailFinancement) {
		this.detailFinancement = detailFinancement;
	}

	public BigDecimal getSurfaceUnitaire() {
		return surfaceUnitaire;
	}

	public void setSurfaceUnitaire(BigDecimal surfaceUnitaire) {
		this.surfaceUnitaire = surfaceUnitaire;
	}

	public String getReglementation() {
		return reglementation;
	}

	public void setReglementation(String reglementation) {
		this.reglementation = reglementation;
	}

}
