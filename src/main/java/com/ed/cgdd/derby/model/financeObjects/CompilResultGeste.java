package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;
import java.util.HashMap;

public class CompilResultGeste {
	private BigDecimal partMaxRegl = BigDecimal.ZERO;
	private CoutFinal coutFinalRegl;
	private String cleRegl;
	private BigDecimal partMaxSys = BigDecimal.ZERO;
	private CoutFinal coutFinalSys;
	private String cleSys;
	private BigDecimal partMaxBat = BigDecimal.ZERO;
	private CoutFinal coutFinalBat;
	private String cleBat;
	private HashMap<String, PartMarcheRenov> results = new HashMap<String, PartMarcheRenov>();
	private BigDecimal sommeSys = BigDecimal.ZERO;
	private BigDecimal sommeBat = BigDecimal.ZERO;
	private BigDecimal sommeTot = BigDecimal.ZERO;
	private boolean bool;

	public HashMap<String, PartMarcheRenov> getResults() {
		return results;
	}

	public void setResults(HashMap<String, PartMarcheRenov> results) {
		this.results = results;
	}

	public BigDecimal getSommeSys() {
		return sommeSys;
	}

	public void setSommeSys(BigDecimal sommeSys) {
		this.sommeSys = sommeSys;
	}

	public BigDecimal getSommeTot() {
		return sommeTot;
	}

	public void setSommeTot(BigDecimal sommeTot) {
		this.sommeTot = sommeTot;
	}

	public boolean isBool() {
		return bool;
	}

	public void setBool(boolean bool) {
		this.bool = bool;
	}

	public BigDecimal getPartMaxRegl() {
		return partMaxRegl;
	}

	public void setPartMaxRegl(BigDecimal partMaxRegl) {
		this.partMaxRegl = partMaxRegl;
	}

	public CoutFinal getCoutFinalRegl() {
		return coutFinalRegl;
	}

	public void setCoutFinalRegl(CoutFinal coutFinalRegl) {
		this.coutFinalRegl = coutFinalRegl;
	}

	public String getCleRegl() {
		return cleRegl;
	}

	public void setCleRegl(String cleRegl) {
		this.cleRegl = cleRegl;
	}

	public BigDecimal getPartMaxSys() {
		return partMaxSys;
	}

	public void setPartMaxSys(BigDecimal partMaxSys) {
		this.partMaxSys = partMaxSys;
	}

	public CoutFinal getCoutFinalSys() {
		return coutFinalSys;
	}

	public void setCoutFinalSys(CoutFinal coutFinalSys) {
		this.coutFinalSys = coutFinalSys;
	}

	public String getCleSys() {
		return cleSys;
	}

	public void setCleSys(String cleSys) {
		this.cleSys = cleSys;
	}

	public BigDecimal getPartMaxBat() {
		return partMaxBat;
	}

	public void setPartMaxBat(BigDecimal partMaxBat) {
		this.partMaxBat = partMaxBat;
	}

	public CoutFinal getCoutFinalBat() {
		return coutFinalBat;
	}

	public void setCoutFinalBat(CoutFinal coutFinalBat) {
		this.coutFinalBat = coutFinalBat;
	}

	public String getCleBat() {
		return cleBat;
	}

	public void setCleBat(String cleBat) {
		this.cleBat = cleBat;
	}

	public BigDecimal getSommeBat() {
		return sommeBat;
	}

	public void setSommeBat(BigDecimal sommeBat) {
		this.sommeBat = sommeBat;
	}

}
