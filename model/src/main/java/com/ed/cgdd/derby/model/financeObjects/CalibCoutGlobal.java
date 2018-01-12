package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class CalibCoutGlobal {

	private BigDecimal CInt;
	private BigDecimal CoutVariable;
	
	public BigDecimal getCInt() {
		return CInt;
	}

	public void setCInt(BigDecimal CInt) {
		this.CInt = CInt;
	}

	public BigDecimal getCoutVariable() {
		return CoutVariable;
	}

	public void setCoutVariable(BigDecimal coutVariable) {
		CoutVariable = coutVariable;
	}

	public CalibCoutGlobal(){
	}

	public CalibCoutGlobal(BigDecimal CInt, BigDecimal CoutVariable){
		this.CInt=CInt;
		this.CoutVariable=CoutVariable;
	}

}
