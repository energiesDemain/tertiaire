package com.ed.cgdd.derby.model.parc;


public class ConsommationResultats{
	
	

/* Extraction table parc_resultats */
private String branche;
private String sousBranche;
private String batimentType;
private String occupation;
private String periodeDetail;
private String periodeSimple;
private String energieUsage;
private Double annee; 
private String usage;
private String usageSimple;
private Double facteurEnergiePrimaire;
private Double facteurEmission;
private Double consommation;


	public Double getAnnee() {
		return annee;
	}
	public void setAnnee(Double annee) {
		this.annee = annee;
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
	public String getBatimentType() {
		return batimentType;
	}
	public void setBatimentType(String batimentType) {
		this.batimentType = batimentType;
	}
	public String getEnergieUsage() {
		return energieUsage;
	}
	public void setEnergieUsage(String energieUsage) {
		this.energieUsage = energieUsage;
	}
	public Double getConsommationNonRT() {
		return consommation;
	}
	public void setConsommationNonRT(Double consommationNonRT) {
		this.consommation = consommationNonRT;
	}
	public String getUsage() {
		return usage;
	}
	public void setUsage(String usage) {
		this.usage = usage;
	}
	public Double getFacteurEnergiePrimaire() {
		return facteurEnergiePrimaire;
	}
	public void setFacteurEnergiePrimaire(Double facteurEnergiePrimaire) {
		this.facteurEnergiePrimaire = facteurEnergiePrimaire;
	}
	public Double getFacteurEmission() {
		return facteurEmission;
	}
	public void setFacteurEmission(Double facteurEmission) {
		this.facteurEmission = facteurEmission;
	}
	public String getUsageSimple() {
		return usageSimple;
	}
	public void setUsageSimple(String usageSimple) {
		this.usageSimple = usageSimple;
	}

	
}
