package org.pentaho.test.platform.web.ui.component;
import java.io.File;

import org.pentaho.test.platform.web.ui.BaseUITest;




public class SecureFilterUITest extends BaseUITest {

  private static final String SOLUTION_PATH = "test-src/solution";

  private static final String ALT_SOLUTION_PATH = "test-src/solution";

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
    if (file.exists()) {
      return SOLUTION_PATH;
    } else {
      return ALT_SOLUTION_PATH;
    }
  }
  public void runXAction(String test, String solution, String path, String action) {
		runUrl( "ViewAction?solution="+solution+"&path="+path+"&action="+action, "SecureFilterUITest." + test ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	
	public void testLogin() {
		
		runUrl( "j_acegi_security_check?j_username=suzy&j_password=password", "NavigateUITest.testLogin" ); //$NON-NLS-1$ //$NON-NLS-2$
		
	}

	public void testInvoice() {
		
		runXAction( "testInvoice", "samples", "steel-wheels/reports", "invoice.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
	}
	
	public void testInventoryList() {
		
		runXAction( "testInventoryList", "samples", "steel-wheels/reports", "Inventory%20List.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
	}

	public void testSalesByCustomer() {
		
		runXAction( "testSalesByCustomer", "samples", "steel-wheels/reports", "Sales_by_Customer.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
	}

	public void testSalesByTerritory() {
		
		runXAction( "testSalesByTerritory", "samples", "steel-wheels/reports", "Sales_by_Territory.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
	}

	
	public void testDynamicallyShowColumns() {
		
		runXAction( "testDynamicallyShowColumns", "samples", "steel-wheels/reports", "dynamically_show_columns.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
	}

	public void testInventSubscribe() {
		
		runXAction( "testInventSubscribe", "samples", "steel-wheels/reports", "invent_subscribe.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
	}

	public void testCustomParameterPageExample() {

		runXAction( "testCustomPageParameterExample", "samples", "reporting", "custom-parameter-page-example.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
	}

	public void testCustomParameterPageExample2() {
		
		runXAction( "testCustomPageParameterExample2", "samples", "reporting", "custom-parameter-page-example2.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
	}
	
	public void testDependentParameterExample() {
		
		runXAction( "testDependentParameterExample", "samples", "reporting/dep-param", "DependentParameterExample.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
	}
	
	public void testSecureParameterPageExample() {
		
		runXAction( "testSecureParameterPageExample", "samples", "secure", "secure-parameter-page-example.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
	}
	
	public void testSecureGlobalSample() {
		
		runXAction( "testSecureGlobalSample", "samples", "secure", "global-sample.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
	}
	
	public void testSecureSample() {
		
		runXAction( "testSecureSample", "samples", "secure", "secure-sample.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
	}
}
