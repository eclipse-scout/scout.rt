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
package org.eclipse.scout.rt.ui.rap.form.fields;

import java.io.File;
import java.util.List;

import org.eclipse.scout.rt.ui.rap.dnd.RwtScoutFileUploadEvent;
import org.eclipse.swt.dnd.DropTargetEvent;

/**
 * Used for drag and drop support. Classes implementing this interface will be notified about the
 * file upload operation, i.e. they will receive information about the upload progress, when the upload
 * has finished and/or when the upload fails.
 * 
 * @since 4.0.0-M7
 */
public interface IRwtScoutDndUploadCallback {

  /**
   * Called when the upload is in progress.
   * 
   * @param dropEvent
   *          - contains information about the drop operation.
   * @param uploadEvent
   *          - contains information about the file upload operation
   */
  void uploadProgress(DropTargetEvent dropEvent, RwtScoutFileUploadEvent uploadEvent);

  /**
   * Called when the upload has finished successfully.
   * 
   * @param dropEvent
   *          - contains information about the drop operation.
   * @param uploadEvent
   *          - contains information about the file upload operation
   * @param uploadedFiles
   *          - contains the uploaded files
   */
  void uploadFinished(DropTargetEvent dropEvent, RwtScoutFileUploadEvent uploadEvent, List<File> uploadedFiles);

  /**
   * Called when the upload has failed.
   * 
   * @param dropEvent
   *          - contains information about the drop operation.
   * @param uploadEvent
   *          - contains information about the file upload operation
   */
  void uploadFailed(DropTargetEvent dropEvent, RwtScoutFileUploadEvent uploadEvent);

}
