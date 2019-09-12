/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.resource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * This utility class is used to create unique filenames and URLs with a fingerprint for binary resources. Since binary
 * resources may be referenced in a HTML code and HTML code is often generated in the Scout server this class is in the
 * platform.
 */
public final class BinaryResourceUtility {

  public static final String URL_PREFIX = "binaryResource:";

  private static final Pattern REGEX_FINGERPRINT_PATTERN = Pattern.compile("^(([0-9]+)\\/)?(.*)$");
  private static final Pattern REGEX_URL_FILENAME_PATTERN = Pattern.compile(".*\\/(.*)$");

  private BinaryResourceUtility() {
  }

  /**
   * Creates a unique fingerprint/filename path for the given binary resource, if the resource provides a fingerprint.
   */
  public static String createFilenameWithFingerprint(BinaryResource binaryResource) {
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
    return createUrl(createFilenameWithFingerprint(binaryResource));
  }

  /**
   * Creates an URL to be used to reference the given binary resource in an HTML fragment. Since only a filename is
   * given, the method does not contain a fingerprint.
   * <p>
   * Note: whenever you have an instance of a binary resource, you should use the method
   * {@link #createUrl(BinaryResource)} instead of this method. The fingerprint helps to avoid multiple state and
   * caching problems.
   */
  public static String createUrl(String path) {
    if (path == null) {
      return null;
    }
    return URL_PREFIX + path;
  }

  /**
   * Get the path part of a binaryResource: URL.
   */
  public static String getPathFromUrl(String url) {
    if (url == null) {
      return null;
    }
    if (!url.startsWith(URL_PREFIX)) {
      return null;
    }
    return url.substring(URL_PREFIX.length());
  }

  /**
   * Get the filename part of a binaryResource:fingerprint/filename URL
   */
  public static String getFilenameFromUrl(String url) {
    String path = getPathFromUrl(url);
    if (path == null) {
      return null;
    }
    Matcher m = REGEX_URL_FILENAME_PATTERN.matcher(path);
    if (m.find()) {
      // last path segment
      return m.group(1);
    }
    // no segments
    return path;
  }

  /**
   * If the given string is not null but does not contain a fingerprint part, 0L is returned as the fingerprint.
   *
   * @return non-null Pair
   */
  public static Pair<String, Long> extractFilenameWithFingerprint(String path) {
    if (path == null) {
      return null;
    }
    Matcher m = REGEX_FINGERPRINT_PATTERN.matcher(path);
    String fingerprintString = null;
    String filename = null;
    if (m.find()) {
      fingerprintString = m.group(2);
      filename = m.group(3);
    }
    Long fingerprint = 0L;
    if (StringUtility.hasText(fingerprintString)) {
      fingerprint = Long.valueOf(fingerprintString);
    }

    return new ImmutablePair<>(filename, fingerprint);
  }
}
