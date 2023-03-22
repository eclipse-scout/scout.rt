/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ObjectModel, ObjectOrModel, Session, Tree, TreeNode} from '../index';

export interface TreeNodeModel extends ObjectModel<TreeNode> {
  parent?: Tree;
  checked?: boolean;
  childNodes?: ObjectOrModel<TreeNode>[];
  /**
   * Refers to the position of the node in the list of {@link childNodes} of its {@link TreeNode.parentNode}.
   * If the node is a root node, it refers to the position in {@link Tree.nodes}.
   *
   * It is not necessary to provide a childNodeIndex because it will be calculated automatically.
   * However, if it is provided, it will be respected.
   */
  childNodeIndex?: number;
  cssClass?: string;
  enabled?: boolean;
  expanded?: boolean;
  expandedLazy?: boolean;
  htmlEnabled?: boolean;
  iconId?: string;
  initialExpanded?: boolean;
  lazyExpandingEnabled?: boolean;
  leaf?: boolean;
  level?: number;
  session?: Session;
  text?: string;
  tooltipText?: string;
  foregroundColor?: string;
  backgroundColor?: string;
  font?: string;

  [property: string]: any; // allow custom properties
}

