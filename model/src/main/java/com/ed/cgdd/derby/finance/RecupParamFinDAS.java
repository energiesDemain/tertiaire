package com.ed.cgdd.derby.finance;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import com.ed.cgdd.derby.model.financeObjects.CoutEnergie;
import com.ed.cgdd.derby.model.financeObjects.ElasticiteMap;
import com.ed.cgdd.derby.model.financeObjects.Emissions;
import com.ed.cgdd.derby.model.financeObjects.EvolValeurVerte;
import com.ed.cgdd.derby.model.financeObjects.Financement;
import com.ed.cgdd.derby.model.financeObjects.Geste;
import com.ed.cgdd.derby.model.financeObjects.Maintenance;
import com.ed.cgdd.derby.model.financeObjects.Reglementations;
import com.ed.cgdd.derby.model.financeObjects.RepartStatutOccup;
import com.ed.cgdd.derby.model.financeObjects.SurfMoy;
import com.ed.cgdd.derby.model.financeObjects.TauxInteret;

public interface RecupParamFinDAS {

	List<Financement> recupFinancement(String occupant, String branche);

	HashMap<String, Maintenance> recupMaintenance();

	List<Geste> getGesteBatiData(String idGesteBati);

	HashMap<String, SurfMoy> recupSurfMoy();

	HashMap<Integer, CoutEnergie> recupCoutEnergie(String tableName);

	HashMap<String, TauxInteret> recupTauxInteret();

	HashMap<String, RepartStatutOccup> recupRepartStatutOccup();

	HashMap<String, Emissions> recupEmissions(String tableName);

	Reglementations recupObligExig(String tableName, Reglementations reglementation);

	HashMap<String, EvolValeurVerte> recupEvolValeurVerte();

	Reglementations recupObligSurf(String tableName, Reglementations reglementation);

	Reglementations recupDecret(String tableName, Reglementations reglementation);

	Reglementations recupRtExistant(String tableName, Reglementations reglementation);

	HashMap<String, BigDecimal> getEvolutionCoutTechno();

	HashMap<String, BigDecimal> getEvolutionCoutBati();

	ElasticiteMap elasticite(String tableName, ElasticiteMap elasticiteMap);

}
