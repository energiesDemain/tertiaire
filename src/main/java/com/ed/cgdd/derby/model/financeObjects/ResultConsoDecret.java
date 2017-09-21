package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;

public class ResultConsoDecret {

	private BigDecimal consoEclairageEP = BigDecimal.ZERO;
	private BigDecimal consoECSEP = BigDecimal.ZERO;
	private BigDecimal consoChauffEP = BigDecimal.ZERO;
	private BigDecimal consoAuxEP = BigDecimal.ZERO;
	private BigDecimal consoVentilEP = BigDecimal.ZERO;
	private BigDecimal consoClimEP = BigDecimal.ZERO;
	private BigDecimal consoEclairageEF = BigDecimal.ZERO;
	private BigDecimal consoECSEF = BigDecimal.ZERO;
	private BigDecimal consoChauffEF = BigDecimal.ZERO;
	private BigDecimal consoAuxEF = BigDecimal.ZERO;
	private BigDecimal consoVentilEF = BigDecimal.ZERO;
	private BigDecimal consoClimEF = BigDecimal.ZERO;

	public ResultConsoDecret() {
	}

	public BigDecimal getConsoEclairageEP() {
		return consoEclairageEP;
	}

	public void setConsoEclairageEP(BigDecimal consoEclairageEP) {
		this.consoEclairageEP = consoEclairageEP;
	}

	public BigDecimal getConsoECSEP() {
		return consoECSEP;
	}

	public void setConsoECSEP(BigDecimal consoECSEP) {
		this.consoECSEP = consoECSEP;
	}

	public BigDecimal getConsoChauffEP() {
		return consoChauffEP;
	}

	public void setConsoChauffEP(BigDecimal consoChauffEP) {
		this.consoChauffEP = consoChauffEP;
	}

	public BigDecimal getConsoAuxEP() {
		return consoAuxEP;
	}

	public void setConsoAuxEP(BigDecimal consoAuxEP) {
		this.consoAuxEP = consoAuxEP;
	}

	public BigDecimal getConsoVentilEP() {
		return consoVentilEP;
	}

	public void setConsoVentilEP(BigDecimal consoVentilEP) {
		this.consoVentilEP = consoVentilEP;
	}

	public BigDecimal getConsoClimEP() {
		return consoClimEP;
	}

	public void setConsoClimEP(BigDecimal consoClimEP) {
		this.consoClimEP = consoClimEP;
	}

	public BigDecimal getConsoEclairageEF() {
		return consoEclairageEF;
	}

	public void setConsoEclairageEF(BigDecimal consoEclairageEF) {
		this.consoEclairageEF = consoEclairageEF;
	}

	public BigDecimal getConsoECSEF() {
		return consoECSEF;
	}

	public void setConsoECSEF(BigDecimal consoECSEF) {
		this.consoECSEF = consoECSEF;
	}

	public BigDecimal getConsoChauffEF() {
		return consoChauffEF;
	}

	public void setConsoChauffEF(BigDecimal consoChauffEF) {
		this.consoChauffEF = consoChauffEF;
	}

	public BigDecimal getConsoAuxEF() {
		return consoAuxEF;
	}

	public void setConsoAuxEF(BigDecimal consoAuxEF) {
		this.consoAuxEF = consoAuxEF;
	}

	public BigDecimal getConsoVentilEF() {
		return consoVentilEF;
	}

	public void setConsoVentilEF(BigDecimal consoVentilEF) {
		this.consoVentilEF = consoVentilEF;
	}

	public BigDecimal getConsoClimEF() {
		return consoClimEF;
	}

	public void setConsoClimEF(BigDecimal consoClimEF) {
		this.consoClimEF = consoClimEF;
	}

	public ResultConsoDecret(ResultConsoDecret copy) {
		this.setConsoAuxEP(copy.getConsoAuxEP());
		this.setConsoChauffEP(copy.getConsoChauffEP());
		this.setConsoClimEP(copy.getConsoClimEP());
		this.setConsoEclairageEP(copy.getConsoEclairageEP());
		this.setConsoECSEP(copy.getConsoECSEP());
		this.setConsoVentilEP(copy.getConsoVentilEP());
		this.setConsoAuxEF(copy.getConsoAuxEF());
		this.setConsoChauffEF(copy.getConsoChauffEF());
		this.setConsoClimEF(copy.getConsoClimEF());
		this.setConsoEclairageEF(copy.getConsoEclairageEF());
		this.setConsoECSEF(copy.getConsoECSEF());
		this.setConsoVentilEF(copy.getConsoVentilEF());

	}

}
