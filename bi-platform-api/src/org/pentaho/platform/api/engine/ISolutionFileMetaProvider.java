package org.pentaho.platform.api.engine;

import java.io.InputStream;

public interface ISolutionFileMetaProvider extends IFileInfoGenerator {
  public IFileInfo getFileInfo(ISolutionFile solutionFile, InputStream in);
}
