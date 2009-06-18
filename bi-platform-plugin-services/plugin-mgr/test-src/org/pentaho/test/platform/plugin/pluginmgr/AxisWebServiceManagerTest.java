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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.test.platform.plugin.pluginmgr;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.IServiceTypeManager;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.ServiceInitializationException;
import org.pentaho.platform.api.engine.WebServiceConfig;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.WebServiceConfig.ServiceType;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.services.pluginmgr.AxisWebServiceManager;
import org.pentaho.platform.plugin.services.pluginmgr.DefaultServiceManager;
import org.pentaho.platform.plugin.services.pluginmgr.PluginManager;
import org.pentaho.platform.plugin.services.webservices.content.StyledHtmlAxisServiceLister;
import org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.EchoServiceBean;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.plugin.services.webservices.MimeTypeListener;

@SuppressWarnings("nls")
public class AxisWebServiceManagerTest {

  private MicroPlatform microPlatform;

  StandaloneSession session;

  IServiceManager serviceManager;

  @Before
  public void init0() throws ServiceInitializationException {
    microPlatform = new MicroPlatform("plugin-mgr/test-res/AxisWebServiceManagerTest/");
    microPlatform.define(ISolutionEngine.class, SolutionEngine.class);
    microPlatform.define(ISolutionRepository.class, FileBasedSolutionRepository.class);
    microPlatform.define(IPluginManager.class, PluginManager.class, Scope.GLOBAL);
    microPlatform.define(IServiceManager.class, DefaultServiceManager.class, Scope.GLOBAL);
    
    serviceManager = new DefaultServiceManager();
    IServiceTypeManager axisManager = new AxisWebServiceManager();
    serviceManager.setServiceTypeManagers(Arrays.asList(axisManager));
    
    new StandaloneSession();
  }

  @Test
  public void testWebserviceRegistration() throws Exception {
    microPlatform.init();

    WebServiceConfig config = new WebServiceConfig();
    String serviceId = "echoService";
    config.setId(serviceId);
    config.setServiceType(ServiceType.XML);
    config.setDescription("testDescription");
    config.setEnabled(true);
    config.setTitle("testTitle");
    config.setDescription("testDescription");
    config.setServiceClass(EchoServiceBean.class);
    
    serviceManager.registerService(config);
    
    serviceManager.initServices();

    IContentGenerator serviceLister = new StyledHtmlAxisServiceLister();

    String html = getContentAsString(serviceLister);
    System.out.println(html);
    
    assertTrue("EchoService was not listed", StringUtils.contains(html, serviceId));
  }
  
  private String getContentAsString(IContentGenerator cg) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOutputHandler outputHandler = new SimpleOutputHandler(out, false);

//    String baseUrl = "http://testhost:testport/testcontent";
    String baseUrl = "http://localhost:8080/pentaho/";
    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider requestParams = new SimpleParameterProvider();
    parameterProviders.put(IParameterProvider.SCOPE_REQUEST, requestParams);
    SimpleUrlFactory urlFactory = new SimpleUrlFactory(baseUrl + "?");
    List<String> messages = new ArrayList<String>();
    cg.setOutputHandler(outputHandler);
    MimeTypeListener mimeTypeListener = new MimeTypeListener();
    outputHandler.setMimeTypeListener(mimeTypeListener);
    cg.setMessagesList(messages);
    cg.setParameterProviders(parameterProviders);
    cg.setSession(PentahoSessionHolder.getSession());
    cg.setUrlFactory(urlFactory);
    cg.createContent();
    String content = new String(out.toByteArray());
    return content;
  }

}
