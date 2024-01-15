/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, FilterOrFunction, Menu, ObjectOrChildModel, ObjectOrModel, TreeCheckableStyle, TreeDisplayStyle, TreeNode, WidgetModel} from '../index';

export interface TreeModel extends WidgetModel {
  /**
   * Default is false.
   */
  toggleBreadcrumbStyleEnabled?: boolean;
  breadcrumbTogglingThreshold?: number;
  /**
   * Describes the behavior of children and parent nodes, when a node is checked/unchecked
   *
   * Default is {@code false}
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
  /**
   * Specifies whether it should be possible to drop elements onto the tree.
   *
   * Currently, only {@link DropType.FILE_TRANSFER} is supported.
   *
   * By default, dropping is disabled.
   */
  dropType?: number;
  /**
   * Specifies the maximum size in bytes a file can have if it is being dropped.
   *
   * It only has an effect if {@link dropType} is set to {@link DropType.FILE_TRANSFER}.
   *
   * Default is {@link dragAndDrop.DEFAULT_DROP_MAXIMUM_SIZE}
   */
  dropMaximumSize?: number;
  /**
   * Default is true.
   */
  lazyExpandingEnabled?: boolean;
  /**
   * Configures the menus to be displayed in the {@link MenuBar} of the tree.
   *
   * The visibility of the {@link Menu} and where it should appear depends on the used {@link Tree.MenuType} configured in {@link Menu.menuTypes}.
   */
  menus?: ObjectOrChildModel<Menu>[];
  /**
   * Configures the keystrokes that should be registered in the current {@link keyStrokeContext}.
   *
   * Use the {@link ActionModel.keyStroke} to assign the keys that need to be pressed.
   *
   * @see KeyStrokeContext
   */
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
   * Defines whether the tree should automatically scroll to the selected nodes when it is rendered.
   *
   * Default is false.
   */
  scrollToSelection?: boolean;
  selectedNodes?: string[] | TreeNode[];
  /**
   * The filters control which nodes are allowed to be displayed in the table.
   *
   * If one of the filters does not accept a specific node, the node won't be shown. Hence, all filters must agree to make a node visible.
   *
   * By default, there are no filters.
   *
   * @see Tree.visibleNodesFlat
   * @see Tree.nodes
   */
  filters?: FilterOrFunction<TreeNode>[];
  textFilterEnabled?: boolean;
  /**
   * Defines whether to focus the tree when the node control is clicked.
   *
   * Default is true.
   */
  requestFocusOnNodeControlMouseDown?: boolean;
  defaultMenuTypes?: string[];
}
