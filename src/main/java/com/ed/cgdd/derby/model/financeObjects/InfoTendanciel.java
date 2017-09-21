package com.ed.cgdd.derby.model.financeObjects;

import java.math.BigDecimal;
import java.util.List;

public class InfoTendanciel {
	boolean rienFaire;
	boolean fenMod;
	boolean fen_murMod;
	boolean ensMod;
	boolean fenBbc;
	boolean fen_murBbc;
	boolean ensBbc;
	boolean gtb;
	boolean renoTendance;
	int compteurFENMOD;
	int compteurFEN_MURMOD;
	int compteurENSMOD;
	int compteurFENBBC;
	int compteurFEN_MURBBC;
	int compteurENSBBC;
	int compteurGTB;
	BigDecimal partRienFaire;
	List<String> ids;
	BigDecimal surface;

	public boolean isRienFaire() {
		return rienFaire;
	}

	public void setRienFaire(boolean rienFaire) {
		this.rienFaire = rienFaire;
	}

	public boolean isFenMod() {
		return fenMod;
	}

	public void setFenMod(boolean fenMod) {
		this.fenMod = fenMod;
	}

	public boolean isFen_murMod() {
		return fen_murMod;
	}

	public void setFen_murMod(boolean fen_murMod) {
		this.fen_murMod = fen_murMod;
	}

	public boolean isEnsMod() {
		return ensMod;
	}

	public void setEnsMod(boolean ensMod) {
		this.ensMod = ensMod;
	}

	public boolean isFenBbc() {
		return fenBbc;
	}

	public void setFenBbc(boolean fenBbc) {
		this.fenBbc = fenBbc;
	}

	public boolean isFen_murBbc() {
		return fen_murBbc;
	}

	public void setFen_murBbc(boolean fen_murBbc) {
		this.fen_murBbc = fen_murBbc;
	}

	public boolean isEnsBbc() {
		return ensBbc;
	}

	public void setEnsBbc(boolean ensBbc) {
		this.ensBbc = ensBbc;
	}

	public boolean isGtb() {
		return gtb;
	}

	public void setGtb(boolean gtb) {
		this.gtb = gtb;
	}

	public boolean isRenoTendance() {
		return renoTendance;
	}

	public void setRenoTendance(boolean renoTendance) {
		this.renoTendance = renoTendance;
	}

	public int getCompteurFENMOD() {
		return compteurFENMOD;
	}

	public void setCompteurFENMOD(int compteurFENMOD) {
		this.compteurFENMOD = compteurFENMOD;
	}

	public int getCompteurFEN_MURMOD() {
		return compteurFEN_MURMOD;
	}

	public void setCompteurFEN_MURMOD(int compteurFEN_MURMOD) {
		this.compteurFEN_MURMOD = compteurFEN_MURMOD;
	}

	public int getCompteurENSMOD() {
		return compteurENSMOD;
	}

	public void setCompteurENSMOD(int compteurENSMOD) {
		this.compteurENSMOD = compteurENSMOD;
	}

	public int getCompteurFENBBC() {
		return compteurFENBBC;
	}

	public void setCompteurFENBBC(int compteurFENBBC) {
		this.compteurFENBBC = compteurFENBBC;
	}

	public int getCompteurFEN_MURBBC() {
		return compteurFEN_MURBBC;
	}

	public void setCompteurFEN_MURBBC(int compteurFEN_MURBBC) {
		this.compteurFEN_MURBBC = compteurFEN_MURBBC;
	}

	public int getCompteurENSBBC() {
		return compteurENSBBC;
	}

	public void setCompteurENSBBC(int compteurENSBBC) {
		this.compteurENSBBC = compteurENSBBC;
	}

	public int getCompteurGTB() {
		return compteurGTB;
	}

	public void setCompteurGTB(int compteurGTB) {
		this.compteurGTB = compteurGTB;
	}

	public BigDecimal getPartRienFaire() {
		return partRienFaire;
	}

	public void setPartRienFaire(BigDecimal compteurRienFaire) {
		this.partRienFaire = compteurRienFaire;
	}

	public List<String> getIds() {
		return ids;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}

	public InfoTendanciel(List<String> ids, BigDecimal surface) {
		super();
		this.compteurENSBBC = 0;
		this.compteurENSMOD = 0;
		this.compteurFEN_MURBBC = 0;
		this.compteurFEN_MURMOD = 0;
		this.compteurFENBBC = 0;
		this.compteurFENMOD = 0;
		this.compteurGTB = 0;
		this.partRienFaire = BigDecimal.ZERO;
		this.ensBbc = false;
		this.ensMod = false;
		this.fen_murBbc = false;
		this.fen_murMod = false;
		this.fenBbc = false;
		this.fenMod = false;
		this.gtb = false;
		this.ids = ids;
		this.renoTendance = false;
		this.rienFaire = false;
		this.surface = surface;

	}

	public BigDecimal getSurface() {
		return surface;
	}

	public void setSurface(BigDecimal surface) {
		this.surface = surface;
	}

}
