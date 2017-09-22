package com.ed.cgdd.derby.parc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import com.ed.cgdd.derby.model.calcconso.ParamTxClimExistant;
import com.ed.cgdd.derby.model.calcconso.ParamTxClimNeuf;
import com.ed.cgdd.derby.model.calcconso.ResultConsoUClim;
import com.ed.cgdd.derby.model.calcconso.ResultConsoURt;
import com.ed.cgdd.derby.model.parc.ParamParcArray;
import com.ed.cgdd.derby.model.parc.Parc;
import com.ed.cgdd.derby.model.parc.PmNeuf;
import com.ed.cgdd.derby.model.parc.ResultParc;

public interface ParcService {

	HashMap<String, ParamParcArray> sortData(List<ParamParcArray> liste);

	HashMap<String, PmNeuf> sortDataPm(List<PmNeuf> pm);

	HashMap<String, Parc> sortDataParc(List<Parc> parc, HashMap<String, ResultConsoURt> resultConsoURtMap,
			int pasdeTemps, HashMap<String, ResultConsoUClim> resultConsoUClimMap);

	ResultParc parc(HashMap<String, ParamTxClimExistant> txClimExistantMap,
			HashMap<String, BigDecimal> partsMarchesNeuf, HashMap<String, Parc> parcTotMap,
			HashMap<String, ParamParcArray> entreesMap, HashMap<String, ParamParcArray> sortiesMap,
			HashMap<String, ParamTxClimNeuf> txClimNeufMap, int pasdeTemps, int anneeNTab, int annee);

}
