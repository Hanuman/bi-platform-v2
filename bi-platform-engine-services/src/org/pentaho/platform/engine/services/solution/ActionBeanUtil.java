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
 * Copyright 2009 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.engine.services.solution;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.pentaho.platform.api.action.IAction;

/**
 * Utility methods for processing {@link IAction} beans.  The methods in this class
 * are not static on purpose.  We want to have to be able to hook up an alternate implementation
 * of this utility and that would be a pain to do if we were statically invoking methods.
 * @author aphillips
 */
public class ActionBeanUtil  {
  private static PropertyUtilsBean propUtil = new PropertyUtilsBean();

  private static BeanUtilsBean typeConvertingBeanUtil;

  {
    //
    //Configure a bean util that throws exceptions during type conversion
    //
    ConvertUtilsBean convertUtil = new ConvertUtilsBean();
    convertUtil.register(true, true, 0);

    typeConvertingBeanUtil = new BeanUtilsBean(convertUtil);
  }

  public boolean isReadable(Object bean, String name) {
    return propUtil.isReadable(bean, name);
  }

  public Object getValue(Object bean, String name) throws IllegalAccessException, InvocationTargetException,
      NoSuchMethodException {
    return propUtil.getSimpleProperty(bean, name);
  }

  public Class<?> getClass(Object bean, String name) throws IllegalAccessException, InvocationTargetException,
      NoSuchMethodException {
    PropertyDescriptor desc = propUtil.getPropertyDescriptor(bean, name);
    return desc.getPropertyType();
  }

  public boolean isWriteable(Object actionBean, String name) {
    return propUtil.isWriteable(actionBean, name)
        || (propUtil.getResolver().isIndexed(name) && propUtil.isReadable(actionBean, name));
  }

  public void setValue(Object bean, String name, Object value) throws IllegalAccessException, InvocationTargetException {
    typeConvertingBeanUtil.copyProperty(bean, name, value);
  }

}
