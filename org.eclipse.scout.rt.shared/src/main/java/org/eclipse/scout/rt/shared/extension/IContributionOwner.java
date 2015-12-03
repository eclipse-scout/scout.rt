/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.extension;

import java.util.List;

/**
 * This interface describes objects that are capable to hold contributions.<br>
 * Contributions may be added using the {@link IExtensionRegistry} service.
 *
 * @since 4.2
 * @see IExtensionRegistry
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
   *          The filter class to use. Must not be <code>null</code>.
   * @return A {@link List} containing all contribution instances which are <code>instanceof</code> the given type
   *         class.
   * @throws IllegalArgumentException
   *           when the type parameter is null.
   */
  <T> List<T> getContributionsByClass(Class<T> type);

  /**
   * Returns the contribution that exactly matches the given contribution class.
   *
   * @param contribution
   *          The class of the contribution to return. Must not be null.
   * @return The contribution instance that is exactly of the given class type.
   * @throws IllegalExtensionException
   *           when no extension with the given class can be found.
   * @throws IllegalArgumentException
   *           when the contribution parameter is null.
   */
  <T> T getContribution(Class<T> contribution);
}
