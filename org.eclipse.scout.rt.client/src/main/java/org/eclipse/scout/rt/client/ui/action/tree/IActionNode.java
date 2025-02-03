/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.tree;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.IAction;

public interface IActionNode<T extends IActionNode> extends IAction {

  /**
   * property-type: {@link List}&lt;T&gt;
   */
  String PROP_CHILD_ACTIONS = "childActions";

  /**
   * @return if child has actions
   */
  boolean hasChildActions();

  int getChildActionCount();

  /**
   * @return a copy of the list of actions<br>
   * When changing this list, use {@link #setChildActions(Collection)} to apply them to the model
   */
  List<T> getChildActions();

  void setChildActions(Collection<? extends T> actionList);

  void addChildAction(T action);

  void addChildActions(Collection<? extends T> actionList);

  void removeChildAction(T action);

  void removeChildActions(Collection<? extends T> actionList);
}
