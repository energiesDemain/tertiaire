package com.ed.cgdd.derby.finance.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
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
import com.ed.cgdd.derby.process.impl.ProcessServiceImpl;
import com.ed.cgdd.derby.common.CommonService;

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

		// on declare un tableau de la taille duree de vie du geste pour stocker
		// les annuites

		BigDecimal[] tableauAnnuite = new BigDecimal[dureeDeVieTravaux];
		// initialisation du tableau avec les consommations d'energie
		// si pas de travaux, on reprend les charges ener ini
//		long startCalculCharge= System.currentTimeMillis();

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

			// ajout charges de maintenance

			BigDecimal coutMaintenance = gesteFin.getGeste().getCoutMaintenance().multiply(surface, MathContext.DECIMAL32);
			charge = charge.add(coutMaintenance, MathContext.DECIMAL32);
		}
		Arrays.fill(tableauAnnuite, charge);
//		long endCalculCharge= System.currentTimeMillis();
//		if(endCalculCharge - startCalculCharge>1){
//			LOG.info("Calculdes Charges : {}ms", endCalculCharge - startCalculCharge);}

		// TODO BV : verifier les calculs
		BigDecimal coutGlobal = charge.multiply(commonService.serieGeometrique(BigDecimal.ONE.divide(tauxActu.getTauxInteret(), MathContext.DECIMAL32),
				BigDecimal.ONE.divide(tauxActu.getTauxInteret(), MathContext.DECIMAL32), tableauAnnuite.length- 1));
//		long startAnnuite= System.currentTimeMillis();
		for (ListeFinanceValeur liste : gesteFin.getListeFinancement()) {

			if (liste.getFinance().getType() != FinancementType.CEE) {
				BigDecimal tauxInt = BigDecimal.ONE.add(tauxActu.getTauxInteret());
				// XXX probleme si la duree de pret est inferieure a la duree de
				// vie du geste
				// Solution : on limite la duree du pret a la duree de vie du
				// geste

				int dureeDeVie = Math.min(calculDuree(liste.getFinance()), dureeDeVieTravaux);
				BigDecimal annuite = calculAnnuite(liste.getFinance(), liste.getValeur(), dureeDeVie);

				// on additionne les annuitees dans le tableau avec les charges
				// energetiques
				for (int j = 0; j < dureeDeVie; j++) {
					tableauAnnuite[j] = tableauAnnuite[j].add(annuite);
						// diviseur = sommme(annuite/(1+ taux)^(j+1))
						coutGlobal = coutGlobal.add((annuite).divide((tauxInt).pow(j + 1, MathContext.DECIMAL32)
								, MathContext.DECIMAL32));
				}
			}
		}


		String anneeRenovBat = parcInit.getAnneeRenov();
		String anneeRenovSys = parcInit.getAnneeRenovSys();
		if (!gesteFin.getGeste().getTypeRenovBati().equals(TypeRenovBati.ETAT_INIT)) {
			anneeRenovBat = String.valueOf(annee);
		}
		if (!gesteFin.getGeste().getTypeRenovSys().equals(TypeRenovSysteme.ETAT_INIT)) {
			anneeRenovSys = String.valueOf(annee);
		}

//		long startFillCout= System.currentTimeMillis();

		coutFin.setAnnuites(tableauAnnuite);
		coutFin.setAnneeRenovBat(anneeRenovBat);
		coutFin.setTypeRenovBat(gesteFin.getGeste().getTypeRenovBati());
		coutFin.setAnneeRenovSys(anneeRenovSys);
		coutFin.setTypeRenovSys(gesteFin.getGeste().getTypeRenovSys());
		coutFin.setId(idParc);
		coutFin.setEnergieFin(gesteFin.getGeste().getEnergie());
		coutFin.setSysChaud(gesteFin.getGeste().getSysChaud());
		coutFin.setGainEner(gesteFin.getGeste().getGainEner());
		coutFin.setRdtFin(gesteFin.getGeste().getRdt());
		coutFin.setCoutGlobal(calculCoutGlobal(coutFin, tauxActu, coutGlobal));
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
	protected BigDecimal calculCoutGlobal(CoutFinal coutFinal, PBC tauxActualisation, BigDecimal coutGlobal) {
//		long startCoefActu = System.currentTimeMillis();

		// on recupere le cout intangible
		BigDecimal valCoutInt = coutFinal.getCoutIntangible();
		// modif on ajoute  la hashmap en actualisant sur la duree de vie des travaux pour se ramener a une annee
		BigDecimal inverse = BigDecimal.ONE.divide(tauxActualisation.getTauxInteret(), MathContext.DECIMAL32);

		BigDecimal coefactu = commonService.serieGeometrique(inverse, inverse, coutFinal.getAnnuites().length- 1);
//		long endCoefActu = System.currentTimeMillis();
//		if(endCoefActu - startCoefActu>1){
//			LOG.info("Actualisation coef : {}ms - duree annuites {}", endCoefActu - startCoefActu, coutFinal.getAnnuites().length);}

		return coutGlobal.divide(coefactu, MathContext.DECIMAL32).add(valCoutInt,
						MathContext.DECIMAL32);


	}

}
