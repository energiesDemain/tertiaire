package com.ed.cgdd.derby.model.parc;


public class ParcResulat{
	
	

/* Extraction table parc_resultats */
private String branche;
private String sousBranche;
private String occupation;
private String periodeDetail;
private String periodeSimple;
private String energieChauffage;
private String anneeRenov;
private String typeRenov;
private Double annee; 
private Double surface;


	public String getAnneeRenov() {
		return anneeRenov;
	}
	public void setAnneeRenov(String anneeRenov) {
		this.anneeRenov = anneeRenov;
	}
	public Double getAnnee() {
		return annee;
	}
	public void setAnnee(Double annee) {
		this.annee = annee;
	}
	public String getTypeRenov() {
		return typeRenov;
	}
	public void setTypeRenov(String typeRenov) {
		this.typeRenov = typeRenov;
	}
	public Double getSurface() {
		return surface;
	}
	public void setSurface(Double surface) {
		this.surface = surface;
	}	
	public String getBranche() {
		return branche;
	}
	public void setBranche(String branche) {
		this.branche = branche;
	}
	public String getSousBranche() {
		return sousBranche;
	}
	public void setSousBranche(String sousBranche) {
		this.sousBranche = sousBranche;
	}
	public String getOccupation() {
		return occupation;
	}
	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}
	public String getPeriodeDetail() {
		return periodeDetail;
	}
	public void setPeriodeDetail(String periodeDetail) {
		this.periodeDetail = periodeDetail;
	}
	public String getPeriodeSimple() {
		return periodeSimple;
	}
	public void setPeriodeSimple(String periodeSimple) {
		this.periodeSimple = periodeSimple;
	}
	public String getEnergieChauffage() {
		return energieChauffage;
	}
	public void setEnergieChauffage(String energieChauffage) {
		this.energieChauffage = energieChauffage;
	}

	
	
}
