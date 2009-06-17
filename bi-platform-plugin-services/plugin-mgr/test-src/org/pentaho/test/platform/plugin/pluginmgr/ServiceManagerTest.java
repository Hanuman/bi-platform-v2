package org.pentaho.test.platform.plugin.pluginmgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.IServiceTypeManager;
import org.pentaho.platform.api.engine.ServiceException;
import org.pentaho.platform.api.engine.WebServiceConfig;
import org.pentaho.platform.api.engine.WebServiceConfig.ServiceType;
import org.pentaho.platform.plugin.services.pluginmgr.DefaultServiceManager;
import org.pentaho.platform.plugin.services.pluginmgr.GwtRpcServiceManager;
import org.pentaho.test.platform.engine.core.EchoServiceBean;

@SuppressWarnings("nls")
public class ServiceManagerTest {
  
  IServiceManager serviceManager;
  
  @Before
  public void init() {
    serviceManager = new DefaultServiceManager();
    IServiceTypeManager gwtHandler = new GwtRpcServiceManager();
    serviceManager.setServiceTypeManagers(Arrays.asList(gwtHandler));
  }
  
  @Test
  public void testServiceRegistration() throws ServiceException {
    WebServiceConfig config = new WebServiceConfig();
    config.setId("testId");
    config.setServiceClass(EchoServiceBean.class);
    config.setServiceType(ServiceType.GWT);
    serviceManager.registerService(config);
  }
  
  @Test
  public void testGetServiceBean() throws ServiceException {
    testServiceRegistration();
    
    Object serviceBean = serviceManager.getServiceBean(ServiceType.GWT, "testId");
    assertNotNull(serviceBean);
    assertTrue(serviceBean instanceof EchoServiceBean);
  }
  
  @Test
  public void testGetServiceConfig() throws ServiceException {
    testServiceRegistration();
    
    WebServiceConfig config = serviceManager.getServiceConfig(ServiceType.GWT, "testId");
    assertNotNull(config);
    assertEquals("testId", config.getId());
    assertEquals(EchoServiceBean.class, config.getServiceClass());
    assertEquals(ServiceType.GWT, config.getServiceType());
  }
}
