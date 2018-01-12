package com.ed.cgdd.derby.model.parc;
import java.math.BigDecimal;

public class ParamCalib {

	private BigDecimal BesoinChauff; 
	private BigDecimal ConsoChauff;
	private BigDecimal Surface;
	private BigDecimal ChargesChauff;
	
	public BigDecimal getBesoinChauff() {
		return BesoinChauff;
	}
	public void setBesoinChauff(BigDecimal besoinChauff) {
		BesoinChauff = besoinChauff;
	}
	public BigDecimal getConsoChauff() {
		return ConsoChauff;
	}
	public void setConsoChauff(BigDecimal consoChauff) {
		ConsoChauff = consoChauff;
	}
	public BigDecimal getSurface() {
		return Surface;
	}
	public void setSurface(BigDecimal surface) {
		Surface = surface;
	}
	public BigDecimal getChargesChauff() {
		return ChargesChauff;
	}
	public void setChargesChauff(BigDecimal chargesChauff) {
		ChargesChauff = chargesChauff;
	}
	
	
}
