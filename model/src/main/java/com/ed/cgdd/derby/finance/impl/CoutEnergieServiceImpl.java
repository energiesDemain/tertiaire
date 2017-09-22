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
import com.ed.cgdd.derby.model.financeObjects.Exigence;
import com.ed.cgdd.derby.model.financeObjects.Geste;
import com.ed.cgdd.derby.model.parc.Energies;
import com.ed.cgdd.derby.model.parc.TypeRenovSysteme;

public class CoutEnergieServiceImpl implements CoutEnergieService {
	private CommonService commonService;
	private final static Logger LOG = LogManager.getLogger(CoutEnergieServiceImpl.class);
	/*
	 * methode qui renvoie les charges energetiques annuelles a charge du
	 * tertiaire avec comme hypotheses : - pas de changement d'Ã©nergie utilisÃ©e
	 * - anticipation myope sur les prix de l'energie
	 */

	public CommonService getCommonService() {
		return commonService;
	}

	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}

	public BigDecimal chargesEnerAnnuelles(BigDecimal surface, BigDecimal besoinEnerUnitaireIni, Geste geste,
			BigDecimal coutEnergie, int annee) {
		/*	
		// BV changement de gains pour les gestes respectant la RT existant en 2018, TODO faire proprement avec un paramètre en entrée 
		
		BigDecimal Gain = geste.getGainEner();
		BigDecimal Rdt = geste.getRdt();
	
	//	if (annee > 2017) {
	//	LOG.debug("{} {} {} {} {} {} {}", geste.getExigence(),geste.getTypeRenovBati(),
	//			geste.getTypeRenovSys(),Gain,geste.getSysChaud(),
	//			Rdt, geste.getSysChaud().substring(0,1));
	//	}
		
		if (geste.getExigence().equals(Exigence.RT_PAR_ELEMENT) && annee > 2017) {
	//		BigDecimal GainSupRTex = new BigDecimal("0.05");
	//		Gain = Gain.add(GainSupRTex);
			LOG.debug("vérif prise en compte dans calc PM exi={} geste={} syst={} syschaud={} gain={} Rdt ={}", 
					geste.getExigence(),geste.getTypeRenovBati(),
					geste.getTypeRenovSys(),geste.getSysChaud(),Gain,
					Rdt);
		}
		
		// BV changement de rdt pour les systèmes respectant la RT existant en 2018, TODO faire proprement avec un paramètre en entrée 
		
		if (geste.getTypeRenovSys().equals(TypeRenovSysteme.CHGT_SYS) &&
				geste.getSysChaud().substring(0,1).equals("0") && annee > 2017 && 
				!(geste.getEnergie().contentEquals("03"))) {
		//	BigDecimal GainRdtSupRTex = new BigDecimal("0.1");
		//	Rdt = Rdt.add(GainRdtSupRTex);
			LOG.debug("vérif prise en compte dans calc PM exi={} geste={} syst={} syschaud={} gain={} Rdt ={}", 
					geste.getExigence(),geste.getTypeRenovBati(),
					geste.getTypeRenovSys(),geste.getSysChaud(),Gain,
					Rdt);
	
		}
	
		// (besoin*surface/rdt)*(1-gain)*coutEner
			return besoinEnerUnitaireIni.multiply(surface, MathContext.DECIMAL32)
					.divide(Rdt, MathContext.DECIMAL32).multiply(BigDecimal.ONE.subtract(Gain))
					.multiply(coutEnergie);	
				
			*/	
		return besoinEnerUnitaireIni.multiply(surface, MathContext.DECIMAL32)
				.divide(geste.getRdt(), MathContext.DECIMAL32).multiply(BigDecimal.ONE.subtract(geste.getGainEner()))
				.multiply(coutEnergie);
	}

	public BigDecimal coutEnergie(HashMap<Integer, CoutEnergie> coutEnergieMap,
			HashMap<String, Emissions> emissionsMap, int annee, String energie, String usage, BigDecimal tva) {
		// Energie = code energie
		BigDecimal prixEnerg = coutEnergieMap.get(annee).getEnergie(Energies.getEnumName(energie)).multiply(tva);
		// Conversion de l'â‚¬/tCO2 en â‚¬/grCO2
		BigDecimal cce = coutEnergieMap.get(annee).getCCE().divide((new BigDecimal("1000000")), MathContext.DECIMAL32);
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
