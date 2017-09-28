/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.dimension;

/**
 * <h3>{@link IDimensions}</h3><br>
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
   * Dimension for a custom enabled flag
   */
  String ENABLED_CUSTOM = "ENABLED_CUSTOM";
}
