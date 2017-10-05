package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.model.parc.TypeRenovSysteme;

public class Geste {

	public Geste() {
		super();
	}

	private String idGesteAggreg;
	private String gesteNom; // concatener des gestes et de l'exigence
	private TypeRenovBati typeRenovBati; // nom du geste applique sur le bati
	private TypeRenovSysteme typeRenovSys; // nom du geste applique sur le
											// systeme
	private String reglementation; // reglementation appliquee
	private String energie; // energie utilisee pour le nouvel equipement
	private Exigence exigence;
	private int dureeBati; // duree de vie de la renovation de l'enveloppe
	private int dureeSys; // duree de vie de la renovation du systeme
	private BigDecimal coutGesteBati = BigDecimal.ZERO; // le cout en �/m2 de
														// la
														// renovation
	private BigDecimal coutGesteSys = BigDecimal.ZERO; // le cout en �/m2 du
														// geste systeme

	private BigDecimal coutTravauxAddGeste; // le cout des travaux additionnels
	private BigDecimal coutMaintenance; // le cout de maintenance annuel
	
	private BigDecimal gainEner; // le gain d'�nergie par m2 en %
	private BigDecimal valeurCEE;
	private BigDecimal rdt; // rendement du systeme
	private String sysChaud;

	public Geste(Geste copy) {
		this.setIdGesteAggreg(copy.getIdGesteAggreg());
		this.setGesteNom(copy.getGesteNom());
		this.setTypeRenovSys(copy.getTypeRenovSys());
		this.setTypeRenovBati(copy.getTypeRenovBati());
		this.setReglementation(copy.getReglementation());
		this.setEnergie(copy.getEnergie());
		this.setExigence(Exigence.valueOf(copy.getExigence().name()));
		this.setDureeBati(copy.getDureeBati());
		this.setDureeSys(copy.getDureeSys());
		this.setSysChaud(copy.getSysChaud());

		if (copy.getCoutGesteBati() != null) {
			this.setCoutGesteBati(new BigDecimal(copy.getCoutGesteBati().toString()));
		}
		if (copy.getCoutGesteSys() != null) {
			this.setCoutGesteSys(new BigDecimal(copy.getCoutGesteSys().toString()));
		}
		if (copy.getCoutTravauxAddGeste() != null) {
			this.setCoutTravauxAddGeste(new BigDecimal(copy.getCoutTravauxAddGeste().toString()));
		}
		if (copy.getCoutMaintenance() != null) {
			this.setCoutMaintenance(new BigDecimal(copy.getCoutMaintenance().toString()));
		}
		if (copy.getGainEner() != null) {
			this.setGainEner(new BigDecimal(copy.getGainEner().toString()));
		}
		if (copy.getValeurCEE() != null) {
			this.setValeurCEE(new BigDecimal(copy.getValeurCEE().toString()));
		}
		if (copy.getRdt() != null) {
			this.setRdt(new BigDecimal(copy.getRdt().toString()));
		}

	}

	public String getIdGesteAggreg() {
		return idGesteAggreg;
	}

	public void setIdGesteAggreg(String idGesteAggreg) {
		this.idGesteAggreg = idGesteAggreg;
	}

	public TypeRenovSysteme getTypeRenovSys() {
		return typeRenovSys;
	}

	public void setTypeRenovSys(TypeRenovSysteme typeRenovSys) {
		this.typeRenovSys = typeRenovSys;
	}

	public String getGesteNom() {
		return gesteNom;
	}

	public void setGesteNom(String typeRenov) {
		this.gesteNom = typeRenov;
	}

	public TypeRenovBati getTypeRenovBati() {
		return typeRenovBati;
	}

	public void setTypeRenovBati(TypeRenovBati typeRenovBati) {
		this.typeRenovBati = typeRenovBati;
	}

	public String getEnergie() {
		return energie;
	}

	public void setEnergie(String energie) {
		this.energie = energie;
	}

	public Exigence getExigence() {
		return exigence;
	}

	public void setExigence(Exigence exigence) {
		this.exigence = exigence;
	}

	public int getDureeBati() {
		return dureeBati;
	}

	public void setDureeBati(int dureeBati) {
		this.dureeBati = dureeBati;
	}

	public int getDureeSys() {
		return dureeSys;
	}

	public void setDureeSys(int dureeSys) {
		this.dureeSys = dureeSys;
	}

	public BigDecimal getCoutGesteBati() {
		return coutGesteBati;
	}

	public void setCoutGesteBati(BigDecimal coutGesteBati) {
		this.coutGesteBati = coutGesteBati;
	}

	public BigDecimal getCoutGesteSys() {
		return coutGesteSys;
	}

	public void setCoutGesteSys(BigDecimal coutGesteSys) {
		this.coutGesteSys = coutGesteSys;
	}

	public BigDecimal getCoutTravauxAddGeste() {
		return coutTravauxAddGeste;
	}

	public void setCoutTravauxAddGeste(BigDecimal coutTravauxAddGeste) {
		this.coutTravauxAddGeste = coutTravauxAddGeste;
	}

	public BigDecimal getGainEner() {
		return gainEner;
	}

	public void setGainEner(BigDecimal gainEner) {
		this.gainEner = gainEner;
	}

	public BigDecimal getValeurCEE() {
		return valeurCEE;
	}

	public void setValeurCEE(BigDecimal valeurCEE) {
		this.valeurCEE = valeurCEE;
	}

	public BigDecimal getRdt() {
		return rdt;
	}

	public void setRdt(BigDecimal rdt) {
		this.rdt = rdt;
	}

	public String getSysChaud() {
		return sysChaud;
	}

	public void setSysChaud(String sysChaud) {
		this.sysChaud = sysChaud;
	}

	public String getReglementation() {
		return reglementation;
	}

	public void setReglementation(String reglementation) {
		this.reglementation = reglementation;
	}

	public Geste(String typeRenov) {
		super();
		this.gesteNom = typeRenov;
	}

	public BigDecimal getCoutMaintenance() {
		return coutMaintenance;
	}

	public void setCoutMaintenance(BigDecimal coutMaintenance) {
		this.coutMaintenance = coutMaintenance;
	}

}
