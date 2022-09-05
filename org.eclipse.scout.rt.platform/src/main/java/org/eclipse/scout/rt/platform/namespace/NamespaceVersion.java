/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.namespace;

import static java.util.Objects.requireNonNull;
import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Class representing a version with a namespace.
 * <p>
 * Examples:
 * <ul>
 * <li>scout-10.0.1</li>
 * <li>ipsum-1.2</li>
 * </ul>
 */
public final class NamespaceVersion {

  private static final Pattern NAMESPACE_VERSION_PATTERN = Pattern.compile("(\\w+)-((\\d+\\.?)+)");
  private static final Pattern VERSION_PATTERN = Pattern.compile("((\\d+\\.?)+)");
  private static final Pattern VERSION_PARTS_SPLIT_PATTERN = Pattern.compile(Pattern.quote("."));

  private final String m_namespace;
  private final String m_versionText; // not part of equals/hash code
  private final int[] m_versionParts;

  /**
   * @param namespaceVersion
   *          string with a namespace prefix: [namespace]-[version number]
   * @return new {@link NamespaceVersion} or <code>null</code> if input is <code>null</code> or empty.
   */
  public static NamespaceVersion of(String namespaceVersion) {
    if (StringUtility.isNullOrEmpty(namespaceVersion)) {
      return null;
    }

    Matcher m = NAMESPACE_VERSION_PATTERN.matcher(namespaceVersion);
    if (!m.matches()) {
      throw new IllegalArgumentException(String.format("given string does not match expected namespace version pattern [s='%s', expectedPattern=%s]", namespaceVersion, NAMESPACE_VERSION_PATTERN));
    }

    return of(m.group(1), m.group(2));
  }

  /**
   * @return new {@link NamespaceVersion} or <code>null</code> if either namespace or version is <code>null</code>.
   */
  public static NamespaceVersion of(String namespace, String version) {
    if (namespace == null || version == null) {
      return null;
    }

    Matcher m = VERSION_PATTERN.matcher(version);
    if (!m.matches()) {
      throw new IllegalArgumentException(String.format("given string does not match expected version pattern [s='%s', expectedPattern=%s]", version, VERSION_PATTERN));
    }

    int[] versionParts = VERSION_PARTS_SPLIT_PATTERN.splitAsStream(version).mapToInt(Integer::valueOf).toArray();
    return new NamespaceVersion(namespace, version, versionParts);
  }

  /**
   * Use {{@link #of(String)}} or {@link #of(String, String)} to construct an instance of {@link NamespaceVersion}.
   */
  NamespaceVersion(String namespace, int... versionParts) {
    this(namespace, null, versionParts);
  }

  private NamespaceVersion(String namespace, String versionText, int... versionParts) {
    m_namespace = requireNonNull(namespace, "namespace is required");
    m_versionText = versionText;
    m_versionParts = requireNonNull(versionParts, "versionParts is required");
  }

  public String getNamespace() {
    return m_namespace;
  }

  public String getVersionText() {
    return m_versionText;
  }

  /**
   * Method may only be called if instance is created by either {{@link #of(String)}} or {@link #of(String, String)}
   * (version text is available).
   *
   * @return Full version string
   */
  public String unwrap() {
    assertNotNull(m_versionText, "Version text is missing");
    return m_namespace + "-" + m_versionText;
  }

  /**
   * @return <code>true</code> if both versions have the same namespace
   */
  public boolean namespaceEquals(NamespaceVersion obj) {
    if (obj == null) {
      return false;
    }
    return m_namespace.equals(obj.m_namespace);
  }

  /**
   * @return compares only the version number parts.
   */
  public static int compareVersion(NamespaceVersion a, NamespaceVersion b) {
    int commonPartsLength = Math.min(
        requireNonNull(a, "a is required").m_versionParts.length,
        requireNonNull(b, "b is required").m_versionParts.length);

    for (int i = 0; i < commonPartsLength; i++) {
      int res = Integer.compare(a.m_versionParts[i], b.m_versionParts[i]);
      if (res != 0) {
        return res;
      }
    }

    return Integer.compare(a.m_versionParts.length, b.m_versionParts.length);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_namespace == null) ? 0 : m_namespace.hashCode());
    result = prime * result + Arrays.hashCode(m_versionParts);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    NamespaceVersion other = (NamespaceVersion) obj;
    if (m_namespace == null) {
      if (other.m_namespace != null) {
        return false;
      }
    }
    else if (!m_namespace.equals(other.m_namespace)) {
      return false;
    }
    if (!Arrays.equals(m_versionParts, other.m_versionParts)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", NamespaceVersion.class.getSimpleName() + "[", "]")
        .add("m_namespace='" + m_namespace + "'")
        .add("m_versionText='" + m_versionText + "'")
        .add("m_versionParts=" + Arrays.toString(m_versionParts))
        .toString();
  }
}
