package com.ed.cgdd.derby.model.calcconso;

import java.math.BigDecimal;
import java.util.Arrays;

public class ResultConsoUClim extends SegmentConso {

	private BigDecimal[] consoUClimEF;
	private BigDecimal[] consoUClimEP;
	private BigDecimal[] surfTot;

	public BigDecimal getSurfTot(int index) {
		return surfTot[index];
	}

	public void setSurfTot(int index, BigDecimal valeur) {
		this.surfTot[index] = valeur.setScale(4, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getConsoUClimEF(int index) {
		return consoUClimEF[index];
	}

	public void setConsoUClimEF(int index, BigDecimal valeur) {
		this.consoUClimEF[index] = valeur.setScale(4, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getConsoUClimEP(int index) {
		return consoUClimEP[index];
	}

	public void setConsoUClimEP(int index, BigDecimal valeur) {
		this.consoUClimEP[index] = valeur.setScale(4, BigDecimal.ROUND_HALF_UP);
	}

	public ResultConsoUClim(int pasdeTemps) {
		consoUClimEF = new BigDecimal[pasdeTemps + 1];
		consoUClimEP = new BigDecimal[pasdeTemps + 1];
		surfTot = new BigDecimal[pasdeTemps + 1];
	}

	public ResultConsoUClim(ResultConsoUClim copy) {
		this.setId(copy.getId());
		this.setAnneeRenov(copy.getAnneeRenov());
		this.setTypeRenovBat(copy.getTypeRenovBat());

		this.consoUClimEF = Arrays.copyOf(copy.consoUClimEF, copy.consoUClimEF.length);
		this.consoUClimEP = Arrays.copyOf(copy.consoUClimEP, copy.consoUClimEP.length);
		this.surfTot = Arrays.copyOf(copy.surfTot, copy.surfTot.length);

	}
}
