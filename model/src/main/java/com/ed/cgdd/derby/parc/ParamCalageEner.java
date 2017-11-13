package com.ed.cgdd.derby.parc;
import java.math.BigDecimal;

public class ParamCalageEner {

	private BigDecimal facteurCalageParc;
	public BigDecimal getFacteurCalageParc() {
		return facteurCalageParc;
	}
	public void setFacteurCalageParc(BigDecimal facteurCalageParc) {
		this.facteurCalageParc = facteurCalageParc;
	}
	public BigDecimal getFacteurCalageConso() {
		return facteurCalageConso;
	}
	public void setFacteurCalageConso(BigDecimal facteurCalageConso) {
		this.facteurCalageConso = facteurCalageConso;
	}
	private BigDecimal facteurCalageConso;
	
	public ParamCalageEner(){
	}

	public ParamCalageEner(BigDecimal facteurCalageParc, BigDecimal facteurCalageConso){
		this.facteurCalageParc=facteurCalageParc;
		this.facteurCalageConso=facteurCalageConso;
	}
	
}
