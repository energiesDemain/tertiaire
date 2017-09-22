package com.ed.cgdd.derby.parc;

import java.util.List;

import com.ed.cgdd.derby.model.parc.ParamParcArray;
import com.ed.cgdd.derby.model.parc.Parc;

public interface LoadParcDataDAS {

	/**
	 * loadData
	 */

	List<ParamParcArray> getParamEntreesMapper();

	List<ParamParcArray> getParamSortiesMapper();

	List<Parc> getParamParcMapper(final String idAgregParc, final int pasdeTemps);

	List<String> getParamParcListeMapper();

}
