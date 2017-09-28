package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public class CalibCoutGlobal {

	private String calKey;
	private BigDecimal CInt;
	private BigDecimal CoutVariable;

	public String getCalKey() {
		return calKey;
	}

	public void setCalKey(String calKey) {
		this.calKey = calKey;
	}

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

	public CalibCoutGlobal(String calKey, BigDecimal CInt, BigDecimal CoutVariable){
		this.calKey=calKey;
		this.CInt=CInt;
		this.CoutVariable=CoutVariable;
	}

}
