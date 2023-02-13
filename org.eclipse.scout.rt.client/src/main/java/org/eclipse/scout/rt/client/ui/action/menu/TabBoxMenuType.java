/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.menu;

/**
 * All possible menu types of a tab box menu. These menu types are used by
 * {@link AbstractMenu#getConfiguredMenuTypes()}.
 */
public enum TabBoxMenuType implements IMenuType {
  /**
   * In most cases, it is not necessary to set this menu type for a tab box menu because it does not affect the
   * visibility of the menu unless the menu is used for widgets other than the tab box. In this case, the menu type can
   * be used to ensure that the menu is only visible on tab boxes.
   */
  Header
}
