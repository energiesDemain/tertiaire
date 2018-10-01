package com.ed.cgdd.derby.finance.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.ed.cgdd.derby.finance.CalculCEEService;
import com.ed.cgdd.derby.model.politiques;
import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.financeObjects.*;
import com.ed.cgdd.derby.model.parc.Parc;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PretBonifServiceImpl extends TypeFinanceServiceImpl {
	private CalculCEEService calculCEEService;
	private final static Logger LOG = LogManager.getLogger(FinanceServiceImpl.class);

	public CalculCEEService getCalculCEEService() {
		return calculCEEService;
	}

	public void setCalculCEEService(CalculCEEService calculCEEService) {
		this.calculCEEService = calculCEEService;
	}

	@Override
	public GesteFinancement createFinancement(Parc parc, Conso consoEner, Geste geste, Financement financement,
	  int anneeNtab, int annee, PBC pretDeBase, CEE valeurCEE, BigDecimal surface,
	HashMap<String,CalibCoutGlobal> coutIntangible, HashMap<String,CalibCoutGlobal> coutIntangibleBati,
	  BigDecimal coutEnergie, HashMap<String, BigDecimal> evolCoutBati, HashMap<String, BigDecimal> evolCoutTechno, HashMap<String, BigDecimal> evolCoutIntTechno) {
		// on check si le geste est compatible
		// TODO test pour Systeme/enveloppe Bati

		BigDecimal aide = calculCEEService.calculCEE(surface, geste, valeurCEE);
//		long startRecupParamSegment = System.currentTimeMillis();
		CoutRenovation coutRenov = recupParamSegment(parc, consoEner, geste, anneeNtab, annee, surface, coutIntangible,
				coutIntangibleBati, coutEnergie, evolCoutBati, evolCoutTechno, evolCoutIntTechno);
//		long endRecupParamSegment = System.currentTimeMillis();
//		if(endRecupParamSegment - startRecupParamSegment >1){
//			LOG.info("Recup Param Segment Pret Bonif : {}ms - geste {}", endRecupParamSegment - startRecupParamSegment);}

//		long startCreate = System.currentTimeMillis();
		GesteFinancement financementReturn = createFinancementBPI(geste, (PretBonif) financement, pretDeBase, aide, coutRenov);
//		long endCreate = System.currentTimeMillis();
//		if(endCreate - startCreate >1){
//			LOG.info("Create finacement Pret Bonif : {}ms", endCreate - startCreate);}

		return financementReturn;

	}

	GesteFinancement createFinancementBPI(Geste geste, PretBonif financement, PBC pretDeBase, BigDecimal aide,
			CoutRenovation coutRenov) {
		if ((financement.getEcoCond().equals(Exigence.AUCUNE) || (financement.getEcoCond().equals(geste.getExigence())))
				|| (financement.getEcoCond().equals(Exigence.RT_PAR_ELEMENT) && geste.getExigence().equals(
						Exigence.BBC_RENOVATION))) {

			GesteFinancement gesteFinance = new GesteFinancement();

			List<ListeFinanceValeur> listeFinancement = new LinkedList<>();

			BigDecimal coutAFinance = coutRenov.getCT();
			if (coutRenov.getCTA() != null) {
				coutAFinance = coutAFinance.add(coutRenov.getCTA());
			}


			// on enleve du cout a financer les CEE
			// si l'aide est trop importante, on met le cout a zero et l'aide au
			// niveau de coutAfinance
			// si on choisit d'inclure les couts intangibles dans la subvention, on fait le meme calcul sur coutafinance + CINT
//			if (coutAFinance.compareTo(aide) < 0) {
//			aide = BigDecimal.valueOf(coutAFinance.doubleValue());
//			coutAFinance = BigDecimal.ZERO;
//		} else {
//			coutAFinance = coutAFinance.subtract(aide);
//		}
			
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
			// condition specifique BPI

			BigDecimal financementPretBPI = (financement.getPretMax().multiply(BigDecimal.valueOf(1000),
					MathContext.DECIMAL32)).min(coutAFinance);

			listeFinancement.add(0, new ListeFinanceValeur(financement, financementPretBPI));
			if (coutAFinance.compareTo(financementPretBPI) != 0) {
				listeFinancement.add(new ListeFinanceValeur(pretDeBase, coutAFinance.subtract(financementPretBPI)));
			}
			listeFinancement.add(new ListeFinanceValeur(new CEE(), aide));

			gesteFinance.setCoutRenov(coutRenov);
			gesteFinance.setGeste(geste);
			gesteFinance.setListeFinancement(listeFinancement);
			gesteFinance.setNomGesteFinance(getName(geste, financement));

			// TODO ajouter les frais de dossier dans valeurFin

			return gesteFinance;
		} else {
			return null;
		}
	}

}
