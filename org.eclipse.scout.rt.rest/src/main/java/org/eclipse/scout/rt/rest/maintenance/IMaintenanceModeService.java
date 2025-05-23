/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.maintenance;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * <p>Interface for an application scoped bean to determine whether maintenance mode is currently active/current session may override it.</p>
 * <p>An implementation must be provided if {@link MaintenanceMode} annotation is used.</p>
 *
 * @see MaintenanceMode
 */
@ApplicationScoped
public interface IMaintenanceModeService {

  /**
   * @return true if maintenance mode is active
   */
  boolean isActive();

  /**
   * @return true if specific request/session may override maintenance mode
   */
  boolean isGrantOverride();
}
