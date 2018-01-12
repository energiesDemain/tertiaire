package com.ed.cgdd.derby.finance.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ed.cgdd.derby.common.CommonService;
import com.ed.cgdd.derby.finance.CoutEnergieService;
import com.ed.cgdd.derby.model.financeObjects.CoutEnergie;
import com.ed.cgdd.derby.model.financeObjects.Emissions;
import com.ed.cgdd.derby.model.financeObjects.Geste;
import com.ed.cgdd.derby.model.parc.Energies;

public class CoutEnergieServiceImpl implements CoutEnergieService {
	private CommonService commonService;
	private final static Logger LOG = LogManager.getLogger(CoutEnergieServiceImpl.class);
	/*
	 * methode qui renvoie les charges energetiques annuelles a charge du
	 * tertiaire avec comme hypotheses : - pas de changement d'énergie utilisée
	 * - anticipation myope sur les prix de l'energie
	 */

	public CommonService getCommonService() {
		return commonService;
	}

	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}

	@Override
	public BigDecimal chargesEnerAnnuelles(BigDecimal surface, BigDecimal besoinEnerUnitaireIni, Geste geste,
			BigDecimal coutEnergie, int annee) {
		
		return besoinEnerUnitaireIni.multiply(surface, MathContext.DECIMAL32)
				.divide(geste.getRdt(), MathContext.DECIMAL32).multiply(BigDecimal.ONE.subtract(geste.getGainEner()))
				.multiply(coutEnergie);
	}

	@Override
	public BigDecimal coutEnergie(HashMap<Integer, CoutEnergie> coutEnergieMap,
			HashMap<String, Emissions> emissionsMap, int annee, String energie, String usage, BigDecimal tva) {
		CoutEnergie coutEnergie = coutEnergieMap.get(annee);
		// Energie = code energie
		BigDecimal prixEnerg = coutEnergie.getEnergie(Energies.getEnumName(energie)).multiply(tva);
		// Conversion de l'€/tCO2 en €/grCO2
		BigDecimal cce = coutEnergie.getCCE().divide((new BigDecimal("1000000")), MathContext.DECIMAL32);
		BigDecimal txEmission = BigDecimal.ZERO;
		if (emissionsMap.get(energie + usage) != null) {
			txEmission = emissionsMap.get(energie + usage).getPeriode(commonService.correspPeriode(annee));
		} else {
			txEmission = emissionsMap.get(energie + "Tous").getPeriode(commonService.correspPeriode(annee));
		}
		prixEnerg = (prixEnerg.add(txEmission.multiply(cce))).setScale(4, BigDecimal.ROUND_HALF_UP);

		return prixEnerg;
	}
}
