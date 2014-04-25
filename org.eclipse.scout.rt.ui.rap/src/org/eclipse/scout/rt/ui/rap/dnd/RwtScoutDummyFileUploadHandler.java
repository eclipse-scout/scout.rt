/*******************************************************************************
 * Copyright (c) 2010, 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.rap.dnd;

import org.eclipse.swt.dnd.DropTargetEvent;

/**
 * The dummy implementation of the file upload handler.
 * Since drag & drop from client desktop into the application is only supported when
 * the RAP file chooser bundle is installed {@see org.eclipse.scout.rt.ui.rap.incubator.filechooser},
 * this dummy class won't handle the upload so that nothing will happen.
 * 
 * @since 4.0.0-M7
 */
public class RwtScoutDummyFileUploadHandler implements IRwtScoutFileUploadHandler {

  @Override
  public boolean startFileUpload(DropTargetEvent event) {
    return false;
  }

}
