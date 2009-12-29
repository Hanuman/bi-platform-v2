package org.pentaho.platform.repository.pcr.jcr;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.api.repository.RepositoryFileSid;

/**
 * Provides a pluggable way to lookup the owner of a {@link RepositoryFile}. Typically, the owner of a file is stored
 * with the access control information for the file. This is because the owner can affect access control decisions. But
 * it's also a nice piece of metadata to store with the {@code RepositoryFile}. So implementations of this interface 
 * know how to fetch the owner from wherever it resides and convert it to the required {@link RepositoryFileSid}.
 * 
 * @author mlowery
 */
public interface IOwnerLookupHelper {
  RepositoryFileSid getOwner(final Session session, final PentahoJcrConstants pentahoJcrConstants, final Node node)
      throws RepositoryException;
}