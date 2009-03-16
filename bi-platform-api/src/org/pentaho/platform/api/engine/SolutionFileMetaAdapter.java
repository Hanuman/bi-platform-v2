package org.pentaho.platform.api.engine;

import java.io.InputStream;

import org.dom4j.Document;

/**
 * This class only exists for backwards compatibility with {@link IFileInfoGenerator}.
 * Once {@link IFileInfoGenerator} is removed, this class will follow it.
 */
public abstract class SolutionFileMetaAdapter implements ISolutionFileMetaProvider {

  public ContentType getContentType() {
    return null;
  }
  
  public IFileInfo getFileInfo(String solution, String path, String filename, InputStream in) {
    return null;
  }

  public IFileInfo getFileInfo(String solution, String path, String filename, Document in) {
    return null;
  }

  public IFileInfo getFileInfo(String solution, String path, String filename, byte[] bytes) {
    return null;
  }

  public IFileInfo getFileInfo(String solution, String path, String filename, String str) {
    return null;
  }

}
