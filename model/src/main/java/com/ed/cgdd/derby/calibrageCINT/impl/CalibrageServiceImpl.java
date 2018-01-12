package com.ed.cgdd.derby.calibrageCINT.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.ed.cgdd.derby.model.financeObjects.*;
import com.ed.cgdd.derby.model.parc.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.ed.cgdd.derby.calibrageCINT.CalibrageService;
import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.common.CommonService;
import com.ed.cgdd.derby.model.CalibParameters;
import com.ed.cgdd.derby.finance.RecupParamFinDAS;

public class CalibrageServiceImpl implements CalibrageService {
	private final static Logger LOG = LogManager.getLogger(CalibrageServiceImpl.class);
	private CommonService commonService;
	private RecupParamFinDAS recupParamFinDAS;

	public CommonService getCommonService() {
		return commonService;
	}
	
	public void initServices(CommonService commonService,
			RecupParamFinDAS recupParamFinDAS) {
		this.commonService = commonService;
		this.recupParamFinDAS = recupParamFinDAS;
	}
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	
	public RecupParamFinDAS getRecupParamFinDAS() {
		return recupParamFinDAS;
	}

	public void setRecupParamFinDAS(RecupParamFinDAS recupParamFinDAS) {
		this.recupParamFinDAS = recupParamFinDAS;
	}
	
	private static final String INIT_STATE = "Etat initial";
	private static final int PERIOD_CALIB=1;
	
	// methode de calcul des CI pour le BATI --> cette methode renvoie une
	// hashmap de CI pour le BATI
	// la cle contient la branche et le geste (voir methode recupCIBati)
	
	public HashMap<String, CalibCoutGlobal> calibreCIBati(HashMap<String, CalibCIBati> dataCalib, 
			ParamCInt paramCInt) {
		HashMap<String,CalibCoutGlobal> results = new HashMap<>();
		// pour le calage, on utilise ne rien faire
		HashMap<String, CalibCIRef> calibRef = new HashMap<String, CalibCIRef>();
		for (String st : dataCalib.keySet()) {
			if (dataCalib.get(st).getGeste().equals(INIT_STATE)) {
				calibRef.put(dataCalib.get(st).getBranche(), coutGlobalReference(dataCalib.get(st), paramCInt));
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
			
			// BV ajout actualisation 
			// TODO mettre un taux d'actualisation different pour le public et le prive voire pptaire/locataire
			BigDecimal tauxInt = BigDecimal.ONE.add(CalibParameters.TAUX_ACTU_CALIB);
			BigDecimal inverse = BigDecimal.ONE.divide(tauxInt, MathContext.DECIMAL32);

			// BigDecimal coefactu = commonService.serieGeometrique(inverse, inverse, dataCalib.get(str).getDureeVie() - 1);
			
			BigDecimal coefactu = inverse.multiply(BigDecimal.ONE.subtract(inverse.pow(dataCalib.get(str).getDureeVie(), MathContext.DECIMAL32), 
					MathContext.DECIMAL32),MathContext.DECIMAL32)
			.divide(BigDecimal.ONE.subtract(inverse, MathContext.DECIMAL32),
			MathContext.DECIMAL32);
			
			BigDecimal coutVariable = dataCalib
					.get(str)
					.getCoutMoy()
					.divide(coefactu, MathContext.DECIMAL32)
					.add(dataCalib
							.get(str)
							.getChargeInit()
							.multiply(
									BigDecimal.ONE.subtract(dataCalib.get(str).getGainMoy(),
											MathContext.DECIMAL32), MathContext.DECIMAL32),
							MathContext.DECIMAL32);
			
			BigDecimal coutIntangible = coutGlobalGeste.subtract(coutVariable, MathContext.DECIMAL32);
			
			results.put(str,new CalibCoutGlobal(coutIntangible,coutVariable));
		}

		return results;

	}
	
	
//	public HashMap<String, CalibCoutGlobal> calibreCIBatidesag(HashMap<String, CalibCIBati> dataCalib, 
//			ParamCInt paramCInt, HashMap<String, ParamCalib> paramCalibMap,  
//			HashMap<String, List<Geste>>  bibliGesteBatiMap, HashMap<String, TauxInteret> tauxInteretMap) {
//		HashMap<String,CalibCoutGlobal> results = new HashMap<>();
//
//		for (String st : paramCalibMap.keySet()) {
//			List<Geste> listGest = bibliGesteBatiMap.get(st.substring(0,10));
//			// On commence par atribuer des PM aux gestes pour le segment de par st
//			
//			HashMap<String, BigDecimal> PMgeste = new HashMap<String, BigDecimal>();
//			
//			// PM ne rien faire
//			PMgeste.put(TypeRenovBati.ETAT_INIT.getLabel(), dataCalib.get(st.substring(0,2) + INIT_STATE).getPartMarche());
//			
//			// Pour les gestes sans couts renseignes, on ajoute leur PM au geste ne rien faire et 
//			// on met leur PM a zero.
//			// Pour les autres on met les PM du fichier de parametre cout_intangibles_init	    
//			
//			for(TypeRenovBati typeRenov : TypeRenovBati.values()){
//				
//				if(typeRenov.equals(TypeRenovBati.ETAT_INIT) == false){
//					
//					Geste gestetmp = listGest.stream()
//							.filter(x -> typeRenov.equals(x.getTypeRenovBati()) && 
//									x.getCoutGesteBati().equals(BigDecimal.ZERO) == false).findAny()
//			                .orElse(null);
//					
//					if(gestetmp == null){	
//						PMgeste.put(typeRenov.getLabel(), BigDecimal.ZERO);
//						PMgeste.put(TypeRenovBati.ETAT_INIT.getLabel(), 
//								PMgeste.get(TypeRenovBati.ETAT_INIT.getLabel())
//										.add(dataCalib.get(st.substring(0,2) + typeRenov).getPartMarche()));
//					} else {
//						PMgeste.put(typeRenov.getLabel(), dataCalib.get(st.substring(0,2) + typeRenov).getPartMarche());
//					}
//				}
//			}	
//		
//		// pour le calage, on utilise ne rien faire
//		HashMap<String, CalibCIRef> calibRef = new HashMap<String, CalibCIRef>();
//		// on calcule le cout global du geste ne rien faire 
//		calibRef.put(st, coutGlobalReferenceDesag(
//				dataCalib.get(st.substring(0,2) + INIT_STATE),
//				PMgeste.get(TypeRenovBati.ETAT_INIT.getLabel()),
//				paramCInt, paramCalibMap.get(st)));
//					
//				BigDecimal coutVariableRef = calibRef.get(st).getCoutGlobal().subtract(paramCInt.getCintRef(), MathContext.DECIMAL32);
//				BigDecimal coutIntangibleRef = paramCInt.getCintRef();
//				results.put(st+TypeRenovBati.ETAT_INIT.getLabel(), 
//						new CalibCoutGlobal(coutIntangibleRef,coutVariableRef));
//				
//		// ajouts des periodes de simulations : on met les memes couts intangibles que pour les batiments les plus recents
//				if(st.substring(8,10).equals("11") || st.substring(8,10).equals("12")){
//					if(results.get(st.substring(0,8)+"19"+ st.substring(10)+TypeRenovBati.ETAT_INIT.getLabel()) == null){
//					results.put(st.substring(0,8)+"19"+ st.substring(10)+TypeRenovBati.ETAT_INIT.getLabel(),new CalibCoutGlobal(coutIntangibleRef,coutVariableRef));
//					results.put(st.substring(0,8)+"20"+ st.substring(10)+TypeRenovBati.ETAT_INIT.getLabel(),new CalibCoutGlobal(coutIntangibleRef,coutVariableRef));
//					results.put(st.substring(0,8)+"21"+ st.substring(10)+TypeRenovBati.ETAT_INIT.getLabel(),new CalibCoutGlobal(coutIntangibleRef,coutVariableRef));
//					results.put(st.substring(0,8)+"22"+ st.substring(10)+TypeRenovBati.ETAT_INIT.getLabel(),new CalibCoutGlobal(coutIntangibleRef,coutVariableRef));
//					results.put(st.substring(0,8)+"23"+ st.substring(10)+TypeRenovBati.ETAT_INIT.getLabel(),new CalibCoutGlobal(coutIntangibleRef,coutVariableRef));
//					}
//					}
//				
//		// on parcourt la liste des autres gestes pour faire le calcul des CINT sur chaque geste 
//			
//			for (Geste geste : listGest) {
//				if (geste.getTypeRenovBati().equals(TypeRenovBati.ETAT_INIT) == false ) {	
//					
//				Double intermed = (PMgeste.get(geste.getTypeRenovBati().getLabel())
//						.divide(calibRef.get(st).getPartMarche2009(), MathContext.DECIMAL32)).doubleValue();
//				Double intermed2 = BigDecimal.ONE.divide(BigDecimal.valueOf(-paramCInt.getNu()), MathContext.DECIMAL32).doubleValue();
//				BigDecimal intermed3 = BigDecimal.valueOf(Math.pow(intermed, intermed2));
//				BigDecimal coutGlobalGeste = intermed3.multiply(calibRef.get(st).getCoutGlobal(), MathContext.DECIMAL32);
//				
//				// ajout actualisation 
//				BigDecimal tauxInt = BigDecimal.ONE.add(tauxInteretMap.get(st.substring(0,2) + st.substring(6,8) + "Proprietaire").getPBC().getTauxInteret());
//				BigDecimal inverse = BigDecimal.ONE.divide(tauxInt, MathContext.DECIMAL32);
//
//
//				BigDecimal coefactu = inverse.multiply(BigDecimal.ONE
//						.subtract(inverse.pow(dataCalib.get(st.substring(0,2) + geste.getTypeRenovBati()).getDureeVie(), MathContext.DECIMAL32), 
//						MathContext.DECIMAL32),MathContext.DECIMAL32)
//				.divide(BigDecimal.ONE.subtract(inverse, MathContext.DECIMAL32),
//				MathContext.DECIMAL32);
//
//				BigDecimal coutVariable = geste.getCoutGesteBati()
//						.divide(coefactu, MathContext.DECIMAL32)
//						.add((paramCalibMap.get(st).getChargesChauff()
//								.divide(paramCalibMap.get(st).getSurface(), MathContext.DECIMAL32))
//								.multiply(BigDecimal.ONE.subtract(geste.getGainEner(),MathContext.DECIMAL32), 
//										MathContext.DECIMAL32),MathContext.DECIMAL32);
//				
//				BigDecimal coutIntangible = coutGlobalGeste.subtract(coutVariable, MathContext.DECIMAL32);
//				
//				results.put(st+geste.getTypeRenovBati().getLabel(),new CalibCoutGlobal(coutIntangible,coutVariable));
//				
//				// ajouts des periodes de simulations : on met les memes couts intangibles que pour les batiments les plus recents
//				if(st.substring(8,10).equals("11") || st.substring(8,10).equals("12")){
//					if(results.get(st.substring(0,8)+"19"+ st.substring(10)+geste.getTypeRenovBati().getLabel()) == null){
//					results.put(st.substring(0,8)+"19"+ st.substring(10)+geste.getTypeRenovBati().getLabel(),new CalibCoutGlobal(coutIntangible,coutVariable));
//					results.put(st.substring(0,8)+"20"+ st.substring(10)+geste.getTypeRenovBati().getLabel(),new CalibCoutGlobal(coutIntangible,coutVariable));
//					results.put(st.substring(0,8)+"21"+ st.substring(10)+geste.getTypeRenovBati().getLabel(),new CalibCoutGlobal(coutIntangible,coutVariable));
//					results.put(st.substring(0,8)+"22"+ st.substring(10)+geste.getTypeRenovBati().getLabel(),new CalibCoutGlobal(coutIntangible,coutVariable));
//					results.put(st.substring(0,8)+"23"+ st.substring(10)+geste.getTypeRenovBati().getLabel(),new CalibCoutGlobal(coutIntangible,coutVariable));
//					}
//					}
//				}	
//				
//			}
//			
//		}	
//		return results;
//	}

	public HashMap<String, CalibCoutGlobal> calibreCIBatidesag(HashMap<String, CalibCIBati> dataCalib, 
			ParamCInt paramCInt, HashMap<String, ParamCalib> paramCalibMap,  
			HashMap<String, List<Geste>>  bibliGesteBatiMap, HashMap<String, TauxInteret> tauxInteretMap) {
		HashMap<String,CalibCoutGlobal> results = new HashMap<>();

		for (String st : paramCalibMap.keySet()) {
			List<Geste> listGest = bibliGesteBatiMap.get(st);
			// On commence par atribuer des PM aux gestes pour le segment de par st
			
			HashMap<String, BigDecimal> PMgeste = new HashMap<String, BigDecimal>();
			
			// PM ne rien faire
			PMgeste.put(TypeRenovBati.ETAT_INIT.getLabel(), dataCalib.get(st.substring(0,2) + INIT_STATE).getPartMarche());
			
			// Pour les gestes sans couts renseignes, on ajoute leur PM au geste ne rien faire et 
			// on met leur PM a zero.
			// Pour les autres on met les PM du fichier de parametre cout_intangibles_init	    
			
			for(TypeRenovBati typeRenov : TypeRenovBati.values()){
				
				if(typeRenov.equals(TypeRenovBati.ETAT_INIT) == false){
					
					Geste gestetmp = listGest.stream()
							.filter(x -> typeRenov.equals(x.getTypeRenovBati()) && 
									x.getCoutGesteBati().equals(BigDecimal.ZERO) == false).findAny()
			                .orElse(null);
					
					if(gestetmp == null){	
						PMgeste.put(typeRenov.getLabel(), BigDecimal.ZERO);
						PMgeste.put(TypeRenovBati.ETAT_INIT.getLabel(), 
								PMgeste.get(TypeRenovBati.ETAT_INIT.getLabel())
										.add(dataCalib.get(st.substring(0,2) + typeRenov).getPartMarche()));
					} else {
						PMgeste.put(typeRenov.getLabel(), dataCalib.get(st.substring(0,2) + typeRenov).getPartMarche());
					}
				}
			}	
		
		// pour le calage, on utilise ne rien faire
		HashMap<String, CalibCIRef> calibRef = new HashMap<String, CalibCIRef>();
		// on calcule le cout global du geste ne rien faire 
		calibRef.put(st, coutGlobalReferenceDesag(
				dataCalib.get(st.substring(0,2) + INIT_STATE),
				PMgeste.get(TypeRenovBati.ETAT_INIT.getLabel()),
				paramCInt, paramCalibMap.get(st)));
					
				BigDecimal coutVariableRef = calibRef.get(st).getCoutGlobal().subtract(paramCInt.getCintRef(), MathContext.DECIMAL32);
				BigDecimal coutIntangibleRef = paramCInt.getCintRef();
				results.put(st+TypeRenovBati.ETAT_INIT.getLabel(), 
						new CalibCoutGlobal(coutIntangibleRef,coutVariableRef));
				
		// ajouts des periodes de simulations : on met les memes couts intangibles que pour les batiments les plus recents
				if(st.substring(8,10).equals("11") || st.substring(8,10).equals("12")){
					if(results.get(st.substring(0,8)+"19"+TypeRenovBati.ETAT_INIT.getLabel()) == null){
					results.put(st.substring(0,8)+"19"+TypeRenovBati.ETAT_INIT.getLabel(),new CalibCoutGlobal(coutIntangibleRef,coutVariableRef));
					results.put(st.substring(0,8)+"20"+TypeRenovBati.ETAT_INIT.getLabel(),new CalibCoutGlobal(coutIntangibleRef,coutVariableRef));
					results.put(st.substring(0,8)+"21"+TypeRenovBati.ETAT_INIT.getLabel(),new CalibCoutGlobal(coutIntangibleRef,coutVariableRef));
					results.put(st.substring(0,8)+"22"+TypeRenovBati.ETAT_INIT.getLabel(),new CalibCoutGlobal(coutIntangibleRef,coutVariableRef));
					results.put(st.substring(0,8)+"23"+TypeRenovBati.ETAT_INIT.getLabel(),new CalibCoutGlobal(coutIntangibleRef,coutVariableRef));
					}
					}
				
		// on parcourt la liste des autres gestes pour faire le calcul des CINT sur chaque geste 
			
			for (Geste geste : listGest) {
				if (geste.getTypeRenovBati().equals(TypeRenovBati.ETAT_INIT) == false ) {	
					
				Double intermed = (PMgeste.get(geste.getTypeRenovBati().getLabel())
						.divide(calibRef.get(st).getPartMarche2009(), MathContext.DECIMAL32)).doubleValue();
				Double intermed2 = BigDecimal.ONE.divide(BigDecimal.valueOf(-paramCInt.getNu()), MathContext.DECIMAL32).doubleValue();
				BigDecimal intermed3 = BigDecimal.valueOf(Math.pow(intermed, intermed2));
				BigDecimal coutGlobalGeste = intermed3.multiply(calibRef.get(st).getCoutGlobal(), MathContext.DECIMAL32);
				
				// ajout actualisation 
				BigDecimal tauxInt = BigDecimal.ONE.add(tauxInteretMap.get(st.substring(0,2) + st.substring(6,8) + "Proprietaire").getPBC().getTauxInteret());
				BigDecimal inverse = BigDecimal.ONE.divide(tauxInt, MathContext.DECIMAL32);


				BigDecimal coefactu = inverse.multiply(BigDecimal.ONE
						.subtract(inverse.pow(dataCalib.get(st.substring(0,2) + geste.getTypeRenovBati()).getDureeVie(), MathContext.DECIMAL32), 
						MathContext.DECIMAL32),MathContext.DECIMAL32)
				.divide(BigDecimal.ONE.subtract(inverse, MathContext.DECIMAL32),
				MathContext.DECIMAL32);

				BigDecimal coutVariable = geste.getCoutGesteBati()
						.divide(coefactu, MathContext.DECIMAL32)
						.add((paramCalibMap.get(st).getChargesChauff()
								.divide(paramCalibMap.get(st).getSurface(), MathContext.DECIMAL32))
								.multiply(BigDecimal.ONE.subtract(geste.getGainEner(),MathContext.DECIMAL32), 
										MathContext.DECIMAL32),MathContext.DECIMAL32);
				
				BigDecimal coutIntangible = coutGlobalGeste.subtract(coutVariable, MathContext.DECIMAL32);
				
				results.put(st+geste.getTypeRenovBati().getLabel(),new CalibCoutGlobal(coutIntangible,coutVariable));
				
				// ajouts des periodes de simulations : on met les memes couts intangibles que pour les batiments les plus recents
				if(st.substring(8,10).equals("11") || st.substring(8,10).equals("12")){
					if(results.get(st.substring(0,8)+"19"+geste.getTypeRenovBati().getLabel()) == null){
					results.put(st.substring(0,8)+"19"+geste.getTypeRenovBati().getLabel(),new CalibCoutGlobal(coutIntangible,coutVariable));
					results.put(st.substring(0,8)+"20"+geste.getTypeRenovBati().getLabel(),new CalibCoutGlobal(coutIntangible,coutVariable));
					results.put(st.substring(0,8)+"21"+geste.getTypeRenovBati().getLabel(),new CalibCoutGlobal(coutIntangible,coutVariable));
					results.put(st.substring(0,8)+"22"+geste.getTypeRenovBati().getLabel(),new CalibCoutGlobal(coutIntangible,coutVariable));
					results.put(st.substring(0,8)+"23"+geste.getTypeRenovBati().getLabel(),new CalibCoutGlobal(coutIntangible,coutVariable));
					}
					}
				}	
				
			}
				
		}	
		
		// periode manquant TODO faire une map avec la derniere periode existante
		for (String st : paramCalibMap.keySet()) {
			List<Geste> listGest = bibliGesteBatiMap.get(st);
			for (Geste geste : listGest) {
				
			if(results.get(st.substring(0,8)+"19"+geste.getTypeRenovBati().getLabel()) == null){		
				results.put(st.substring(0,8)+"19"+geste.getTypeRenovBati().getLabel(),
						results.get(st.substring(0,10)+geste.getTypeRenovBati().getLabel()));
				results.put(st.substring(0,8)+"20"+geste.getTypeRenovBati().getLabel(),
						results.get(st.substring(0,10)+geste.getTypeRenovBati().getLabel()));
				results.put(st.substring(0,8)+"21"+geste.getTypeRenovBati().getLabel(),
						results.get(st.substring(0,10)+geste.getTypeRenovBati().getLabel()));
				results.put(st.substring(0,8)+"22"+geste.getTypeRenovBati().getLabel(),
						results.get(st.substring(0,10)+geste.getTypeRenovBati().getLabel()));
				results.put(st.substring(0,8)+"23"+geste.getTypeRenovBati().getLabel(),
						results.get(st.substring(0,10)+geste.getTypeRenovBati().getLabel()));
			}
			}
		}
		
		return results;
	}
	protected CalibCIRef coutGlobalReference(CalibCIBati calibCIBati, ParamCInt paramCInt) {
		CalibCIRef reference = new CalibCIRef();
		// calcul du cout global pour le geste Rien faire
		// les couts intangibles sont nuls
		// la duree de vie est de 1
		// TODO verifier le calcul du cout global ne rien faire dans excel
		
		BigDecimal coutGlobal = calibCIBati.getChargeInit().add(paramCInt.getCintRef());
		reference.setCoutGlobal(coutGlobal);
		
		BigDecimal partRef = calibCIBati.getPartMarche();
		reference.setPartMarche2009(partRef);

		return reference;
	}
    

	
	protected CalibCIRef coutGlobalReferenceDesag(CalibCIBati calibCIBati, BigDecimal pmNRF, ParamCInt paramCInt, 
			ParamCalib paramCalib) {
		CalibCIRef reference = new CalibCIRef();
		// calcul du cout global pour le geste Rien faire
		// les couts intangibles sont nuls
		// la duree de vie est de 1
		// TODO verifier le calcul du cout global ne rien faire dans excel
		//paramCalib.getConsoChauff().divide(paramCalib.getSurface(),MathContext.DECIMAL32);
		//paramCalib.getBesoinChauff().divide(paramCalib.getSurface(),MathContext.DECIMAL32);
	
		BigDecimal coutGlobal = paramCalib.getChargesChauff()
				.divide(paramCalib.getSurface(),MathContext.DECIMAL32).add(paramCInt.getCintRef());
		reference.setCoutGlobal(coutGlobal);
		reference.setPartMarche2009( pmNRF);

		return reference;
	}
	
	
	// methode de calcul des CI --> cette methode renvoie une hashmap de CI
	@Override
	public HashMap<String, CalibCoutGlobal> calibreCI(HashMap<String, CalibCI> dataCalib, 
			ParamCInt paramCint, HashMap<String, Maintenance> maintenanceMap) {
		
		HashMap<String,CalibCoutGlobal> results = new HashMap<String,CalibCoutGlobal>();

		// pour le calage : Chaudiere gaz
		HashMap<String, CalibCIRef> calibRef = new HashMap<String, CalibCIRef>();

		for (String st : dataCalib.keySet()) {

			// traitement specifique pour la branche transport
			if (dataCalib.get(st).getBranche().equals(Branche.TRANSPORT.getCode())
					&& dataCalib.get(st).getEnergies().equals(Energies.GAZ.getCode())) {
				calibRef.put(generateKey(dataCalib.get(st)), refCopy(dataCalib.get(st), paramCint.getCintRef(), maintenanceMap));

			} else {
				if ((dataCalib.get(st).getSysteme().equals(SysChaud.CHAUDIERE_GAZ.getCode()))
						&& (dataCalib.get(st).getEnergies().equals(Energies.GAZ.getCode()) && (!dataCalib.get(st)
								.getPerformant()))) {
					calibRef.put(generateKey(dataCalib.get(st)), refCopy(dataCalib.get(st), paramCint.getCintRef(), maintenanceMap));
				}
			}
		}

		for (String str : dataCalib.keySet()) {
			// if (!(dataCalib.get(str).getPerformant())) {
			if (paramCint.getNu() == 0) { // dans ce cas les PM sont les memes pour tous et
							// les CI ne peuvent pas etre calcule
				results.put(str,new CalibCoutGlobal(paramCint.getCintRef(),BigDecimal.ZERO));

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
				
				BigDecimal tauxInt = BigDecimal.ONE.add(CalibParameters.TAUX_ACTU_CALIB);
				BigDecimal inverse = BigDecimal.ONE.divide(tauxInt, MathContext.DECIMAL32);
				// BigDecimal coefactu = commonService.serieGeometrique(inverse, inverse, dataCalib.get(str).getDureeVie() - 1);
				
				BigDecimal coefactu = inverse.multiply(BigDecimal.ONE.subtract(inverse.pow(dataCalib.get(str).getDureeVie(), MathContext.DECIMAL32), 
						MathContext.DECIMAL32),MathContext.DECIMAL32)
				.divide(BigDecimal.ONE.subtract(inverse, MathContext.DECIMAL32),
				MathContext.DECIMAL32);
				
				BigDecimal besoinUnitaire =  dataCalib.get(str).getBesoinUnitaire();	
				
				// 	calcul charges energetiques annuelles, agent myope
				BigDecimal chargesEnerinter = dataCalib.get(str).getCoutEner()
				.multiply(besoinUnitaire
				.divide(dataCalib.get(str).getRdt(), MathContext.DECIMAL32), 
				MathContext.DECIMAL32);
				
				// ajout des couts de maintenance dans la calibration 
				BigDecimal coutMaintenance = dataCalib.get(str).getCoutM2().multiply(maintenanceMap.get(dataCalib.get(str).getSysteme())
						.getPart(), MathContext.DECIMAL32);
				
				// Calcul du cout variable annualise en divisant l'investissement par le coef d'actualisation
				
				BigDecimal coutVariable = (dataCalib.get(str).getCoutM2().divide(
						coefactu, MathContext.DECIMAL32)).add(chargesEnerinter,
						MathContext.DECIMAL32).add(coutMaintenance, MathContext.DECIMAL32 );
				
				
				BigDecimal interFinal = (intermediaire.multiply(calibRef.get(generateKey(dataCalib.get(str)))
				.getCoutGlobal(), MathContext.DECIMAL32)).subtract(coutVariable, MathContext.DECIMAL32);

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
					results.put(str,new CalibCoutGlobal(interFinal,coutVariable));
				} else {
					results.put(modif(str),new CalibCoutGlobal(interFinal,coutVariable));
				}

			}
		}

		return results;

	}
	// Ajout des besoins et des prix de l'energie en 2009 pour la calibration des PM dans le neuf
    @Override
    public void addingRowsInHashMap(HashMap<String, CalibCI> cintMapNeuf, HashMap<Integer,
			CoutEnergie> coutEnergieMap, HashMap<String, ParamBesoinsNeufs> bNeufsMap) {
        for (String calibKey : cintMapNeuf.keySet()){
        	CalibCI calib = cintMapNeuf.get(calibKey);
    		calib.setCoutEner(coutEnergieMap.get(CalibParameters.YEAR_CALIB).getEnergie(Energies.getEnumName(calib.getEnergies())));
    		calib.setBesoinUnitaire(bNeufsMap.get(calib.getBranche()+calib.getBatType()+ Usage.CHAUFFAGE.getLabel()).getPeriode(PERIOD_CALIB));
			cintMapNeuf.put(calibKey,calib);
		}

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

	protected CalibCIRef refCopy(CalibCI calibCI, BigDecimal cintRef, 
			HashMap<String, Maintenance> maintenanceMap) {
		CalibCIRef result = new CalibCIRef();
		
		BigDecimal chargesEner = calibCI.getBesoinUnitaire().multiply(calibCI.getCoutEner(), MathContext.DECIMAL32)
				.divide(calibCI.getRdt(), MathContext.DECIMAL32);
		
		// ajout des couts de maintenance dans la calibration 
		BigDecimal coutMaintenance = calibCI.getCoutM2().multiply(maintenanceMap.get(calibCI.getSysteme())
				.getPart(), MathContext.DECIMAL32);
		
		// BV ajout actualisation de l'investissement (4%)
		// TODO mettre un taux d'actualisation different pour le public et le prive voire pptaire/locataire
		
		BigDecimal tauxInt = BigDecimal.ONE.add(CalibParameters.TAUX_ACTU_CALIB);
		BigDecimal inverse = BigDecimal.ONE.divide(tauxInt, MathContext.DECIMAL32);
		
		// BigDecimal coefactu = commonService.serieGeometrique(inverse, inverse, calibCI.getDureeVie() - 1);
		
		BigDecimal coefactu = inverse.multiply(BigDecimal.ONE.subtract(inverse.pow(calibCI.getDureeVie(), MathContext.DECIMAL32), 
				MathContext.DECIMAL32),MathContext.DECIMAL32)
		.divide(BigDecimal.ONE.subtract(inverse, MathContext.DECIMAL32),
		MathContext.DECIMAL32);
		
		BigDecimal calcCG = calibCI.getCoutM2()
				.divide(coefactu, MathContext.DECIMAL32)
				.add(chargesEner, MathContext.DECIMAL32).add(coutMaintenance);
		result.setPartMarche2009(new BigDecimal(calibCI.getPartMarche2009().toString()));

		// Ajoute le min entre le cout intangible de reference et 50 % du cout global de reference 
		
		// Ajoute le cint de l'option de reference
		//result.setCoutGlobal(calcCG.add(cintRef.min(calcCG.multiply(new BigDecimal("0.5"))), MathContext.DECIMAL32));
		result.setCoutGlobal(calcCG.add(cintRef, MathContext.DECIMAL32));
		
		return result;
	}

	protected String generateKey(CalibCI calibCI) {
		return calibCI.getBranche().concat(calibCI.getBatType());
	}

	
}
