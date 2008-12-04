package org.pentaho.platform.engine.core.solution;

import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IFileInfoGenerator;
import org.pentaho.platform.api.engine.IObjectCreator;

public class ContentGeneratorInfo implements IContentGeneratorInfo {

	private String description;
	
	private String id;
	
	private String title;
	
	private String url;

	private IFileInfoGenerator fileInfoGenerator; 
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setObjectCreator( IObjectCreator creator ) {
	}

	public IObjectCreator getCreator() {
		return null;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public IFileInfoGenerator getFileInfoGenerator() {
		return fileInfoGenerator;
	}

	public void setFileInfoGenerator(IFileInfoGenerator fileInfoGenerator) {
		this.fileInfoGenerator = fileInfoGenerator;
	}
}
