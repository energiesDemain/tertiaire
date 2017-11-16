package com.ed.cgdd.deby.excelresult;

import com.ed.cgdd.derby.excelresult.CSVService;
import com.ed.cgdd.derby.excelresult.CSVServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Emmanuel on 16/11/2017.
 */
public class CSVServiceImplTest {


    private ApplicationContext context;

    public ApplicationContext getContext() {
        return context;
    }

    public void setContext(ApplicationContext context) {
        this.context = context;
    }


    @Before
    public void initContext() {
        context = new ClassPathXmlApplicationContext("applicationContextProcess.xml");
    }
    CSVServiceImpl impl = new CSVServiceImpl();

    @Test
    public void csvTest(){
        CSVService impl = (CSVService) context.getBean("csvService");
        impl.csvService(1);
    }

}
