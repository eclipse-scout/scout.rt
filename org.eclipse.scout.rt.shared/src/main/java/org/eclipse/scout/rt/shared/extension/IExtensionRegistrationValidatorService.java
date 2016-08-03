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

import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.service.IService;

/**
 * Validator service used by the {@link IExtensionRegistry} to check if a contribution or move is allowed.<br>
 * The {@link IExtensionRegistry} calls all validator services. A contribution or move is considered to be valid as soon
 * as the first service (ordered by priority) declares it as valid.
 *
 * @since 4.2
 * @see IExtensionRegistry
 */
public interface IExtensionRegistrationValidatorService extends IService {
  /**
   * Checks if the given contribution is allowed or not.
   *
   * @param contribution
   *          The contribution class to check. Is never null.
   * @param container
   *          The container into the given contribution should be added. Is never null.
   * @return <code>true</code> if the given contribution is allowed for the given container. <code>false</code>
   *         otherwise.
   * @throws IllegalExtensionException
   *           May be used to throw a more detailed exception. Throwing such an {@link IllegalExtensionException} also
   *           declares the contribution to be NOT valid.
   */
  boolean isValidContribution(Class<?> contribution, Class<?> container);

  /**
   * Checks if the given move is allowed or not.<br>
   *
   * @param modelClass
   *          The class that should be moved. Is never null.
   * @param newContainerClass
   *          The new container the given model class should be moved to. Is never null.
   * @return <code>true</code> if the given move is allowed. <code>false</code> otherwise.
   * @throws IllegalExtensionException
   *           May be used to throw a more detailed exception. Throwing such an {@link IllegalExtensionException} also
   *           declares the move to be NOT valid.
   */
  boolean isValidMove(Class<? extends IOrdered> modelClass, Class<? extends IOrdered> newContainerClass);
}
