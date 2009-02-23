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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.jfreereport.castormodel.reportspec.ReportSpec;
import org.pentaho.jfreereport.wizard.utility.CastorUtility;
import org.pentaho.jfreereport.wizard.utility.report.ReportGenerationUtility;
import org.pentaho.jfreereport.wizard.utility.report.ReportParameterUtility;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.action.jfreereport.AbstractJFreeReportComponent;
import org.pentaho.platform.plugin.action.messages.Messages;

/**
 * This component generates report definitions. It uses the report wizard for the magical generation task.
 * 
 * @deprecated
 * @author Thomas Morgner
 */
@Deprecated
public class JFreeReportGenerateDefinitionComponent extends AbstractJFreeReportComponent {
  private static final long serialVersionUID = -6364796568478754207L;

  public JFreeReportGenerateDefinitionComponent() {
  }

  @Override
  protected boolean validateAction() {
    if (isDefinedResource(AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTSPECINPUT) == false) {
      return false;
    }
    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  @Override
  public void done() {

  }

  /**
   * Look through the Zip stream and find an entry whose name is .xreportspec. If the entry is found, return it, otherwise return null.
   * 
   * @param zStrm
   * @return If the entry is found, return it, otherwise return null.
   * @throws IOException
   */
  private ZipEntry findReportSpec(final ZipInputStream zStrm) throws IOException {
    ZipEntry reportSpecEntry = null;
    // for loop has no body
    for (reportSpecEntry = zStrm.getNextEntry(); (null != reportSpecEntry)
        && !reportSpecEntry.getName().endsWith(".xreportspec"); reportSpecEntry = zStrm //$NON-NLS-1$
        .getNextEntry()) {
      ;
    }

    return reportSpecEntry;
  }

  private ReportSpec loadFromZip(final IActionSequenceResource resource) {
    final ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, getSession());

    try {
      InputStream reportSpecInputStream = solutionRepository.getResourceInputStream(resource, true);
      ZipInputStream zis = new ZipInputStream(reportSpecInputStream);

      ZipEntry reportSpecEntry = findReportSpec(zis);

      if (reportSpecEntry == null) {
        return null;
      }

      // is this really sane? Blindly using the first zip entry is ... argh!
      // maybe you should use GZipped streams instead...
      return (ReportSpec) CastorUtility.getInstance().readCastorObject(zis, ReportSpec.class);
    } catch (Exception e) {
      return null;
    }
  }

  public ReportSpec getReportSpec() throws FileNotFoundException {
    ReportSpec reportSpec = null;
    if (isDefinedResource(AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTSPECINPUT)) {
      IActionSequenceResource resource = getResource(AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTSPECINPUT);
      reportSpec = loadFromZip(resource);
      if (reportSpec == null) {
        InputStream reportSpecInputStream = PentahoSystem.get(ISolutionRepository.class, getSession()).getResourceInputStream(
            resource, true);
        reportSpec = (ReportSpec) CastorUtility.getInstance().readCastorObject(reportSpecInputStream, ReportSpec.class);
      }
    }
    return reportSpec;
  }

  public IPentahoResultSet getResultSet(final ReportSpec reportSpec) throws Exception {
    String jndiName = reportSpec.getReportSpecChoice().getJndiSource();
    IPentahoConnection connection = null;
    if (reportSpec.getIsMDX()) {
      // did this ever work??
      String connectStr = "";
      IDatasourceService datasourceService =  (IDatasourceService) PentahoSystem.getObjectFactory().getObject(IDatasourceService.IDATASOURCE_SERVICE,null);
      String dsName = datasourceService.getDSBoundName(jndiName);
      if (dsName != null) {
        connectStr = "dataSource=" + dsName + "; Catalog=mondrian"; //$NON-NLS-1$ //$NON-NLS-2$
      } else {
        error(Messages.getErrorString("MDXBaseComponent.ERROR_0005_INVALID_CONNECTION")); //$NON-NLS-1$
        return null;
      }
      Properties props = new Properties();
      props.setProperty(IPentahoConnection.CONNECTION, connectStr);
      props.setProperty(IPentahoConnection.PROVIDER, reportSpec.getMondrianCubeDefinitionPath());

      connection = PentahoConnectionFactory.getConnection(IPentahoConnection.MDX_DATASOURCE, props, getSession(), this);
    } else {
      connection = PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, jndiName, getSession(),
          this);
    }
    String query = ReportParameterUtility.setupParametersForActionSequence(reportSpec.getQuery());
    query = setupQueryParameters(query);
    IPentahoResultSet res = connection.executeQuery(query);
    return res;
  }

  @Override
  protected boolean executeAction() {
    boolean result = true;
    try {
      ReportSpec reportSpec = getReportSpec();
      if (reportSpec != null) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ReportGenerationUtility.createJFreeReportXML(reportSpec, outputStream, 0, 0, false, "", 0, 0); //$NON-NLS-1$
        String reportDefinition = new String(outputStream.toByteArray());
        addTempParameterObject(AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTDEFN, reportDefinition);

        // if that parameter is not defined, we do query for backward compatibility.
        if (!isDefinedInput(AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTTEMP_PERFQRY)
            || "true".equals(getInputParameter(AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTTEMP_PERFQRY))) {
          if (reportSpec.getReportSpecChoice().getXqueryUrl() != null) {
            // handle xquery
          } else {
            try {
              addTempParameterObject(AbstractJFreeReportComponent.DATACOMPONENT_DATAINPUT, getResultSet(reportSpec));
            } catch (Exception e) {
              result = false;
            }
          }
        }
      }
    } catch (FileNotFoundException ex) {
      error(ex.getLocalizedMessage());
      result = false;
    }
    return result;
  }

  @Override
  public boolean init() {
    return true;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog(JFreeReportGenerateDefinitionComponent.class);
  }

  public String setupQueryParameters(String query) {
    Set inputNames = getInputNames();
    Iterator iter = inputNames.iterator();
    while (iter.hasNext()) {
      String inputName = (String) iter.next();
      final IActionParameter inputParameter = getInputParameter(inputName);
      final Object value = inputParameter.getValue();
      if ((value instanceof String) == false) {
        continue;
      }
      String paramValue = (String) value;
      String param = "\\{" + inputName + "\\}"; //$NON-NLS-1$ //$NON-NLS-2$
      query = query.replaceAll(param, paramValue);
    }
    return query;
  }

}
