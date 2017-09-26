package com.ed.cgdd.derby.calibrageCINT.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;

import com.ed.cgdd.derby.model.parc.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ed.cgdd.derby.calibrageCINT.CalibrageService;
import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.common.CommonService;
import com.ed.cgdd.derby.model.financeObjects.CalibCI;
import com.ed.cgdd.derby.model.financeObjects.CalibCIBati;
import com.ed.cgdd.derby.model.financeObjects.CalibCIRef;
import com.ed.cgdd.derby.model.financeObjects.CoutEnergie;
//import com.ed.cgdd.derby.process.impl.ProcessServiceImpl;

public class CalibrageServiceImpl implements CalibrageService {
	private final static Logger LOG = LogManager.getLogger(CalibrageServiceImpl.class);
	public CommonService commonService;

	public CommonService getCommonService() {
		return commonService;
	}

	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	
	private static final String INIT_STATE = "Etat initial";
	private static final Integer YEAR_CALIB=2009;
	private static final int PERIOD_CALIB=1;

	// methode de calcul des CI pour le BATI --> cette methode renvoie une
	// hashmap de CI pour le BATI
	// la cle contient la branche et le geste (voir methode recupCIBati)
	public HashMap<String, BigDecimal> calibreCIBati(HashMap<String, CalibCIBati> dataCalib, ParamCInt paramCInt) {
		HashMap<String, BigDecimal> results = new HashMap<String, BigDecimal>();
		// pour le calage, on utilise ne rien faire
		HashMap<String, CalibCIRef> calibRef = new HashMap<String, CalibCIRef>();
		for (String st : dataCalib.keySet()) {
			if (dataCalib.get(st).getGeste().equals(INIT_STATE)) {
				calibRef.put(dataCalib.get(st).getBranche(), coutGlobalReference(dataCalib.get(st)));
			}
		}
		// on parcourt la hashmap pour avoir faire le calcul
		for (String str : dataCalib.keySet()) {
			Double inter1 = (dataCalib.get(str).getPartMarche().divide(calibRef.get(dataCalib.get(str).getBranche())
					.getPartMarche2009(), MathContext.DECIMAL32)).doubleValue();
			Double inter2 = BigDecimal.ONE.divide(BigDecimal.valueOf(-paramCInt.getNu()), MathContext.DECIMAL32).doubleValue();
			BigDecimal intermediaire = BigDecimal.valueOf(Math.pow(inter1, inter2));
			BigDecimal coutGlobalGeste = intermediaire.multiply(calibRef.get(dataCalib.get(str).getBranche())
					.getCoutGlobal(), MathContext.DECIMAL32);
			
			
			// je laisse de cote l'actualisation des charges ener pour
			// simplifier
			// quel taux d'actualisation prendre ?

//			BigDecimal coutIntangible = coutGlobalGeste.subtract(
//					dataCalib
//							.get(str)
//							.getCoutMoy()
//							.divide(BigDecimal.valueOf(dataCalib.get(str).getDureeVie()), MathContext.DECIMAL32)
//							.add(dataCalib
//									.get(str)
//									.getChargeInit()
//									.multiply(
//											BigDecimal.ONE.subtract(dataCalib.get(str).getGainMoy(),
//													MathContext.DECIMAL32), MathContext.DECIMAL32),
//									MathContext.DECIMAL32), MathContext.DECIMAL32);
//			
			
			// BV ajout actualisation 
			// TODO mettre un taux d'actualisation different pour le public et le prive voire pptaire/locataire
			BigDecimal tauxInt = BigDecimal.ONE.add(new BigDecimal("0.04"));
			BigDecimal inverse = BigDecimal.ONE.divide(tauxInt, MathContext.DECIMAL32);

			// BigDecimal coefactu = commonService.serieGeometrique(inverse, inverse, dataCalib.get(str).getDureeVie() - 1);
			
			BigDecimal coefactu = inverse.multiply(BigDecimal.ONE.subtract(inverse.pow(dataCalib.get(str).getDureeVie(), MathContext.DECIMAL32), 
					MathContext.DECIMAL32),MathContext.DECIMAL32)
			.divide(BigDecimal.ONE.subtract(inverse, MathContext.DECIMAL32),
			MathContext.DECIMAL32);
			
			BigDecimal coutIntangible = coutGlobalGeste.subtract(
					dataCalib
							.get(str)
							.getCoutMoy()
							.divide(coefactu, MathContext.DECIMAL32)
							.add(dataCalib
									.get(str)
									.getChargeInit()
									.multiply(
											BigDecimal.ONE.subtract(dataCalib.get(str).getGainMoy(),
													MathContext.DECIMAL32), MathContext.DECIMAL32),
									MathContext.DECIMAL32), MathContext.DECIMAL32);
			
			results.put(str, coutIntangible);
		}

		return results;

	}
	// Ajout 21092017
    @Override
    public void addingRowsInHashMap(HashMap<String, CalibCI> cintMapNeuf, HashMap<Integer,
			CoutEnergie> coutEnergieMap, HashMap<String, ParamBesoinsNeufs> bNeufsMap) {
        for (String calibKey : cintMapNeuf.keySet()){
        	CalibCI calib = cintMapNeuf.get(calibKey);
    		calib.setCoutEner(coutEnergieMap.get(YEAR_CALIB).getEnergie(Energies.getEnumName(calib.getEnergies())));
    		calib.setBesoinUnitaire(bNeufsMap.get(calib.getBranche()+calib.getBatType()+ Usage.CHAUFFAGE.getLabel()).getPeriode(PERIOD_CALIB));
			cintMapNeuf.put(calibKey,calib);
		}

    }


	protected CalibCIRef coutGlobalReference(CalibCIBati calibCIBati) {
		CalibCIRef reference = new CalibCIRef();
		// calcul du cout global pour le geste Rien faire
		// les couts intangibles sont nuls
		// la dureee de vie est de 1
		// TODO verifier le calcul du cout global ne rien faire dans excel
		
		BigDecimal coutGlobal = calibCIBati.getChargeInit();
		reference.setCoutGlobal(coutGlobal);

		BigDecimal partRef = calibCIBati.getPartMarche();
		reference.setPartMarche2009(partRef);

		return reference;
	}

	// methode de calcul des CI --> cette methode renvoie une hashmap de CI
	public HashMap<String, BigDecimal> calibreCI(HashMap<String, CalibCI> dataCalib, ParamCInt paramCint) {
		
		HashMap<String, BigDecimal> results = new HashMap<String, BigDecimal>();

		// pour le calage : Chaudiere gaz
		HashMap<String, CalibCIRef> calibRef = new HashMap<String, CalibCIRef>();

		for (String st : dataCalib.keySet()) {

			// traitement specifique pour la branche transport
			if (dataCalib.get(st).getBranche().equals(Branche.TRANSPORT.getCode())
					&& dataCalib.get(st).getEnergies().equals(Energies.GAZ.getCode())) {
				calibRef.put(generateKey(dataCalib.get(st)), refCopy(dataCalib.get(st), paramCint.getCintRef()));

			} else {
				if ((dataCalib.get(st).getSysteme().equals(SysChaud.CHAUDIERE_GAZ.getCode()))
						&& (dataCalib.get(st).getEnergies().equals(Energies.GAZ.getCode()) && (!dataCalib.get(st)
								.getPerformant()))) {
					calibRef.put(generateKey(dataCalib.get(st)), refCopy(dataCalib.get(st), paramCint.getCintRef()));
				}
			}
		}

		for (String str : dataCalib.keySet()) {
			// if (!(dataCalib.get(str).getPerformant())) {
			if (paramCint.getNu() == 0) { // dans ce cas les PM sont les memes pour tous et
							// les CI ne peuvent pas etre calcule
				results.put(str, BigDecimal.TEN);

			} else {

				// dans ce cas les pm ne sont pas nulles
				// inter1 rapport entre la part de marche de l'option calibree et celle de l'option de reference 
				Double inter1 = dataCalib
						.get(str)
						.getPartMarche2009()
						.divide(calibRef.get(generateKey(dataCalib.get(str))).getPartMarche2009(),
								MathContext.DECIMAL32).doubleValue();
				
				
				Double inter2 = BigDecimal.ONE.divide(BigDecimal.valueOf(-paramCint.getNu()), MathContext.DECIMAL32).doubleValue();
				BigDecimal intermediaire = BigDecimal.valueOf(Math.pow(inter1, inter2));
				
				// BV ajout actualisation de l'investissement (4%)
				// TODO mettre un taux d'actualisation different pour le public et le prive voire pptaire/locataire
				
				BigDecimal tauxInt = BigDecimal.ONE.add(new BigDecimal("0.04"));
				BigDecimal inverse = BigDecimal.ONE.divide(tauxInt, MathContext.DECIMAL32);
				// BigDecimal coefactu = commonService.serieGeometrique(inverse, inverse, dataCalib.get(str).getDureeVie() - 1);
				
				BigDecimal coefactu = inverse.multiply(BigDecimal.ONE.subtract(inverse.pow(dataCalib.get(str).getDureeVie(), MathContext.DECIMAL32), 
						MathContext.DECIMAL32),MathContext.DECIMAL32)
				.divide(BigDecimal.ONE.subtract(inverse, MathContext.DECIMAL32),
				MathContext.DECIMAL32);
				
//				BigDecimal chargesEnerinter = dataCalib.get(str).getCoutEner()
//						.multiply(dataCalib.get(str).getBesoinUnitaire()
//										.divide(dataCalib.get(str).getRdt(), MathContext.DECIMAL32)
//										.multiply(coefactu,MathContext.DECIMAL32)
//										.divide(BigDecimal.valueOf(dataCalib.get(str).getDureeVie()), MathContext.DECIMAL32), 
//										MathContext.DECIMAL32);
				
				BigDecimal besoinUnitaire =  dataCalib.get(str).getBesoinUnitaire();	
				
//				if(str.substring(7,8).equals("02")){
//				besoinUnitaire =   besoinUnitaire.multiply(new BigDecimal("2.58"), MathContext.DECIMAL32);
//				}

//				
				// 	calcul charges energetiques annuelles, agent myope
				BigDecimal chargesEnerinter = dataCalib.get(str).getCoutEner()
				.multiply(besoinUnitaire
				.divide(dataCalib.get(str).getRdt(), MathContext.DECIMAL32), 
				MathContext.DECIMAL32);
				
				// on calcule le cout annualise en divisant l'investissement par le coef d'actualisation
				
				BigDecimal interFinal = (intermediaire.multiply(calibRef.get(generateKey(dataCalib.get(str)))
				.getCoutGlobal(), MathContext.DECIMAL32)).subtract((dataCalib.get(str).getCoutM2().divide(
						coefactu, MathContext.DECIMAL32)).add(chargesEnerinter,
								MathContext.DECIMAL32), MathContext.DECIMAL32);
				
				//if(str.substring(0,2).equals("01") && str.substring(2,4).equals("42")){
				//LOG.debug("cef={} id={} BE ={}, COUT = {}, CGref = {}, CE = {}, PE={}, RDT={}", coefactu, str, besoinUnitaire, 
				//dataCalib.get(str).getCoutM2(), calibRef.get(generateKey(dataCalib.get(str)))
				//.getCoutGlobal(),chargesEnerinter,dataCalib.get(str).getCoutEner(),dataCalib.get(str).getRdt());
				//}
				
				// version initiale
//				BigDecimal interFinal = (intermediaire.multiply(calibRef.get(generateKey(dataCalib.get(str)))
//						.getCoutGlobal(), MathContext.DECIMAL32)).subtract((dataCalib.get(str).getCoutM2().divide(
//						BigDecimal.valueOf(dataCalib.get(str).getDureeVie()), MathContext.DECIMAL32)).add(
//						dataCalib
//								.get(str)
//								.getCoutEner()
//								.multiply(
//										dataCalib.get(str).getBesoinUnitaire()
//												.divide(dataCalib.get(str).getRdt(), MathContext.DECIMAL32),
//										MathContext.DECIMAL32), MathContext.DECIMAL32), MathContext.DECIMAL32);
//				
				
				if (!dataCalib.get(str).getPerformant()) {
					results.put(str, interFinal);
				} else {
					results.put(modif(str), interFinal);
				}

			}
		}

		return results;

	}

	protected String modif(String stri) {

		String membre1 = stri.substring(0, 4);
		String sysChaud = stri.substring(4, 6);
		String membre2 = stri.substring(6, 9);

		BigDecimal sysChaudTemp = new BigDecimal(sysChaud).add(new BigDecimal("20"));
		String sysChaufFinal = sysChaudTemp.toString();

		return membre1 + sysChaufFinal + membre2;
	}

	protected String equivCINonPerformant(String stri) {
		return stri.substring(0, stri.length() - 1) + "0";
	}

	protected CalibCIRef refCopy(CalibCI calibCI, BigDecimal cintRef) {
		CalibCIRef result = new CalibCIRef();
		
		BigDecimal chargesEner = calibCI.getBesoinUnitaire().multiply(calibCI.getCoutEner(), MathContext.DECIMAL32)
				.divide(calibCI.getRdt(), MathContext.DECIMAL32);
		
		// BV ajout actualisation de l'investissement (4%)
		// TODO mettre un taux d'actualisation different pour le public et le prive voire pptaire/locataire
	
		BigDecimal tauxInt = BigDecimal.ONE.add(new BigDecimal("0.04"));
		BigDecimal inverse = BigDecimal.ONE.divide(tauxInt, MathContext.DECIMAL32);
		
		// BigDecimal coefactu = commonService.serieGeometrique(inverse, inverse, calibCI.getDureeVie() - 1);

		
		BigDecimal coefactu = inverse.multiply(BigDecimal.ONE.subtract(inverse.pow(calibCI.getDureeVie(), MathContext.DECIMAL32), 
				MathContext.DECIMAL32),MathContext.DECIMAL32)
		.divide(BigDecimal.ONE.subtract(inverse, MathContext.DECIMAL32),
		MathContext.DECIMAL32);
		// LOG.debug("cef={} DV={} coef={}", tauxInt, calibCI.getDureeVie(),
		//		 coefactu);
		// chargesEner =  chargesEner.multiply(coefactu,MathContext.DECIMAL32).divide(BigDecimal.valueOf(calibCI.getDureeVie()), MathContext.DECIMAL32);
				
		BigDecimal calcCG = calibCI.getCoutM2()
				.divide(coefactu, MathContext.DECIMAL32)
				.add(chargesEner, MathContext.DECIMAL32);
		result.setPartMarche2009(new BigDecimal(calibCI.getPartMarche2009().toString()));

		// TODO a revoir peut etre CIbase = 10...
		result.setCoutGlobal(calcCG.add(cintRef, MathContext.DECIMAL32));
		//result.setCoutGlobal(calcCG.add(BigDecimal.TEN, MathContext.DECIMAL32));
		//result.setCoutGlobal(calcCG.add(new BigDecimal("20.00"), MathContext.DECIMAL32));
		//result.setCoutGlobal(calcCG.subtract(new BigDecimal("2.00"), MathContext.DECIMAL32));
		//result.setCoutGlobal(calcCG.subtract(new BigDecimal("1.57"), MathContext.DECIMAL32));
		
		return result;
	}

	protected String generateKey(CalibCI calibCI) {
		return calibCI.getBranche().concat(calibCI.getBatType());
	}
}
