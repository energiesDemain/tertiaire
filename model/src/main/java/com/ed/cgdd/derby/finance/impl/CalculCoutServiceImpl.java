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

		// on declare un tableau de la taille duree de vie du geste pour stocker
		// les annuites

		BigDecimal[] tableauAnnuite = new BigDecimal[dureeDeVieTravaux];
		// initialisation du tableau avec les consommations d'energie
		// si pas de travaux, on reprend les charges ener ini
		BigDecimal charge;
		if (gesteFin.getGeste().getTypeRenovSys().equals(TypeRenovSysteme.ETAT_INIT)
				&& gesteFin.getGeste().getTypeRenovBati().equals(TypeRenovBati.ETAT_INIT)) {
			charge = new BigDecimal(gesteFin.getCoutRenov().getCEini().toString());
		} else {
			BigDecimal coutEnergie = coutEnergieService.coutEnergie(coutEnergieMap, emissionsMap, annee, gesteFin
					.getGeste().getEnergie(), Usage.CHAUFFAGE.getLabel(), BigDecimal.ONE);

			// TODO ajouter charges de maintenance (attention aux duree de
			// vie)
			
			// BV DEBUG GESTE
			//LOG.debug("{} {} {} {} {} {} {}", gesteFin.getGeste().getExigence(),gesteFin.getGeste().getTypeRenovBati(),
			//		gesteFin.getGeste().getTypeRenovSys(),gesteFin.getGeste().getGainEner(),gesteFin.getGeste().getSysChaud(),
			//		gesteFin.getGeste().getRdt(), gesteFin.getGeste().getSysChaud().substring(0,1));
			
			charge = coutEnergieService.chargesEnerAnnuelles(surface, besoinInitUnitaire, gesteFin.getGeste(),
					coutEnergie,annee);
		}
		Arrays.fill(tableauAnnuite, charge);
		
		for (ListeFinanceValeur liste : gesteFin.getListeFinancement()) {

			if (liste.getFinance().getType() != FinancementType.CEE) {
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
				}
				// on ajoute les charges energetiques pour les annees restantes
				// (au
				// dela de l'horizon de financement)
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
		coutFin.setCoutGlobal(calculCoutGlobal(coutFin, tauxActu));
		coutFin.setDetailFinancement(gesteFin.getListeFinancement());
		coutFin.setSurfaceUnitaire(surface);
		coutFin.setReglementation(gesteFin.getGeste().getReglementation());

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
	public BigDecimal calculCoutGlobal(CoutFinal coutFinal, PBC tauxActualisation) {

		// on recupere le cout intangible
		BigDecimal valCoutInt = coutFinal.getCoutIntangible();

		BigDecimal val = BigDecimal.ZERO;
		// on utilise le tableau de bigDecimal pour le calcul du cout global
		// actualise
		BigDecimal[] tab = coutFinal.getAnnuites();
		BigDecimal tauxInt = BigDecimal.ONE.add(tauxActualisation.getTauxInteret());
		for (int j = 0; j < coutFinal.getAnnuites().length; j++) {
			// diviseur = (1+ taux)^(j+1)
			BigDecimal diviseur = (tauxInt).pow(j + 1, MathContext.DECIMAL32);
			// val = CINT + sommme(annuite/diviseur)
			val = val.add((tab[j]).divide(diviseur, MathContext.DECIMAL32));
		}
		// on ajoute la valeur dans la hashmap en divisant par la duree de
		// vie de la renovation pour rapporter le cout global a une annee
		
		//return val.divide(BigDecimal.valueOf(coutFinal.getAnnuites().length), MathContext.DECIMAL32).add(valCoutInt,
		//		MathContext.DECIMAL32);
		
		// modif on ajoute  la hashmap en actualisant sur la duree de vie des travaux pour se ramener a une annee
		BigDecimal inverse = BigDecimal.ONE.divide(tauxInt, MathContext.DECIMAL32);

		BigDecimal coefactu = commonService.serieGeometrique(inverse, inverse, coutFinal.getAnnuites().length - 1);

		return val.divide(coefactu, MathContext.DECIMAL32).add(valCoutInt,
						MathContext.DECIMAL32);


	}

}
