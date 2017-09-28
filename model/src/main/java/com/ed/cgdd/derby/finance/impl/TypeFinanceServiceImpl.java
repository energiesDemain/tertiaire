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

public abstract class TypeFinanceServiceImpl implements TypeFinanceService {

	// TODO revoir recupParamSegment
	@Override
	public CoutRenovation recupParamSegment(Parc parcIni, Conso consoEner, Geste geste, int anneeNtab, int annee,
			BigDecimal surface, List<CalibCoutGlobal> coutIntangible,List<CalibCoutGlobal> coutIntangibleBati, BigDecimal coutEnergie,
			HashMap<String, BigDecimal> evolCoutBati, HashMap<String, BigDecimal> evolCoutTechno) {
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

		BigDecimal coutAdd = BigDecimal.ZERO;
		if (geste.getCoutTravauxAddGeste() != null) {
			resultat.setCTA(surface.multiply(coutAdd));
		} else {
			resultat.setCTA(coutAdd);
		}

		resultat.setCT(surface.multiply(coutRen, MathContext.DECIMAL32));

		resultat.setDuree(Math.max(geste.getDureeSys(), geste.getDureeBati()));

		// calcul des CEini
		resultat.setCEini(surface.multiply(consoN).multiply(coutEnergie));

		// Recuperation du cout intangible du systeme
		BigDecimal coutIntangibleSys = coutIntangible.stream().filter(p->p.getCalKey().equals(generateIDCoutInt(parcIni, geste))).findFirst().get().getCInt();
		// Recuperation du cout intangible bati
		BigDecimal coutIntangibleBat = coutIntangibleBati.stream().filter(p->p.getCalKey().equals(generateIDCoutIntBati(parcIni, geste))).findFirst().get().getCInt();

		// XXX integration CINT
		BigDecimal coutIntSys = coutIntangibleSys.multiply(
				getVariation(geste.getSysChaud(), annee, evolCoutTechno), MathContext.DECIMAL32);
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
		BigDecimal coutAdd = BigDecimal.ZERO;
		if (geste.getCoutTravauxAddGeste() != null) {
			resultat.setCTA(surface.multiply(coutAdd));
		} else {
			resultat.setCTA(coutAdd);
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
