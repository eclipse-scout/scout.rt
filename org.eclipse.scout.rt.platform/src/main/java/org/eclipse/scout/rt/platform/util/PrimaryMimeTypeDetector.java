/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import java.nio.file.Path;
import java.util.Locale;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.resource.MimeType;

/**
 * Definition of primary mime types from {@link MimeType}
 * <p>
 * see {@link IMimeTypeDetector}
 *
 * @author BSI AG
 * @since 5.2
 */
@Order(0)
@ApplicationScoped
public class PrimaryMimeTypeDetector implements IMimeTypeDetector {

  @Override
  public String getMimeType(Path path) {
    if (path == null) {
      return null;
    }
    Path fileName = path.getFileName();
    if (fileName == null) {
      return null;
    }

    String name = fileName.toString();
    int dot = name.lastIndexOf('.');
    if (dot < 0) {
      return null;
    }
    String ext = name.substring(dot + 1).toLowerCase(Locale.US);
    return fileExtensionToMimeType(ext);
  }

  /**
   * @param ext
   *          is not null and lowercase
   * @return the mime type or null if not known
   */
  protected String fileExtensionToMimeType(String ext) {
    MimeType mimeType = MimeType.findByFileExtension(ext);
    if (mimeType != null) {
      return mimeType.getType();
    }
    return null;
  }
}
