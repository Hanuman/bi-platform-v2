package org.pentaho.test.platform.web.doubles;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSystemEntryPoint;
import org.pentaho.platform.api.engine.IPentahoSystemExitPoint;

public class ApplicationContextDouble implements IApplicationContext {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(ApplicationContextDouble.class);

  // ~ Instance fields =================================================================================================

  private String baseUrl;

  private Set<IPentahoSystemEntryPoint> entryPoints = new HashSet<IPentahoSystemEntryPoint>();

  private Set<IPentahoSystemExitPoint> exitPoints = new HashSet<IPentahoSystemExitPoint>();

  private String solutionRootPath;

  private Object context;

  // ~ Constructors ====================================================================================================

  public ApplicationContextDouble() {
    super();
    if (logger.isDebugEnabled()) {
      logger.debug("looking for info as system properties");
    }
    solutionRootPath = System.getProperty("org.pentaho.doubles.ApplicationContext.solutionRootPath");
    baseUrl = System.getProperty("org.pentaho.doubles.ApplicationContext.baseUrl");
    if (logger.isDebugEnabled()) {
      logger.debug("solutionRootPath=" + solutionRootPath);
      logger.debug("baseUrl=" + baseUrl);
    }
    if (StringUtils.isBlank(solutionRootPath) || StringUtils.isBlank(baseUrl)) {
      throw new IllegalArgumentException("missing required system properties");
    }
  }

  // ~ Methods =========================================================================================================

  public void addEntryPointHandler(IPentahoSystemEntryPoint entryPoint) {
    entryPoints.add(entryPoint);
  }

  public void addExitPointHandler(IPentahoSystemExitPoint exitPoint) {
    exitPoints.add(exitPoint);
  }

  public String getApplicationPath(String arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public Object getContext() {
    return context;
  }

  public String getFileOutputPath(String arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getPentahoServerName() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getProperty(String arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getProperty(String arg0, String arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getSolutionPath(String path) {
    return solutionRootPath + "/" + path; //$NON-NLS-1$
  }

  public String getSolutionRootPath() {
    return solutionRootPath;
  }

  public void invokeEntryPoints() {
    // TODO Auto-generated method stub

  }

  public void invokeExitPoints() {
    // TODO Auto-generated method stub

  }

  public void removeEntryPointHandler(IPentahoSystemEntryPoint entryPoint) {
    entryPoints.remove(entryPoint);
  }

  public void removeExitPointHandler(IPentahoSystemExitPoint exitPoint) {
    exitPoints.remove(exitPoint);
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public void setContext(Object context) {
    this.context = context;
  }

  public void setSolutionRootPath(String solutionRootPath) {
    this.solutionRootPath = solutionRootPath;

  }

}
