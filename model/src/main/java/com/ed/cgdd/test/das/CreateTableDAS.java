package com.ed.cgdd.test.das;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.ed.cgdd.test.model.Contrainte;
import com.ed.cgdd.test.model.Determinant;
import com.ed.cgdd.test.model.LigneBat;
import com.ed.cgdd.test.model.TestMapper;

public interface CreateTableDAS {
	public void createTable() ;
	public void createTableParam(String nomTable, LinkedList<Determinant> det) ;

	public void insertTableParam(LigneBat ligne, String nomTable) ;
	public void insertTable(LigneBat ligne) ;
	public void insertTableGeneric(HashMap<String, LigneBat> data, LinkedList<Determinant> det, String nomTable);
	public void insertTableGenericV2(HashMap<String, LigneBat> data, LinkedList<Determinant> det, String nomTable) ;
	
	
	public void deleteTable(String nomTable);
	public List<LigneBat> selectTable(LinkedList<Determinant> det, String nomTable) ;
	public void updateTable(LinkedList<Determinant> updet, String nomTable);
	public void updateTableConstr(LinkedList<Determinant> det, LinkedList<Contrainte> constr, String nomTable);
	
	
	


}
