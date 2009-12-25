package org.pentaho.platform.repository.pcr.jcr.transform;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.pentaho.platform.repository.pcr.SimpleRepositoryFileContent;
import org.pentaho.platform.repository.pcr.jcr.NodeIdStrategy;
import org.pentaho.platform.repository.pcr.jcr.PentahoJcrConstants;
import org.pentaho.platform.repository.pcr.jcr.JcrRepositoryFileDao.Transformer;
import org.springframework.util.StringUtils;

public class SimpleRepositoryFileContentTransformer implements Transformer<SimpleRepositoryFileContent> {

  public SimpleRepositoryFileContent fromContentNode(Session session, final PentahoJcrConstants pentahoJcrConstants,
      NodeIdStrategy nodeIdStrategy, Node resourceNode) throws RepositoryException, IOException {
    String encoding = null;
    if (resourceNode.hasProperty(pentahoJcrConstants.getJCR_ENCODING())) {
      encoding = resourceNode.getProperty(pentahoJcrConstants.getJCR_ENCODING()).getString();
    }
    InputStream data = resourceNode.getProperty(pentahoJcrConstants.getJCR_DATA()).getStream();
    String mimeType = resourceNode.getProperty(pentahoJcrConstants.getJCR_MIMETYPE()).getString();
    return new SimpleRepositoryFileContent(data, encoding, mimeType);

  }

  public void createContentNode(Session session, final PentahoJcrConstants pentahoJcrConstants,
      NodeIdStrategy nodeIdStrategy, SimpleRepositoryFileContent content, Node resourceNode)
      throws RepositoryException, IOException {
    if (StringUtils.hasText(content.getEncoding())) {
      resourceNode.setProperty(pentahoJcrConstants.getJCR_ENCODING(), content.getEncoding());
    }
    resourceNode.setProperty(pentahoJcrConstants.getJCR_DATA(), content.getData());
    resourceNode.setProperty(pentahoJcrConstants.getJCR_MIMETYPE(), content.getMimeType());
  }

  public boolean supports(final String contentType) {
    return SimpleRepositoryFileContent.CONTENT_TYPE.equals(contentType);
  }

  public void updateContentNode(Session session, final PentahoJcrConstants pentahoJcrConstants,
      NodeIdStrategy nodeIdStrategy, SimpleRepositoryFileContent content, Node resourceNode)
      throws RepositoryException, IOException {
    if (StringUtils.hasText(content.getEncoding())) {
      resourceNode.setProperty(pentahoJcrConstants.getJCR_ENCODING(), content.getEncoding());
    }
    resourceNode.setProperty(pentahoJcrConstants.getJCR_DATA(), content.getData());
    resourceNode.setProperty(pentahoJcrConstants.getJCR_MIMETYPE(), content.getMimeType());
  }

}
