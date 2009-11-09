package org.pentaho.platform.repository.pcr.transform;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.pentaho.platform.api.repository.IRepositoryFileContent;
import org.pentaho.platform.repository.pcr.PentahoJcrConstants;
import org.pentaho.platform.repository.pcr.SimpleRepositoryFileContent;
import org.pentaho.platform.repository.pcr.JcrPentahoContentDao.NodeIdStrategy;
import org.pentaho.platform.repository.pcr.JcrPentahoContentDao.Transformer;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class SimpleRepositoryFileContentTransformer implements Transformer<SimpleRepositoryFileContent> {

  public SimpleRepositoryFileContent fromContentNode(Session session, NodeIdStrategy nodeIdStrategy, Node resourceNode)
      throws RepositoryException, IOException {
    String encoding = null;
    if (resourceNode.hasProperty(PentahoJcrConstants.JCR_ENCODING)) {
      encoding = resourceNode.getProperty(PentahoJcrConstants.JCR_ENCODING).getString();
    }
    InputStream data = resourceNode.getProperty(PentahoJcrConstants.JCR_DATA).getStream();
    return new SimpleRepositoryFileContent(data, encoding);

  }

  public void createContentNode(Session session, NodeIdStrategy nodeIdStrategy, SimpleRepositoryFileContent content,
      Node resourceNode) throws RepositoryException, IOException {
    if (StringUtils.hasText(content.getEncoding())) {
      resourceNode.setProperty(PentahoJcrConstants.JCR_ENCODING, content.getEncoding());
    }
    if (content.getData() != null) {
      resourceNode.setProperty(PentahoJcrConstants.JCR_DATA, content.getData());
    }
  }

  public <S extends IRepositoryFileContent> boolean supports(final Class<S> clazz) {
    Assert.notNull(clazz);
    return clazz.isAssignableFrom(SimpleRepositoryFileContent.class);
  }

  public void updateContentNode(Session session, NodeIdStrategy nodeIdStrategy, SimpleRepositoryFileContent content,
      Node resourceNode) throws RepositoryException, IOException {
    if (StringUtils.hasText(content.getEncoding())) {
      resourceNode.setProperty(PentahoJcrConstants.JCR_ENCODING, content.getEncoding());
    }
    if (content.getData() != null) {
      resourceNode.setProperty(PentahoJcrConstants.JCR_DATA, content.getData());
    }
  }

}
