package com.ed.cgdd.derby.model.excelobjects;

import java.util.ArrayList;

public class GenericExcelData {

	private ArrayList<ArrayList<Object>> liste;
	private ArrayList<String> names;

	public ArrayList<ArrayList<Object>> getListe() {
		return liste;
	}

	public void setListe(ArrayList<ArrayList<Object>> liste) {
		this.liste = liste;
	}

	public ArrayList<String> getNames() {
		return names;
	}

	public void setNames(ArrayList<String> names) {
		this.names = names;
	}

}
