package org.pentaho.platform.engine.core.solution;

import org.pentaho.platform.api.engine.IContentInfo;

public class ContentInfo implements IContentInfo {

	String description;
	
	String extension;
	
	String mimeType;
	
	String title;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
}
