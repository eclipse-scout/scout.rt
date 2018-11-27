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
package org.eclipse.scout.rt.client.ui.action.menu;

/**
 * Represents a read-only menu that wraps an existing normal menu.
 * <p>
 * All write operations on such a menu have either no effect or throw {@link UnsupportedOperationException}s.
 */
public interface IReadOnlyMenu extends IMenu {

  /**
   * @return The wrapped original menu.
   */
  IMenu getWrappedMenu();

}
