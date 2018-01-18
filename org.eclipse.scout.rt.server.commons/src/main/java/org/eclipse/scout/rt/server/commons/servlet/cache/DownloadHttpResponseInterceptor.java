/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.servlet.cache;

import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Sets the <code>Content-Disposition</code> HTTP header for downloads (with value <code>attachment</code>).
 * Additionally, a hint for the filename is added according to RFC 5987, both in UTF-8 and ISO-8859-1 encoding.
 * Newlines, tabs, and other control characters in filenames are removed, as Internet Explorer cannot parse or download
 * filenames containing these.
 * <p>
 * This interceptor is useful when the user requested the download of a resource ("save as" dialog). Do not use it for
 * inline resources such as images for ImageField.
 *
 * @see http://stackoverflow.com/questions/93551/how-to-encode-the-filename-parameter-of-content-disposition-header-in-http
 * @see https://stackoverflow.com/questions/18337630/what-is-x-content-type-options-nosniff
 * @see http://tools.ietf.org/html/rfc6266#section-5
 * @see http://tools.ietf.org/html/rfc6266#appendix-D
 */
public class DownloadHttpResponseInterceptor implements IHttpResponseInterceptor {
  private static final long serialVersionUID = 1L;

  public static final String DEFAULT_FILENAME = "Download";

  private final String m_contentDispositionHeaderValue;
  private final String m_contentTypeOptionsHeaderValue;

  public DownloadHttpResponseInterceptor(String originalFilename) {
    m_contentDispositionHeaderValue = calcContentDispositionHeaderValue(originalFilename);
    m_contentTypeOptionsHeaderValue = calcContentTypeOptionsHeaderValue();
  }

  protected String calcContentDispositionHeaderValue(String originalFilename) {
    // Internet Explorer 11 cannot parse names with characters 0x00-0x1F, neither in filename= nor encoded in filename*=
    // Note: 0x00-0x1F are the same in UTF-16 and ISO-8859-1, thus replacing them here is safe.
    if (originalFilename != null) {
      originalFilename = originalFilename.replaceAll("[\\x00-\\x1F]", "");
    }

    if (StringUtility.isNullOrEmpty(originalFilename)) {
      originalFilename = DEFAULT_FILENAME;
    }

    String isoFilename = getIsoFilename(originalFilename);
    //remove ", because it is used to encapsulate the file name
    isoFilename = StringUtility.replace(isoFilename, "\"", "");
    if (StringUtility.isNullOrEmpty(isoFilename)) { // in case no valid character remains
      isoFilename = DEFAULT_FILENAME;
    }

    return "attachment; filename=\"" + isoFilename + "\"; filename*=utf-8''" + IOUtility.urlEncode(originalFilename);
  }

  protected String calcContentTypeOptionsHeaderValue() {
    return "nosniff";
  }

  protected final String getContentDispositionHeaderValue() {
    return m_contentDispositionHeaderValue;
  }

  protected final String getContentTypeOptionsHeaderValue() {
    return m_contentTypeOptionsHeaderValue;
  }

  @Override
  public void intercept(HttpServletRequest req, HttpServletResponse resp) {
    resp.setHeader("Content-Disposition", m_contentDispositionHeaderValue);
    resp.setHeader("X-Content-Type-Options", m_contentTypeOptionsHeaderValue);
  }

  /**
   * Returns the given filename in ISO-8859-1. All characters that are not part of this charset are stripped.
   */
  protected String getIsoFilename(String originalFilename) {
    String isoFilename = originalFilename;
    CharsetEncoder iso8859Encoder = StandardCharsets.ISO_8859_1.newEncoder();
    if (iso8859Encoder.canEncode(originalFilename)) {
      return isoFilename;
    }

    StringBuilder sb = new StringBuilder(originalFilename.length() - 1);
    for (char c : originalFilename.toCharArray()) {
      if (c != '"' && iso8859Encoder.canEncode(c)) {
        sb.append(c);
      }
    }
    return sb.toString();
  }
}
