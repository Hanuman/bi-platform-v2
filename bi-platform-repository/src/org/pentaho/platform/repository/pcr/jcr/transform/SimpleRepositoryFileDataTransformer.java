package org.pentaho.platform.repository.pcr.jcr.transform;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.pentaho.platform.api.repository.IRepositoryFileData;
import org.pentaho.platform.repository.pcr.SimpleRepositoryFileData;
import org.pentaho.platform.repository.pcr.jcr.ITransformer;
import org.pentaho.platform.repository.pcr.jcr.NodeIdStrategy;
import org.pentaho.platform.repository.pcr.jcr.PentahoJcrConstants;
import org.springframework.util.StringUtils;

/**
 * An {@link ITransformer} that can read and write {@code nt:resource} nodes. The {@link #supports(String)} method in 
 * this implementation always returns {@code true}. For this reason, it is most typically the last transformer in the 
 * transformer list.
 * 
 * @author mlowery
 */
public class SimpleRepositoryFileDataTransformer implements ITransformer<SimpleRepositoryFileData> {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  public SimpleRepositoryFileDataTransformer() {
    super();
  }

  // ~ Methods =========================================================================================================

  /**
   * {@inheritDoc}
   */
  public boolean supports(final String extension, final Class<? extends IRepositoryFileData> clazz) {
    return clazz.isAssignableFrom(SimpleRepositoryFileData.class);
  }
  
  /**
   * {@inheritDoc}
   */
  public void createContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final NodeIdStrategy nodeIdStrategy, final SimpleRepositoryFileData data, final Node fileNode)
      throws RepositoryException, IOException {

    Node resourceNode = fileNode.addNode(pentahoJcrConstants.getJCR_CONTENT(), pentahoJcrConstants.getNT_RESOURCE());

    // mandatory property on nt:resource; give them a value to satisfy Jackrabbit
    resourceNode.setProperty(pentahoJcrConstants.getJCR_LASTMODIFIED(), fileNode.getProperty(
        pentahoJcrConstants.getJCR_CREATED()).getDate());

    if (StringUtils.hasText(data.getEncoding())) {
      resourceNode.setProperty(pentahoJcrConstants.getJCR_ENCODING(), data.getEncoding());
    }
    resourceNode.setProperty(pentahoJcrConstants.getJCR_DATA(), data.getStream());
    resourceNode.setProperty(pentahoJcrConstants.getJCR_MIMETYPE(), data.getMimeType());
  }

  /**
   * {@inheritDoc}
   */
  public SimpleRepositoryFileData fromContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final NodeIdStrategy nodeIdStrategy, final Node fileNode) throws RepositoryException, IOException {
    String encoding = null;
    Node resourceNode = fileNode.getNode(pentahoJcrConstants.getJCR_CONTENT());
    if (resourceNode.hasProperty(pentahoJcrConstants.getJCR_ENCODING())) {
      encoding = resourceNode.getProperty(pentahoJcrConstants.getJCR_ENCODING()).getString();
    }
    InputStream data = resourceNode.getProperty(pentahoJcrConstants.getJCR_DATA()).getStream();
    String mimeType = resourceNode.getProperty(pentahoJcrConstants.getJCR_MIMETYPE()).getString();
    return new SimpleRepositoryFileData(data, encoding, mimeType);
  }

  /**
   * {@inheritDoc}
   */
  public void updateContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final NodeIdStrategy nodeIdStrategy, final SimpleRepositoryFileData data, final Node fileNode)
      throws RepositoryException, IOException {
    Node resourceNode = fileNode.getNode(pentahoJcrConstants.getJCR_CONTENT());

    // mandatory property on nt:resource; give them a value to satisfy Jackrabbit
    resourceNode.setProperty(pentahoJcrConstants.getJCR_LASTMODIFIED(), Calendar.getInstance());

    if (StringUtils.hasText(data.getEncoding())) {
      resourceNode.setProperty(pentahoJcrConstants.getJCR_ENCODING(), data.getEncoding());
    }
    resourceNode.setProperty(pentahoJcrConstants.getJCR_DATA(), data.getStream());
    resourceNode.setProperty(pentahoJcrConstants.getJCR_MIMETYPE(), data.getMimeType());
  }
}
