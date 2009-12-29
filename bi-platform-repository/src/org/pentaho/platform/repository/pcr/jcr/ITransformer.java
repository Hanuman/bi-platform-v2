package org.pentaho.platform.repository.pcr.jcr;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.pentaho.platform.api.repository.IRepositoryFileContent;

/**
 * A pluggable method for reading and writing {@link IRepositoryFileContent} implementations.
 * 
 * @param <T> type which this transformer reads and writes
 * @author mlowery
 */
public interface ITransformer<T extends IRepositoryFileContent> {

  /**
   * Returns {@code true} if this transformer can read and write content of the given type.
   * 
   * @param contentType content type to check 
   * @return {@code true} if this transformer can read and write content of the given type
   */
  boolean supports(final String contentType);

  /**
   * Transforms a JCR node subtree into an {@link IRepositoryFileContent}.
   * 
   * @param session JCR session
   * @param pentahoJcrConstants constants
   * @param nodeIdStrategy node id strategy to use
   * @param resourceNode root of JCR subtree containing the data that goes into the {@link IRepositoryFileContent}
   * @return an {@link IRepositoryFileContent} instance
   * @throws RepositoryException if anything goes wrong
   * @throws IOException if anything goes wrong
   */
  T fromContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final NodeIdStrategy nodeIdStrategy, final Node resourceNode) throws RepositoryException, IOException;

  /**
   * Creates a JCR node subtree representing the given {@code content}.
   * 
   * @param session JCR session
   * @param pentahoJcrConstants constants
   * @param nodeIdStrategy node id strategy to use
   * @param content content to create
   * @param resourceNode root of JCR subtree containing the data that goes into the {@link IRepositoryFileContent}
   * @return an {@link IRepositoryFileContent} instance
   * @throws RepositoryException if anything goes wrong
   * @throws IOException if anything goes wrong
   */
  void createContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final NodeIdStrategy nodeIdStrategy, final T content, final Node resourceNode) throws RepositoryException,
      IOException;

  /**
   * Updates a JCR node subtree representing the given {@code content}.
   * 
   * @param session JCR session
   * @param pentahoJcrConstants constants
   * @param nodeIdStrategy node id strategy to use
   * @param content content to update
   * @param resourceNode root of JCR subtree containing the data that goes into the {@link IRepositoryFileContent}
   * @return an {@link IRepositoryFileContent} instance
   * @throws RepositoryException if anything goes wrong
   * @throws IOException if anything goes wrong
   */
  void updateContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final NodeIdStrategy nodeIdStrategy, final T content, final Node resourceNode) throws RepositoryException,
      IOException;

}