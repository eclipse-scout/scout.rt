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
package org.eclipse.scout.rt.platform.resource;

/**
 * This utility class is used to create unique filenames and URLs with a fingerprint for binary resources. Since binary
 * resources may be referenced in a HTML code and HTML code is often generated in the Scout server this class is in the
 * platform.
 */
public final class BinaryResourceUtility {

  public static final String URL_PREFIX = "binaryResource:";

  private BinaryResourceUtility() {
  }

  /**
   * Creates a unique filename for the given binary resource, if the resource provides a fingerprint.
   */
  public static String createFilename(BinaryResource binaryResource) {
    if (binaryResource == null || !binaryResource.hasFilename()) {
      return null;
    }
    if (binaryResource.getFingerprint() <= 0) {
      return binaryResource.getFilename();
    }
    return binaryResource.getFingerprint() + "/" + binaryResource.getFilename();
  }

  /**
   * Creates an URL to be used to reference the given binary resource in an HTML fragment. If the resource provides a
   * fingerprint, the filename in the URL will have a fingerprint too.
   */
  public static String createUrl(BinaryResource binaryResource) {
    return createUrl(createFilename(binaryResource));
  }

  /**
   * Creates an URL to be used to reference the given binary resource in an HTML fragment. Since only a filename is
   * given, the method does not contain a fingerprint.
   * <p>
   * Note: whenever you have an instance of a binary resource, you should use the method
   * {@link #createUrl(BinaryResource)} instead of this method. The fingerprint helps to avoid multiple state and
   * caching problems.
   */
  public static String createUrl(String filename) {
    if (filename == null) {
      return null;
    }
    return URL_PREFIX + filename;
  }

}
