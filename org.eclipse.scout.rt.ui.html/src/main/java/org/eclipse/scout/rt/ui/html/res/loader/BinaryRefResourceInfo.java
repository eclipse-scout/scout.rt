/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.res.loader;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.resource.BinaryRefs;
import org.eclipse.scout.rt.platform.util.IOUtility;

public class BinaryRefResourceInfo {

  /**
   * Pattern to determine if the provided url path is a binaryRef resource path. Allow an additional / at the start.
   */
  public static final Pattern PATTERN_BINARY_REF_RESOURCE_PATH = Pattern.compile("^/?" + BinaryRefs.URI_SCHEME + "/([^/]*/.*)$");

  private final String m_binaryRef;

  public BinaryRefResourceInfo(String binaryRef) {
    m_binaryRef = binaryRef;
  }

  public String toPath() {
    String binaryRef = IOUtility.urlEncode(getBinaryRef());
    // / was encoded by %2F, revert this encoding
    binaryRef = binaryRef.replace("%2F", "/");
    return BinaryRefs.URI_SCHEME + (binaryRef.startsWith("/") ? "" : "/") + binaryRef;
  }

  public URI toBinaryRefUri() {
    return URI.create(BinaryRefs.URI_SCHEME + ":" + (getBinaryRef().startsWith("/") ? "" : "/") + getBinaryRef());
  }

  public String getBinaryRef() {
    return m_binaryRef;
  }

  /**
   * @param path
   *     decoded path (non url-encoded)
   */
  public static BinaryRefResourceInfo fromPath(String path) {
    BinaryRefResourcePathComponents parts = BinaryRefResourcePathComponents.fromPath(path);
    if (parts == null) {
      return null;
    }
    return new BinaryRefResourceInfo(parts.getBinaryRef());
  }

  protected static class BinaryRefResourcePathComponents {
    String m_binaryRef;

    BinaryRefResourcePathComponents(String binaryRef) {
      m_binaryRef = binaryRef;
    }

    public String getBinaryRef() {
      return m_binaryRef;
    }

    /**
     * @param path
     *     decoded path (non url-encoded)
     * @see BinaryRefResourceInfo#fromPath(String)
     */
    public static BinaryRefResourcePathComponents fromPath(String path) {
      if (path == null) {
        return null;
      }

      Matcher m = PATTERN_BINARY_REF_RESOURCE_PATH.matcher(path);
      if (!m.matches()) {
        return null;
      }

      String binaryRef = m.group(1);
      return new BinaryRefResourcePathComponents(binaryRef);
    }
  }
}
