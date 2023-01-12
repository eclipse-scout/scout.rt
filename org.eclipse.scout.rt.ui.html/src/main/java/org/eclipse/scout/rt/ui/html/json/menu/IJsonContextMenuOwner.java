/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.menu;

import org.eclipse.scout.rt.ui.html.json.FilteredJsonAdapterIds;

@FunctionalInterface
public interface IJsonContextMenuOwner {

  String PROP_MENUS = "menus";
  String PROP_MENUS_VISIBLE = "menusVisible";
  String PROP_CURRENT_MENU_TYPES = "currentMenuTypes";

  void handleModelContextMenuChanged(FilteredJsonAdapterIds<?> filteredAdapters);
}
