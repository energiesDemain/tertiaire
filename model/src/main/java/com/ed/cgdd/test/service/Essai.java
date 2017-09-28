package com.ed.cgdd.test.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ed.cgdd.test.das.CreateTableDAS;
import com.ed.cgdd.test.model.Contrainte;
import com.ed.cgdd.test.model.Determinant;
import com.ed.cgdd.test.model.LigneBat;


public class Essai {
	private final static Logger LOG = LogManager.getLogger(Essai.class);
	public static void main(String[] args) {		
		
		LOG.info("Start engine");
		
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		CreateTableDAS service = (CreateTableDAS) context.getBean("createTestTableDAS");
		
		//delete table
		service.deleteTable("TEST");
		
		// creation intitules
		Determinant det1 = new Determinant("ID", "VARCHAR(20)") ;
		Determinant det2 = new Determinant("AGE", "INT") ;
		Determinant det3 = new Determinant("TYPE", "VARCHAR(20)") ;
		Determinant det4 = new Determinant("VALEUR", "FLOAT") ;
		
		LinkedList<Determinant> det = new LinkedList<>() ;
		det.addFirst(det1);
		det.addFirst(det2);
		det.addFirst(det3);
		det.addFirst(det4);
		
		//Copie de la liste d'intitules pour une requete
		LinkedList<Determinant> detliste = new LinkedList<Determinant>(det) ;
		//LOG.info(detliste);
		
		// Creation de la base
		service.createTableParam("TEST", det);
		
		// remplissage base
		LigneBat batiment;
		HashMap<String, LigneBat> data = new HashMap<String, LigneBat>();
		
		for (int i=1;i<11;i++) {
			if (i<6) {
				batiment = new LigneBat("test1", i, "test_type1", (float) i/5 ) ;
			}
			else {
				batiment = new LigneBat("test2", i, "test_type4", (float) i/5 ) ;
			} ;
			//service.insertTableParam(batiment,"TEST") ;
			data.put(String.valueOf(i), batiment);
		};
		
		service.insertTableGenericV2(data, detliste, "TEST");
		
		
		//service.createTableParam("TEST2", det) ;
		
		// requete select et affichage
		LOG.debug("suite");
		List<LigneBat> affich = service.selectTable(detliste, "TEST");
		for (LigneBat ligne : affich) {
			System.out.println(ligne.id + " " + ligne.age +" "+ ligne.type +" "+ ligne.valeur);
		}
		
		//update
		// Creation de la liste pour update (determinant - nouvelle valeur)
		Determinant updet1 = new Determinant("ID", "'type_nouveau'") ;
		Determinant updet2 = new Determinant("AGE", "8") ;
		Determinant updet3 = new Determinant("TYPE", "'uptype'") ;
		
		LinkedList<Determinant> updet = new LinkedList<>() ;
		updet.addFirst(updet1);
		updet.addFirst(updet2);
		updet.addFirst(updet3);
		
		LinkedList<Contrainte> constr = new LinkedList<>() ;
		Contrainte constr1 = new Contrainte("ID", " = 'test1'") ;
		Contrainte constr2 = new Contrainte("AGE", "< 5") ;
		//Determinant constr3 = new Determinant("TYPE", " = 'uptype'") ;
		
		constr.addFirst(constr1);
		constr.addFirst(constr2);
		//constr.addFirst(constr3);
		
		
		service.updateTableConstr(updet, constr, "TEST");


		LOG.info("End engine");
	}


}
