package com.ed.cgdd.derby.model.parc;

public abstract class Segment {
	protected String id;

	private static final int START_AGREG = 0;
	private static final int START_BRANCHE = 0;
	private static final int START_SS_BRANCHE = 2;
	private static final int START_BAT_TYPE = 4;
	private static final int START_OCCUPANT = 6;
	private static final int START_PERIODE_DETAIL = 8;
	private static final int START_PERIODE_SIMPLE = 10;
	private static final int START_SYS_CHAUD = 12;
	private static final int START_SYS_FROID = 14;
	private static final int START_ENERG = 16;

	private static final int LENGTH = 2;
	private static final int LENGTH_AGREG = 12;

	// TODO remplacer les getID.. et les substr et les setID.. par les segments
	// suivants.
	public String getIdagreg() {
		return this.id.substring(START_AGREG, LENGTH_AGREG + START_AGREG);
	}

	public void setIdagreg(String idagreg) {
		this.id = newIdMethode(idagreg, START_AGREG, id);
	}

	public String getIdbranche() {
		return this.id.substring(START_BRANCHE, LENGTH + START_BRANCHE);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setIdbranche(String idbranche) {
		this.id = newIdMethode(idbranche, START_BRANCHE, id);
	}

	public void setIdssbranche(String idssbranche) {
		this.id = newIdMethode(idssbranche, START_SS_BRANCHE, id);
	}

	public void setIdbattype(String idbattype) {
		this.id = newIdMethode(idbattype, START_BAT_TYPE, id);
	}

	public void setIdoccupant(String idoccupant) {
		this.id = newIdMethode(idoccupant, START_OCCUPANT, id);
	}

	public void setIdperiodedetail(String idperiodedetail) {
		this.id = newIdMethode(idperiodedetail, START_PERIODE_DETAIL, id);
	}

	public void setIdperiodesimple(String idperiodesimple) {
		this.id = newIdMethode(idperiodesimple, START_PERIODE_SIMPLE, id);
	}

	public void setIdsyschaud(String idsyschaud) {
		this.id = newIdMethode(idsyschaud, START_SYS_CHAUD, id);
	}

	public void setIdsysfroid(String idsysfroid) {
		this.id = newIdMethode(idsysfroid, START_SYS_FROID, id);
	}

	public void setIdenergchauff(String idenergchauff) {
		this.id = newIdMethode(idenergchauff, START_ENERG, id);
	}

	public String getIdssbranche() {
		return this.id.substring(START_SS_BRANCHE, LENGTH + START_SS_BRANCHE);
	}

	public String getIdbattype() {
		return this.id.substring(START_BAT_TYPE, LENGTH + START_BAT_TYPE);
	}

	public String getIdoccupant() {
		return this.id.substring(START_OCCUPANT, LENGTH + START_OCCUPANT);
	}

	public String getIdperiodedetail() {
		return this.id.substring(START_PERIODE_DETAIL, LENGTH + START_PERIODE_DETAIL);
	}

	public String getIdperiodesimple() {
		return this.id.substring(START_PERIODE_SIMPLE, LENGTH + START_PERIODE_SIMPLE);
	}

	public String getIdsyschaud() {
		if (id.length() < START_SYS_CHAUD) {
			throw new RuntimeException("Impossible de renvoyer le systeme chaud avec un ID agrégé");
		} else {
			return this.id.substring(START_SYS_CHAUD, LENGTH + START_SYS_CHAUD);
		}
	}

	public String getIdsysfroid() {
		if (id.length() < START_SYS_FROID) {
			throw new RuntimeException("Impossible de renvoyer le systeme froid avec un ID agrégé");
		} else {
			return this.id.substring(START_SYS_FROID, LENGTH + START_SYS_FROID);
		}
	}

	public String getIdenergchauff() {
		if (id.length() < START_ENERG) {
			throw new RuntimeException("Impossible de renvoyer l'énergie de chauffage avec un ID agrégé");
		} else {
			return this.id.substring(START_ENERG, LENGTH + START_ENERG);
		}
	}

	protected String newIdMethode(String newId, int begin, String idInit) {

		StringBuffer bufferId = new StringBuffer();
		String tranche1 = idInit.substring(0, begin - 0);
		String tranche2 = idInit.substring(begin + 2, idInit.length() - begin + 2);

		bufferId.append(tranche1);
		bufferId.append(newId);
		bufferId.append(tranche2);

		return bufferId.toString();
	}
}