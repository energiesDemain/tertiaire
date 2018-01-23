package com.ed.cgdd.derby.finance.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.ed.cgdd.derby.finance.CalculCEEService;
import com.ed.cgdd.derby.model.CalibParameters;
import com.ed.cgdd.derby.model.politiques;
import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.financeObjects.*;
import com.ed.cgdd.derby.model.parc.Parc;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PBCServiceImpl extends TypeFinanceServiceImpl {
	private CalculCEEService calculCEEService;
	private final static Logger LOG = LogManager.getLogger(FinanceServiceImpl.class);

	public CalculCEEService getCalculCEEService() {
		return calculCEEService;
	}

	public void setCalculCEEService(CalculCEEService calculCEE) {
		this.calculCEEService = calculCEE;
	}

	@Override
	public GesteFinancement createFinancement(Parc parcIni, Conso consoEner, Geste geste, Financement financement,
			int anneeNtab, int annee, PBC pretDeBase, CEE valeurCEE, BigDecimal surface,
			HashMap<String,CalibCoutGlobal> coutIntangible, HashMap<String,CalibCoutGlobal> coutIntangibleBati,
			BigDecimal coutEnergie, HashMap<String, BigDecimal> evolCoutBati,
			HashMap<String, BigDecimal> evolCoutTechno,HashMap<String, BigDecimal> evolCoutIntTechno) {

		CoutRenovation coutRenov = recupParamSegment(parcIni, consoEner, geste, anneeNtab, annee, surface,
				coutIntangible, coutIntangibleBati, coutEnergie, evolCoutBati, evolCoutTechno, evolCoutIntTechno);

		// subvention sur les economies denergie actualisees
		if(politiques.checkSubEcoEner && geste.getGainEner().equals(BigDecimal.ZERO) == false){
		// calcul gain en cumac du geste
		int dureeDeVieTravaux = Math.max(geste.getDureeBati(), geste.getDureeSys());
		BigDecimal tauxInt = BigDecimal.ONE.add(CalibParameters.TAUX_ACTU_CALIB);
		// calcul coef actu sur la duree de vie des travaux
		BigDecimal coefactu = (BigDecimal.ONE
						.subtract(BigDecimal.ONE.divide(tauxInt.pow(dureeDeVieTravaux), MathContext.DECIMAL32), MathContext.DECIMAL32))
						.divide(tauxInt.subtract(BigDecimal.ONE), MathContext.DECIMAL32);
				
		BigDecimal gainCumac = consoEner.getAnnee(anneeNtab - 1).multiply(geste.getGainEner(), MathContext.DECIMAL32)
				.divide(parcIni.getAnnee(anneeNtab - 1), MathContext.DECIMAL32).multiply(coefactu, MathContext.DECIMAL32);

		geste.setValeurCEE(gainCumac);
		
		}
		
		BigDecimal aide = calculCEEService.calculCEE(surface, geste, valeurCEE);
		GesteFinancement returnGeste = createFinancementPBC(geste, (PBC) financement, aide, coutRenov);

		return returnGeste;
	}

	GesteFinancement createFinancementPBC(Geste geste, PBC financement, BigDecimal aide, CoutRenovation coutRenov) {
		GesteFinancement gesteFinance = new GesteFinancement();
		List<ListeFinanceValeur> listeFinancement = new LinkedList<>();

		BigDecimal coutAFinance = coutRenov.getCT();
		if (coutRenov.getCTA() != null  && coutRenov.getCTA().compareTo(new BigDecimal("0.0001")) == 1) {
			coutAFinance = coutAFinance.add(coutRenov.getCTA());
		}
		
		// on enleve du cout a financer les CEE
		// si l'aide est trop importante, on met le cout a zero et l'aide au
		// niveau de coutAfinance
		// si on choisit d'inclure les couts intangibles dans la subvention, on fait le meme calcul sur coutafinance + CINT
		
		if (politiques.checkCEECINT){
			BigDecimal CINTAFinance = coutRenov.getCINT().multiply(BigDecimal.valueOf(coutRenov.getDuree()), 
					MathContext.DECIMAL32);
			BigDecimal CoutAFinanceCINT = coutAFinance.add(CINTAFinance, MathContext.DECIMAL32);
			
			if (coutAFinance.compareTo(aide) < 0 && aide.compareTo(BigDecimal.ZERO)> 0 && 
					CINTAFinance.compareTo(BigDecimal.ZERO) < 0) {
				aide = BigDecimal.valueOf(coutAFinance.doubleValue());
				coutAFinance = BigDecimal.ZERO;
			} else if (coutAFinance.compareTo(aide) < 0 && aide.compareTo(BigDecimal.ZERO)> 0 &&
					CINTAFinance.compareTo(BigDecimal.ZERO) > 0) {
				coutRenov.setCINT((CINTAFinance.subtract(aide.subtract(coutAFinance, MathContext.DECIMAL32)))
						.divide(BigDecimal.valueOf(coutRenov.getDuree()), MathContext.DECIMAL32));
				aide = BigDecimal.valueOf(coutAFinance.doubleValue());
				coutAFinance = BigDecimal.ZERO;
				if(coutRenov.getCINT().compareTo(BigDecimal.ZERO) < 0){
					coutRenov.setCINT(BigDecimal.ZERO);
				}
				
			} else {
				coutAFinance = coutAFinance.subtract(aide);
			}			
		} else {
			if (coutAFinance.compareTo(aide) < 0 & aide.compareTo(BigDecimal.ZERO)> 0) {
				aide = BigDecimal.valueOf(coutAFinance.doubleValue());
				coutAFinance = BigDecimal.ZERO;
			} else {
				coutAFinance = coutAFinance.subtract(aide);
		}
		}

		
		
		// Pas de partFinance pour le PBC : l'integralite du financement est
		// assure par un pret

		listeFinancement.add(0, new ListeFinanceValeur(financement, coutAFinance));
		listeFinancement.add(1, new ListeFinanceValeur(new CEE(), aide));

		gesteFinance.setCoutRenov(coutRenov);
		gesteFinance.setGeste(geste);
		gesteFinance.setListeFinancement(listeFinancement);
		gesteFinance.setNomGesteFinance(getName(geste, financement));

		return gesteFinance;

	}

}
