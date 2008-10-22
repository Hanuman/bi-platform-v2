/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Apr 12, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.web.http.context;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.util.IVersionHelper;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.PentahoHttpSessionHelper;
import org.pentaho.platform.web.http.messages.Messages;

public class SolutionContextListener implements ServletContextListener {

  protected static String solutionPath;

  protected static String contextPath;

  private static final String DEFAULT_SPRING_CONFIG_FILE_NAME = "pentahoObjects.spring.xml"; //$NON-NLS-1$

  public void contextInitialized(final ServletContextEvent event) {

    ServletContext context = event.getServletContext();

    String encoding = context.getInitParameter("encoding"); //$NON-NLS-1$
    if (encoding != null) {
      LocaleHelper.setSystemEncoding(encoding);
    }

    String textDirection = context.getInitParameter("text-direction"); //$NON-NLS-1$
    if (textDirection != null) {
      LocaleHelper.setTextDirection(textDirection);
    }

    String localeLanguage = context.getInitParameter("locale-language"); //$NON-NLS-1$
    String localeCountry = context.getInitParameter("locale-country"); //$NON-NLS-1$
    boolean localeSet = false;
    if ((localeLanguage != null) && !"".equals(localeLanguage) && (localeCountry != null) && !"".equals(localeCountry)) { //$NON-NLS-1$ //$NON-NLS-2$
      Locale locales[] = Locale.getAvailableLocales();
      if (locales != null) {
        for (Locale element : locales) {
          if (element.getLanguage().equals(localeLanguage) && element.getCountry().equals(localeCountry)) {
            LocaleHelper.setLocale(element);
            localeSet = true;
            break;
          }
        }
      }
    }
    if (!localeSet) {
      // do this thread in the default locale
      LocaleHelper.setLocale(Locale.getDefault());
    }
    LocaleHelper.setDefaultLocale(LocaleHelper.getLocale());
    // log everything that goes on here
    Logger.info(SolutionContextListener.class.getName(), Messages
        .getString("SolutionContextListener.INFO_INITIALIZING")); //$NON-NLS-1$
    Logger.info(SolutionContextListener.class.getName(), Messages
        .getString("SolutionContextListener.INFO_SERVLET_CONTEXT") + context); //$NON-NLS-1$
    SolutionContextListener.contextPath = context.getRealPath(""); //$NON-NLS-1$
    Logger.info(SolutionContextListener.class.getName(), Messages
        .getString("SolutionContextListener.INFO_CONTEXT_PATH") + SolutionContextListener.contextPath); //$NON-NLS-1$

    SolutionContextListener.solutionPath = PentahoHttpSessionHelper.getSolutionPath(context);
    if (StringUtils.isEmpty(SolutionContextListener.solutionPath)) {
      String errorMsg = Messages.getErrorString("SolutionContextListener.ERROR_0001_NO_ROOT_PATH"); //$NON-NLS-1$
      Logger.error(getClass().getName(), errorMsg);
      /*
       * Since we couldn't find solution repository path there is no point in going 
       * forward and the user should know that a major config setting was not found.
       * So we are throwing in a RunTimeException with the requisite message.
       */
      throw new RuntimeException(errorMsg);
    }

    Logger.info(getClass().getName(),
        Messages.getString("SolutionContextListener.INFO_ROOT_PATH") + SolutionContextListener.solutionPath); //$NON-NLS-1$

    // TODO: derive the base URL from somewhere
    String baseUrl = context.getInitParameter("base-url"); //$NON-NLS-1$
    if (baseUrl == null) {
      // assume this is a demo installation
      // TODO: Create a servlet that's loaded on startup to set this value
      baseUrl = "http://localhost:8080/pentaho/"; //$NON-NLS-1$
    }
    IApplicationContext applicationContext = new WebApplicationContext(SolutionContextListener.solutionPath, baseUrl,
        context.getRealPath(""), context); //$NON-NLS-1$

    /*
     * Copy out all the initParameter values from the servlet context and
     * put them in the application context.
     */
    Properties props = new Properties();
    Enumeration<?> initParmNames = context.getInitParameterNames();
    String initParmName;
    while (initParmNames.hasMoreElements()) {
      initParmName = (String) initParmNames.nextElement();
      props.setProperty(initParmName, context.getInitParameter(initParmName));
    }
    ((WebApplicationContext) applicationContext).setProperties(props);

    setSystemCfgFile(context);
    setObjectFactory(context);

    boolean initOk = PentahoSystem.init(applicationContext);

    this.showInitializationMessage(initOk, baseUrl);
  }

  private void setObjectFactory(final ServletContext context) {

    final String SYSTEM_FOLDER = "/system"; //$NON-NLS-1$
    String pentahoObjectFactoryClassName = context.getInitParameter("pentahoObjectFactory"); //$NON-NLS-1$
    String pentahoObjectFactoryConfigFile = context.getInitParameter("pentahoObjectFactoryCfgFile"); //$NON-NLS-1$

    // if web.xml doesnt specify a config file, use the default path.
    if (StringUtils.isEmpty(pentahoObjectFactoryConfigFile)) {
      pentahoObjectFactoryConfigFile = solutionPath + SYSTEM_FOLDER + "/" + DEFAULT_SPRING_CONFIG_FILE_NAME; //$NON-NLS-1$
    } else if (-1 == pentahoObjectFactoryConfigFile.indexOf("/")) { //$NON-NLS-1$
      pentahoObjectFactoryConfigFile = solutionPath + SYSTEM_FOLDER + "/" + pentahoObjectFactoryConfigFile; //$NON-NLS-1$
    }
    // else objectFactoryCreatorCfgFile contains the full path.
    IPentahoObjectFactory pentahoObjectFactory;
    try {
      Class<?> classObject = Class.forName(pentahoObjectFactoryClassName);
      pentahoObjectFactory = (IPentahoObjectFactory) classObject.newInstance();
      pentahoObjectFactory.init(pentahoObjectFactoryConfigFile, context);
      PentahoSystem.setObjectFactory(pentahoObjectFactory);
    } catch (Exception e) {
      Logger.fatal(this, Messages.getString("SolutionContextListener.ERROR_BAD_OBJECT_FACTORY", pentahoObjectFactoryClassName), e); //$NON-NLS-1$
      // Cannot proceed without an object factory, so we'll throw a runtime exception
      throw new RuntimeException(e);
    }
  }

  /**
   * Look for a parameter called "pentaho-system-cfg". If found, use its value to set the
   * the value of the System property "SYSTEM_CFG_PATH_KEY". This value is used by a
   * LiberatedSystemSettings class to determine the location of the system configuration file.
   * This is typically pentaho.xml.
   * 
   * @param context ServletContext
   */
  private void setSystemCfgFile(final ServletContext context) {
    String jvmSpecifiedSysCfgPath = System.getProperty(PathBasedSystemSettings.SYSTEM_CFG_PATH_KEY);
    if (StringUtils.isBlank(jvmSpecifiedSysCfgPath)) {
      String webSpecifiedSysCfgPath = context.getInitParameter("pentaho-system-cfg"); //$NON-NLS-1$
      if (StringUtils.isNotBlank(webSpecifiedSysCfgPath)) {
        System.setProperty(PathBasedSystemSettings.SYSTEM_CFG_PATH_KEY, webSpecifiedSysCfgPath);
      }
    }
    // if it is blank, no big deal, we'll simply fall back on defaults
  }

  public void showInitializationMessage(final boolean initOk, final String baseUrl) {
    IVersionHelper helper = PentahoSystem.get(IVersionHelper.class, null); // No session yet
    if (initOk) {
      System.out
          .println(Messages
              .getString(
                  "SolutionContextListener.INFO_SYSTEM_READY", "(" + helper.getVersionInformation(PentahoSystem.class) + ")", baseUrl, SolutionContextListener.solutionPath)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } else {
      System.err
          .println(Messages
              .getString(
                  "SolutionContextListener.INFO_SYSTEM_NOT_READY", "(" + helper.getVersionInformation(PentahoSystem.class) + ")", baseUrl, SolutionContextListener.solutionPath)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
  }

  protected String getContextPath() {
    return SolutionContextListener.contextPath;
  }

  protected String getRootPath() {
    return SolutionContextListener.solutionPath;
  }

  public void contextDestroyed(final ServletContextEvent event) {

    PentahoSystem.shutdown();
    if (LocaleHelper.getLocale() == null) {
      LocaleHelper.setLocale(Locale.getDefault());
    }
    // log everything that goes on here
    Logger.info(SolutionContextListener.class.getName(), Messages
        .getString("SolutionContextListener.INFO_SYSTEM_EXITING")); //$NON-NLS-1$
  }
}
