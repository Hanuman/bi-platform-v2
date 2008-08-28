package org.pentaho.platform.engine.core.output;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.repository.IContentItem;

public class MultiContentItem extends SimpleContentItem {

	protected List<IContentItem> contentItems = new ArrayList<IContentItem>();
	protected MultiOutputStream out;

	public void addContentItem( IContentItem contentItem ) {
		contentItems.add( contentItem );
	}
	
	@Override
	public OutputStream getOutputStream( String actionName )  throws IOException {
		
		  OutputStream outs[] = new OutputStream[ contentItems.size() ];

		  for( int idx=0; idx<outs.length; idx++ ) {
			  outs[ idx ] = contentItems.get( idx ).getOutputStream(actionName);
		  }
		  out = new MultiOutputStream( outs );
		  return out;
	}
	
	@Override
	public void closeOutputStream() {
		try {
			out.close();
		} catch (Exception e) {
			// TODO log this
		}
	}

}
