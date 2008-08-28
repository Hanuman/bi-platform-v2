/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved.
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
package org.pentaho.platform.web.portal;

import java.util.Map;

import javax.portlet.PortletPreferences;

import org.pentaho.platform.api.engine.IParameterSetter;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;

/**
 * Parameter Provider whose parameter data comes from the Portlet Preferences.
 * 
 * Note: JSR 168 specifices that portlet preferences are defined in the
 * WEB-INF/portlet.xml file.
 * 
 * @author Steven Barkdull
 * 
 */
public class PortletPreferencesParameterProvider extends SimpleParameterProvider implements IParameterSetter {

  public static final String SCOPE_PORTLET_PREFERENCES = "portletPreferences"; //$NON-NLS-1$

  private PortletPreferences portletPrefs;

  public PortletPreferencesParameterProvider(final PortletPreferences portletPrefs) {
    this.portletPrefs = portletPrefs;
    Map portletPrefsParams = portletPrefs.getMap();
    copyAndConvertParameters(portletPrefsParams);
    copyAndConvertAdditionalParameters(portletPrefsParams);
  }

  public PortletPreferences getPorteletPreferences() {
    return portletPrefs;
  }
}
