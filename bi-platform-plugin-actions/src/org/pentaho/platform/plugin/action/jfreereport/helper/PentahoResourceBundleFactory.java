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
 *
 * @created Jan 23, 2006 
 * @author James Dixon
 */
package org.pentaho.platform.plugin.action.jfreereport.helper;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.reporting.engine.classic.core.ResourceBundleFactory;

/**
 * The resource-bundle factory is responsible for loading referenced resourcebundles. The default action is to load these bundles using the standard JDK methods. If no bundle-name is given, the default name for the xaction's assigned resource-bundle is used instead.
 */
public class PentahoResourceBundleFactory implements ResourceBundleFactory {
  private static final long serialVersionUID = -1555502100120929073L;

  private String path;

  private String baseName;

  private ClassLoader loader;

  public PentahoResourceBundleFactory(final String inPath, final String inBaseName, final IPentahoSession inSession) {
    path = inPath;
    baseName = inBaseName;
    loader = PentahoSystem.get(ISolutionRepository.class, inSession).getClassLoader(path);
  }

  public Locale getLocale() {
    return LocaleHelper.getLocale();
  }

  public ResourceBundle getResourceBundle(String resourceName) {
    if (resourceName == null) {
      resourceName = baseName;
    }
    try {
      return ResourceBundle.getBundle(resourceName, getLocale(), loader);
    } catch (Exception e) {
      Logger.error(getClass().getName(), Messages.getErrorString(
          "JFreeReport.ERROR_0024_COULD_NOT_READ_PROPERTIES", path + File.separator + baseName), e); //$NON-NLS-1$
    }
    return null;
  }
  
	public TimeZone getTimeZone() {
	  return TimeZone.getDefault();
	}
  
}
