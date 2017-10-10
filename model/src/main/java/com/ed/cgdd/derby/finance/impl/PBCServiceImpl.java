package com.ed.cgdd.derby.finance.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.ed.cgdd.derby.finance.CalculCEEService;
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
			BigDecimal coutEnergie, HashMap<String, BigDecimal> evolCoutBati, HashMap<String, BigDecimal> evolCoutTechno) {

//		long startRecupParamSegment = System.currentTimeMillis();
		CoutRenovation coutRenov = recupParamSegment(parcIni, consoEner, geste, anneeNtab, annee, surface,
				coutIntangible, coutIntangibleBati, coutEnergie, evolCoutBati, evolCoutTechno);
//		long endRecupParamSegment = System.currentTimeMillis();
//		if(endRecupParamSegment - startRecupParamSegment >1){
//			LOG.info("Recup Param Segment PBC : {}ms - geste {}", endRecupParamSegment - startRecupParamSegment);}

		BigDecimal aide = calculCEEService.calculCEE(surface, geste, valeurCEE);
//		long startCreate = System.currentTimeMillis();
		GesteFinancement returnGeste = createFinancementPBC(geste, (PBC) financement, aide, coutRenov);
//		long endCreate = System.currentTimeMillis();
//		if(endCreate - startCreate >1){
//			LOG.info("Create finacement PBC : {}ms", endCreate - startCreate);}

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
		if (coutAFinance.compareTo(aide) < 0) {
			aide = BigDecimal.valueOf(coutAFinance.doubleValue());
			coutAFinance = BigDecimal.ZERO;
		} else {
			coutAFinance = coutAFinance.subtract(aide);
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
