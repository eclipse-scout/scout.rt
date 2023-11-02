/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.tree;

public enum AutoCheckStyle {
  /**
   * No nodes will be auto-checked
   */
  NONE,

  /**
   * All child nodes will be checked/unchecked together with their parent
   */
  CHILDREN,

  /**
   * The state of the node is a representation of its children.
   * <ul>
   * <li>When none of the children are checked, the node is unchecked</li>
   * <li>When some of the children are checked, the node is partly checked</li>
   * <li>When all of the children are checked, the node is also checked</li>
   * </ul>
   * When a node with children is selected, its children will be auto-checked
   */
  CHILDREN_AND_PARENT
}
