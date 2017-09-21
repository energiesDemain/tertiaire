package com.ed.cgdd.derby.model.parc;

import java.math.BigDecimal;
import java.util.Arrays;

public class Parc extends Segment {

	private String anneeRenov;
	private String anneeRenovSys;
	// TODO EDR enum
	private TypeRenovBati typeRenovBat;
	private TypeRenovSysteme typeRenovSys;

	public TypeRenovSysteme getTypeRenovSys() {
		return typeRenovSys;
	}

	public void setTypeRenovSys(TypeRenovSysteme typeRenovSys) {
		this.typeRenovSys = typeRenovSys;
	}

	private BigDecimal[] annee;

	public String getAnneeRenovSys() {
		return anneeRenovSys;
	}

	public void setAnneeRenovSys(String anneeRenovSys) {
		this.anneeRenovSys = anneeRenovSys;
	}

	public String getAnneeRenov() {
		return anneeRenov;
	}

	public void setAnneeRenov(String anneeRenovBat) {
		this.anneeRenov = anneeRenovBat;
	}

	public TypeRenovBati getTypeRenovBat() {
		return typeRenovBat;
	}

	public void setTypeRenovBat(TypeRenovBati typeRenovBat) {
		this.typeRenovBat = typeRenovBat;
	}

	public BigDecimal getAnnee(int index) {
		return annee[index];
	}

	public void setAnnee(int index, BigDecimal valeurAnnee) {
		this.annee[index] = valeurAnnee.setScale(4, BigDecimal.ROUND_HALF_UP);
	}

	public Parc(int pasdeTemps) {
		annee = new BigDecimal[pasdeTemps + 1];
	}

	public BigDecimal[] getArray() {
		return annee;
	}

	public Parc(Parc copy) {
		this.setId(copy.getId());
		this.setAnneeRenov(copy.getAnneeRenov());
		this.setTypeRenovBat(copy.getTypeRenovBat());
		this.setAnneeRenovSys(copy.getAnneeRenovSys());
		this.setTypeRenovSys(copy.getTypeRenovSys());
		this.annee = Arrays.copyOf(copy.annee, copy.annee.length);

	}
}
