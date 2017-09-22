package com.ed.cgdd.derby.initialize.impl;

import java.util.ArrayList;
import java.util.HashMap;

import com.ed.cgdd.derby.initialize.LoadInfoDAS;
import com.ed.cgdd.derby.model.excelobjects.ExcelParameters;

public class LoadInfoDASImpl implements LoadInfoDAS {

	// Remplissage des map avec les proprietes des parametres
	// a recuperer dans l'Excel (nom du fichier, de la feuille, premiere ligne
	// et premiere colonne du tableau de parametres)

	@Override
	public HashMap<String, ExcelParameters> excelTables() {
		HashMap<String, ExcelParameters> constantMap = new HashMap<String, ExcelParameters>();
		// Parametres d'entrees de parc

		ExcelParameters constant = new ExcelParameters();

		// Chargement des donnees de la table EDL.xlsx
		constant.setFilename("Parc_init.xlsx");
		constant.setSheetname("Parc");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("Parc_init", constant);

		// Chargement des donnees de la table ID.xlsx

		constant = new ExcelParameters();
		constant.setFilename("ID.xlsx");
		constant.setSheetname("ID");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("ID", constant);

		// Chargement des donnees de la table Autre_init.xlsx
		constant = new ExcelParameters();
		constant.setFilename("Autre_init.xlsx");
		constant.setSheetname("Autre");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("Autre_init", constant);

		// Chargement des donnees de la table Auxiliaires_init.xlsx
		constant = new ExcelParameters();
		constant.setFilename("Auxiliaires_init.xlsx");
		constant.setSheetname("Auxiliaires");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("Auxiliaires_init", constant);

		// Chargement des donnees de la table Bureautique_init.xlsx
		constant = new ExcelParameters();
		constant.setFilename("Bureautique_init.xlsx");
		constant.setSheetname("Bureautique");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("Bureautique_init", constant);

		// Chargement des donnees de la table Chauffage_init.xlsx
		constant = new ExcelParameters();
		constant.setFilename("Chauffage_init.xlsx");
		constant.setSheetname("Chauffage");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("Chauffage_init", constant);

		// Chargement des donnees de la table Climatisation_init.xlsx
		constant = new ExcelParameters();
		constant.setFilename("Climatisation_init.xlsx");
		constant.setSheetname("Climatisation");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("Climatisation_init", constant);

		// Chargement des donnees de la table Cuisson_init.xlsx
		constant = new ExcelParameters();
		constant.setFilename("Cuisson_init.xlsx");
		constant.setSheetname("Cuisson");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("Cuisson_init", constant);

		// Chargement des donnees de la table Eclairage_init.xlsx
		constant = new ExcelParameters();
		constant.setFilename("Eclairage_init.xlsx");
		constant.setSheetname("Eclairage");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("Eclairage_init", constant);

		// Chargement des donnees de la table ECS_init.xlsx
		constant = new ExcelParameters();
		constant.setFilename("ECS_init.xlsx");
		constant.setSheetname("ECS");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("ECS_init", constant);

		// Chargement des donnees de la table Froid_alimentaire_init.xlsx
		constant = new ExcelParameters();
		constant.setFilename("Froid_alimentaire_init.xlsx");
		constant.setSheetname("Froid_alimentaire");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("Froid_alimentaire_init", constant);

		// Chargement des donnees de la table Ventilation_init.xlsx
		constant = new ExcelParameters();
		constant.setFilename("Ventilation_init.xlsx");
		constant.setSheetname("Ventilation");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("Ventilation_init", constant);

		// Chargement des donnees de la table Process_init.xlsx
		constant = new ExcelParameters();
		constant.setFilename("Process_init.xlsx");
		constant.setSheetname("Process");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("Process_init", constant);

		// Chargement des donnees de la table Bibli_rdt_clim.xlsx
		constant = new ExcelParameters();
		constant.setFilename("Bibli_rdt_clim.xlsx");
		constant.setSheetname("RDT_FROID");
		constant.setFline(1);
		constant.setFcolumn(9);
		constantMap.put("Rdt_climatisation", constant);

		// Chargement des donnees de la table Geste_Bati
		constant = new ExcelParameters();
		constant.setFilename("Bibli_geste_bati.xlsx");
		constant.setSheetname("Geste_bati");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("Geste_Bati", constant);

		// Chargement des donnees concernant l'ECS la table Couts_unitaires.xlsx
		constant = new ExcelParameters();
		constant.setFilename("Couts_unitaires.xlsx");
		constant.setSheetname("ECS");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("ECS_couts", constant);

		// Chargement des donnees concernant l'eclairage et la ventilation la
		// table Couts_unitaires.xlsx
		constant = new ExcelParameters();
		constant.setFilename("Couts_unitaires.xlsx");
		constant.setSheetname("Ecl_Ventilation");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("Ecl_ventil_couts", constant);

		// Chargement des donnees de la table Bibli_rdt_chauff.xlsx
		constant = new ExcelParameters();
		constant.setFilename("Bibli_rdt_chauff.xlsx");
		constant.setSheetname("RDT_CHAUFF");
		constant.setFline(1);
		constant.setFcolumn(6);
		constantMap.put("Rdt_chauffage", constant);

		// Chargement des ratios de consommation en auxiliaires pour le
		// chauffage
		constant = new ExcelParameters();
		constant.setFilename("Ratio_aux.xlsx");
		constant.setSheetname("SYS_CHAUD");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("Ratio_aux_chaud", constant);

		// Chargement des ratios de consommation en auxiliaires pour la
		// climatisation
		constant = new ExcelParameters();
		constant.setFilename("Ratio_aux.xlsx");
		constant.setSheetname("SYS_FROID");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("Ratio_aux_froid", constant);

		// Chargement des surfaces moyennes par branche
		constant = new ExcelParameters();
		constant.setFilename("Surf_moy_etab.xlsx");
		constant.setSheetname("Surf_moy_etab");
		constant.setFline(1);
		constant.setFcolumn(7);
		constantMap.put("Surface_moy", constant);

		// Chargement des CI pour calibration
		constant = new ExcelParameters();
		constant.setFilename("Cout_Intangible_init.xlsx");
		constant.setSheetname("Cout_Intangibles");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("Calibration_CI", constant);

		// Chargement des CIBati pour calibration
		constant = new ExcelParameters();
		constant.setFilename("Cout_Intangible_init.xlsx");
		constant.setSheetname("Cout_Intangibles_Bati");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("Calibration_CI_Bati", constant);

		// Chargement des correspondances code/sysChaud
		constant = new ExcelParameters();
		constant.setFilename("code_sys.xlsx");
		constant.setSheetname("CODE_SYS");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("Code_sys", constant);

		// Chargement des bornes_etiquettes
		constant = new ExcelParameters();
		constant.setFilename("Etiquettes_Bornes.xlsx");
		constant.setSheetname("ETIQUETTE");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("Etiquettes_Bornes", constant);

		// Chargement des bornes_etiquettes
		constant = new ExcelParameters();
		constant.setFilename("Etiquettes_Categories.xlsx");
		constant.setSheetname("ETIQUETTE");
		constant.setFline(1);
		constant.setFcolumn(1);
		constantMap.put("Etiquettes_Categories", constant);

		return constantMap;
	}

	@Override
	public ArrayList<String> newTables() {

		ArrayList<String> newTables = new ArrayList<String>();
		// Tables a creer lors de l'initialisation

		newTables.add(0, "Parc_entrant");
		newTables.add(1, "Parc_sortant");
		newTables.add(2, "Entrees_parc");
		newTables.add(3, "Sorties_parc");
		newTables.add(4, "Parc_resultats");
		newTables.add(5, "Gains_nonRT");
		newTables.add(6, "PM_cuisson");
		newTables.add(7, "PM_autres");
		newTables.add(8, "PM_cuisson_chgt");
		newTables.add(9, "PM_autres_chgt");
		newTables.add(10, "DV_autres");
		newTables.add(11, "BesoinU_neuf");
		newTables.add(12, "Conso_non_RT_resultats");
		newTables.add(13, "Gains_Froid_Alim_Rglt");
		newTables.add(14, "Rythme_Froid_Alim_Rglt");
		newTables.add(15, "Rendement_ECS");
		newTables.add(16, "Rendement_performant_ECS");
		newTables.add(17, "Part_sys_performants_ECS");
		newTables.add(18, "PM_ECS_Neuf");
		newTables.add(19, "PM_ECS_Chgt");
		newTables.add(20, "Solaire_ECS");
		newTables.add(21, "Tx_Couv_Solaire");
		newTables.add(22, "DV_ECS");
		newTables.add(23, "Conso_RT_resultats");
		newTables.add(24, "Parametres_financiers");
		// newTables.add(25, "Rendement_RT_resultats");
		newTables.add(25, "Besoin_RT_resultats");
		newTables.add(26, "DV_Clim");
		newTables.add(27, "Tx_Clim_Existant");
		newTables.add(28, "Tx_Clim_Neuf");
		newTables.add(29, "Besoin_RT_resultats_test");
		newTables.add(30, "Rendement_RT_resultats_test");
		newTables.add(31, "Gains_eclairage");
		newTables.add(32, "Gains_ventilation");
		newTables.add(33, "Couts_resultats");
		newTables.add(34, "Facteurs_emission");
		newTables.add(35, "DV_chauffage");
		newTables.add(36, "DV_parois");
		newTables.add(37, "Cout_Energies");
		newTables.add(38, "Taux_Actu");
		newTables.add(39, "Proprio_Locataire");
		newTables.add(40, "Emissions");
		newTables.add(41, "Resultats_Financements");
		newTables.add(42, "Rt_existant");
		newTables.add(43, "Valeur_Verte");
		newTables.add(44, "Obligation_travaux_Ex");
		newTables.add(45, "Obligation_travaux_Surf");
		newTables.add(46, "Decret_travaux");
		newTables.add(47, "Effet_Rebond");
		newTables.add(48, "Evolution_couts");
		newTables.add(49, "Evolution_couts_bati");
		// newTables.add(50, "Etiquettes_Bornes");
		// newTables.add(51, "Etiquettes_Categories");
		newTables.add(50, "Maintenance");
		newTables.add(51, "Elasticite_prix");

		return newTables;
	}
}
