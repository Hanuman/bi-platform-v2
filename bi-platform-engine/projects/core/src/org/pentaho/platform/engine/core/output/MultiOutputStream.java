/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Jan 26, 2008
 * @author James Dixon
 * 
 */

package org.pentaho.platform.engine.core.output;

import java.io.IOException;
import java.io.OutputStream;

/*
 * A subclass of java.io.OutputStream. Forks an output stream to multiple 
 * output streams. Takes an array of output streams on construction and copies 
 * all output sent to it to each of the streams.
 */

public class MultiOutputStream extends OutputStream {

	OutputStream outs[];
	
	public MultiOutputStream( OutputStream outs[] ) {
		this.outs = outs;
	}
	
	@Override
	public void write(int b) throws IOException {
		IOException ioEx = null;
		for( int idx=0; idx<outs.length; idx++ ) {
			try {
				outs[idx].write( b );
			} catch (IOException e) {
				ioEx = e;
			}
		}
		if( ioEx != null ) {
			throw ioEx;
		}
	}

	public void write(byte b[]) throws IOException {
		IOException ioEx = null;
		for( int idx=0; idx<outs.length; idx++ ) {
			try {
				outs[idx].write( b );
			} catch (IOException e) {
				ioEx = e;
			}
		}
		if( ioEx != null ) {
			throw ioEx;
		}
	}

	public void write(byte b[], int off, int len) throws IOException {
		IOException ioEx = null;
		for( int idx=0; idx<outs.length; idx++ ) {
			try {
				outs[idx].write( b, off, len );
			} catch (IOException e) {
				ioEx = e;
			}
		}
		if( ioEx != null ) {
			throw ioEx;
		}
	}

	public void close() throws IOException {
		IOException ioEx = null;
		for( int idx=0; idx<outs.length; idx++ ) {
			try {
				outs[idx].close( );
			} catch (IOException e) {
				ioEx = e;
			}
		}
		if( ioEx != null ) {
			throw ioEx;
		}
	}

}
