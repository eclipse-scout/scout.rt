/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.action.tree;

import org.eclipse.scout.rt.client.extension.ui.action.AbstractActionExtension;
import org.eclipse.scout.rt.client.ui.action.tree.AbstractActionNode;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;

public abstract class AbstractActionNodeExtension<T extends IActionNode, OWNER extends AbstractActionNode<T>> extends AbstractActionExtension<OWNER> implements IActionNodeExtension<T, OWNER> {

  public AbstractActionNodeExtension(OWNER owner) {
    super(owner);
  }
}
