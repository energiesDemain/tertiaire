package com.ed.cgdd.derby.finance.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ed.cgdd.derby.finance.TypeFinanceService;
import com.ed.cgdd.derby.model.calcconso.Conso;
import com.ed.cgdd.derby.model.financeObjects.*;
import com.ed.cgdd.derby.model.parc.Parc;
import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.model.parc.TypeRenovSysteme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class TypeFinanceServiceImpl implements TypeFinanceService {
	private final static Logger LOG = LogManager.getLogger(FinanceServiceImpl.class);

	// TODO revoir recupParamSegment
	@Override
	public CoutRenovation recupParamSegment(Parc parcIni, Conso consoEner, Geste geste, int anneeNtab, int annee,
			BigDecimal surface, HashMap<String,CalibCoutGlobal> coutIntangible,HashMap<String,CalibCoutGlobal> coutIntangibleBati, BigDecimal coutEnergie,
			HashMap<String, BigDecimal> evolCoutBati, HashMap<String, BigDecimal> evolCoutTechno, HashMap<String, BigDecimal> evolCoutIntTechno) {
		// Cette methode recupere toutes les infos du segment pour creer le
		// CoutRenovation

		CoutRenovation resultat = new CoutRenovation();
//		long startCoutRen = System.currentTimeMillis();
		// dans conso : les besoins par m2
		// dans surface : la surface moyenne des batiments
		BigDecimal consoN = BigDecimal.ZERO;
		if (consoEner != null) {
			consoN = consoEner.getAnnee(anneeNtab - 1).divide(parcIni.getAnnee(anneeNtab - 1), MathContext.DECIMAL32);
		}
		BigDecimal coutRen = geste.getCoutGesteBati();
		if (geste.getTypeRenovSys() != TypeRenovSysteme.ETAT_INIT) {
			coutRen = coutRen.add(geste.getCoutGesteSys(), MathContext.DECIMAL32);
		}
//		long endCoutRen = System.currentTimeMillis();
//		if(endCoutRen - startCoutRen >1){
//			LOG.info("Couts ren : {}ms", endCoutRen - startCoutRen);}


//		long startTravauxAd = System.currentTimeMillis();
		// ajout des couts de travaux additionnels
		if (geste.getCoutTravauxAddGeste() != null && geste.getCoutTravauxAddGeste().compareTo(new BigDecimal("0.0001")) == 1 ) {
			// BV on ajoute zero dans cette version!
			// resultat.setCTA(surface.multiply(coutAdd));
			resultat.setCTA(surface.multiply(geste.getCoutTravauxAddGeste()));
			// resultat.getCTA();
		} else {
			// Si il n'y a pas de couts additionnels
			resultat.setCTA(BigDecimal.ZERO);
		}
//		long endTravauxAd = System.currentTimeMillis();
//		if(endTravauxAd - startTravauxAd >1){
//			LOG.info("Travaux add : {}ms", endTravauxAd - startTravauxAd);}

		resultat.setCT(surface.multiply(coutRen, MathContext.DECIMAL32));

		// ajout des couts de maintenance annuels 
//		long startMaintenance = System.currentTimeMillis();

		if (geste.getCoutMaintenance() != null && geste.getCoutMaintenance().compareTo(new BigDecimal("0.0001")) == 1 ) {
			resultat.setMaintenance(surface.multiply(geste.getCoutMaintenance()));
		} else {
			// Si il n'y a pas de couts additionnels
			resultat.setMaintenance(BigDecimal.ZERO);
		}
//		long endMaintenance = System.currentTimeMillis();
//		if(endMaintenance - startMaintenance>1){
//			LOG.info("Maintenance : {}ms", endMaintenance - startMaintenance);}

//		long startSetter = System.currentTimeMillis();

		resultat.setDuree(Math.max(geste.getDureeSys(), geste.getDureeBati()));

		// calcul des CEini
		resultat.setCEini(surface.multiply(consoN).multiply(coutEnergie));
//		long endSetter = System.currentTimeMillis();
//		if(endSetter - startSetter >1){
//			LOG.info("Setter : {}ms", endSetter - startSetter);}

		// Recuperation du cout intangible du systeme
//		long startRecupCINT = System.currentTimeMillis();
		BigDecimal coutIntangibleSys = coutIntangible.get(generateIDCoutInt(parcIni, geste)).getCInt();
		// Recuperation du cout intangible bati
		BigDecimal coutIntangibleBat = coutIntangibleBati.get(generateIDCoutIntBati(parcIni, geste)).getCInt();
//		long endRecupCINT = System.currentTimeMillis();
//		if(endRecupCINT - startRecupCINT>1){
//			LOG.info("Recup CINT : {}ms", endRecupCINT - startRecupCINT);}

//		long startCINT = System.currentTimeMillis();

		// XXX integration CINT
		BigDecimal coutIntSys = coutIntangibleSys.multiply(
				getVariation(geste.getSysChaud(), annee, evolCoutIntTechno), MathContext.DECIMAL32);
		
		BigDecimal multi = BigDecimal.ONE;
		if (geste.getTypeRenovBati() != TypeRenovBati.ETAT_INIT) {
			multi = getVariation(geste.getTypeRenovBati().getLabel(), annee, evolCoutBati);
		}
		
		BigDecimal coutIntBati = coutIntangibleBat.multiply(multi,
				MathContext.DECIMAL32);

		BigDecimal coutInt = (coutIntSys.add(coutIntBati, MathContext.DECIMAL32)).multiply(surface,
				MathContext.DECIMAL32);

		resultat.setCINT(coutInt);
		// resultat.setCINT(new BigDecimal("100").multiply(surface,
		// MathContext.DECIMAL32));
		// TODO Integration cout maintenance
//		long endCINT = System.currentTimeMillis();
//		if(endCINT - startCINT>1){
//			LOG.info("CINT : {}ms", endCINT - startCINT);}

		return resultat;

	}

	protected BigDecimal getVariation(String type, int annee, HashMap<String, BigDecimal> evolCout) {
		String cle = String.valueOf(annee) + "_" + type;
		return evolCout.get(cle);
	}

	protected String generateIDCoutIntBati(Parc parcIni, Geste geste) {
		String key = parcIni.getIdbranche() + geste.getTypeRenovBati().getLabel();
		return key;
	}

	// version sans cout intangible
	@Override
	public CoutRenovation recupParamSegment(Parc parcIni, Conso consoEner, Geste geste, int anneeNtab, int annee,
			BigDecimal surface, BigDecimal coutEnergie) {
		// Cette methode recupere toutes les infos du segment pour creer le
		// CoutRenovation

		CoutRenovation resultat = new CoutRenovation();

		// dans conso : les besoins par m2
		// dans surface : la surface moyenne des batiments
		BigDecimal consoN = BigDecimal.ZERO;
		if (consoEner != null) {
			consoN = consoEner.getAnnee(anneeNtab - 1).divide(parcIni.getAnnee(anneeNtab - 1), MathContext.DECIMAL32);
		}

		BigDecimal coutRen = geste.getCoutGesteBati();
		if (geste.getTypeRenovSys() != TypeRenovSysteme.ETAT_INIT) {
			coutRen = coutRen.add(geste.getCoutGesteSys(), MathContext.DECIMAL32);
		}
		
		// ajout des couts de travaux additionnels
		if (geste.getCoutTravauxAddGeste() != null && geste.getCoutTravauxAddGeste().compareTo(new BigDecimal("0.0001")) == 1 ) {
		// BV on ajoute zero dans cette version!
		// resultat.setCTA(surface.multiply(coutAdd));
		resultat.setCTA(surface.multiply(geste.getCoutTravauxAddGeste()));
		// resultat.getCTA();
		} else {
		// Si il n'y a pas de couts additionnels
		resultat.setCTA(BigDecimal.ZERO);
		}

		resultat.setCT(surface.multiply(coutRen, MathContext.DECIMAL32));

		// ajout des couts de maintenance annuels 
		if (geste.getCoutMaintenance() != null && geste.getCoutMaintenance().compareTo(new BigDecimal("0.0001")) == 1 ) {
			resultat.setMaintenance(surface.multiply(geste.getCoutMaintenance()));
		} else {
		// Si il n'y a pas de couts additionnels
		resultat.setMaintenance(BigDecimal.ZERO);
		}
				

		resultat.setCT(surface.multiply(coutRen, MathContext.DECIMAL32));

		resultat.setDuree(Math.max(geste.getDureeSys(), geste.getDureeBati()));
		// on ajoute des CINT nuls
		resultat.setCINT(BigDecimal.ZERO);

		// calcul des CEini
		resultat.setCEini(surface.multiply(consoN).multiply(coutEnergie));

		return resultat;

	}

	protected String generateIDCoutInt(Parc parcIni, Geste geste) {
		String perfor;
		BigDecimal sysChaudTemp = new BigDecimal(geste.getSysChaud());

		if (sysChaudTemp.compareTo(new BigDecimal("20")) > 0) {
			perfor = "1";

		} else {
			perfor = "0";
		}

		return parcIni.getIdbranche().concat(parcIni.getIdbattype()).concat(geste.getSysChaud())
				.concat(geste.getEnergie()).concat(perfor);
	}

	// methode pour creer un gesteFinancement ne rien faire (pas de renovation)
	@Override
	public GesteFinancement createRienFaire(Parc parcIni, Conso consoEner, Geste courant, int anneeNtab,
			PBC pretDeBase, BigDecimal surface, BigDecimal coutEnergie) {
		GesteFinancement gesteFinance = new GesteFinancement();
		CoutRenovation coutRenov = new CoutRenovation();

		List<ListeFinanceValeur> listeFinancement = new ArrayList<>();

		listeFinancement.add(new ListeFinanceValeur(pretDeBase, BigDecimal.ZERO));
		// pour avoir l'information sur les aides
		listeFinancement.add(new ListeFinanceValeur(new CEE(), BigDecimal.ZERO));

		coutRenov = recupParamSegment(parcIni, consoEner, courant, anneeNtab, 0, surface, coutEnergie);

		gesteFinance.setCoutRenov(coutRenov);
		gesteFinance.setGeste(courant);
		gesteFinance.setListeFinancement(listeFinancement);
		gesteFinance.setNomGesteFinance(getName(courant, pretDeBase));

		return gesteFinance;
	}

	// TODO methode generique pour renvoyer le nom
	protected String getName(Geste geste, Financement financement) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(geste.getTypeRenovBati());
		buffer.append(geste.getTypeRenovSys());
		buffer.append("|");
		buffer.append(financement.getType());
		return buffer.toString();
	}

}
