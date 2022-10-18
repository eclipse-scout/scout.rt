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
import {KeyStroke, Menu, TreeNode, TreeNodeModel, WidgetModel} from '../index';
import {FilterOrFunction} from '../widget/FilterSupport';
import {TreeCheckableStyle, TreeDisplayStyle} from './Tree';
import KeyStrokeModel from '../keystroke/KeyStrokeModel';
import {RefModel} from '../types';

export default interface TreeModel extends WidgetModel {
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
  menus?: Menu[];
  keyStrokes?: KeyStroke[] | KeyStrokeModel[];
  /**
   * Default is true.
   */
  multiCheck?: boolean;

  /** top-level nodes */
  nodes?: TreeNode[] | RefModel<TreeNodeModel>[];

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
}
