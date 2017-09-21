package com.ed.cgdd.derby.model.calcconso;

import java.math.BigDecimal;
import java.util.Arrays;

public class ResultConsoURt extends SegmentConso {

	private BigDecimal[] consoUEcsEF;
	private BigDecimal[] consoUEclairageEF;
	private BigDecimal[] consoUEcsEP;
	private BigDecimal[] consoUEclairageEP;
	private BigDecimal[] surfTot;

	public BigDecimal getSurfTot(int index) {
		return surfTot[index];
	}

	public void setSurfTot(int index, BigDecimal valeur) {
		this.surfTot[index] = valeur.setScale(4, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getConsoUEcsEF(int index) {
		return consoUEcsEF[index];
	}

	public void setConsoUEcsEF(int index, BigDecimal valeur) {
		this.consoUEcsEF[index] = valeur.setScale(4, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getConsoUEclairageEF(int index) {
		return consoUEclairageEF[index];
	}

	public void setConsoUEclairageEF(int index, BigDecimal valeur) {
		this.consoUEclairageEF[index] = valeur.setScale(4, BigDecimal.ROUND_HALF_UP);
	}

	public ResultConsoURt(int pasdeTemps) {
		consoUEclairageEF = new BigDecimal[pasdeTemps + 1];
		consoUEcsEF = new BigDecimal[pasdeTemps + 1];
		consoUEclairageEP = new BigDecimal[pasdeTemps + 1];
		consoUEcsEP = new BigDecimal[pasdeTemps + 1];
		surfTot = new BigDecimal[pasdeTemps + 1];
	}

	public ResultConsoURt(ResultConsoURt copy) {
		this.setId(copy.getId());
		this.setAnneeRenov(copy.getAnneeRenov());
		this.setTypeRenovBat(copy.getTypeRenovBat());

		this.consoUEclairageEF = Arrays.copyOf(copy.consoUEclairageEF, copy.consoUEclairageEF.length);
		this.consoUEcsEF = Arrays.copyOf(copy.consoUEcsEF, copy.consoUEcsEF.length);
		this.consoUEclairageEP = Arrays.copyOf(copy.consoUEclairageEP, copy.consoUEclairageEP.length);
		this.consoUEcsEP = Arrays.copyOf(copy.consoUEcsEP, copy.consoUEcsEP.length);
		this.surfTot = Arrays.copyOf(copy.surfTot, copy.surfTot.length);

	}

	public BigDecimal getConsoUEcsEP(int index) {
		return consoUEcsEP[index];
	}

	public void setConsoUEcsEP(int index, BigDecimal valeur) {
		this.consoUEcsEP[index] = valeur.setScale(4, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal getConsoUEclairageEP(int index) {
		return consoUEclairageEP[index];
	}

	public void setConsoUEclairageEP(int index, BigDecimal valeur) {
		this.consoUEclairageEP[index] = valeur.setScale(4, BigDecimal.ROUND_HALF_UP);
	}

}
