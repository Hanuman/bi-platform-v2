/*
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.platform.plugin.action.jfreereport.components;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.report.JFreeReport;
import org.jfree.report.modules.parser.base.ReportGenerator;
import org.jfree.resourceloader.FactoryParameterKey;
import org.jfree.resourceloader.ResourceException;
import org.jfree.resourceloader.ResourceKey;
import org.jfree.resourceloader.ResourceManager;
import org.pentaho.actionsequence.dom.IActionResource;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.jfreereport.AbstractJFreeReportComponent;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoResourceData;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoResourceLoader;
import org.pentaho.platform.plugin.action.jfreereport.helper.ReportUtils;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.xml.sax.InputSource;

/**
 * A JFreeReport run contains at least three steps. Step 1: Parse the report definition. Step 2: Grab some data. Step 3: Spit out some content. Alternativly,
 * show the print-preview. <p/> This class loads or parses the report definition.
 * 
 * 
 * @deprecated This code has known bugs and it is highly recommended that it not be used by any sane person
 * @author Thomas Morgner
 */
@Deprecated
public class JFreeReportLoadComponent extends AbstractJFreeReportComponent {
  private static final long serialVersionUID = -2240691437049710246L;

  public JFreeReportLoadComponent() {
  }

  @Override
  protected boolean validateAction() {
    if (isDefinedResource(AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTDEFN)) {
      return true;
    }

    if (isDefinedInput(AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTDEFN)) {
      IActionParameter o = getInputParameter(AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTDEFN);
      if ((o != null) && (o.getValue() instanceof String)) {
        return true;
      }
      return false;
    }

    // Handle late-bind of report resource name
    if (isDefinedInput(AbstractJFreeReportComponent.REPORTLOAD_RESOURCENAME)) {
      if (isDefinedResource(getInputStringValue(AbstractJFreeReportComponent.REPORTLOAD_RESOURCENAME))) {
        return true;
      } else {
        error(Messages.getErrorString("JFreeReport.ERROR_0004_REPORT_DEFINITION_UNREADABLE")); //$NON-NLS-1$
        return false;
      }
    }

    if (isDefinedResource(AbstractJFreeReportComponent.DATACOMPONENT_JARINPUT)) {
      if (!isDefinedInput(AbstractJFreeReportComponent.REPORTLOAD_REPORTLOC)) {
        error(Messages.getErrorString("JFreeReport.ERROR_0011_REPORT_LOCATION_MISSING")); //$NON-NLS-1$
        return false;
      }

      final IActionSequenceResource resource = getResource(AbstractJFreeReportComponent.DATACOMPONENT_JARINPUT);
      final ISolutionRepository solutionRepository = PentahoSystem.getSolutionRepository(getSession());
      final InputStream in;
      try {
        in = solutionRepository.getResourceInputStream(resource, true);
      } catch (FileNotFoundException e1) {
        error(Messages.getErrorString("JFreeReport.ERROR_0010_REPORT_JAR_MISSING", resource.getAddress())); //$NON-NLS-1$
        return false;
      }

      try {
        // not being able to read a single char is definitly a big boo ..
        if (in.read() == -1) {
          error(Messages.getErrorString("JFreeReport.ERROR_0009_REPORT_JAR_UNREADABLE")); //$NON-NLS-1$
          return false;
        }
      } catch (Exception e) {
        error(Messages.getErrorString("JFreeReport.ERROR_0009_REPORT_JAR_UNREADABLE")); //$NON-NLS-1$
        return false;
      }

      if (!isDefinedInput(AbstractJFreeReportComponent.REPORTLOAD_REPORTLOC)) {
        error(Messages.getErrorString("JFreeReport.ERROR_0012_CLASS_LOCATION_MISSING")); //$NON-NLS-1$
        return false;
      }
      return true;
    }
    return false;
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  @Override
  public void done() {

  }

  private JFreeReport getReportFromResource() throws ResourceException, IOException {
    JFreeReport report = null;
    if (isDefinedResource(AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTDEFN)) {
      final IActionSequenceResource resource = getResource(AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTDEFN);

      if (resource.getSourceType() == IActionResource.XML) {
        String repDef = resource.getAddress();
        ReportGenerator generator = ReportGenerator.getInstance();

        // add the runtime context so that PentahoResourceData class can get access to the solution repo
        generator.setObject(PentahoResourceData.PENTAHO_RUNTIME_CONTEXT_KEY, getRuntimeContext());

        report = generator.parseReport(new InputSource(new ByteArrayInputStream(repDef.getBytes())),
            getDefinedResourceURL(null));
      }
      report = parseReport(resource);
    } else if (isDefinedInput(AbstractJFreeReportComponent.REPORTLOAD_RESOURCENAME)) {
      final String resName = getInputStringValue(AbstractJFreeReportComponent.REPORTLOAD_RESOURCENAME);
      if (isDefinedResource(resName)) {
        final IActionSequenceResource resource = getResource(resName);
        report = parseReport(resource);
      }
    }
    return report;
  }

  private JFreeReport getReportFromInputParam() throws ResourceException, UnsupportedEncodingException, IOException {
    JFreeReport report = null;

    if (isDefinedInput(AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTDEFN)) {
      IActionParameter o = getInputParameter(AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTDEFN);
      if (o != null) {
        String repDef = o.getStringValue();
        ReportGenerator generator = ReportGenerator.getInstance();

        // add the runtime context so that PentahoResourceData class can get access to the solution repo
        generator.setObject(PentahoResourceData.PENTAHO_RUNTIME_CONTEXT_KEY, getRuntimeContext());

        // get base URL
        String baseURL = PentahoSystem.getApplicationContext().getBaseUrl();
        URL url = null;
        try {
          url = new URL(baseURL);
        } catch (Exception e) {
          // a null URL is ok
          warn(Messages.getString("JFreeReportLoadComponent.WARN_COULD_NOT_CREATE_URL")); //$NON-NLS-1$
        }
        report = generator.parseReport(
            new InputSource(new ByteArrayInputStream(repDef.getBytes("UTF-8"))), getDefinedResourceURL(url)); //$NON-NLS-1$
      }
    }

    return report;
  }

  private JFreeReport getReportFromJar() throws Exception {
    JFreeReport report;
    final IActionSequenceResource resource = getResource(AbstractJFreeReportComponent.DATACOMPONENT_JARINPUT);
    final ClassLoader loader = ReportUtils.createJarLoader(getSession(), resource);
    if (loader == null) {
      throw new Exception(Messages.getString("JFreeReportLoadComponent.ERROR_0035_COULD_NOT_CREATE_CLASSLOADER")); //$NON-NLS-1$
    }

    String reportLocation = getInputStringValue(AbstractJFreeReportComponent.REPORTLOAD_REPORTLOC);
    URL resourceUrl = loader.getResource(reportLocation);
    if (resourceUrl == null) {
      throw new Exception(Messages.getErrorString("JFreeReport.ERROR_0016_REPORT_RESOURCE_INVALID", //$NON-NLS-1$
          reportLocation, resource.getAddress()));
    }

    try {
      ReportGenerator generator = ReportGenerator.getInstance();

      // add the runtime context so that PentahoResourceData class can get access to the solution repo
      generator.setObject(PentahoResourceData.PENTAHO_RUNTIME_CONTEXT_KEY, getRuntimeContext());

      report = generator.parseReport(resourceUrl, getDefinedResourceURL(resourceUrl));
    } catch (Exception ex) {
      throw new Exception(Messages.getErrorString("JFreeReport.ERROR_0007_COULD_NOT_PARSE", reportLocation), ex); //$NON-NLS-1$
    }
    return report;
  }

  public JFreeReport getReport() throws Exception {
    JFreeReport report = getReportFromResource();
    if (report == null) {
      report = getReportFromInputParam();
      if (report == null) {
        report = getReportFromJar();
      }
    }
    return report;
  }

  @Override
  protected boolean executeAction() throws Throwable {
    boolean result = false;
    try {
      JFreeReport report = getReport();
      if (report != null) {
        addTempParameterObject(AbstractJFreeReportComponent.DATACOMPONENT_REPORTTEMP_OBJINPUT, report);
        result = true;
      }
    } catch (Exception ex) {
      error(ex.getMessage());
    }
    return result;
  }

  private URL getDefinedResourceURL(final URL defaultValue) {
    if (isDefinedInput(AbstractJFreeReportComponent.REPORTLOAD_RESURL) == false) {
      return defaultValue;
    }

    try {
      final String inputStringValue = getInputStringValue(Messages
          .getString(AbstractJFreeReportComponent.REPORTLOAD_RESURL));
      return new URL(inputStringValue);
    } catch (Exception e) {
      return defaultValue;
    }
  }

  private String getBaseServerURL(final String pentahoBaseURL) {
    try {
      URL url = new URL(pentahoBaseURL);
      return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort(); //$NON-NLS-1$ //$NON-NLS-2$
    } catch (Exception e) {
    }
    return pentahoBaseURL;
  }

  private String getHostColonPort(final String pentahoBaseURL) {
    try {
      URL url = new URL(pentahoBaseURL);
      return url.getHost() + ":" + url.getPort();//$NON-NLS-1$ 
    } catch (Exception e) {
    }
    return pentahoBaseURL;
  }

  /**
   * Parses the report, using the given ActionResource as initial report definition.
   * 
   * @param resource
   * @return
   */
  private JFreeReport parseReport(final IActionSequenceResource resource) {
    try {
      // define the resource url so that PentahoResourceLoader recognizes the path.
      String resourceUrl = PentahoResourceLoader.SOLUTION_SCHEMA_NAME + PentahoResourceLoader.SCHEMA_SEPARATOR
          + resource.getAddress();

      String pentahoBaseURL = PentahoSystem.getApplicationContext().getBaseUrl();

      HashMap helperObjects = new HashMap();

      helperObjects.put(new FactoryParameterKey("pentahoBaseURL"), PentahoSystem.getApplicationContext().getBaseUrl()); //$NON-NLS-1$

      // trim out the server and port
      helperObjects.put(new FactoryParameterKey("serverBaseURL"), getBaseServerURL(pentahoBaseURL)); //$NON-NLS-1$

      helperObjects.put(
          new FactoryParameterKey("solutionRoot"), PentahoSystem.getApplicationContext().getSolutionPath("")); //$NON-NLS-1$ //$NON-NLS-2$

      // get the host:port portion only
      helperObjects.put(new FactoryParameterKey("hostColonPort"), getHostColonPort(pentahoBaseURL)); //$NON-NLS-1$ 

      // add the runtime context so that PentahoResourceData class can get access to the solution repo
      helperObjects.put(new FactoryParameterKey(PentahoResourceData.PENTAHO_RUNTIME_CONTEXT_KEY), getRuntimeContext());

      Iterator it = getInputNames().iterator();
      while (it.hasNext()) {
        try {
          String inputName = (String) it.next();
          String inputValue = getInputStringValue(inputName);
          helperObjects.put(new FactoryParameterKey(inputName), inputValue);
        } catch (Exception e) {
        }
      }

      ResourceManager resourceManager = new ResourceManager();
      resourceManager.registerDefaults();

      ResourceKey contextKey = resourceManager.createKey(resourceUrl, helperObjects);
      ResourceKey key = resourceManager.createKey(resourceUrl, helperObjects);

      return ReportGenerator.getInstance().parseReport(resourceManager, key, contextKey);

    } catch (Exception ex) {
      error(Messages.getErrorString("JFreeReport.ERROR_0007_COULD_NOT_PARSE", resource.getAddress()), ex); //$NON-NLS-1$
      return null;
    }
  }

  @Override
  public boolean init() {
    return true;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog(JFreeReportLoadComponent.class);
  }
}
