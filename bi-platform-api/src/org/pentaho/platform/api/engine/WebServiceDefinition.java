/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2009 Pentaho Corporation.  All rights reserved.
 *
 * Created May 1, 2009
 * @author aphillips
 */
 
package org.pentaho.platform.api.engine;

import java.util.Collection;

public class WebServiceDefinition {
  
  private Class<?> serviceClass;
  private Collection<Class<?>> extraClasses;
  private String title, description;
  private boolean enabled = true;
  
  /**
   * Returns the name of this web service. This should not be localized
   * @return Name
   */
  public String getName() {
    return getServiceClass().getSimpleName();
  }
  
  /**
   * Returns the enabled state of this service
   * @return Current enable/disable state
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Sets the enabled state of this service
   * @param enabled
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
  
  public Collection<Class<?>> getExtraClasses() {
    return extraClasses;
  }
  
  /**
   * Returns the web service bean class
   * @return bean class or id by which the class can be looked up
   */
  public Class<?> getServiceClass() {
    return serviceClass;
  }


  public void setServiceClass(Class<?> serviceClass) {
    this.serviceClass = serviceClass;
  }

  public void setExtraClasses(Collection<Class<?>> extraClasses) {
    this.extraClasses = extraClasses;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns the localized title for this web service. This is shown on the 
   * services list page.
   * @return Description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the localized title for this web service. This is shown on the 
   * services list page.
   * @return Title
   */
  public String getTitle() {
    return title;
  }

}
