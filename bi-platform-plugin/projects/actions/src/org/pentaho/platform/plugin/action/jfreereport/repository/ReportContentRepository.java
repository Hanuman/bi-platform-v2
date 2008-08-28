package org.pentaho.platform.plugin.action.jfreereport.repository;

import org.jfree.repository.ContentIOException;
import org.jfree.repository.ContentLocation;
import org.jfree.repository.DefaultMimeRegistry;
import org.jfree.repository.MimeRegistry;
import org.jfree.repository.Repository;
import org.pentaho.platform.api.repository.IContentLocation;

/**
 * Creation-Date: 05.07.2007, 14:43:40
 *
 * @author Thomas Morgner
 */
public class ReportContentRepository implements Repository {
  private DefaultMimeRegistry mimeRegistry;

  private ReportContentLocation root;

  public ReportContentRepository(final IContentLocation root, final String actionName) {
    this.root = new ReportContentLocation(root, this, actionName);
    this.mimeRegistry = new DefaultMimeRegistry();
  }

  public ContentLocation getRoot() throws ContentIOException {
    return root;
  }

  public MimeRegistry getMimeRegistry() {
    return mimeRegistry;
  }
}
