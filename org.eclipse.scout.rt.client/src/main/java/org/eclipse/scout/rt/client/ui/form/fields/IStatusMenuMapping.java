/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;

/**
 * Defines which menu should be displayed when a field error status is shown.
 * <p>
 * In order to display the menu only when a certain status code or severity is active, use {@link #setCodes(List)} or
 * {@link #setSeverities(List)} to define the restriction.
 * <p>
 * In order to define a mapping for a certain form field use {@link IFormField#setStatusMenuMappings(List)} or define an
 * inner class at the form field extending from {@link AbstractStatusMenuMapping} and use the provided getConfigured*
 * methods.
 * <p>
 * The menu has to be a menu of the form field, otherwise it won't be displayed.
 */
public interface IStatusMenuMapping extends IPropertyObserver {
  String PROP_MENU = "menu";
  String PROP_CODES = "codes";
  String PROP_SEVERITIES = "severities";

  void init();

  void setParentFieldInternal(IFormField parentField);

  void setSeverities(List<Integer> severities);

  List<Integer> getSeverities();

  void setCodes(List<Integer> codes);

  List<Integer> getCodes();

  void setMenu(IMenu menu);

  IMenu getMenu();
}
