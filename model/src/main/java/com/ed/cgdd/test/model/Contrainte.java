package com.ed.cgdd.test.model;

public class Contrainte {
	public String libelle ;
	public String contrainte;
	
	public Contrainte(String lib, String cont) {
		this.libelle = lib ;
		this.contrainte = cont ;
	}
	
	public String getLibelle() {
		return libelle;
	}
	public void setLibelle(String lib) {
		libelle = lib;
	}
	public String getContrainte() {
		return contrainte;
	}
	public void setContrainte(String contrainte) {
		this.contrainte = contrainte;
	}
}
