/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.service;

/**
 * Allows to get inventory information about services.
 */
@FunctionalInterface
public interface IServiceInventory {

  /**
   * @return plain or html styled text describing the current inventory of the service
   */
  String getInventory();
}
