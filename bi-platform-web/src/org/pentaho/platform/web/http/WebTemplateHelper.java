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
 * @created Jul 22, 2005 
 * @author James Dixon
 *
 */

package org.pentaho.platform.web.http;

import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUITemplater;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.ui.IMenuProvider;
import org.pentaho.platform.api.util.IVersionHelper;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.actionsequence.ActionResource;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.messages.Messages;


public class WebTemplateHelper implements IUITemplater {
  private static String footerTemplate = null;

  private static final String FOOTER_TEMPLATE_FILENAME = "template-footer.html"; //$NON-NLS-1$

  private String headerContent = "";
  
  public void setHeaderContent(String headerContent) {
	this.headerContent = headerContent;
}

public String processTemplate(String template, final String title, final String content, final IPentahoSession session) {

    template = processTemplate(template, title, session);
    template = template.replaceFirst("\\{content\\}", content); //$NON-NLS-1$

    return template;
  }

  /*
   * TODO: This needs to be architected to be more performant
   */
  public String processTemplate(String template, final String title, final IPentahoSession session) {

    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, session);
    if (WebTemplateHelper.footerTemplate == null) {
      WebTemplateHelper.footerTemplate = getTemplate(WebTemplateHelper.FOOTER_TEMPLATE_FILENAME, session);
    }
    template = template.replaceAll("\\{menu\\}", getMenuHtml( session ) ); //$NON-NLS-1$
    template = template.replaceFirst("\\{footer\\}", WebTemplateHelper.footerTemplate); //$NON-NLS-1$
    template = template.replaceAll("\\{title\\}", title); //$NON-NLS-1$
    template = template.replaceAll("\\{home\\}", Messages.getString("UI.USER_HOME")); //$NON-NLS-1$ //$NON-NLS-2$
    template = template.replaceAll("\\{navigate\\}", Messages.getString("UI.USER_NAVIGATE")); //$NON-NLS-1$ //$NON-NLS-2$
    template = template.replaceAll("\\{solutions\\}", Messages.getString("UI.USER_SOLUTIONS")); //$NON-NLS-1$ //$NON-NLS-2$
    template = template.replaceAll("\\{file-cache\\}", Messages.getString("UI.USER_FILE_CACHE")); //$NON-NLS-1$ //$NON-NLS-2$
    template = template.replaceAll("\\{new-content\\}", Messages.getString("UI.USER_NEW_CONTENT")); //$NON-NLS-1$ //$NON-NLS-2$

    template = template.replaceAll("\\{file\\}", Messages.getString("UI.USER_FILE")); //$NON-NLS-1$ //$NON-NLS-2$
    template = template.replaceAll("\\{view\\}", Messages.getString("UI.USER_VIEW")); //$NON-NLS-1$ //$NON-NLS-2$
    template = template.replaceAll("\\{nightly\\}", Messages.getString("UI.USER_NIGHTLY")); //$NON-NLS-1$ //$NON-NLS-2$
    template = template.replaceAll("\\{tracker\\}", Messages.getString("UI.USER_TRACKER")); //$NON-NLS-1$ //$NON-NLS-2$
    template = template.replaceAll("\\{new-report\\}", Messages.getString("UI.USER_NEW_REPORT")); //$NON-NLS-1$ //$NON-NLS-2$
    template = template.replaceAll("\\{new-view", Messages.getString("UI.USER_NEW_PIVOT")); //$NON-NLS-1$ //$NON-NLS-2$
    template = template.replaceAll("\\{admin\\}", Messages.getString("UI.USER_ADMIN")); //$NON-NLS-1$ //$NON-NLS-2$
    template = template.replaceAll("\\{about\\}", Messages.getString("UI.USER_ABOUT")); //$NON-NLS-1$ //$NON-NLS-2$
    template = template.replaceAll("\\{pentaho.org\\}", Messages.getString("UI.USER_PENTAHO.COM")); //$NON-NLS-1$ //$NON-NLS-2$
    template = template.replaceAll("\\{close\\}", Messages.getString("UI.USER_CLOSE")); //$NON-NLS-1$ //$NON-NLS-2$
    template = template.replaceAll("\\{forums\\}", Messages.getString("UI.USER_FORUMS")); //$NON-NLS-1$ //$NON-NLS-2$
    template = template.replaceAll("\\{demos\\}", Messages.getString("UI.USER_DEMOS")); //$NON-NLS-1$ //$NON-NLS-2$
    template = template.replaceAll("\\{downloads\\}", Messages.getString("UI.USER_DOWNLOADS")); //$NON-NLS-1$ //$NON-NLS-2$
    template = template
        .replaceAll("text/html; charset=utf-8", "text/html; charset=" + LocaleHelper.getSystemEncoding()); //$NON-NLS-1$//$NON-NLS-2$ 
    template = template.replaceAll("\\{text-direction\\}", LocaleHelper.getTextDirection()); //$NON-NLS-1$
    template = template.replaceAll("\\{logout\\}", Messages.getString("UI.USER_LOGOUT")); //$NON-NLS-1$ //$NON-NLS-2$
    template = template.replaceAll("\\{portal\\}", Messages.getString("UI.USER_PORTAL")); //$NON-NLS-1$ //$NON-NLS-2$
    if( PentahoSystem.getObjectFactory().objectDefined( IVersionHelper.class.getSimpleName() ) ) {
      IVersionHelper versionHelper = PentahoSystem.get(IVersionHelper.class, session);
      template = template.replaceAll("\\{version\\}", "Version: " + versionHelper.getVersionInformation(PentahoSystem.class)); //$NON-NLS-1$ //$NON-NLS-2$
    }
    template = template.replaceAll("\\{system\\}", PentahoSystem.getSystemName()); //$NON-NLS-1$
    template = template.replaceAll("\\{isLoggedIn\\}", session.isAuthenticated() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    template = template.replaceAll("\\{background-alert\\}", session.getBackgroundExecutionAlert() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    template = template.replaceAll("\\{header-content\\}", headerContent); //$NON-NLS-1$ //$NON-NLS-2$
    template = template.replaceAll("\\{body-tag\\}", ""); //$NON-NLS-1$ //$NON-NLS-2$
    template = template.replaceAll("\\{isAdmin\\}", SecurityHelper.isPentahoAdministrator(session) ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    template = template.replaceAll("\\{repository-name\\}", repository.getRepositoryName() ); //$NON-NLS-1$
    template = template.replaceAll("\\{copyright\\}", Messages.getString("UI.USER_COPYRIGHT")); //$NON-NLS-1$ //$NON-NLS-2$

    return template;
  }

  public String getTemplate(final String templateName, final IPentahoSession session) {

    IActionSequenceResource resource = new ActionResource(
        "", IActionSequenceResource.SOLUTION_FILE_RESOURCE, "text/xml", "system/custom/" + templateName); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    String template = null;
    try {
      template = PentahoSystem.get(ISolutionRepository.class, session).getResourceAsString(resource, ISolutionRepository.ACTION_EXECUTE);
    } catch (Throwable t) {
    }

    if (template == null) {
      return Messages.getString("UI.ERROR_0001_BAD_TEMPLATE", resource.getAddress()); //$NON-NLS-1$
    } else {
      return template;
    }
  }

  public String[] breakTemplate(final String templateName, final String title, final IPentahoSession session) {
    String template = getTemplate(templateName, session);
    return breakTemplateString(template, title, session);
  }

  public String[] breakTemplateString(String template, final String title, final IPentahoSession session) {
    String token = "{content}"; //$NON-NLS-1$
    template = processTemplate(template, title, session);
    int index = template.indexOf(token);
    if (index == -1) {
      return new String[] { template };
    } else {
      String sections[] = new String[2];
      sections[0] = template.substring(0, index);
      sections[1] = template.substring(index + token.length());
      return sections;
        }
      }

  public String getMenuHtml( final IPentahoSession session ) {
    
    IMenuProvider menuProvider = (IMenuProvider) PentahoSystem.get(IMenuProvider.class, session);
    if( menuProvider != null ) {
      return menuProvider.getMenuBar("menu", "system/ui/menubar.xul", session).toString(); //$NON-NLS-1$ //$NON-NLS-2$
    }
    Logger.error( WebTemplateHelper.class.getName(), Messages.getString("WebTemplateHelper.ERROR_0001_COULD_NOT_CREATE_MENUBAR") ); //$NON-NLS-1$
    return ""; //$NON-NLS-1$
  }
}
