/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.rap.window.filechooser;

import org.eclipse.swt.SWT;

/**
 * <h3>IRwtScoutFileChooser</h3> ...
 * RWT File chooser is only designed to UPLOAD files, regardless of the {@link SWT#SAVE} flag.
 * <p>
 * Therefore the DOWNLOAD of files is done by displaying a link to the resource and let the user click on it resp. right
 * click "Save As..." on it.
 * 
 * @since 3.8.0 June 2012
 */
public interface IRwtScoutFileChooser {

  void showFileChooser();

}
