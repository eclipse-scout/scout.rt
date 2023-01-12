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
