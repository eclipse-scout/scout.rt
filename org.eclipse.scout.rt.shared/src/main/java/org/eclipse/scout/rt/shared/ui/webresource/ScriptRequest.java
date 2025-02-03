/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.ui.webresource;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Represents a script resource request path. Such requests include js and css/less files and are always of the form
 * {@link #SCRIPT_URL_PATTERN}.
 */
public final class ScriptRequest {

  /**
   * Script-File name marker for minimized scripts
   */
  public static final String MINIMIZED_URL_KEYWORD = "min";

  /**
   * Pattern for a script url
   * <p>
   * <b>Regex groups:</b> <code>$1$2[-$3][.$4].$5</code><br>
   * <ul>
   * <li><code>$1</code> = path (optional)
   * <li><code>$2</code> = basename
   * <li><code>$3</code> = fingerprint (optional)
   * <li><code>$4</code> = min (optional)
   * <li><code>$5</code> = <code>"js"</code> or <code>"css"</code>
   * </ul>
   * Examples:
   *
   * <pre>
   * path/basename.js
   * path/entryPoint1~entryPoint2.min.js
   * path/basename-34fce3bc.min.js
   * basename.css
   * </pre>
   */
  public static final Pattern SCRIPT_URL_PATTERN = Pattern.compile("([^\"']*/)?([-_.~\\w\\d]+?)(?:-([a-f0-9]+))?(?:\\.(" + MINIMIZED_URL_KEYWORD + ")?)?\\.(js|css)");

  private final String m_path;
  private final String m_baseName;
  private final String m_fingerprint;
  private final boolean m_minimized;
  private final String m_fileExtension;

  private ScriptRequest(Matcher m) {
    m_path = m.group(1);
    m_baseName = m.group(2);
    m_fingerprint = m.group(3);
    m_minimized = MINIMIZED_URL_KEYWORD.equals(m.group(4));
    m_fileExtension = m.group(5);
  }

  /**
   * Parses a request {@link String} into an optional {@link ScriptRequest} instance.
   *
   * @return An empty {@link Optional} if the {@link String} specified is no valid script request. Otherwise the
   * {@link Optional} contains a parsed {@link ScriptRequest} instance.
   */
  public static Optional<ScriptRequest> tryParse(String scriptRequestPath) {
    if (!StringUtility.hasText(scriptRequestPath)) {
      return Optional.empty();
    }

    Matcher matcher = SCRIPT_URL_PATTERN.matcher(scriptRequestPath);
    if (!matcher.matches()) {
      return Optional.empty();
    }

    return Optional.of(new ScriptRequest(matcher));
  }

  /**
   * Parses the specified elements into an optional {@link ScriptRequest} instance.
   *
   * @param path
   *     The parent path of the resource having a trailing slash. May be {@code null} but must be specified to be
   *     valid according to {@link #SCRIPT_URL_PATTERN}.
   * @param baseName
   *     The file name (without extension and trailing dot). May be {@code null} but must be specified to be valid
   *     according to {@link #SCRIPT_URL_PATTERN}.
   * @param fingerprint
   *     A hex fingerprint (see {@link BinaryResource#getFingerprintAsHexString()) for the resource. May be
   *     {@code null}.
   * @param min
   *     {@code true} if the path should contain the minimize keyword. {@code false} otherwise.
   * @param extension
   *     The file extension without leading dot. May be {@code null} but must be 'js', 'css' or 'less' to be valid
   *     according to {@link #SCRIPT_URL_PATTERN}.
   * @return An empty {@link Optional} if the elements specified are not valid. Otherwise the {@link Optional} contains
   * a parsed {@link ScriptRequest} instance.
   */
  public static Optional<ScriptRequest> tryParse(String path, String baseName, String fingerprint, boolean min, String extension) {
    return tryParse(toFullPath(path, baseName, fingerprint, min, extension));
  }

  /**
   * @return The path segment of the request including a trailing slash (if present). May be {@code  null} if the
   * request does not contain a path information.
   */
  public String path() {
    return m_path;
  }

  /**
   * @return The basename of the resource which is the resource filename without extension. This element is always
   * present (never {@code null}).
   */
  public String baseName() {
    return m_baseName;
  }

  /**
   * @return The hex fingerprint of the request (without a preceding dash). May be {@code null} if no fingerprint was
   * included in the request (e.g. in dev mode).
   * @see BinaryResource#getFingerprintAsHexString()
   */
  public String fingerprint() {
    return m_fingerprint;
  }

  /**
   * @return {@code true} if the request included the minimized keyword (see {@link #MINIMIZED_URL_KEYWORD}).
   * {@code false} otherwise.
   */
  public boolean minimized() {
    return m_minimized;
  }

  /**
   * @return The file extension of the requested file (without preceding dot). Can be 'js', 'css' or 'less'.
   */
  public String fileExtension() {
    return m_fileExtension;
  }

  /**
   * @return The full request path. This is the same path as this instance was created from.
   */
  public String fullPath() {
    return toString(true, true);
  }

  /**
   * @return The request path without the fingerprint. This is the path where the resource should be located on the
   * classpath
   */
  public String lookupPath() {
    return toString(false, true);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScriptRequest that = (ScriptRequest) o;
    return m_minimized == that.m_minimized
        && Objects.equals(m_path, that.m_path)
        && Objects.equals(m_baseName, that.m_baseName)
        && Objects.equals(m_fingerprint, that.m_fingerprint)
        && Objects.equals(m_fileExtension, that.m_fileExtension);
  }

  @Override
  public int hashCode() {
    return Objects.hash(m_path, m_baseName, m_fingerprint, m_minimized, m_fileExtension);
  }

  @Override
  public String toString() {
    return fullPath();
  }

  /**
   * Converts this {@link ScriptRequest} into its {@link String} representation.
   *
   * @param fingerprint
   *     {@code true} if the fingerprint (if available) should be included in the resulting path.
   * @param min
   *     {@code true} if the minimize keyword should be included in the resulting path. It can only be included if
   *     this request has the minimized flag.
   * @return The request as {@link String} optionally including fingerprint and/or minimize keyword.
   * @see #toString(boolean, boolean, boolean, boolean, boolean)
   * @see #fullPath()
   */
  public String toString(boolean fingerprint, boolean min) {
    return toString(true, true, fingerprint, min, true);
  }

  /**
   * Converts this {@link ScriptRequest} into its {@link String} representation.
   *
   * @param path
   *     {@code true} if the path should be included in the resulting path.
   * @param baseName
   *     {@code true} if the baseName should be included in the resulting path.
   * @param fingerprint
   *     {@code true} if the fingerprint (if available) should be included in the resulting path.
   * @param min
   *     {@code true} if the minimize keyword should be included in the resulting path. It can only be included if
   *     this request has the minimized flag.
   * @param extension
   *     {@code true} if the extension should be included in the resulting path.
   * @return The request as {@link String} having all the enabled components.
   * @see #toString(boolean, boolean)
   * @see #fullPath()
   */
  public String toString(boolean path, boolean baseName, boolean fingerprint, boolean min, boolean extension) {
    return toFullPath(path ? path() : null, baseName ? baseName() : null, fingerprint ? fingerprint() : null, min && minimized(), extension ? fileExtension() : null);
  }

  /**
   * Creates a script request path having the specified components.
   *
   * @param path
   *     The parent path of the resource having a trailing slash. May be {@code null} but must be specified to be
   *     valid according to {@link #SCRIPT_URL_PATTERN}.
   * @param baseName
   *     The file name (without extension and trailing dot). May be {@code null} but must be specified to be valid
   *     according to {@link #SCRIPT_URL_PATTERN}.
   * @param fingerprint
   *     A hex fingerprint (see {@link BinaryResource#getFingerprintAsHexString()) for the resource. May be
   *     {@code null}.
   * @param min
   *     {@code true} if the path should contain the minimize keyword. {@code false} otherwise.
   * @param extension
   *     The file extension without leading dot. May be {@code null} but must be 'js', 'css' or 'less' to be valid
   *     according to {@link #SCRIPT_URL_PATTERN}.
   * @return A script request path with the specified components that conforms to the format defined by
   * {@link #SCRIPT_URL_PATTERN}. It can only be parsed using {@link #tryParse(String)} if the specified
   * components are valid.
   */
  public static String toFullPath(String path, String baseName, String fingerprint, boolean min, String extension) {
    StringBuilder result = new StringBuilder();
    if (path != null && !"./".equals(path)) {
      result.append(path);
    }
    if (baseName != null) {
      result.append(baseName);
    }
    if (StringUtility.hasText(fingerprint)) {
      result.append('-').append(fingerprint);
    }
    if (min) {
      result.append('.').append(MINIMIZED_URL_KEYWORD);
    }
    if (StringUtility.hasText(extension)) {
      result.append('.').append(extension);
    }
    return result.toString();
  }
}
