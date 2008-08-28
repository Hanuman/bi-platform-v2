package org.pentaho.test.platform.web.ui.component;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import junit.framework.TestSuite;

public class TestManager extends org.pentaho.test.platform.engine.core.TestManager {

	private static final long serialVersionUID = 5079704303052866469L;

	protected Enumeration getSuites() {
    	
		TestSuite testSuite = new TestSuite();

    		Enumeration tests = super.getSuites( testSuite );
    		

    		// add the default list
    		ArrayList list = new ArrayList();
    		while( tests.hasMoreElements() ) {
    			list.add( tests.nextElement() );
    		}
    		
    		// add the tests from Pro
//    		list.add( new TestSuite( JdbcUserRoleListServiceTests.class ) );

    		list.add( new TestSuite(NavigateUITest.class) );
    		list.add( new TestSuite(SecureFilterUITest.class) );
    		// TODO add the rest
    		
    		Enumeration allTests = Collections.enumeration( list );

    		return allTests;

    }

	
}
