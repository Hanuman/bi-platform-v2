package org.pentaho.platform.api.engine;

import java.io.InputStream;

import org.dom4j.Document;

/**
 * This class only exists for backwards compatibility with
 * {@link IFileInfoGenerator}. Once {@link IFileInfoGenerator} is removed, this
 * class will follow it. It is also possible for this class to live on, by
 * simply changing the ISolutionFileMetaProvider interface.
 */
public abstract class SolutionFileMetaAdapter implements
		ISolutionFileMetaProvider {

	// allow subclasses to access logger as is
	protected ILogger logger;

	public void setLogger(ILogger logger) {
		this.logger = logger;
	}

	@SuppressWarnings("deprecation")
  public ContentType getContentType() {
		return null;
	}

	public IFileInfo getFileInfo(String solution, String path, String filename,
			InputStream in) {
		return null;
	}

	public IFileInfo getFileInfo(String solution, String path, String filename,
			Document in) {
		return null;
	}

	public IFileInfo getFileInfo(String solution, String path, String filename,
			byte[] bytes) {
		return null;
	}

	public IFileInfo getFileInfo(String solution, String path, String filename,
			String str) {
		return null;
	}

}
