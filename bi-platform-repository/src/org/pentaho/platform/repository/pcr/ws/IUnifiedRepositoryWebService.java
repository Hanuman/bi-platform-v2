package org.pentaho.platform.repository.pcr.ws;

import java.util.List;

import javax.jws.WebService;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.api.repository.RepositoryFileAce;
import org.pentaho.platform.api.repository.RepositoryFileAcl;
import org.pentaho.platform.api.repository.RepositoryFilePermission;
import org.pentaho.platform.api.repository.VersionSummary;
import org.pentaho.platform.repository.pcr.data.node.NodeRepositoryFileData;

/**
 * JAX-WS-safe version of {@code IUnifiedRepositoryService}.
 * 
 * <ul>
 * <li>No method overloading (e.g. getFile(id, versionId) becomes getFileAtVersion(id, versionId).</li>
 * <li>No interfaces (e.g. Serializable becomes String).</li>
 * <li>No references to public static inner classes.</li>
 * </ul>
 * 
 * @author mlowery
 */
@WebService
public interface IUnifiedRepositoryWebService {

  void startup();

  @XmlJavaTypeAdapter(RepositoryFileAdapter.class)
  RepositoryFile getFile(final String absPath);

  @XmlJavaTypeAdapter(RepositoryFileAdapter.class)
  RepositoryFile getFileById(final String fileId);

  @XmlJavaTypeAdapter(NodeRepositoryFileDataAdapter.class)
  NodeRepositoryFileData getDataAsNodeForRead(final String fileId);

  @XmlJavaTypeAdapter(RepositoryFileAdapter.class)
  RepositoryFile createFile(final String parentFolderId,
      @XmlJavaTypeAdapter(RepositoryFileAdapter.class) final RepositoryFile file,
      @XmlJavaTypeAdapter(NodeRepositoryFileDataAdapter.class) final NodeRepositoryFileData data,
      final String versionMessage);

  @XmlJavaTypeAdapter(RepositoryFileAdapter.class)
  RepositoryFile createFolder(final String parentFolderId,
      @XmlJavaTypeAdapter(RepositoryFileAdapter.class) final RepositoryFile file, final String versionMessage);

  @XmlJavaTypeAdapter(RepositoryFileAdapter.class)
  List<RepositoryFile> getChildren(final String folderId);

  @XmlJavaTypeAdapter(RepositoryFileAdapter.class)
  List<RepositoryFile> getChildrenWithFilter(final String folderId, final String filter);

  @XmlJavaTypeAdapter(RepositoryFileAdapter.class)
  RepositoryFile updateFile(@XmlJavaTypeAdapter(RepositoryFileAdapter.class) final RepositoryFile file,
      @XmlJavaTypeAdapter(NodeRepositoryFileDataAdapter.class) final NodeRepositoryFileData data,
      final String versionMessage);

  void deleteFileWithPermanentFlag(final String fileId, final boolean permanent, final String versionMessage);

  void deleteFile(final String fileId, final String versionMessage);

  void undeleteFile(final String fileId, final String versionMessage);

  @XmlJavaTypeAdapter(RepositoryFileAdapter.class)
  List<RepositoryFile> getDeletedFilesInFolder(final String folderId);

  @XmlJavaTypeAdapter(RepositoryFileAdapter.class)
  List<RepositoryFile> getDeletedFilesInFolderWithFilter(final String folderId, final String filter);

  @XmlJavaTypeAdapter(RepositoryFileAdapter.class)
  List<RepositoryFile> getDeletedFiles();

  void moveFile(final String fileId, final String destAbsPath, final String versionMessage);

  void lockFile(final String fileId, final String message);

  void unlockFile(final String fileId);

  @XmlJavaTypeAdapter(RepositoryFileAclAdapter.class)
  RepositoryFileAcl getAcl(final String fileId);

  @XmlJavaTypeAdapter(RepositoryFileAclAdapter.class)
  RepositoryFileAcl updateAcl(@XmlJavaTypeAdapter(RepositoryFileAclAdapter.class) final RepositoryFileAcl acl);

  boolean hasAccess(final String absPath, final RepositoryFilePermission[] permissions);

  @XmlJavaTypeAdapter(RepositoryFileAclAceAdapter.class)
  List<RepositoryFileAce> getEffectiveAces(final String fileId);

  @XmlJavaTypeAdapter(NodeRepositoryFileDataAdapter.class)
  NodeRepositoryFileData getDataAsNodeForReadAtVersion(final String fileId, final String versionId);

  @XmlJavaTypeAdapter(VersionSummaryAdapter.class)
  VersionSummary getVersionSummary(String fileId, String versionId);

  @XmlJavaTypeAdapter(VersionSummaryAdapter.class)
  List<VersionSummary> getVersionSummaries(final String fileId);

  @XmlJavaTypeAdapter(RepositoryFileAdapter.class)
  RepositoryFile getFileAtVersion(final String fileId, final String versionId);

}
