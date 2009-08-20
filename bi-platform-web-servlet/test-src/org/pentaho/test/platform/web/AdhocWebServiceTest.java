/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.test.platform.web;


import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.UserSession;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.platform.web.servlet.AdhocWebService;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.engine.core.BaseTestCase;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockHttpSession;

/**
 * Tests for <code>org.pentaho.platform.web.servlet.AdhocWebService</code>.
 * 
 * @author mlowery
 */
@SuppressWarnings("nls")
public class AdhocWebServiceTest extends BaseTestCase {

  private static final String userName = "joe"; //$NON-NLS-1$
  private static final String role = "Admin"; //$NON-NLS-1$   
  private static final MockHttpServletRequest request = new MockHttpServletRequest();
  private static final AdhocWebService servlet = new AdhocWebService();
  @SuppressWarnings("unused")
  private static final String templatePath = "/templates/Pentaho/jfreereport-template.xml"; //$NON-NLS-1$   
  @SuppressWarnings("unused")
  private static final AdhocWebServiceTestUserRoleListService userRolesLS = new AdhocWebServiceTestUserRoleListService();
  private static final MockHttpSession session = new MockHttpSession();
  private static final String SOLUTION_PATH = "test-src/solution";


  public String getSolutionPath() {
      return SOLUTION_PATH;  
  }
  static class AdhocWebServiceTestUserRoleListService implements IUserRoleListService {

    public GrantedAuthority[] getAllAuthorities() {
      return new GrantedAuthority[] { new GrantedAuthorityImpl(role) };
    }

    public String[] getAllUsernames() {
      String[] s = {userName};
      return s;
    }

    public GrantedAuthority[] getAuthoritiesForUser(String username) {
      return new GrantedAuthority[] { new GrantedAuthorityImpl(role) };
    }

    public String[] getUsernamesInRole(GrantedAuthority authority) {
      String[] s = {userName};
      return s;
    }
  
  }
  
  @SuppressWarnings("unused")
  private static final String reportSpecContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" //$NON-NLS-1$
    + "<report-spec tool='waqr' tool-version='1.0' template-name='Pentaho'>" //$NON-NLS-1$
    + "<jndi-source><![CDATA[SampleData]]></jndi-source>" //$NON-NLS-1$
    + "<report-name><![CDATA[testSave]]></report-name>" //$NON-NLS-1$
    + "<report-desc><![CDATA[Created by the Web-based Adhoc Query and Reporting Client]]></report-desc>" //$NON-NLS-1$
    + "<query>" //$NON-NLS-1$
    + "<mql>" //$NON-NLS-1$
    + "<domain_type>relational</domain_type>" //$NON-NLS-1$
    + "<domain_id><![CDATA[samples]]></domain_id><model_id><![CDATA[BV_ORDERS]]></model_id>" //$NON-NLS-1$
    + "<options>" //$NON-NLS-1$
    + "<disable_distinct>false</disable_distinct>" //$NON-NLS-1$
    + "</options>" //$NON-NLS-1$
    + "<selections>" //$NON-NLS-1$
    + "<selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTLINE</column></selection>" //$NON-NLS-1$
    + "</selections>" //$NON-NLS-1$
    + "<constraints>" //$NON-NLS-1$
    + "</constraints>" //$NON-NLS-1$
    + "<orders>" //$NON-NLS-1$
    + "</orders>" //$NON-NLS-1$
    + "</mql>" //$NON-NLS-1$
    + "</query>" //$NON-NLS-1$
    + "<field-mapping key=\"reportheader\" value=\"\"/><field-mapping key=\"reportfooter\" value=\"\"/><field-mapping key=\"pageheader\" value=\"\"/><field-mapping key=\"pagefooter\" value=\"\"/><page-format><![CDATA[LETTER]]></page-format>" //$NON-NLS-1$
    + "<orientation><![CDATA[landscape]]></orientation>" //$NON-NLS-1$
    + "<use-row-banding><![CDATA[true]]></use-row-banding>" //$NON-NLS-1$
    + "<row-banding-color><![CDATA[#e7e7e7]]></row-banding-color>" //$NON-NLS-1$
    + "<use-column-header-background-color><![CDATA[true]]></use-column-header-background-color>" //$NON-NLS-1$
    + "<column-header-background-color><![CDATA[#9eaa36]]></column-header-background-color>" //$NON-NLS-1$
    + "<grand-totals-label><![CDATA[Grand Total]]></grand-totals-label>" //$NON-NLS-1$
    + "<grand-totals-horizontal-alignment><![CDATA[left]]></grand-totals-horizontal-alignment>" //$NON-NLS-1$
    + "<use-dummy-group-footer-background-color><![CDATA[true]]></use-dummy-group-footer-background-color>" //$NON-NLS-1$
    + "<dummy-group-footer-background-color><![CDATA[#e0d0c0]]></dummy-group-footer-background-color>" //$NON-NLS-1$
    + "<use-horizontal-gridlines><![CDATA[true]]></use-horizontal-gridlines>" //$NON-NLS-1$
    + "<use-vertical-gridlines><![CDATA[true]]></use-vertical-gridlines>" //$NON-NLS-1$
    + "<horizontal-gridlines-color><![CDATA[#c0c0c0]]></horizontal-gridlines-color>" //$NON-NLS-1$
    + "<vertical-gridlines-color><![CDATA[#c0c0c0]]></vertical-gridlines-color>" //$NON-NLS-1$
    + "<group-header-font-name><![CDATA[Arial]]></group-header-font-name>" //$NON-NLS-1$
    + "<group-header-font-style><![CDATA[1]]></group-header-font-style>" //$NON-NLS-1$
    + "<group-header-font-size><![CDATA[12]]></group-header-font-size>" //$NON-NLS-1$
    + "<group-header-font-color><![CDATA[#767676]]></group-header-font-color>" //$NON-NLS-1$
    + "<group-footer-font-name><![CDATA[Arial]]></group-footer-font-name>" //$NON-NLS-1$
    + "<group-footer-font-style><![CDATA[1]]></group-footer-font-style>" //$NON-NLS-1$
    + "<group-footer-font-size><![CDATA[12]]></group-footer-font-size>" //$NON-NLS-1$
    + "<group-footer-font-color><![CDATA[#ff7d17]]></group-footer-font-color>" //$NON-NLS-1$
    + "<column-header-font-name><![CDATA[Arial]]></column-header-font-name>" //$NON-NLS-1$
    + "<column-header-font-style><![CDATA[0]]></column-header-font-style>" //$NON-NLS-1$
    + "<column-header-font-size><![CDATA[10]]></column-header-font-size>" //$NON-NLS-1$
    + "<column-header-font-color><![CDATA[#ffffff]]></column-header-font-color>" //$NON-NLS-1$
    + "<column-header-gap><![CDATA[1]]></column-header-gap>" //$NON-NLS-1$
    + "<items-font-name><![CDATA[Arial]]></items-font-name>" //$NON-NLS-1$
    + "<items-font-style><![CDATA[0]]></items-font-style>" //$NON-NLS-1$
    + "<items-font-size><![CDATA[9]]></items-font-size>" //$NON-NLS-1$
    + "<items-font-color><![CDATA[#000000]]></items-font-color>" //$NON-NLS-1$
    + "<field is-detail=\"true\" name=\"BC_PRODUCTS_PRODUCTLINE\" horizontal-alignment=\"left\" expression=\"none\" type=\"12\"/>" //$NON-NLS-1$
    + "</report-spec>"; //$NON-NLS-1$
  
    @SuppressWarnings("unused")
    private static final String ACL_TEST_DOC =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" //$NON-NLS-1$
      + "<acl>" //$NON-NLS-1$
      + "<entry name='suzy' permissions='3'/>" //$NON-NLS-1$
      + "<entry name='joe' permissions='7'/>" //$NON-NLS-1$
       + "</acl>"; //$NON-NLS-1$
  
  
  public void setUp() {
    IUserDetailsRoleListService userDetailsLS = (IUserDetailsRoleListService) new MockUserDetailsRoleListService();
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    parameterProvider.setParameter("SOLUTION_PATH", "solution.path");
    session.setAttribute(UserSession.PENTAHO_SESSION_KEY, userDetailsLS.getEffectiveUserSession(userName, (IParameterProvider)parameterProvider) );

    request.setRemoteUser( userName );
    request.setBodyContent(""); //$NON-NLS-1$
    request.setSession(session);
  }

  private Node getNodeFromResponseDoc( MockHttpServletResponse response, String xpath )
  {
    String responseContent = response.getOutputStreamContent();
    Document doc = null;
    try {
      doc = XmlDom4JHelper.getDocFromString( responseContent, null );
    } catch(XmlParseException e) {
      assertFalse( e.getMessage(), true );
      return null;
    }
    Node nd = doc.selectSingleNode( xpath );
    return nd;
  }

  
  public void testGetJFreePaperSizes() {
    request.clearParameters();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.setupAddParameter("component", "getJFreePaperSizes" ); //$NON-NLS-1$ //$NON-NLS-2$
    try {
      servlet.service(request, response);
    } catch (Exception e) {
      assertFalse( e.getMessage(), true );
      return;
    }
    // need to look in the response to verify the content
    String xpath = "/pageFormats/pageFormat[@name='A3 ROTATED']";    //$NON-NLS-1$
    Node nd = getNodeFromResponseDoc( response, xpath );
    
    assertTrue( "getJFreePaperSizes service call failed.", nd != null ); //$NON-NLS-1$
    
    System.out.println( "Done testGetJFreePaperSizes." ); //$NON-NLS-1$   
  }
  
  public void testGetWaqrRepositoryIndexDoc() {
    request.clearParameters();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.setupAddParameter("component", "getWaqrRepositoryIndexDoc" ); //$NON-NLS-1$ //$NON-NLS-2$
    request.setupAddParameter("templateFolderPath", "/templates/Fall" ); //$NON-NLS-1$ //$NON-NLS-2$
    try {
      servlet.service(request, response);
    } catch (Exception e) {
      assertFalse( e.getMessage(), true );
      return;
    }
    // need to look in the response to verify the content
    String xpath = "/index/name']"; //$NON-NLS-1$   
    Node nd = getNodeFromResponseDoc( response, xpath );
    assertTrue( "getWaqrRepositoryIndexDoc service call failed.", nd != null ); //$NON-NLS-1$
    
    xpath = "/index/description']"; //$NON-NLS-1$   
    nd = getNodeFromResponseDoc( response, xpath );
    assertTrue( "getWaqrRepositoryIndexDoc service call failed.", nd != null ); //$NON-NLS-1$
    
    System.out.println( "Done testGetWaqrRepositoryIndexDoc." ); //$NON-NLS-1$   
  }
  
  
  public static void main(String[] args) {
    AdhocWebServiceTest test = new AdhocWebServiceTest();
    test.setUp();
    try {
        test.testGetJFreePaperSizes();
        test.testGetWaqrRepositoryIndexDoc();
    } finally {
        BaseTest.shutdown();
    }
}
}