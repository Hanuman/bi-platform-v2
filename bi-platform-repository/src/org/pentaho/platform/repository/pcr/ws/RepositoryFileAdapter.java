package org.pentaho.platform.repository.pcr.ws;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.pentaho.platform.api.repository.RepositoryFile;

/**
 * Converts {@code RepositoryFile} into JAXB-safe object and vice-versa.
 * 
 * @author mlowery
 */
public class RepositoryFileAdapter extends XmlAdapter<JaxbSafeRepositoryFile, RepositoryFile> {

  public RepositoryFileAdapter() {
    super();
  }

  @Override
  public JaxbSafeRepositoryFile marshal(final RepositoryFile v) throws Exception {
    if (v == null) {
      return null;
    }
    JaxbSafeRepositoryFile f = new JaxbSafeRepositoryFile();
    f.name = v.getName();
    f.absolutePath = v.getAbsolutePath();
    f.createdDate = v.getCreatedDate();
    f.description = v.getDescription();
    f.folder = v.isFolder();
    if (v.getId() != null) {
      f.id = v.getId().toString();
    }
    f.lastModifiedDate = v.getLastModifiedDate();
    f.locale = v.getLocale();
    f.lockDate = v.getLockDate();
    f.locked = v.isLocked();
    f.lockMessage = v.getLockMessage();
    f.lockOwner = v.getLockOwner();
    f.title = v.getTitle();
    f.versioned = v.isVersioned();
    if (v.getVersionId() != null) {
      f.versionId = v.getVersionId().toString();
    }
    return f;
  }

  @Override
  public RepositoryFile unmarshal(final JaxbSafeRepositoryFile v) throws Exception {
    if (v == null) {
      return null;
    }
    RepositoryFile.Builder builder = null;
    if (v.id != null) {
      builder = new RepositoryFile.Builder(v.id, v.name);
    } else {
      builder = new RepositoryFile.Builder(v.name);
    }
    return builder.absolutePath(v.absolutePath).createdDate(v.createdDate).description(v.description).folder(v.folder)
        .lastModificationDate(v.lastModifiedDate).locale(v.locale).lockDate(v.lockDate).locked(v.locked).lockMessage(
            v.lockMessage).lockOwner(v.lockOwner).title(v.title).versioned(v.versioned).versionId(v.versionId).build();
  }

}
