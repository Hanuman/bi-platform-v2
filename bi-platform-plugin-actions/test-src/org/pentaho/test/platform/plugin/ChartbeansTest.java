package org.pentaho.test.platform.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.action.chartbeans.DefaultChartBeansGenerator;
import org.pentaho.platform.plugin.action.chartbeans.IChartBeansGenerator;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.plugin.services.pluginmgr.PluginManager;
import org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository;
import org.pentaho.platform.uifoundation.component.ActionComponent;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import edu.emory.mathcs.backport.java.util.Arrays;

/* This test required SampleData to be running
 * 
 */

public class ChartbeansTest{
  protected class ChartbeansActionComponent extends ActionComponent{

    public ChartbeansActionComponent(String actionString, String instanceId, int outputPreference,
        IPentahoUrlFactory urlFactory, List messages) {
      super(actionString, instanceId, outputPreference, urlFactory, messages);
    }

    public byte[] getContentBytes() {
      return super.getContentAsStream(null).toByteArray();
    }
  }
  
  private MicroPlatform microPlatform;

  StandaloneSession session;

  IPluginManager pluginManager;

  @Before
  public void init0() {
    microPlatform = new MicroPlatform("test-src/solution/");
    microPlatform.define(ISolutionEngine.class, SolutionEngine.class);
    microPlatform.define(ISolutionRepository.class, FileBasedSolutionRepository.class);
    microPlatform.define(IChartBeansGenerator.class, DefaultChartBeansGenerator.class);

    session = new StandaloneSession();
    pluginManager = new PluginManager();
  }
  
  @Test
  public void testChartbeansActionComponentJFree() {
    microPlatform.init();
    
    IPentahoUrlFactory urlFactory = new SimpleUrlFactory(PentahoSystem.getApplicationContext().getBaseUrl());
    ArrayList messages = new ArrayList();
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    parameterProvider.setParameter("solution", "test"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("path", "chartbeans"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("action", "Chartbeans_All_Inclusive_JFree.xaction"); //$NON-NLS-1$ //$NON-NLS-2$
    ChartbeansActionComponent component = new ChartbeansActionComponent(
        "test/chartbeans/Chartbeans_All_Inclusive_JFree.xaction", null, IOutputHandler.OUTPUT_TYPE_DEFAULT, urlFactory, messages); //$NON-NLS-1$
    component.setParameterProvider(IParameterProvider.SCOPE_REQUEST, parameterProvider);
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
    component.validate(session, null);
    OutputStream outputStream = getOutputStream("Chartbeans.testChartbeansAllInclusive_JFree", ".png"); //$NON-NLS-1$ //$NON-NLS-2$
    
    // Compare resultant byte array to the originally generated one
    byte[] generatedContents = component.getContentBytes();

    File hFile = getOriginalFile("Chartbeans.testChartbeansAllInclusive_JFree.png"); //$NON-NLS-1$
    
    byte[] originalContents = new byte[(int)hFile.length()];
    
    try {
      outputStream.write(generatedContents);
      
      new FileInputStream(hFile).read(originalContents);
      assertTrue("Generated content is incorrect", Arrays.equals(generatedContents, originalContents)); //$NON-NLS-1$
    } catch (Exception e) {
      assertTrue("An exception has been thrown: " + e.getMessage(), false); //$NON-NLS-1$
    }
  }
  
  @Test
  public void testChartbeansActionComponentOFC() {
    microPlatform.init();
    
    IPentahoUrlFactory urlFactory = new SimpleUrlFactory(PentahoSystem.getApplicationContext().getBaseUrl());
    ArrayList messages = new ArrayList();
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    parameterProvider.setParameter("solution", "test"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("path", "chartbeans"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("action", "Chartbeans_All_Inclusive_OFC.xaction"); //$NON-NLS-1$ //$NON-NLS-2$
    ActionComponent component = new ActionComponent("test/chartbeans/Chartbeans_All_Inclusive_OFC.xaction", null, IOutputHandler.OUTPUT_TYPE_DEFAULT, urlFactory, messages); //$NON-NLS-1$
    component.setParameterProvider(IParameterProvider.SCOPE_REQUEST, parameterProvider);
    component.validate(session, null);
    OutputStream outputStream = getOutputStream("Chartbeans.testChartbeansAllInclusive_OFC", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
    
    String result = component.getContent("text/html"); //$NON-NLS-1$
    
    try {
      outputStream.write(result.getBytes());
      
      assertTrue(result.startsWith("<html><head>")); //$NON-NLS-1$
      assertEquals("function getChartData()", result.substring(74, 97)); //$NON-NLS-1$
      assertEquals("/*JSON*/", result.substring(107, 115)); //$NON-NLS-1$
      assertEquals("/*END_JSON*/", result.substring(1306, 1318)); //$NON-NLS-1$
      assertEquals("</body></html>", result.substring(2241)); //$NON-NLS-1$
    } catch (Exception e) {
      assertTrue("An exception has been thrown: " + e.getMessage(), false); //$NON-NLS-1$
    }
  }
  
  protected OutputStream getOutputStream(String testName, String extension) {
    OutputStream outputStream = null;
    try {
      String tmpDir = PentahoSystem.getApplicationContext().getFileOutputPath("test/tmp"); //$NON-NLS-1$
      File file = new File(tmpDir);
      file.mkdirs();
      String path = PentahoSystem.getApplicationContext().getFileOutputPath("test/tmp/" + testName + extension); //$NON-NLS-1$
      outputStream = new FileOutputStream(path);
    } catch (FileNotFoundException e) {

    }
    return outputStream;
  }
  
  protected File getOriginalFile(String fileName){
    return new File(System.getProperty("user.dir") + "\\test-src\\solution\\test\\chartbeans" +  //$NON-NLS-1$//$NON-NLS-2$
    "\\results\\" + fileName); //$NON-NLS-1$
  }
  
}
