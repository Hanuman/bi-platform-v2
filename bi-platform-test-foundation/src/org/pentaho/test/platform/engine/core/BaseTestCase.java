/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 3 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jun 22, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.test.platform.engine.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IObjectFactoryCreator;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.platform.util.web.SimpleUrlFactory;

public abstract class BaseTestCase extends TestCase {
  public static final String SOLUTION_PATH = "test-src/solution";
  public static final String SCOPE_REQUEST = "request"; //$NON-NLS-1$
  public static final String SCOPE_SESSION = "session"; //$NON-NLS-1$ 
  private String solutionPath;
  final String SYSTEM_FOLDER = "/system";
  private static final String DEFAULT_SPRING_CONFIG_FILE_NAME = "pentahoObjects.spring.xml";

  StandaloneApplicationContext applicationContext = null;

  IPentahoSession session;
  public BaseTestCase() {
    init(getSolutionPath());
  }
  
  public BaseTestCase(String solutionPath) {
    init(solutionPath);
  }
  
  protected void init(String solutionPath) {
    applicationContext = new StandaloneApplicationContext(solutionPath, ""); //$NON-NLS-1$
    applicationContext.setBaseUrl(getBaseUrl());
    String inContainer = System.getProperty("incontainer", "false"); //$NON-NLS-1$ //$NON-NLS-2$
    if (inContainer.equalsIgnoreCase("false")) { //$NON-NLS-1$
      // Setup simple-jndi for datasources
      System.setProperty("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory"); //$NON-NLS-1$ //$NON-NLS-2$
      System.setProperty("org.osjava.sj.root", getSolutionPath() + "/system/simple-jndi"); //$NON-NLS-1$ //$NON-NLS-2$
      System.setProperty("org.osjava.sj.delimiter", "/"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    String objectFactoryCreatorCfgFile = getSolutionPath() + SYSTEM_FOLDER + "/" + DEFAULT_SPRING_CONFIG_FILE_NAME; //$NON-NLS-1$
    
    IPentahoObjectFactory pentahoObjectFactory = new StandaloneSpringPentahoObjectFactory();
    pentahoObjectFactory.init(objectFactoryCreatorCfgFile, null);
    PentahoSystem.setObjectFactory( pentahoObjectFactory );
    PentahoSystem.init(applicationContext);
    session = new StandaloneSession("system");
  }

  protected InputStream getInputStreamFromOutput(String solutionPath, String testName, String extension) {
    String path = PentahoSystem.getApplicationContext().getFileOutputPath(
        solutionPath + "test/tmp/" + testName + extension); //$NON-NLS-1$
    File f = new File(path);
    if (f.exists()) {
      try {
        FileInputStream fis = new FileInputStream(f);
        return fis;
      } catch (Exception ignored) {
        return null;
      }
    } else {
      return null;
    }
  }

  protected OutputStream getOutputStream(String solutionPath, String testName, String extension) {
    OutputStream outputStream = null;
    try {
      IApplicationContext applicationContext = PentahoSystem.getApplicationContext();
      String outputPath = solutionPath + "/test/tmp";
      String tmpDir = applicationContext.getFileOutputPath(outputPath);
      //String tmpDir = PentahoSystem.getApplicationContext().getFileOutputPath(SOLUTION_PATH +"test/tmp"); //$NON-NLS-1$
      File file = new File(tmpDir);
      file.mkdirs();
      String path = PentahoSystem.getApplicationContext().getFileOutputPath(
          solutionPath + "/test/tmp/" + testName + extension); //$NON-NLS-1$
      outputStream = new FileOutputStream(path);
    } catch (FileNotFoundException e) {

    }
    return outputStream;
  }

  public IRuntimeContext run(String actionSequencePath, String actionSequence, IParameterProvider parameterProvider,
      IOutputHandler outputHandler) {
    List messages = new ArrayList();
    String instanceId = null;
    ISolutionEngine solutionEngine = PentahoSystem.getSolutionEngineInstance(session);
    solutionEngine.setLoggingLevel(ILogger.ERROR);
    solutionEngine.init(session);
    String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();
    HashMap parameterProviderMap = new HashMap();
    IPentahoUrlFactory urlFactory = new SimpleUrlFactory(baseUrl);
    IRuntimeContext runtimeContext = null;
    try {
      File file = new File(actionSequencePath + actionSequence);
      StringBuilder str = new StringBuilder();
      Reader reader = new FileReader(file);
      char buffer[] = new char[4096];
      int n = reader.read(buffer);
      while (n != -1) {
        str.append(buffer, 0, n);
        n = reader.read(buffer);
      }
      String xactionStr = str.toString();

      solutionEngine.setSession(session);
      runtimeContext = solutionEngine
          .execute(
              xactionStr,
              actionSequence,
              "action sequence test", false, true, instanceId, false, parameterProviderMap, null, null, urlFactory, messages); //$NON-NLS-1$ //$NON-NLS-2$
    } catch (Exception e) {
      // we should not get here
      e.printStackTrace();
      assertTrue(e.getMessage(), false);
    }
    return runtimeContext;
  }
  
  public String getSolutionPath() {
    return solutionPath;
  }
  public String getBaseUrl() {
	return "http://localhost:8080/pentaho/"; //$NON-NLS-1$
  }
}
