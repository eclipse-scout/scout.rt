/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.Bean;

/**
 * Calculates the <code>Content-Disposition</code> HTTP header for downloads (with value <code>attachment</code>).
 * Additionally, a hint for the filename is added according to RFC 5987, both in UTF-8 and ISO-8859-1 encoding.
 * Newlines, tabs, and other control characters in filenames are removed, as Internet Explorer cannot parse or download
 * filenames containing these.<br>
 * The <code>X-Content-Type-Options</code> HTTP header is also included when calling {@link #getDownloadHeaders(String)}
 * in order to prevent the browser to sniff the content type by himself and instead adhere to the content type provided
 * in the respective response header.
 * <p>
 * NOTE: This utility was added to the platform for the lack of a better place. May be moved to a more suitable place in
 * future versions.
 * <p>
 * References:
 * <ul>
 * <li>https://stackoverflow.com/questions/93551/how-to-encode-the-filename-parameter-of-content-disposition-header-in-http</li>
 * <li>https://stackoverflow.com/questions/18337630/what-is-x-content-type-options-nosniff</li>
 * <li>https://tools.ietf.org/html/rfc6266#section-5</li>
 * <li>https://tools.ietf.org/html/rfc6266#appendix-D</li>
 * </ul>
 */
@Bean
public class DownloadResponseHelper {

  public static final String HEADER_X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
  public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

  public static final String DEFAULT_FILENAME = "Download";

  /**
   * This method returns the <code>Content-Disposition</code> and <code>X-Content-Type-Options</code> HTTP headers with
   * their respective values in order to construct an HTTP response that contains a download of a file with the provided
   * name.
   *
   * @param filename
   *     The name of the downloaded file. The filename is sanitized to conform to the relevant RFCs. In case it
   *     does not contain any usable characters (or is null), the default value of {@value #DEFAULT_FILENAME} is
   *     used.
   * @return Returns a {@link Map} containing the values for the {@value #HEADER_CONTENT_DISPOSITION} and
   * {@value #HEADER_X_CONTENT_TYPE_OPTIONS} headers. The keys in the map are the names of the respective
   * headers.
   */
  public Map<String, String> getDownloadHeaders(String filename) {
    Map<String, String> downloadHeaders = new HashMap<>();
    downloadHeaders.put(HEADER_CONTENT_DISPOSITION, getContentDispositionHeaderValue(filename));
    downloadHeaders.put(HEADER_X_CONTENT_TYPE_OPTIONS, getContentTypeOptionsHeaderValue());
    return downloadHeaders;
  }

  /**
   * Creates the value of the {@value #HEADER_CONTENT_DISPOSITION} header for an HTTP response that contains a download
   * of a file with the provided name.
   *
   * @param originalFilename
   *     The name of the downloaded file. The filename is sanitized to conform to the relevant RFCs. In case it
   *     does not contain any usable characters (or is null), the default value of {@value #DEFAULT_FILENAME} is
   *     used.
   * @return Returns the constructed header value.
   */
  public String getContentDispositionHeaderValue(String originalFilename) {
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

  /**
   * Creates the value of the {@value #HEADER_X_CONTENT_TYPE_OPTIONS} header for an HTTP response that contains a
   * download.
   *
   * @return Returns <code>nosniff</code>.
   */
  public String getContentTypeOptionsHeaderValue() {
    return "nosniff";
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

    StringBuilder sb = new StringBuilder();
    for (char c : originalFilename.toCharArray()) {
      if (iso8859Encoder.canEncode(c)) {
        sb.append(c);
      }
    }
    return sb.toString();
  }
}
