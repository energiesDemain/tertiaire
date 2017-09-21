package com.ed.cgdd.derby.model.parc;

public class ConsommationResultatsAnnee {

	/* Extractions des consos */
	private String branche;
	private String sousBranche;
	private String batimentType;
	private String occupation;
	private String periodeDetail;
	private String periodeSimple;
	private String energieUsage;
	private String usage;
	private String usageSimple;
	private Double facteurEnergiePrimaire;
	private Double consommation;
	private String systemeChaud;
	private String systemeFroid;
	private Double consommationUnitaire;
	private String etiquette;

	// Extraction des couts
	private String typeRenovationBati;
	private String typeRenovationSysteme;
	private Integer cible;
	private int annee;
	private Double surface;
	private Double aides;
	private Double investissement;
	private Double prets;
	private Double pretsBonifies;
	private String reglementation;

	// Années Conso
	private Double annee2009;
	private Double annee2010;
	private Double annee2011;
	private Double annee2012;
	private Double annee2013;
	private Double annee2014;
	private Double annee2015;
	private Double annee2016;
	private Double annee2017;
	private Double annee2018;
	private Double annee2019;
	private Double annee2020;
	private Double annee2021;
	private Double annee2022;
	private Double annee2023;
	private Double annee2024;
	private Double annee2025;
	private Double annee2026;
	private Double annee2027;
	private Double annee2028;
	private Double annee2029;
	private Double annee2030;
	private Double annee2031;
	private Double annee2032;
	private Double annee2033;
	private Double annee2034;
	private Double annee2035;
	private Double annee2036;
	private Double annee2037;
	private Double annee2038;
	private Double annee2039;
	private Double annee2040;
	private Double annee2041;
	private Double annee2042;
	private Double annee2043;
	private Double annee2044;
	private Double annee2045;
	private Double annee2046;
	private Double annee2047;
	private Double annee2048;
	private Double annee2049;
	private Double annee2050;

	private Double[] annees = new Double[42];

	// Getters + Setters

	public Double getAnnees(int index) {
		return annees[index];
	}

	public void setAnnees(int index, Double annees) {
		this.annees[index] = annees;
	}

	// Couts
	public Double getConsommation() {
		return consommation;
	}

	public void setConsommation(Double consommation) {
		this.consommation = consommation;
	}

	public String getTypeRenovationBati() {
		return typeRenovationBati;
	}

	public void setTypeRenovationBati(String typeRenovationBati) {
		this.typeRenovationBati = typeRenovationBati;
	}

	public String getTypeRenovationSysteme() {
		return typeRenovationSysteme;
	}

	public void setTypeRenovationSysteme(String typeRenovationSysteme) {
		this.typeRenovationSysteme = typeRenovationSysteme;
	}

	public int getAnnee() {
		return annee;
	}

	public void setAnnee(int annee) {
		this.annee = annee;
	}

	public Double getSurface() {
		return surface;
	}

	public void setSurface(Double surface) {
		this.surface = surface;
	}

	public Double getAides() {
		return aides;
	}

	public void setAides(Double aides) {
		this.aides = aides;
	}

	public Double getInvestissement() {
		return investissement;
	}

	public void setInvestissement(Double investissement) {
		this.investissement = investissement;
	}

	public Double getPrets() {
		return prets;
	}

	public void setPrets(Double prets) {
		this.prets = prets;
	}

	public Double getPretsBonifies() {
		return pretsBonifies;
	}

	public void setPretsBonifies(Double pretsBonifies) {
		this.pretsBonifies = pretsBonifies;
	}

	public Double[] getAnnees() {
		return annees;
	}

	public void setAnnees(Double[] annees) {
		this.annees = annees;
	}

	// Annees

	public Double getAnnee2009() {
		return annee2009;
	}

	public void setAnnee2009(Double annee2009) {
		this.annee2009 = annee2009;
	}

	public Double getAnnee2010() {
		return annee2010;
	}

	public void setAnnee2010(Double annee2010) {
		this.annee2010 = annee2010;
	}

	public Double getAnnee2011() {
		return annee2011;
	}

	public void setAnnee2011(Double annee2011) {
		this.annee2011 = annee2011;
	}

	public Double getAnnee2012() {
		return annee2012;
	}

	public void setAnnee2012(Double annee2012) {
		this.annee2012 = annee2012;
	}

	public Double getAnnee2013() {
		return annee2013;
	}

	public void setAnnee2013(Double annee2013) {
		this.annee2013 = annee2013;
	}

	public Double getAnnee2014() {
		return annee2014;
	}

	public void setAnnee2014(Double annee2014) {
		this.annee2014 = annee2014;
	}

	public Double getAnnee2015() {
		return annee2015;
	}

	public void setAnnee2015(Double annee2015) {
		this.annee2015 = annee2015;
	}

	public Double getAnnee2016() {
		return annee2016;
	}

	public void setAnnee2016(Double annee2016) {
		this.annee2016 = annee2016;
	}

	public Double getAnnee2017() {
		return annee2017;
	}

	public void setAnnee2017(Double annee2017) {
		this.annee2017 = annee2017;
	}

	public Double getAnnee2018() {
		return annee2018;
	}

	public void setAnnee2018(Double annee2018) {
		this.annee2018 = annee2018;
	}

	public Double getAnnee2019() {
		return annee2019;
	}

	public void setAnnee2019(Double annee2019) {
		this.annee2019 = annee2019;
	}

	public Double getAnnee2020() {
		return annee2020;
	}

	public void setAnnee2020(Double annee2020) {
		this.annee2020 = annee2020;
	}

	public Double getAnnee2021() {
		return annee2021;
	}

	public void setAnnee2021(Double annee2021) {
		this.annee2021 = annee2021;
	}

	public Double getAnnee2022() {
		return annee2022;
	}

	public void setAnnee2022(Double annee2022) {
		this.annee2022 = annee2022;
	}

	public Double getAnnee2023() {
		return annee2023;
	}

	public void setAnnee2023(Double annee2023) {
		this.annee2023 = annee2023;
	}

	public Double getAnnee2024() {
		return annee2024;
	}

	public void setAnnee2024(Double annee2024) {
		this.annee2024 = annee2024;
	}

	public Double getAnnee2025() {
		return annee2025;
	}

	public void setAnnee2025(Double annee2025) {
		this.annee2025 = annee2025;
	}

	public Double getAnnee2026() {
		return annee2026;
	}

	public void setAnnee2026(Double annee2026) {
		this.annee2026 = annee2026;
	}

	public Double getAnnee2027() {
		return annee2027;
	}

	public void setAnnee2027(Double annee2027) {
		this.annee2027 = annee2027;
	}

	public Double getAnnee2028() {
		return annee2028;
	}

	public void setAnnee2028(Double annee2028) {
		this.annee2028 = annee2028;
	}

	public Double getAnnee2029() {
		return annee2029;
	}

	public void setAnnee2029(Double annee2029) {
		this.annee2029 = annee2029;
	}

	public Double getAnnee2030() {
		return annee2030;
	}

	public void setAnnee2030(Double annee2030) {
		this.annee2030 = annee2030;
	}

	public Double getAnnee2031() {
		return annee2031;
	}

	public void setAnnee2031(Double annee2031) {
		this.annee2031 = annee2031;
	}

	public Double getAnnee2032() {
		return annee2032;
	}

	public void setAnnee2032(Double annee2032) {
		this.annee2032 = annee2032;
	}

	public Double getAnnee2033() {
		return annee2033;
	}

	public void setAnnee2033(Double annee2033) {
		this.annee2033 = annee2033;
	}

	public Double getAnnee2034() {
		return annee2034;
	}

	public void setAnnee2034(Double annee2034) {
		this.annee2034 = annee2034;
	}

	public Double getAnnee2035() {
		return annee2035;
	}

	public void setAnnee2035(Double annee2035) {
		this.annee2035 = annee2035;
	}

	public Double getAnnee2036() {
		return annee2036;
	}

	public void setAnnee2036(Double annee2036) {
		this.annee2036 = annee2036;
	}

	public Double getAnnee2037() {
		return annee2037;
	}

	public void setAnnee2037(Double annee2037) {
		this.annee2037 = annee2037;
	}

	public Double getAnnee2038() {
		return annee2038;
	}

	public void setAnnee2038(Double annee2038) {
		this.annee2038 = annee2038;
	}

	public Double getAnnee2039() {
		return annee2039;
	}

	public void setAnnee2039(Double annee2039) {
		this.annee2039 = annee2039;
	}

	public Double getAnnee2040() {
		return annee2040;
	}

	public void setAnnee2040(Double annee2040) {
		this.annee2040 = annee2040;
	}

	public Double getAnnee2041() {
		return annee2041;
	}

	public void setAnnee2041(Double annee2041) {
		this.annee2041 = annee2041;
	}

	public Double getAnnee2042() {
		return annee2042;
	}

	public void setAnnee2042(Double annee2042) {
		this.annee2042 = annee2042;
	}

	public Double getAnnee2043() {
		return annee2043;
	}

	public void setAnnee2043(Double annee2043) {
		this.annee2043 = annee2043;
	}

	public Double getAnnee2044() {
		return annee2044;
	}

	public void setAnnee2044(Double annee2044) {
		this.annee2044 = annee2044;
	}

	public Double getAnnee2045() {
		return annee2045;
	}

	public void setAnnee2045(Double annee2045) {
		this.annee2045 = annee2045;
	}

	public Double getAnnee2046() {
		return annee2046;
	}

	public void setAnnee2046(Double annee2046) {
		this.annee2046 = annee2046;
	}

	public Double getAnnee2047() {
		return annee2047;
	}

	public void setAnnee2047(Double annee2047) {
		this.annee2047 = annee2047;
	}

	public Double getAnnee2048() {
		return annee2048;
	}

	public void setAnnee2048(Double annee2048) {
		this.annee2048 = annee2048;
	}

	public Double getAnnee2049() {
		return annee2049;
	}

	public void setAnnee2049(Double annee2049) {
		this.annee2049 = annee2049;
	}

	public Double getAnnee2050() {
		return annee2050;
	}

	public void setAnnee2050(Double annee2050) {
		this.annee2050 = annee2050;
	}

	// Consommations
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

	public String getUsageSimple() {
		return usageSimple;
	}

	public void setUsageSimple(String usageSimple) {
		this.usageSimple = usageSimple;
	}

	public Integer getCible() {
		return cible;
	}

	public void setCible(Integer cible) {
		this.cible = cible;
	}

	public String getReglementation() {
		return reglementation;
	}

	public void setReglementation(String reglementation) {
		this.reglementation = reglementation;
	}

	public String getSystemeChaud() {
		return systemeChaud;
	}

	public void setSystemeChaud(String systemeChaud) {
		this.systemeChaud = systemeChaud;
	}

	public String getSystemeFroid() {
		return systemeFroid;
	}

	public void setSystemeFroid(String systemeFroid) {
		this.systemeFroid = systemeFroid;
	}

	public Double getConsommationUnitaire() {
		return consommationUnitaire;
	}

	public void setConsommationUnitaire(double d) {
		this.consommationUnitaire = d;
	}

	public String getEtiquette() {
		return etiquette;
	}

	public void setEtiquette(String etiquette) {
		this.etiquette = etiquette;
	}

}
