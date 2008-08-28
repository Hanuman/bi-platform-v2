package org.pentaho.test.platform.web.ui.component;


import java.util.List;

import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.uifoundation.component.xml.XmlComponent;
import org.pentaho.test.platform.engine.core.TestManager;
/**
 * This class provides an interface for the org.pentaho.test.TestManager class
 * 
 * @author James Dixon
 */
public class TestManagerComponent extends XmlComponent {

	private String suiteName = null;
	private String testName = null;
	private boolean auto = false;
    /**
     * 
     */
    private static final long serialVersionUID = -7689172811188651493L;

    private static final Log logger = LogFactory.getLog(TestManagerComponent.class);

    public TestManagerComponent(IPentahoUrlFactory urlFactory, List messages) {
        super(urlFactory, messages, null);
        setXsl("text/html", "TestSuiteResults.xsl"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void run() {
//        TestManager.run();
    }
    
    public void setAuto( boolean auto ) {
    		this.auto = auto;
    }
    
    public void runSuite(String suite ) {
    		suiteName = suite; 
    		TestSuite testSuite = new TestSuite();
    		try {
    			TestManager.getInstance( testSuite ).runSuite( suite );
    		} catch (Exception e) {
    			
    		}
}

    public void runTest(String suite, String test ) {
		suiteName = suite; 
		testName = test; 
		try {
    		TestSuite testSuite = new TestSuite();
			TestManager.getInstance( testSuite ).runTest( suite, test );
		} catch (Exception e) {
			
		}
}

    public Document getXmlContent() {

    		try {
        		TestSuite testSuite = new TestSuite();
        		TestManager testManager = TestManager.getInstance( testSuite );
                Document result = testManager.getStatus( getSession() );
                Element root = result.getRootElement();
                if( suiteName != null ) {
                    root.addAttribute( "last-suite", suiteName ); //$NON-NLS-1$
                    if( auto ) {
                        int suiteIdx = testManager.getSuiteIndex( suiteName );
                        String nextSuite = testManager.getSuite( suiteIdx+1 );
                        if( nextSuite != null ) {
                            root.addAttribute( "next-suite", nextSuite );            		 //$NON-NLS-1$
                        }
                    }
                }
                if( testName != null ) {
                    root.addAttribute( "last-test", testName ); //$NON-NLS-1$
                }
                setXslProperty("baseUrl", urlFactory.getDisplayUrlBuilder().getUrl()); //$NON-NLS-1$ 
                return result;
    		} catch (Exception e) {
    			return null;
    		}
    }

    public Log getLogger() {
        return logger;
    }

    public boolean validate() {
        return true;
    }

}
