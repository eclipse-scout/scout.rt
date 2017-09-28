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
package org.eclipse.scout.rt.platform.util;

/**
 * Represents a <em>handle</em> for a registration, and can later be used to undo the registration.
 *
 * @since 5.2
 */
@FunctionalInterface
public interface IRegistrationHandle extends IDisposable {

  /**
   * Unregisters the registration represented by this handle. This call has no effect if already disposed.
   */
  @Override
  void dispose();

  /**
   * Handle that does nothing upon {@link #dispose()}.
   */
  IRegistrationHandle NULL_HANDLE = () -> {
    // NOOP
  };
}
