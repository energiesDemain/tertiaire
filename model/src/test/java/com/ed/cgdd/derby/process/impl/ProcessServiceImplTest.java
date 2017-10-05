package com.ed.cgdd.derby.process.impl;

import com.ed.cgdd.derby.model.calcconso.ParamBesoinsNeufs;
import com.ed.cgdd.derby.model.parc.Usage;
import com.ed.cgdd.derby.model.parc.UsageRT;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 * Created by ed on 05/10/2017.
 */
public class ProcessServiceImplTest {
    ProcessServiceImpl impl = new ProcessServiceImpl();

    @Test
    public void modifBesoinUBatExTest(){
        HashMap<String,ParamBesoinsNeufs> mapInit = new HashMap<String, ParamBesoinsNeufs>();
        ParamBesoinsNeufs paramBesoinsNeufs = new ParamBesoinsNeufs();
        paramBesoinsNeufs.setIDBranche("Bureaux");
        paramBesoinsNeufs.setUsage(UsageRT.CHAUFFAGE.getLabel());
        BigDecimal[] periode = new BigDecimal[6];
        periode[0] = BigDecimal.ONE;
        periode[1] = BigDecimal.TEN;
        periode[2] = BigDecimal.ZERO;
        periode[3] = BigDecimal.ZERO;
        periode[4] = BigDecimal.ZERO;
        periode[5] = BigDecimal.ZERO;
        paramBesoinsNeufs.setPeriode(periode);
        mapInit.put("test1",paramBesoinsNeufs);

        paramBesoinsNeufs = new ParamBesoinsNeufs();
        paramBesoinsNeufs.setIDBranche("Enseignement");
        paramBesoinsNeufs.setUsage(Usage.AUTRES.getLabel());
        periode = new BigDecimal[6];
        periode[0] = BigDecimal.TEN;
        periode[1] = BigDecimal.ONE;
        periode[2] = BigDecimal.ZERO;
        periode[3] = BigDecimal.ZERO;
        periode[4] = BigDecimal.ZERO;
        periode[5] = BigDecimal.ZERO;
        paramBesoinsNeufs.setPeriode(periode);
        mapInit.put("test2",paramBesoinsNeufs);

        HashMap<String,ParamBesoinsNeufs> resultMap = impl.modifBesoinUBatEx(mapInit);
        // Test 1 : la premiere periode doit etre inchangee
        Assert.assertEquals(resultMap.get("test1").getPeriode(0),mapInit.get("test1").getPeriode(0));
        // Test 2 : test du resultat
        Assert.assertEquals(new BigDecimal("8.3250"),resultMap.get("test1").getPeriode(1));
        // Test 3 : pas de modification pour les usages non RT
        Assert.assertEquals(BigDecimal.ONE,resultMap.get("test2").getPeriode(1));

    }


}
