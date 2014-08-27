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

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutDndUploadCallback;
import org.eclipse.scout.rt.ui.rap.window.filechooser.IRwtScoutFileChooserService;
import org.eclipse.scout.service.SERVICES;

/**
 * Factory for the file upload handler.
 * If the bundle org.eclipse.scout.rt.ui.rap.incubator.filechooser is missing, a dummy implementation
 * of the file upload handler will be created which does nothing when a drag and drop operation occurs.
 * In case the bundle is installed, a file handler responsible for the drag and drop operation will be used.
 *
 * @since 4.0.0-M7
 */
public class RwtScoutFileUploadHandlerFactory {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutFileUploadHandlerFactory.class);

  private RwtScoutFileUploadHandlerFactory() {
  }

  public static IRwtScoutFileUploadHandler createFileUploadHandler(IRwtScoutDndUploadCallback uploadCallback) {
    IRwtScoutFileUploadHandler fileUploadHandler = null;
    IRwtScoutFileChooserService fileChooserService = SERVICES.getService(IRwtScoutFileChooserService.class);
    if (fileChooserService == null) {
      LOG.warn("Missing bundle: org.eclipse.scout.rt.ui.rap.incubator.filechooser. Drag & Drop support not possible. Please activate it in your Scout perspective under Technologies.");
      fileUploadHandler = new RwtScoutDummyFileUploadHandler();
    }
    else {
      fileUploadHandler = fileChooserService.createFileUploadHandler(uploadCallback);
    }
    return fileUploadHandler;
  }

}
