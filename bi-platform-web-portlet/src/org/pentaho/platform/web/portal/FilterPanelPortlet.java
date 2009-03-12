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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created Aug 24, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.web.portal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.uifoundation.component.FilterDefinition;
import org.pentaho.platform.uifoundation.component.xml.FilterPanelComponent;
import org.pentaho.platform.util.xml.XmlHelper;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.portal.messages.Messages;

public class FilterPanelPortlet extends ViewPortlet {

  private static final String FILTERS = "filters"; //$NON-NLS-1$

  private static final String XSLNAME = "xsl"; //$NON-NLS-1$

  private String filterPanelDefinition = null;

  private static final Log portletLogger = LogFactory.getLog(FilterPanelPortlet.class);

  @Override
  public Log getLogger() {
    return FilterPanelPortlet.portletLogger;
  }

  @Override
  public void processPortletAction(final ActionRequest request, final ActionResponse response, final PentahoPortletSession userSession)
      throws PortletException, IOException {

    // TODO get any changes in value and make it available through the
    // session
    if (filterPanelDefinition == null) {
      return;
    }
    // see if this component is cached already
    FilterPanelComponent filterPanel = (FilterPanelComponent) userSession.getAttribute(filterPanelDefinition,
        PortletSession.PORTLET_SCOPE);
    if (filterPanel == null) {
      return;
    }
    // iterate thru the list of filter objects
    List filterList = filterPanel.getFilters();
    Iterator filtersIterator = filterList.iterator();
    while (filtersIterator.hasNext()) {
      // get the filter definition
      FilterDefinition filterDefinition = (FilterDefinition) filtersIterator.next();
      String filterName = filterDefinition.getName();
      // get the new value of the filter from the requst
      String[] values = request.getParameterValues(filterName);
      XmlHelper.decode(values);

      if (values != null) {

        // see if this value is valid for the filter
        if (filterDefinition.isValid(values)) {
          filterDefinition.setDefaultValue(values);
          userSession.removeAttribute(filterName, PortletSession.APPLICATION_SCOPE);
          userSession.setAttribute(filterName, values, PortletSession.APPLICATION_SCOPE);
        } else {
          userSession.removeAttribute(filterName, PortletSession.APPLICATION_SCOPE);

        }
      }
    }
  }

  @Override
  public void doPortletView(final RenderRequest request, final RenderResponse response, final PentahoPortletSession userSession)
      throws PortletException, IOException {

    response.setContentType("text/html"); //$NON-NLS-1$

    PortletUrlFactory urlFactory = new PortletUrlFactory(response, request.getWindowState(), request.getPortletMode());

    PortletRequestParameterProvider requestParameters = new PortletRequestParameterProvider(request);
    PortletSessionParameterProvider sessionParameters = new PortletSessionParameterProvider(userSession);
    PortletPreferences prefs = request.getPreferences();
    PortletPreferencesParameterProvider portletPrefsParameters = new PortletPreferencesParameterProvider(prefs);

    filterPanelDefinition = getSetting(FilterPanelPortlet.FILTERS, null, request, requestParameters);
    String xslName = getSetting(FilterPanelPortlet.XSLNAME, null, request, requestParameters);

    if (filterPanelDefinition == null) {
      response.getWriter().print(Messages.getString("FilterPanel.ERROR_0001_NO_FILTERS")); //$NON-NLS-1$
      return;
    }

    ArrayList messages = new ArrayList();

    // see if this component is cached already
    FilterPanelComponent filterPanel = (FilterPanelComponent) userSession.getAttribute(filterPanelDefinition,
        PortletSession.PORTLET_SCOPE);

    // NOTE to developers: if you are having trouble seeing your changes to your filter panel config file while debugging, it may be because your old filter panel has been cached in the session
    if (filterPanel == null) {
      // we need to create the filter panel and add it to the session
      filterPanel = new FilterPanelComponent(filterPanelDefinition, xslName, urlFactory, messages);
      userSession.setAttribute(filterPanelDefinition, filterPanel, PortletSession.PORTLET_SCOPE);
    } else {
      filterPanel.setUrlFactory(urlFactory);
    }

    filterPanel.validate(userSession, null);
    filterPanel.init();
    List filters = filterPanel.getFilters();
    int index = 0;
    while (index < filters.size()) {
      FilterDefinition filter = (FilterDefinition) filters.get(index);
      filterPanel.setDefaultValue(filter.getName(), (String[]) userSession.getAttribute(filter.getName(),
          PortletSession.APPLICATION_SCOPE));
      index++;
    }

    filterPanel.setParameterProvider(HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters);
    filterPanel.setParameterProvider(IParameterProvider.SCOPE_SESSION, sessionParameters);
    filterPanel.setParameterProvider(PortletPreferencesParameterProvider.SCOPE_PORTLET_PREFERENCES,
        portletPrefsParameters);
    String content = filterPanel.getContent("text/html"); //$NON-NLS-1$
    filterPanel.setUrlFactory(null);
    if ((content == null) || content.equals("")) { //$NON-NLS-1$
      content = "&nbsp;"; //$NON-NLS-1$
    }

    if (content == null) {
      StringBuffer buffer = new StringBuffer();
      PentahoSystem.get(IMessageFormatter.class, userSession).formatErrorMessage(
          "text/html", Messages.getString("FilterPanelComponent.ERROR_0003_CREATE"), messages, buffer); //$NON-NLS-1$ //$NON-NLS-2$
      content = buffer.toString();
    }

    response.getWriter().print(content);
  }
}
