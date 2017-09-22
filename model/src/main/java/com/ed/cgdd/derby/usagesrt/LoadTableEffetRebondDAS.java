package com.ed.cgdd.derby.usagesrt;

import java.util.HashMap;

import com.ed.cgdd.derby.model.calcconso.EffetRebond;

public interface LoadTableEffetRebondDAS {
	public HashMap<String, EffetRebond> recupEffetRebond(String tableName);
}
