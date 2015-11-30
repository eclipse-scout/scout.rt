/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.resource;

import java.util.Arrays;
import java.util.Collection;

/**
 * Enumeration for a few well-known mime types.
 */
public enum MimeType {
  TEXT_PLAIN("text/plain", "txt"),
  IMAGE_PNG("image/png", "png"),
  IMAGE_JPG("image/jpg", "jpg"),
  IMAGE_JPEG("image/jpeg", "jpg"),
  IMAGE_GIF("image/gif", "gif");

  private final String m_type;
  private final String m_preferedFileExtension;

  MimeType(String type, String preferedFileExtension) {
    m_type = type;
    m_preferedFileExtension = preferedFileExtension;
  }

  public String getType() {
    return m_type;
  }

  public String getPreferedFileExtension() {
    return m_preferedFileExtension;
  }

  public static MimeType convertToMimeType(String input) {
    for (MimeType mimeType : values()) {
      if (mimeType.getType().equals(input)) {
        return mimeType;
      }
    }
    return null;
  }

  /**
   * Common image mime types.
   */
  public static Collection<MimeType> getCommonImageTypes() {
    return Arrays.asList(getCommonImageTypesAsArray());
  }

  /**
   * Common image mime types.
   */
  public static MimeType[] getCommonImageTypesAsArray() {
    return new MimeType[]{IMAGE_GIF, IMAGE_JPG, IMAGE_JPEG, IMAGE_PNG};
  }

  public static boolean isOneOf(Collection<MimeType> mimeTypes, String input) {
    for (MimeType mimeType : mimeTypes) {
      if (mimeType.getType().equals(input)) {
        return true;
      }
    }
    return false;
  }
}
