package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;
import java.util.Collection;

import com.ed.cgdd.derby.model.parc.Segment;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.model.parc.TypeRenovSysteme;

public class PartMarcheRenov extends Segment {

	private BigDecimal part;
	private String anneeRenovBat; // annee courante si renov
	private String anneeRenovSys; // annee courante si renov
	private TypeRenovBati typeRenovBat; // nouvelle renov
	private TypeRenovSysteme typeRenovSys;// nouvelle renov
	private String sysChaud; // systeme chaud apres travaux
	private String energie; // energie apres travaux
	private BigDecimal gainEnerg; // gain sur le besoin en chauffage
	private BigDecimal rdt; // rendement du systeme chaud
	private Collection<ListeFinanceValeur> financements; // type de financement
	private BigDecimal surfaceUnitaire;
	private String id; // id initial du parc
	private String newId; // nouvel id (rempli dans le chauffageService)
	private String reglementation; // reglementation ayant pousse au passage a
									// l'acte

	
	public BigDecimal getRdt() {
		return rdt;
	}

	public void setRdt(BigDecimal rdt) {
		this.rdt = rdt;
	}

	public Collection<ListeFinanceValeur> getFinancements() {
		return financements;
	}

	public void setFinancements(Collection<ListeFinanceValeur> financements) {
		this.financements = financements;
	}

	public BigDecimal getSurfaceUnitaire() {
		return surfaceUnitaire;
	}

	public void setSurfaceUnitaire(BigDecimal surfaceUnitaire) {
		this.surfaceUnitaire = surfaceUnitaire;
	}

	public BigDecimal getGainEnerg() {
		return gainEnerg;
	}

	public void setGainEnerg(BigDecimal gainEnerg) {
		this.gainEnerg = gainEnerg;
	}

	public String getSysChaud() {
		return sysChaud;
	}

	public void setSysChaud(String sysChaud) {
		this.sysChaud = sysChaud;
	}

	public String getEnergie() {
		return energie;
	}

	public void setEnergie(String energie) {
		this.energie = energie;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public PartMarcheRenov() {
	}

	public TypeRenovBati getTypeRenovBat() {
		return typeRenovBat;
	}

	public void setTypeRenovBat(TypeRenovBati typeRenovBati) {
		this.typeRenovBat = typeRenovBati;
	}

	public TypeRenovSysteme getTypeRenovSys() {
		return typeRenovSys;
	}

	public void setTypeRenovSys(TypeRenovSysteme typeRenovSys) {
		this.typeRenovSys = typeRenovSys;
	}

	public BigDecimal getPart() {
		return part;
	}

	public void setPart(BigDecimal part) {
		this.part = part;
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

	public String getNewId() {
		return newId;
	}

	public void setNewId(String newId) {
		this.newId = newId;
	}

	public String getReglementation() {
		return reglementation;
	}

	public void setReglementation(String reglementation) {
		this.reglementation = reglementation;
	}

}
