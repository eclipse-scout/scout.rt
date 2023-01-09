/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Action, FilterOrFunction, Menu, ObjectOrChildModel, ObjectOrModel, TreeCheckableStyle, TreeDisplayStyle, TreeNode, WidgetModel} from '../index';

export interface TreeModel extends WidgetModel {
  /**
   * Default is false.
   */
  toggleBreadcrumbStyleEnabled?: boolean;
  breadcrumbTogglingThreshold?: number;
  /**
   * Default is false.
   */
  autoCheckChildren?: boolean;
  /**
   * Default is false.
   */
  checkable?: boolean;
  /**
   * Default is {@link Tree.CheckableStyle.CHECKBOX_TREE_NODE}.
   */
  checkableStyle?: TreeCheckableStyle;
  /**
   * Default is {@link Tree.DisplayStyle.DEFAULT}.
   */
  displayStyle?: TreeDisplayStyle;
  dropType?: number;
  /**
   * Default is {@link dragAndDrop.DEFAULT_DROP_MAXIMUM_SIZE}.
   */
  dropMaximumSize?: number;
  /**
   * Default is true.
   */
  lazyExpandingEnabled?: boolean;
  menus?: ObjectOrChildModel<Menu>[];
  keyStrokes?: ObjectOrChildModel<Action>[];
  /**
   * Default is true.
   */
  multiCheck?: boolean;
  /**
   * Top-level nodes
   */
  nodes?: ObjectOrModel<TreeNode>[];
  /**
   * Default is 23.
   */
  nodePaddingLevelCheckable?: number;
  /**
   * Default is 18.
   */
  nodePaddingLevelNotCheckable?: number;
  /**
   * Default is false
   */
  scrollToSelection?: boolean;
  selectedNodes?: string[] | TreeNode[];
  filters?: FilterOrFunction<TreeNode>[];
  textFilterEnabled?: boolean;
  /**
   * Whether to focus the tree when the node control is clicked. Default is true.
   */
  requestFocusOnNodeControlMouseDown?: boolean;
  defaultMenuTypes?: string[];
}
