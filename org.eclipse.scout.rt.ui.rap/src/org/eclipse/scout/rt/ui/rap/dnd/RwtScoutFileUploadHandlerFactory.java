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

import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutDndUploadCallback;

/**
 * Factory for the file upload handler.
 *
 * @since 4.0.0-M7
 */
public class RwtScoutFileUploadHandlerFactory {

  private RwtScoutFileUploadHandlerFactory() {
  }

  public static IRwtScoutFileUploadHandler createFileUploadHandler(IRwtScoutDndUploadCallback uploadCallback) {
    return new RwtScoutFileUploadHandler(uploadCallback);
  }

}
