package com.ed.cgdd.derby.model.financeObjects;

import com.ed.cgdd.derby.model.parc.TypeRenovBati;
import com.ed.cgdd.derby.model.parc.TypeRenovSysteme;

public class ResFin {
	private String idAgregParc;
	private String branche;
	private TypeRenovBati typeRenovBati;
	private TypeRenovSysteme typeRenovSysteme;
	private String anneeRenovBati;
	private String anneeRenovSysteme;
	private String sysChaud;
	private int hashCode = 0;
	private String reglementation;

	public ResFin(ResultatsFinancements result, String idAgregParc) {
		super();
		this.branche = result.getBranche();
		this.typeRenovBati = result.getTypeRenovBati();
		this.typeRenovSysteme = result.getTypeRenovSys();
		this.anneeRenovBati = result.getAnneeRenovBat();
		this.anneeRenovSysteme = result.getAnneeRenovSys();
		this.sysChaud = result.getSysChaud();
		this.idAgregParc = idAgregParc;
		this.reglementation = result.getReglementation();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if (hashCode == 0) {
			result = prime * result + ((anneeRenovBati == null) ? 0 : anneeRenovBati.hashCode());
			result = prime * result + ((anneeRenovSysteme == null) ? 0 : anneeRenovSysteme.hashCode());
			result = prime * result + ((branche == null) ? 0 : branche.hashCode());
			result = prime * result + ((idAgregParc == null) ? 0 : idAgregParc.hashCode());
			result = prime * result + ((reglementation == null) ? 0 : reglementation.hashCode());
			result = prime * result + ((sysChaud == null) ? 0 : sysChaud.hashCode());
			result = prime * result + ((typeRenovBati == null) ? 0 : typeRenovBati.hashCode());
			result = prime * result + ((typeRenovSysteme == null) ? 0 : typeRenovSysteme.hashCode());
			return result;
		} else {
			return hashCode;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResFin other = (ResFin) obj;
		if (anneeRenovBati == null) {
			if (other.anneeRenovBati != null)
				return false;
		} else if (!anneeRenovBati.equals(other.anneeRenovBati))
			return false;
		if (anneeRenovSysteme == null) {
			if (other.anneeRenovSysteme != null)
				return false;
		} else if (!anneeRenovSysteme.equals(other.anneeRenovSysteme))
			return false;
		if (branche == null) {
			if (other.branche != null)
				return false;
		} else if (!branche.equals(other.branche))
			return false;
		if (idAgregParc == null) {
			if (other.idAgregParc != null)
				return false;
		} else if (!idAgregParc.equals(other.idAgregParc))
			return false;
		if (reglementation == null) {
			if (other.reglementation != null)
				return false;
		} else if (!reglementation.equals(other.reglementation))
			return false;
		if (sysChaud == null) {
			if (other.sysChaud != null)
				return false;
		} else if (!sysChaud.equals(other.sysChaud))
			return false;
		if (typeRenovBati != other.typeRenovBati)
			return false;
		if (typeRenovSysteme != other.typeRenovSysteme)
			return false;
		return true;
	}

	public String getIdAgregParc() {
		return idAgregParc;
	}

	public void setIdAgregParc(String idAgregParc) {
		this.idAgregParc = idAgregParc;
	}

	public String getBranche() {
		return branche;
	}

	public void setBranche(String branche) {
		this.branche = branche;
		this.hashCode = 0;
	}

	public TypeRenovBati getTypeRenovBati() {
		return typeRenovBati;
	}

	public void setTypeRenovBati(TypeRenovBati typeRenovBati) {
		this.typeRenovBati = typeRenovBati;
		this.hashCode = 0;
	}

	public TypeRenovSysteme getTypeRenovSysteme() {
		return typeRenovSysteme;
	}

	public void setTypeRenovSysteme(TypeRenovSysteme typeRenovSysteme) {
		this.typeRenovSysteme = typeRenovSysteme;
		this.hashCode = 0;
	}

	public String getAnneeRenovBati() {
		return anneeRenovBati;
	}

	public void setAnneeRenovBati(String anneeRenovBati) {
		this.anneeRenovBati = anneeRenovBati;
		this.hashCode = 0;
	}

	public String getAnneeRenovSysteme() {
		return anneeRenovSysteme;
	}

	public void setAnneeRenovSysteme(String anneeRenovSysteme) {
		this.anneeRenovSysteme = anneeRenovSysteme;
		this.hashCode = 0;
	}

	public String getSysChaud() {
		return sysChaud;
	}

	public void setSysChaud(String sysChaud) {
		this.sysChaud = sysChaud;
		this.hashCode = 0;
	}

	public String getReglementation() {
		return reglementation;
	}

	public void setReglementation(String reglementation) {
		this.reglementation = reglementation;
	}

}
