/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.res;

import java.util.Collection;

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

  /**
   * @return the set of accepted lowercase file extensions or media types for that uploadable. '*' is supported but not
   * recommended!
   */
  default Collection<String> getAcceptedUploadFileExtensions() {
    return null;
  }
}
