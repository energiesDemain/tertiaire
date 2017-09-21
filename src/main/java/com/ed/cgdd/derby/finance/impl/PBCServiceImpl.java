package com.ed.cgdd.derby.finance.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.ed.cgdd.derby.finance.CalculCEEService;
import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.financeObjects.CEE;
import com.ed.cgdd.derby.model.financeObjects.CoutRenovation;
import com.ed.cgdd.derby.model.financeObjects.Financement;
import com.ed.cgdd.derby.model.financeObjects.Geste;
import com.ed.cgdd.derby.model.financeObjects.GesteFinancement;
import com.ed.cgdd.derby.model.financeObjects.ListeFinanceValeur;
import com.ed.cgdd.derby.model.financeObjects.PBC;
import com.ed.cgdd.derby.model.parc.Parc;

public class PBCServiceImpl extends TypeFinanceServiceImpl {
	private CalculCEEService calculCEEService;

	public CalculCEEService getCalculCEEService() {
		return calculCEEService;
	}

	public void setCalculCEEService(CalculCEEService calculCEE) {
		this.calculCEEService = calculCEE;
	}

	@Override
	public GesteFinancement createFinancement(Parc parcIni, Conso consoEner, Geste geste, Financement financement,
			int anneeNtab, int annee, PBC pretDeBase, CEE valeurCEE, BigDecimal surface,
			HashMap<String, BigDecimal> coutIntangible, HashMap<String, BigDecimal> coutIntangibleBati,
			BigDecimal coutEnergie, HashMap<String, BigDecimal> evolCoutBati, HashMap<String, BigDecimal> evolCoutTechno) {

		CoutRenovation coutRenov = recupParamSegment(parcIni, consoEner, geste, anneeNtab, annee, surface,
				coutIntangible, coutIntangibleBati, coutEnergie, evolCoutBati, evolCoutTechno);
		BigDecimal aide = calculCEEService.calculCEE(surface, geste, valeurCEE);
		return createFinancementPBC(geste, (PBC) financement, aide, coutRenov);

	}

	GesteFinancement createFinancementPBC(Geste geste, PBC financement, BigDecimal aide, CoutRenovation coutRenov) {
		GesteFinancement gesteFinance = new GesteFinancement();
		List<ListeFinanceValeur> listeFinancement = new LinkedList<>();

		BigDecimal coutAFinance = coutRenov.getCT();
		if (coutRenov.getCTA() != null) {
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
