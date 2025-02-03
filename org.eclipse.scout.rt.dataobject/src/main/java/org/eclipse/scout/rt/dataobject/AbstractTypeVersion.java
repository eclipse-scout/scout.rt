/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Abstract implementation of a type version with support for extracting {@link NamespaceVersion} based on simple class
 * name and referencing classes of {@link ITypeVersion} as dependencies.
 */
public abstract class AbstractTypeVersion implements ITypeVersion {

  private final NamespaceVersion m_version;
  private final Collection<NamespaceVersion> m_dependencies;

  /**
   * Extracts the {@link NamespaceVersion} from the class name (first letter of namespace is decapitalized).
   * <p>
   * Examples:
   * <ul>
   * <li>Scout_8_0_0 -> scout-8.0.0</li>
   * <li>Lorem_1 -> lorem-1</li>
   * </ul>
   */
  protected AbstractTypeVersion() {
    m_version = assertNotNull(fromClassName(getClass()), "version is required");
    m_dependencies = resolveDependencies(getDependencyClasses());
  }

  /**
   * @param version
   *     Version
   */
  protected AbstractTypeVersion(NamespaceVersion version) {
    m_version = assertNotNull(version, "version is required");
    m_dependencies = resolveDependencies(getDependencyClasses());
  }

  @Override
  public final NamespaceVersion getVersion() {
    return m_version;
  }

  @Override
  public final Collection<NamespaceVersion> getDependencies() {
    return m_dependencies;
  }

  /**
   * These versions are used to determine a list of dependant type versions across all namespaces.
   * <p>
   * Declare at most one dependant type version of the same namespace. Type versions of the same namespace must not be
   * listed because within the same namespace, the type versions are comparable (see
   * {@link NamespaceVersion#compareVersion(NamespaceVersion, NamespaceVersion)}).
   *
   * @return Type version classes that occur before the current one (never <code>null</code>)
   */
  protected Collection<Class<? extends ITypeVersion>> getDependencyClasses() {
    return Collections.emptyList();
  }

  /**
   * Optional suffix (class name only) starting with __ followed by any word characters.
   */
  private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("(\\w+?)_(\\d+(?:_\\d+)*)(?:__\\w+)?");

  static NamespaceVersion fromClassName(Class<? extends ITypeVersion> typeVersionClass) {
    if (typeVersionClass == null) {
      return null;
    }
    Matcher m = CLASS_NAME_PATTERN.matcher(typeVersionClass.getSimpleName());
    if (!m.matches()) {
      throw new IllegalArgumentException(String.format("Class name does not match expected pattern [simpleName='%s', expectedPattern=%s]", typeVersionClass.getSimpleName(), CLASS_NAME_PATTERN));
    }
    return NamespaceVersion.of(StringUtility.lowercaseFirst(m.group(1)), m.group(2).replace('_', '.'));
  }

  static List<NamespaceVersion> resolveDependencies(Collection<Class<? extends ITypeVersion>> dependencyClasses) {
    return dependencyClasses.stream().map(dependencyClass -> BEANS.get(dependencyClass).getVersion()).collect(Collectors.toUnmodifiableList());
  }
}
