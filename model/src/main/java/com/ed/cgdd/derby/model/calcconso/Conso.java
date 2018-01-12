package com.ed.cgdd.derby.model.calcconso;

import java.math.BigDecimal;
import java.util.Arrays;

public class Conso extends SegmentConso {

	private BigDecimal[] annee;
	public BigDecimal getAnnee(int index) {
		return annee[index];
	}

	public void setAnnee(int index, BigDecimal valeurAnnee) {
		this.annee[index] = valeurAnnee.setScale(4, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal[] getArray() {
		return annee;
	}

	public Conso(int pasdeTemps) {
		annee = new BigDecimal[pasdeTemps + 1];
	}

	public Conso(Conso copy) {
		this.setId(copy.getId());
		this.setAnneeRenovSys(copy.getAnneeRenovSys());
		this.setAnneeRenov(copy.getAnneeRenov());
		this.setTypeRenovBat(copy.getTypeRenovBat());
		this.setTypeRenovSys(copy.getTypeRenovSys());
		this.annee = Arrays.copyOf(copy.annee, copy.annee.length);

	}

}
