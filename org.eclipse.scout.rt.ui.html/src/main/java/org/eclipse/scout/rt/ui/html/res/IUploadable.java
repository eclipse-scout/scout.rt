/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.res;

/**
 * Base interface for code that handles file uploads in a HTTP context. Currently this is only used as a common base
 * class for <code>IBinaryResourceConsumer</code> and <code>IBinaryResourceUploader</code>.
 *
 * @see IBinaryResourceConsumer
 * @see IBinaryResourceUploader
 */
public interface IUploadable {

  /**
   * @return maximum upload size in bytes
   */
  long getMaximumUploadSize();

}
