package org.pentaho.platform.repository.pcr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.JcrConstants;
import org.pentaho.platform.api.repository.IRepositoryFileContent;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.repository.pcr.JcrPentahoContentDao.NodeIdStrategy;
import org.pentaho.platform.repository.pcr.JcrPentahoContentDao.Transformer;
import org.springframework.util.Assert;

public class JcrRepositoryFileUtils {
  public static RepositoryFile fromNode(final Session session, final NodeIdStrategy nodeIdStrategy, final Node node)
      throws RepositoryException, IOException {
    Assert.isTrue(isSupportedNodeType(node));

    Date createdDateTime = null;
    Date lastModifiedDateTime = null;
    String mimeType = null;
    boolean folder = false;

    if (isFolder(node)) {
      folder = true;
    }

    if (node.hasProperty(JcrConstants.JCR_CREATED)) {
      Calendar tmpCal = node.getProperty(JcrConstants.JCR_CREATED).getDate();
      if (tmpCal != null) {
        createdDateTime = tmpCal.getTime();
      }
    }

    if (isFileOrLinkedFile(node)) {
      Node resourceNode = getResourceNode(session, node);

      if (isResource(resourceNode)) {
        Calendar tmpCal = resourceNode.getProperty(JcrConstants.JCR_LASTMODIFIED).getDate();
        if (tmpCal != null) {
          lastModifiedDateTime = tmpCal.getTime();
        }
        mimeType = resourceNode.getProperty(JcrConstants.JCR_MIMETYPE).getString();
      }
    }

    RepositoryFile file = new RepositoryFile.Builder(node.getName(), nodeIdStrategy.getId(node), !node.getParent()
        .isSame(session.getRootNode()) ? nodeIdStrategy.getId(node.getParent()) : null).createdDate(createdDateTime)
        .lastModificationDate(lastModifiedDateTime).mimeType(mimeType).folder(folder).absolutePath(node.getPath())
        .build();

    return file;
  }

  public static Node toFolderNode(final Session session, final NodeIdStrategy nodeIdStrategy,
      final RepositoryFile parentFolder, final RepositoryFile file) throws RepositoryException, IOException {
    Node parentFolderNode;
    if (parentFolder != null) {
      parentFolderNode = nodeIdStrategy.findNodeById(session, parentFolder.getId());
    } else {
      parentFolderNode = session.getRootNode();
    }
    Node folderNode = parentFolderNode.addNode(file.getName(), JcrConstants.NT_FOLDER);
    nodeIdStrategy.setId(folderNode, null);
    return folderNode;
  }

  public static Node toFileNode(final Session session, final NodeIdStrategy nodeIdStrategy,
      final RepositoryFile parentFolder, final RepositoryFile file, final IRepositoryFileContent content,
      final Transformer transformer) throws RepositoryException, IOException {

    Node parentFolderNode;
    if (parentFolder != null) {
      parentFolderNode = nodeIdStrategy.findNodeById(session, parentFolder.getId());
    } else {
      parentFolderNode = session.getRootNode();
    }
    Node fileNode = parentFolderNode.addNode(file.getName(), JcrConstants.NT_FILE);
    nodeIdStrategy.setId(fileNode, null);
    Node resourceNode = fileNode.addNode(JcrConstants.JCR_CONTENT, JcrConstants.NT_RESOURCE);
    resourceNode.setProperty(JcrConstants.JCR_MIMETYPE, file.getMimeType());
    // set created and last modified to same date when creating a new file
    resourceNode.setProperty(JcrConstants.JCR_LASTMODIFIED, fileNode.getProperty(JcrConstants.JCR_CREATED).getDate());

    transformer.toNode(session, nodeIdStrategy, content, resourceNode);

    return fileNode;
  }

  public static Node getResourceNode(final Session session, final Node node) throws RepositoryException, IOException {
    Assert.isTrue(isFileOrLinkedFile(node));
    Node resourceNode = null;
    if (isFile(node)) {
      resourceNode = node.getNode(JcrConstants.JCR_CONTENT);
    } else {
      // linked file
      String resourceNodeUuid = node.getProperty(JcrConstants.JCR_CONTENT).getString();
      resourceNode = session.getNodeByUUID(resourceNodeUuid);
    }
    return resourceNode;
  }

  public static IRepositoryFileContent getContent(final Session session, final NodeIdStrategy nodeIdStrategy,
      final RepositoryFile file, final Transformer transformer) throws RepositoryException, IOException {
    Node fileNode = nodeIdStrategy.findNodeById(session, file.getId());
    Assert.isTrue(!isFolder(fileNode));

    return transformer.fromNode(session, nodeIdStrategy, getResourceNode(session, fileNode));
  }

  public static List<RepositoryFile> getChildren(final Session session, final NodeIdStrategy nodeIdStrategy,
      final RepositoryFile folder) throws RepositoryException, IOException {
    Node folderNode = nodeIdStrategy.findNodeById(session, folder.getId());
    Assert.isTrue(isFolder(folderNode));

    List<RepositoryFile> children = new ArrayList<RepositoryFile>();
    // get all immediate child nodes that are of type NT_FOLDER, NT_FILE, or NT_LINKEDFILE
    NodeIterator nodeIterator = folderNode.getNodes();
    while (nodeIterator.hasNext()) {
      Node node = nodeIterator.nextNode();
      if (isFolder(node) || isFileOrLinkedFile(node)) {
        children.add(fromNode(session, nodeIdStrategy, node));
      }
    }
    Collections.sort(children);
    return children;
  }

  private static boolean isFolder(final Node node) throws RepositoryException {
    return node.getProperty(JcrConstants.JCR_PRIMARYTYPE).getString().equals(JcrConstants.NT_FOLDER);
  }

  private static boolean isResource(final Node node) throws RepositoryException {
    return node.getProperty(JcrConstants.JCR_PRIMARYTYPE).getString().equals(JcrConstants.NT_RESOURCE);
  }

  private static boolean isFileOrLinkedFile(final Node node) throws RepositoryException {
    String nodeTypeName = node.getProperty(JcrConstants.JCR_PRIMARYTYPE).getString();
    return JcrConstants.NT_LINKEDFILE.equals(nodeTypeName) || JcrConstants.NT_FILE.equals(nodeTypeName);
  }

  private static boolean isFile(final Node node) throws RepositoryException {
    return node.getProperty(JcrConstants.JCR_PRIMARYTYPE).getString().equals(JcrConstants.NT_FILE);
  }

  private static boolean isSupportedNodeType(final Node node) throws RepositoryException {
    String nodeTypeName = node.getProperty(JcrConstants.JCR_PRIMARYTYPE).getString();
    return JcrConstants.NT_FOLDER.equals(nodeTypeName) || JcrConstants.NT_LINKEDFILE.equals(nodeTypeName)
        || JcrConstants.NT_FILE.equals(nodeTypeName) || JcrConstants.NT_RESOURCE.equals(nodeTypeName);
  }
}
