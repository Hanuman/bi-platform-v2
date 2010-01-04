package org.pentaho.platform.repository.pcr.jcr;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.pentaho.platform.api.repository.IRepositoryFileData;

/**
 * A pluggable method for reading and writing {@link IRepositoryFileData} implementations.
 * 
 * @param <T> type which this transformer reads and writes
 * @author mlowery
 */
public interface ITransformer<T extends IRepositoryFileData> {

  /**
   * Returns {@code true} if this transformer can read and write data for files with the given extension and return the
   * data in the format indicated.
   * 
   * @param extension extension type to check 
   * @return {@code true} if this transformer supports this extension and class
   */
  boolean supports(final String extension, final Class<? extends IRepositoryFileData> clazz);

  /**
   * Transforms a JCR node subtree into an {@link IRepositoryFileData}.
   * 
   * @param session JCR session
   * @param pentahoJcrConstants constants
   * @param nodeIdStrategy node id strategy to use
   * @param fileNode node of type pho_nt:pentahoFile or pho_nt:pentahoLinkedFile
   * @return an {@link IRepositoryFileData} instance
   * @throws RepositoryException if anything goes wrong
   * @throws IOException if anything goes wrong
   */
  T fromContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final NodeIdStrategy nodeIdStrategy, final Node fileNode) throws RepositoryException, IOException;

  /**
   * Creates a JCR node subtree representing the given {@code content}.
   * 
   * @param session JCR session
   * @param pentahoJcrConstants constants
   * @param nodeIdStrategy node id strategy to use
   * @param data data to create
   * @param fileNode node of type pho_nt:pentahoFile or pho_nt:pentahoLinkedFile
   * @return an {@link IRepositoryFileData} instance
   * @throws RepositoryException if anything goes wrong
   * @throws IOException if anything goes wrong
   */
  void createContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final NodeIdStrategy nodeIdStrategy, final T data, final Node fileNode) throws RepositoryException,
      IOException;

  /**
   * Updates a JCR node subtree representing the given {@code content}.
   * 
   * @param session JCR session
   * @param pentahoJcrConstants constants
   * @param nodeIdStrategy node id strategy to use
   * @param data data to update
   * @param fileNode node of type pho_nt:pentahoFile or pho_nt:pentahoLinkedFile
   * @return an {@link IRepositoryFileData} instance
   * @throws RepositoryException if anything goes wrong
   * @throws IOException if anything goes wrong
   */
  void updateContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final NodeIdStrategy nodeIdStrategy, final T data, final Node fileNode) throws RepositoryException,
      IOException;

}