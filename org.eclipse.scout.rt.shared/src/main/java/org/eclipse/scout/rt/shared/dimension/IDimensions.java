/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.dimension;

/**
 * Contains the dimension names.
 */
public interface IDimensions {

  /**
   * Dimension name for the visible flag
   */
  String VISIBLE = "VISIBLE";

  /**
   * Dimension name for the visible-granted flag
   */
  String VISIBLE_GRANTED = "VISIBLE_GRANTED";

  /**
   * Dimension for a custom visibility flag
   */
  String VISIBLE_CUSTOM = "VISIBLE_CUSTOM";

  /**
   * Dimension for the enabled flag
   */
  String ENABLED = "ENABLED";

  /**
   * Dimension for the enabled-granted flag
   */
  String ENABLED_GRANTED = "ENABLED_GRANTED";

  /**
   * Dimension for internal secondary dependance between components
   */
  String ENABLED_SLAVE = "ENABLED_SLAVE";

  /**
   * Dimension for a custom enabled flag
   */
  String ENABLED_CUSTOM = "ENABLED_CUSTOM";
}
