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
 * File upload handler for the Drag & Drop functionality used when dragging files from the
 * client desktop into the application.
 * 
 * @since 4.0.0-M7
 */
public interface IRwtScoutFileUploadHandler {

  /**
   * Prepares and starts the file upload
   * 
   * @param event
   *          - the event resulting from the drop action.
   * @return {@code true} if the file handler is able to prepare and start the file upload, {@code false} otherwise.
   */
  boolean startFileUpload(DropTargetEvent event);

}
