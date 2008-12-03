package org.pentaho.platform.engine.core.solution;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPluginOperation;

public class ContentInfo implements IContentInfo {

  private String description;
	
	private String extension;
	
	private String mimeType;
	
	private String title;

	private List<IPluginOperation> operations = new ArrayList<IPluginOperation>();
	
	private String iconUrl;
	
	public void setIconUrl(String iconUrl) {
    this.iconUrl = iconUrl;
  }

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
	
	 public List<IPluginOperation> getOperations() {
	   return operations;
	 }

	  public String getIconUrl() {
	    return iconUrl;
	  }
	  
	  public void addOperation( IPluginOperation operation ) {
	    operations.add( operation );
	  }
}
