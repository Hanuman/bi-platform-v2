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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.platform.api.engine.IComponent;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginOperation;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.engine.WebServiceDefinition;
import org.pentaho.platform.api.engine.IPlatformPlugin.BeanDefinition;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.ContentGeneratorInfo;
import org.pentaho.platform.engine.core.solution.ContentInfo;
import org.pentaho.platform.engine.core.solution.PluginOperation;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.services.pluginmgr.AxisWebServiceManager;
import org.pentaho.platform.plugin.services.pluginmgr.PlatformPlugin;
import org.pentaho.platform.plugin.services.pluginmgr.PluginManager;
import org.pentaho.platform.plugin.services.pluginmgr.PluginMessageLogger;
import org.pentaho.platform.plugin.services.pluginmgr.SystemPathXmlPluginProvider;
import org.pentaho.platform.plugin.services.webservices.content.StyledHtmlAxisServiceLister;
import org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.EchoServiceBean;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.plugin.services.webservices.MimeTypeListener;
import org.pentaho.ui.xul.IMenuCustomization;
import org.pentaho.ui.xul.XulOverlay;

@SuppressWarnings("nls")
public class AxisWebServiceManagerTest {

  private MicroPlatform microPlatform;

  StandaloneSession session;

  IServiceManager serviceManager;

  @Before
  public void init0() {
    microPlatform = new MicroPlatform("plugin-mgr/test-res/AxisWebServiceManagerTest/");
    microPlatform.define(ISolutionEngine.class, SolutionEngine.class);
    microPlatform.define(ISolutionRepository.class, FileBasedSolutionRepository.class);

    session = new StandaloneSession();
    serviceManager = new AxisWebServiceManager();

  }

  @Test
  public void testWebserviceRegistration() throws Exception {
    microPlatform.init();

    WebServiceDefinition wsDfn = new WebServiceDefinition();
    wsDfn.setDescription("testDescription");
    wsDfn.setEnabled(true);
    wsDfn.setTitle("testTitle");
    wsDfn.setDescription("testDescription");
    wsDfn.setServiceClass(EchoServiceBean.class);
    serviceManager.defineService(wsDfn);
    serviceManager.initServices(session);

    IContentGenerator serviceLister = new StyledHtmlAxisServiceLister();

    String html = getContentAsString(serviceLister);
    System.out.println(html);
    
    assertTrue("EchoService was not listed", StringUtils.contains(html, "EchoService"));
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
    cg.setSession(session);
    cg.setUrlFactory(urlFactory);
    cg.createContent();
    String content = new String(out.toByteArray());
    return content;
  }

}
