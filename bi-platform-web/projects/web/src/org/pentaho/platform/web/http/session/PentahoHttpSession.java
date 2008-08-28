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
 * @created Jul 11, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.web.http.session;

import java.util.Iterator;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.solution.PentahoSessionParameterProvider;
import org.pentaho.platform.engine.core.system.BaseSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class PentahoHttpSession extends BaseSession {

  private static final long serialVersionUID = 1500696455420691764L;

  private HttpSession session;

  private static final Log logger = LogFactory.getLog(PentahoHttpSession.class);

  @Override
  public Log getLogger() {
    return PentahoHttpSession.logger;
  }

  public PentahoHttpSession(final String userName, final HttpSession session, final Locale locale, final IPentahoSession userSession) {
    super(userName, session.getId(), locale);

    this.session = session;

    // run any session initialization actions
    IParameterProvider sessionParameters = new PentahoSessionParameterProvider(userSession);
    PentahoSystem.sessionStartup(this, sessionParameters);
  }

  public Iterator getAttributeNames() {

    return new EnumerationIterator(session.getAttributeNames());
  }

  public Object getAttribute(final String attributeName) {
    return session.getAttribute(attributeName);
  }

  public void setAttribute(final String attributeName, final Object value) {
    session.setAttribute(attributeName, value);
  }

  public Object removeAttribute(final String attributeName) {
    Object result = getAttribute(attributeName);
    session.removeAttribute(attributeName);
    return result;
  }
}
