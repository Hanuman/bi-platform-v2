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
import javax.jcr.Value;

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
    boolean versioned = false;

    if (isFolder(node)) {
      folder = true;
    }

    if (node.hasProperty(PentahoJcrConstants.JCR_CREATED)) {
      Calendar tmpCal = node.getProperty(PentahoJcrConstants.JCR_CREATED).getDate();
      if (tmpCal != null) {
        createdDateTime = tmpCal.getTime();
      }
    }

    if (isPentahoFile(session, node)) {
      Calendar tmpCal = node.getProperty(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_LASTMODIFIED)).getDate();
      if (tmpCal != null) {
        lastModifiedDateTime = tmpCal.getTime();
      }
      mimeType = node.getProperty(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_MIMETYPE)).getString();
      versioned = isVersioned(session, node);
    }

    RepositoryFile file = new RepositoryFile.Builder(node.getName(), nodeIdStrategy.getId(node), !node.getParent()
        .isSame(session.getRootNode()) ? nodeIdStrategy.getId(node.getParent()) : null).createdDate(createdDateTime)
        .lastModificationDate(lastModifiedDateTime).mimeType(mimeType).folder(folder).versioned(versioned)
        .absolutePath(node.getPath()).build();

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
    Node folderNode = parentFolderNode.addNode(file.getName(), PentahoJcrConstants.NT_FOLDER);
    nodeIdStrategy.setId(folderNode, null);
    return folderNode;
  }

  public static Node toFileNode(final Session session, final NodeIdStrategy nodeIdStrategy,
      final RepositoryFile parentFolder, final RepositoryFile file, final IRepositoryFileContent content,
      final Transformer<IRepositoryFileContent> transformer) throws RepositoryException, IOException {

    Node parentFolderNode;
    if (parentFolder != null) {
      parentFolderNode = nodeIdStrategy.findNodeById(session, parentFolder.getId());
    } else {
      parentFolderNode = session.getRootNode();
    }
    Node fileNode = parentFolderNode.addNode(file.getName(), PentahoJcrConstants.NT_FILE);
    fileNode.addMixin(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_PENTAHOFILE));
    fileNode.setProperty(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_MIMETYPE), file.getMimeType());
    // set created and last modified to same date when creating a new file
    fileNode.setProperty(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_LASTMODIFIED), fileNode.getProperty(
        PentahoJcrConstants.JCR_CREATED).getDate());
    nodeIdStrategy.setId(fileNode, null);
    Node resourceNode = fileNode.addNode(PentahoJcrConstants.JCR_CONTENT, PentahoJcrConstants.NT_RESOURCE);

    // mandatory properties on nt:resource; give them a value to satisfy Jackrabbit
    resourceNode.setProperty(PentahoJcrConstants.JCR_MIMETYPE, file.getMimeType());
    resourceNode.setProperty(PentahoJcrConstants.JCR_LASTMODIFIED, fileNode
        .getProperty(PentahoJcrConstants.JCR_CREATED).getDate());

    resourceNode.addMixin(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_PENTAHORESOURCE));
    if (file.isVersioned()) {
      fileNode.setProperty(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_VERSIONED), true);
      resourceNode.addMixin(PentahoJcrConstants.MIX_VERSIONABLE);
    }

    transformer.createContentNode(session, nodeIdStrategy, content, resourceNode);
    return fileNode;
  }

  public static Node updateFileNode(final Session session, final NodeIdStrategy nodeIdStrategy,
      final RepositoryFile file, final IRepositoryFileContent content,
      final Transformer<IRepositoryFileContent> transformer) throws RepositoryException, IOException {

    Calendar lastModified = Calendar.getInstance();

    Node fileNode = nodeIdStrategy.findNodeById(session, file.getId());
    fileNode.setProperty(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_MIMETYPE), file.getMimeType());
    fileNode.setProperty(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_LASTMODIFIED), lastModified);

    Node resourceNode = fileNode.getNode(PentahoJcrConstants.JCR_CONTENT);

    // mandatory properties on nt:resource; give them a value to satisfy Jackrabbit
    resourceNode.setProperty(PentahoJcrConstants.JCR_MIMETYPE, file.getMimeType());
    resourceNode.setProperty(PentahoJcrConstants.JCR_LASTMODIFIED, lastModified);

    transformer.updateContentNode(session, nodeIdStrategy, content, resourceNode);
    return fileNode;
  }

  public static Node getResourceNode(final Session session, final Node node) throws RepositoryException, IOException {
    Assert.isTrue(isFileOrLinkedFile(node));
    Node resourceNode = null;
    if (isFile(node)) {
      resourceNode = node.getNode(PentahoJcrConstants.JCR_CONTENT);
    } else {
      // linked file
      String resourceNodeUuid = node.getProperty(PentahoJcrConstants.JCR_CONTENT).getString();
      resourceNode = session.getNodeByUUID(resourceNodeUuid);
    }
    return resourceNode;
  }

  public static IRepositoryFileContent getContent(final Session session, final NodeIdStrategy nodeIdStrategy,
      final RepositoryFile file, final Transformer<IRepositoryFileContent> transformer) throws RepositoryException,
      IOException {
    Node fileNode = nodeIdStrategy.findNodeById(session, file.getId());
    Assert.isTrue(!isFolder(fileNode));

    return transformer.fromContentNode(session, nodeIdStrategy, getResourceNode(session, fileNode));
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
    return node.getProperty(PentahoJcrConstants.JCR_PRIMARYTYPE).getString().equals(PentahoJcrConstants.NT_FOLDER);
  }

  private static boolean isResource(final Node node) throws RepositoryException {
    return node.getProperty(PentahoJcrConstants.JCR_PRIMARYTYPE).getString().equals(PentahoJcrConstants.NT_RESOURCE);
  }

  private static boolean isFileOrLinkedFile(final Node node) throws RepositoryException {
    String nodeTypeName = node.getProperty(PentahoJcrConstants.JCR_PRIMARYTYPE).getString();
    return PentahoJcrConstants.NT_LINKEDFILE.equals(nodeTypeName) || PentahoJcrConstants.NT_FILE.equals(nodeTypeName);
  }

  private static boolean isPentahoFile(final Session session, final Node node) throws RepositoryException {
    Value[] mixinTypeNames = node.getProperty(PentahoJcrConstants.JCR_MIXINTYPES).getValues();
    for (Value v : mixinTypeNames) {
      if (addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_PENTAHOFILE).equals(v.getString())) {
        return true;
      }
    }
    return false;
  }

  private static boolean isVersioned(final Session session, final Node node) throws RepositoryException {
    return node.getProperty(addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_VERSIONED)).getBoolean();
  }

  private static boolean isFile(final Node node) throws RepositoryException {
    return node.getProperty(PentahoJcrConstants.JCR_PRIMARYTYPE).getString().equals(PentahoJcrConstants.NT_FILE);
  }

  private static boolean isSupportedNodeType(final Node node) throws RepositoryException {
    String nodeTypeName = node.getProperty(PentahoJcrConstants.JCR_PRIMARYTYPE).getString();
    return PentahoJcrConstants.NT_FOLDER.equals(nodeTypeName) || PentahoJcrConstants.NT_LINKEDFILE.equals(nodeTypeName)
        || PentahoJcrConstants.NT_FILE.equals(nodeTypeName) || PentahoJcrConstants.NT_RESOURCE.equals(nodeTypeName);
  }

  public static String addPentahoPrefix(final Session session, final String name) throws RepositoryException {
    String prefix = session.getWorkspace().getNamespaceRegistry().getPrefix(PentahoJcrConstants.PENTAHO_URI);
    return prefix + ":" + name;
  }

  public static String removePentahoPrefix(final Session session, final String nameWithPrefix)
      throws RepositoryException {
    String prefix = session.getWorkspace().getNamespaceRegistry().getPrefix(PentahoJcrConstants.PENTAHO_URI);
    return nameWithPrefix.substring(prefix.length() + 1); // plus one for the colon
  }

  public static void checkoutIfNecessary(final Session session, final NodeIdStrategy nodeIdStrategy,
      final RepositoryFile file) throws RepositoryException, IOException {
    if (file.isVersioned()) {
      Node fileNode = nodeIdStrategy.findNodeById(session, file.getId());
      Assert.isTrue(isFileOrLinkedFile(fileNode));
      Node resourceNode = getResourceNode(session, fileNode);
      Assert.isTrue(isResource(resourceNode));
      resourceNode.checkout();
    }
  }

  public static void checkinIfNecessary(final Session session, final NodeIdStrategy nodeIdStrategy,
      final RepositoryFile file) throws RepositoryException, IOException {
    if (file.isVersioned()) {
      Node fileNode = nodeIdStrategy.findNodeById(session, file.getId());
      Assert.isTrue(isFileOrLinkedFile(fileNode));
      Node resourceNode = getResourceNode(session, fileNode);
      Assert.isTrue(isResource(resourceNode));
      resourceNode.checkin();
    }
  }
}
