package com.ed.cgdd.derby.finance;

import java.util.List;
import java.util.Map;

import com.ed.cgdd.derby.model.financeObjects.Geste;

public interface GesteDAS {

	List<Geste> getGesteBatiData(String idGesteBati);

	Map<String, List<String>> getPeriodMap();
}
