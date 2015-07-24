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
public enum MimeTypes {
  TEXT_PLAIN("text/plain", "txt"),
  IMAGE_PNG("image/png", "png"),
  IMAGE_JPG("image/jpg", "jpg"),
  IMAGE_JPEG("image/jpeg", "jpg"),
  IMAGE_GIF("image/gif", "gif");

  private final String preferedFileExtension, type;

  MimeTypes(String type, String preferedFileExtension) {
    this.preferedFileExtension = preferedFileExtension;
    this.type = type;
  }

  public String getPreferedFileExtension() {
    return preferedFileExtension;
  }

  public String getType() {
    return type;
  }

  public static MimeTypes convertToMimeType(String mimeType) {
    for (MimeTypes mimeType0 : values()) {
      if (mimeType0.getType().equals(mimeType)) {
        return mimeType0;
      }
    }
    return null;
  }

  /**
   * Common image mime types.
   */
  public static Collection<MimeTypes> getCommonImageTypes() {
    return Arrays.asList(new MimeTypes[]{IMAGE_GIF, IMAGE_JPG, IMAGE_JPEG, IMAGE_PNG});
  }

  public static boolean isOneOf(Collection<MimeTypes> mimeTypes, String mimeType) {
    for (MimeTypes mimeType0 : mimeTypes) {
      if (mimeType0.getType().equals(mimeType)) {
        return true;
      }
    }
    return false;
  }
}
