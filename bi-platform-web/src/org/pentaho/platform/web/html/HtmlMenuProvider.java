
package org.pentaho.platform.web.html;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.plugin.services.pluginmgr.BaseMenuProvider;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.web.http.messages.Messages;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.containers.XulMenubar;
import org.pentaho.ui.xul.containers.XulMenupopup;
import org.pentaho.ui.xul.html.HtmlXulLoader;
import org.pentaho.ui.xul.html.IHtmlElement;

public class HtmlMenuProvider extends BaseMenuProvider {

	@Override
	protected XulLoader getXulLoader( ) {
		  try {
		      return new HtmlXulLoader();
		  } catch (Exception e) {
			  Logger.error( this.getClass().toString(), Messages.getString("HtmlMenuProvider.ERROR_0001_COULD_NOT_CREATE_XUL_LOADER"), e); //$NON-NLS-1$
		  }
		  return null;
	}

	public String getMenuBar( String id, String documentPath, IPentahoSession session ) {
		XulMenubar menubar = getXulMenubar( id, documentPath, session );
	    StringBuilder sb = new StringBuilder();
		if( menubar instanceof IHtmlElement ) {
			((IHtmlElement) menubar).getHtml(sb);
		}
		return sb.toString();

	}
	
	public String getPopupMenu( String id, String documentPath, IPentahoSession session ) {
		XulMenupopup popup = getXulPopupMenu( id, documentPath, session );
	    StringBuilder sb = new StringBuilder();
		if( popup instanceof IHtmlElement ) {
			((IHtmlElement) popup).getHtml(sb);
			((IHtmlElement) popup).getScript(sb);
		}
		return sb.toString();
	}

	
}
