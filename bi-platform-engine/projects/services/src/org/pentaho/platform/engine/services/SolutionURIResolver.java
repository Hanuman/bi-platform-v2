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
 * @created Jul 13, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.engine.services;

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.pentaho.platform.api.engine.IDocumentResourceLoader;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.logging.Logger;
import org.xml.sax.InputSource;

public class SolutionURIResolver implements URIResolver, IDocumentResourceLoader {

  IPentahoSession session = null;

  ISolutionRepository repository = null;

  public SolutionURIResolver(final IPentahoSession session) {
    super();
    this.session = session;
    repository = PentahoSystem.getSolutionRepository(session);
  }

  public SolutionURIResolver(final ISolutionRepository repository) {
    super();
    this.repository = repository;
  }

  public InputSource resolveEntity(final String publicId, final String systemId) {

    if (repository != null) {
      InputStream xslIS = null;
      try {
        xslIS = repository.getResourceInputStream(systemId, true);
        return new InputSource(xslIS);
      } catch (FileNotFoundException e) {
        Logger.error(this, e.getLocalizedMessage());
      }
    }
    return null;

  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.xml.transform.URIResolver#resolve(java.lang.String,
   *      java.lang.String)
   */
  public Source resolve(final String href, final String base) {
    StreamSource xslSrc = null;
    if (repository != null) {
      InputStream xslIS = null;
      try {
        xslIS = repository.getResourceInputStream(href, true);
      } catch (FileNotFoundException e) {
        Logger.error(this, e.getLocalizedMessage());
        return null;
      }
      xslSrc = new StreamSource(xslIS);
    }

    return xslSrc;
  }

  public InputStream loadXsl(final String name) {
    InputStream xslIS = null;
    if (repository != null) {
      try {
        xslIS = repository.getResourceInputStream(name, true);
      } catch (FileNotFoundException e) {
        Logger.error(this, e.getLocalizedMessage());
        return null;
      }
    }

    return xslIS;
  }

}
