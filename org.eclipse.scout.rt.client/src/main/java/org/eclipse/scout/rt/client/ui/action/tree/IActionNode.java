/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.tree;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.IAction;

public interface IActionNode<T extends IActionNode> extends IAction {

  /**
   * property-type: {@link List}&lt;T&gt;
   */
  String PROP_CHILD_ACTIONS = "childActions";

  /*
   * Runtime
   */
  /**
   * @since 3.8.1
   */
  T getParent();

  /**
   * @since 3.8.1
   */
  void setParent(T parent);

  /**
   * @return if child has actions
   */
  boolean hasChildActions();

  int getChildActionCount();

  /**
   * @return a copy of the list of actions<br>
   *         When changing this list, use {@link #setChildActions(Collection)} to apply them to the model
   */
  List<T> getChildActions();

  void setChildActions(Collection<? extends T> actionList);

  void addChildAction(T action);

  void addChildActions(Collection<? extends T> actionList);

  void removeChildAction(T action);

  void removeChildActions(Collection<? extends T> actionList);
}
