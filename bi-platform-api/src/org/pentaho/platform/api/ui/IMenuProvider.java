package org.pentaho.platform.api.ui;

import org.pentaho.platform.api.engine.IPentahoSession;

public interface IMenuProvider {

	public void addCustomization( IMenuCustomization custom );
	
	public abstract Object getMenuBar( String id, IPentahoSession session );
	
	public abstract Object getPopupMenu( String id, IPentahoSession session );
	
}
