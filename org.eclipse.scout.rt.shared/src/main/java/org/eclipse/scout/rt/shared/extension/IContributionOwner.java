/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension;

import java.util.List;

/**
 * This interface describes objects that are capable to hold contributions.<br>
 * Contributions may be added using the {@link IExtensionRegistry} service.
 *
 * @see IExtensionRegistry
 * @since 4.2
 */
public interface IContributionOwner {
  /**
   * Returns all contributions that are available for this object.
   *
   * @return {@link List} containing all contribution instances that belong to this object.
   */
  List<Object> getAllContributions();

  /**
   * Returns all contributions that are <code>instanceof</code> the given filter class.
   *
   * @param type
   *     The filter class to use. Must not be <code>null</code>.
   * @return A {@link List} containing all contribution instances which are <code>instanceof</code> the given type
   * class.
   * @throws IllegalArgumentException
   *     when the type parameter is null.
   */
  <T> List<T> getContributionsByClass(Class<T> type);

  /**
   * Returns the contribution that exactly matches the given contribution class.
   *
   * @param contribution
   *     The class of the contribution to return. Must not be {@code null}.
   * @return The contribution instance that is exactly of the given class type. Never returns {@code null}.
   * @throws IllegalExtensionException
   *     if no extension with the given class can be found.
   * @throws IllegalArgumentException
   *     if the contribution parameter is {@code null}.
   */
  <T> T getContribution(Class<T> contribution);

  /**
   * Returns the contribution that exactly matches the given contribution class.
   *
   * @param contribution
   *     The class of the contribution to return. Must not be {@code null}.
   * @return The contribution instance that is exactly of the given class type or {@code null} if it cannot be found.
   * @throws IllegalArgumentException
   *     if the contribution parameter is {@code null}.
   */
  <T> T optContribution(Class<T> contribution);
}
