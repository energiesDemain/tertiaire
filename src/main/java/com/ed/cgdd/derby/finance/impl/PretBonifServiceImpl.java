package com.ed.cgdd.derby.finance.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.ed.cgdd.derby.finance.CalculCEEService;
import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.financeObjects.CEE;
import com.ed.cgdd.derby.model.financeObjects.CoutRenovation;
import com.ed.cgdd.derby.model.financeObjects.Exigence;
import com.ed.cgdd.derby.model.financeObjects.Financement;
import com.ed.cgdd.derby.model.financeObjects.Geste;
import com.ed.cgdd.derby.model.financeObjects.GesteFinancement;
import com.ed.cgdd.derby.model.financeObjects.ListeFinanceValeur;
import com.ed.cgdd.derby.model.financeObjects.PBC;
import com.ed.cgdd.derby.model.financeObjects.PretBonif;
import com.ed.cgdd.derby.model.parc.Parc;

public class PretBonifServiceImpl extends TypeFinanceServiceImpl {
	private CalculCEEService calculCEEService;

	public CalculCEEService getCalculCEEService() {
		return calculCEEService;
	}

	public void setCalculCEEService(CalculCEEService calculCEEService) {
		this.calculCEEService = calculCEEService;
	}

	@Override
	public GesteFinancement createFinancement(Parc parc, Conso consoEner, Geste geste, Financement financement,
			int anneeNtab, int annee, PBC pretDeBase, CEE valeurCEE, BigDecimal surface,
			HashMap<String, BigDecimal> coutIntangible, HashMap<String, BigDecimal> coutIntangibleBati,
			BigDecimal coutEnergie, HashMap<String, BigDecimal> evolCoutBati, HashMap<String, BigDecimal> evolCoutTechno) {
		// on check si le geste est compatible
		// TODO test pour Systeme/enveloppe Bati

		BigDecimal aide = calculCEEService.calculCEE(surface, geste, valeurCEE);
		CoutRenovation coutRenov = recupParamSegment(parc, consoEner, geste, anneeNtab, annee, surface, coutIntangible,
				coutIntangibleBati, coutEnergie, evolCoutBati, evolCoutTechno);
		return createFinancementBPI(geste, (PretBonif) financement, pretDeBase, aide, coutRenov);

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
			if (coutAFinance.compareTo(aide) < 0) {
				aide = BigDecimal.valueOf(coutAFinance.doubleValue());
				coutAFinance = BigDecimal.ZERO;
			} else {
				coutAFinance = coutAFinance.subtract(aide);
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
