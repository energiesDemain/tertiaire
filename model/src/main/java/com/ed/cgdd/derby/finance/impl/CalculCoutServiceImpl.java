package com.ed.cgdd.derby.finance.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;

import com.ed.cgdd.derby.common.CommonService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.ed.cgdd.derby.finance.CalculCoutService;
import com.ed.cgdd.derby.finance.CoutEnergieService;
import com.ed.cgdd.derby.model.financeObjects.CoutEnergie;
import com.ed.cgdd.derby.model.financeObjects.CoutFinal;
import com.ed.cgdd.derby.model.financeObjects.Emissions;
import com.ed.cgdd.derby.model.financeObjects.Financement;
import com.ed.cgdd.derby.model.financeObjects.FinancementType;
import com.ed.cgdd.derby.model.financeObjects.GesteFinancement;
import com.ed.cgdd.derby.model.financeObjects.ListeFinanceValeur;
import com.ed.cgdd.derby.model.financeObjects.PBC;
import com.ed.cgdd.derby.model.financeObjects.PretBonif;
import com.ed.cgdd.derby.model.parc.Parc;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.model.parc.TypeRenovSysteme;
import com.ed.cgdd.derby.model.parc.Usage;

public class CalculCoutServiceImpl implements CalculCoutService {

	private final static Logger LOG = LogManager.getLogger(CalculCoutServiceImpl.class);
	CoutEnergieService coutEnergieService;
	CommonService commonService;

	public CommonService getCommonService() {
		return commonService;
	}

	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}

	public CoutEnergieService getCoutEnergieService() {
		return coutEnergieService;
	}

	public void setCoutEnergieService(CoutEnergieService coutEnergieService) {
		this.coutEnergieService = coutEnergieService;
	}

	@Override
	public String outputName(String idParc, GesteFinancement courant, int annee, CoutFinal coutFinal) {

		return idParc + "|" + String.valueOf(annee) + "|" + coutFinal.getSysChaud() + "|" + coutFinal.getEnergieFin()
				+ "|" + courant.getNomGesteFinance();

	}

	@Override
	public CoutFinal calculCoutFinal(BigDecimal surface, BigDecimal besoinInitUnitaire, Parc parcInit,
			GesteFinancement gesteFin, int annee, String idParc, int anneeNTab, PBC tauxActu,
			HashMap<Integer, CoutEnergie> coutEnergieMap, HashMap<String, Emissions> emissionsMap,
			BigDecimal valeurVerte) {

		CoutFinal coutFin = new CoutFinal();
		int dureeDeVieTravaux = Math.max(gesteFin.getGeste().getDureeBati(), gesteFin.getGeste().getDureeSys());
		coutFin.setDureeDeVie(dureeDeVieTravaux);
//		long startValeurVerte= System.currentTimeMillis();
		// test pour la valeur verte
		if (gesteFin.getGeste().getTypeRenovBati().equals(TypeRenovBati.ENSBBC)) {
			// traitement du cas ou le cout intangible est negatif (c'est
			// possible)
			if (gesteFin.getCoutRenov().getCINT().compareTo(BigDecimal.ZERO) >= 0) {
				coutFin.setCoutIntangible(gesteFin.getCoutRenov().getCINT()
						.multiply(BigDecimal.ONE.add(valeurVerte, MathContext.DECIMAL32), MathContext.DECIMAL32));
			} else {
				coutFin.setCoutIntangible(gesteFin.getCoutRenov().getCINT()
						.multiply(BigDecimal.ONE.subtract(valeurVerte, MathContext.DECIMAL32), MathContext.DECIMAL32));
			}

		} else {
			coutFin.setCoutIntangible(gesteFin.getCoutRenov().getCINT());
		}
//		long endValeurVerte= System.currentTimeMillis();
//		if(endValeurVerte - startValeurVerte>1){
//			LOG.info("Valeur verte : {}ms", endValeurVerte - startValeurVerte);}

		// initialisation avec les consommations d'energie
		// si pas de travaux, on reprend les charges ener ini
		//		long startCalculCharge= System.currentTimeMillis();
		
		BigDecimal tauxInt = BigDecimal.ONE.add(tauxActu.getTauxInteret());

		// calcul coef actu sur la duree de vie des travaux
		BigDecimal coefactu = (BigDecimal.ONE
				.subtract(BigDecimal.ONE.divide(tauxInt.pow(dureeDeVieTravaux), MathContext.DECIMAL32), MathContext.DECIMAL32))
				.divide(tauxInt.subtract(BigDecimal.ONE), MathContext.DECIMAL32);
		
		BigDecimal charge;
		if (gesteFin.getGeste().getTypeRenovSys().equals(TypeRenovSysteme.ETAT_INIT)
				&& gesteFin.getGeste().getTypeRenovBati().equals(TypeRenovBati.ETAT_INIT)) {
			charge = new BigDecimal(gesteFin.getCoutRenov().getCEini().toString());
			// on ajoute la maintenance meme sans changement de systeme
			charge = charge.add(gesteFin.getGeste().getCoutMaintenance().multiply(surface, MathContext.DECIMAL32));
		} else {
			BigDecimal coutEnergie = coutEnergieService.coutEnergie(coutEnergieMap, emissionsMap, annee, gesteFin
					.getGeste().getEnergie(), Usage.CHAUFFAGE.getLabel(), BigDecimal.ONE);

			charge = coutEnergieService.chargesEnerAnnuelles(surface, besoinInitUnitaire, gesteFin.getGeste(),
					coutEnergie,annee);

			// ajout charges de maintenance annuelles
			
			BigDecimal coutMaintenance = gesteFin.getGeste().getCoutMaintenance().multiply(surface, MathContext.DECIMAL32);
			charge = charge.add(coutMaintenance, MathContext.DECIMAL32);
		}
		
//		long endCalculCharge= System.currentTimeMillis();
//		if(endCalculCharge - startCalculCharge>1){
//			LOG.info("Calcul des Charges : {}ms", endCalculCharge - startCalculCharge);}

		// initialisation cout global 
		BigDecimal coutGlobal = charge.multiply(coefactu);
		
//		long startAnnuite= System.currentTimeMillis();
		for (ListeFinanceValeur liste : gesteFin.getListeFinancement()) {
			if (liste.getFinance().getType() != FinancementType.CEE) {
				// XXX probleme si la duree de pret est inferieure a la duree de
				// vie du geste
				// Solution : on limite la duree du pret a la duree de vie du
				// geste
				if(((PBC) liste.getFinance()).getTauxInteret().equals(tauxActu.getTauxInteret())){
					// on ajoute le financement total au cout global si au meme taux d'interet que taux d'actu
					coutGlobal = coutGlobal.add(liste.getValeur());
				} else {
				int dureeDeVie = Math.min(calculDuree(liste.getFinance()), dureeDeVieTravaux);
				BigDecimal annuite = calculAnnuite(liste.getFinance(), liste.getValeur(), dureeDeVie);
				// on ajoute les annuites actualisees sur la duree du pret! au cout global
				coutGlobal = coutGlobal.add((annuite).multiply((BigDecimal.ONE
						.subtract(BigDecimal.ONE.divide(tauxInt.pow(dureeDeVie), MathContext.DECIMAL32), MathContext.DECIMAL32))
						.divide(tauxInt.subtract(BigDecimal.ONE), MathContext.DECIMAL32)));
				}
			}
		}
		
		// on ajoute le cout intangible et on l'ajoute au cout global et divise tout par le coef d'actu pour se ramener a une annee
		coutGlobal = coutGlobal.divide(coefactu, MathContext.DECIMAL32).add(coutFin.getCoutIntangible(),
						MathContext.DECIMAL32);
		
		
		String anneeRenovBat = parcInit.getAnneeRenov();
		String anneeRenovSys = parcInit.getAnneeRenovSys();
		if (!gesteFin.getGeste().getTypeRenovBati().equals(TypeRenovBati.ETAT_INIT)) {
			anneeRenovBat = String.valueOf(annee);
		}
		if (!gesteFin.getGeste().getTypeRenovSys().equals(TypeRenovSysteme.ETAT_INIT)) {
			anneeRenovSys = String.valueOf(annee);
		}

//		long startFillCout= System.currentTimeMillis();

//		coutFin.setAnnuites(tableauAnnuite);
		coutFin.setAnneeRenovBat(anneeRenovBat);
		coutFin.setTypeRenovBat(gesteFin.getGeste().getTypeRenovBati());
		coutFin.setAnneeRenovSys(anneeRenovSys);
		coutFin.setTypeRenovSys(gesteFin.getGeste().getTypeRenovSys());
		coutFin.setId(idParc);
		coutFin.setEnergieFin(gesteFin.getGeste().getEnergie());
		coutFin.setSysChaud(gesteFin.getGeste().getSysChaud());
		coutFin.setGainEner(gesteFin.getGeste().getGainEner());
		coutFin.setRdtFin(gesteFin.getGeste().getRdt());
		coutFin.setCoutGlobal(coutGlobal);
		coutFin.setDetailFinancement(gesteFin.getListeFinancement());
		coutFin.setSurfaceUnitaire(surface);
		coutFin.setReglementation(gesteFin.getGeste().getReglementation());
//		long endFillCout= System.currentTimeMillis();
//		if(endFillCout - startFillCout>1){
//			LOG.info("Remplissage cout final : {}ms", endFillCout - startFillCout);}

		return coutFin;
	}

	// methode qui renvoie une annuite en fonction du capital, de la duree et du
	// taux d'interet
	protected BigDecimal calculAnnuite(Financement fin, BigDecimal valeurFin, int duree) {
		// pour les TIP et les CPE, pas d'annuite : le remboursement se fait
		// sur
		// les chargesEner economisees
		// Le proprietaire n'a donc aucune annuite, mais des charges ener
		// augmentees
//		if(valeurFin.compareTo(BigDecimal.ZERO) <0){
//			LOG.debug("test");
//		}
	
		// tauxInt = ((PBC) fin).getTauxInteret() + 1
		BigDecimal tauxInt = BigDecimal.ONE.add(((PBC) fin).getTauxInteret());
		BigDecimal inverse = BigDecimal.ONE.divide(tauxInt, MathContext.DECIMAL32);		
		BigDecimal intermediaire = commonService.serieGeometrique(inverse, inverse, duree - 1);

		return valeurFin.divide(intermediaire, MathContext.DECIMAL32);

	}

	// methode pour avoir la duree d'un financement
	protected int calculDuree(Financement fin) {
		if (fin instanceof PretBonif) {
			return ((PretBonif) fin).getDuree();
		} else if (fin instanceof PBC) {
			return ((PBC) fin).getDuree();
		}
		// Ne peux pas arriver
		return 0;
	}

	// methode qui renvoie un cout global par an a partir du CoutFinal
	protected BigDecimal calculCoutGlobal(CoutFinal coutFinal, BigDecimal  coefactu, BigDecimal coutGlobal) {
//		long startCoefActu = System.currentTimeMillis();

		// on recupere le cout intangible
		BigDecimal valCoutInt = coutFinal.getCoutIntangible();
		// modif on ajoute  la hashmap en actualisant sur la duree de vie des travaux pour se ramener a une annee

		return coutGlobal.divide(coefactu, MathContext.DECIMAL32).add(valCoutInt,
						MathContext.DECIMAL32);


	}

}
