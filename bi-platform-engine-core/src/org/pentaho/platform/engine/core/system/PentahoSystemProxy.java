/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
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
 * @created June 27, 2008 
 * @author Angelo Rodriguez
 * 
 */

package org.pentaho.platform.engine.core.system;

import java.util.List;

import org.pentaho.platform.api.engine.IPentahoPublisher;
import org.pentaho.platform.api.engine.IPentahoSystem;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.ISessionStartupAction;
import org.pentaho.platform.api.engine.ISystemSettings;

/**
 * TODO: 
 * Since we have moved to a service locator pattern with PentahoSystem being the static root reference,
 * we do not support other implementations of PentahoSystem.  A better approach is to inject plugins
 * listeners and actions locators into PentahoSystem.
 */
public class PentahoSystemProxy implements IPentahoSystem {
  
  public void setAdministrationPlugins(List<IPentahoPublisher> administrationPlugins) {
    PentahoSystem.setAdministrationPlugins(administrationPlugins);
  }

  public void setSystemListeners(List<IPentahoSystemListener> systemListeners) {
    PentahoSystem.setSystemListeners(systemListeners);
  }

  public void setSessionStartupActions(List<ISessionStartupAction> registries) {
    PentahoSystem.setSessionStartupActions(registries);
  }
  
  public void setSystemSettingsService(ISystemSettings systemSettingsService) {
    PentahoSystem.setSystemSettingsService(systemSettingsService);
  }
}
