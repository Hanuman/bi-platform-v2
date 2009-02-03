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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.server.reporting;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.reporting.libraries.resourceloader.FactoryParameterKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceData;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceLoadingException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.libraries.resourceloader.loader.AbstractResourceData;

/**
 * This class is implemented to support loading solution files from the pentaho repository into JFreeReport
 * 
 * @author Will Gorman/Michael D'Amour
 */
public class MantleRepositoryResourceData extends AbstractResourceData {

  public static final String PENTAHO_REPOSITORY_KEY = "pentahoRepositoryKey"; //$NON-NLS-1$

  private String filename;

  private ResourceKey key;

  private ISolutionRepository solutionRepository;

  /**
   * constructor which takes a resource key for data loading specifics
   * 
   * @param key
   *          resource key
   */
  public MantleRepositoryResourceData(final ResourceKey key) {
    if (key == null) {
      throw new NullPointerException();
    }

    this.key = key;
    this.filename = (String) key.getIdentifier();
    try {
      this.solutionRepository = (ISolutionRepository) key.getFactoryParameters().get(new FactoryParameterKey(PENTAHO_REPOSITORY_KEY));
    } catch (Throwable t) {
      t.printStackTrace();
    }
    if (solutionRepository == null) {
      throw new InstantiationError("MantleRepositoryResourceData: Failed to solution repository from resource key"); //$NON-NLS-1$
    }
  }

  /**
   * gets a resource stream from the runtime context.
   * 
   * @param caller
   *          resource manager
   * @return input stream
   */
  public InputStream getResourceAsStream(ResourceManager caller) throws ResourceLoadingException {
    try {
      return solutionRepository.getResourceInputStream(key.getIdentifier().toString(), false);
    } catch (FileNotFoundException e) {
      throw new ResourceLoadingException(e.getLocalizedMessage(), e);
    }
  }

  /**
   * returns a requested attribute, currently only supporting filename.
   * 
   * @param key
   *          attribute requested
   * @return attribute value
   */
  public Object getAttribute(String lookupKey) {
    if (lookupKey.equals(ResourceData.FILENAME)) {
      return filename;
    }
    return null;
  }

  /**
   * return the version number
   * 
   * @param caller
   *          resource manager
   * 
   * @return version
   */
  public long getVersion(ResourceManager caller) throws ResourceLoadingException {
    return solutionRepository.getFileByPath(key.getIdentifier().toString()).getLastModified();
  }

  /**
   * get the resource key
   * 
   * @return resource key
   */
  public ResourceKey getKey() {
    return key;
  }
}
