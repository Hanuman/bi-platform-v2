package org.pentaho.platform.api.engine;

public interface IContentGeneratorInfo {

	public String getDescription();

	public void setDescription(String description);

	public String getId();

	public void setId(String id);

	public String getUrl();

	public void setUrl(String url);

	public String getTitle();

	public void setTitle(String title);
	
	public IFileInfoGenerator getFileInfoGenerator( );
}
