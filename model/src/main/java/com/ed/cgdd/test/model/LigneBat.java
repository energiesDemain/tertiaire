package com.ed.cgdd.test.model;

public class LigneBat {
	public String id ;
	public int age ;
	public String type ;
	public float valeur ;
	
	public LigneBat(String id, int age, String type, float valeur) {
		this.id = id ;
		this.age = age ;
		this.type = type ;
		this.valeur = valeur ;
	}
	public LigneBat() {
		// TODO Auto-generated constructor stub
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public float getValeur() {
		return valeur;
	}
	public void setValeur(float valeur) {
		this.valeur = valeur;
	}
	public Object getGeneric(String champ) {
		if (champ == "ID") {
			return getId();
		};
		if (champ == "AGE") {
			return getAge();
		};
		if (champ == "TYPE") {
			return getType();
		};
		if (champ == "VALEUR") {
			return getValeur();
		};
		return null;
	}

	
	
		
	

}
