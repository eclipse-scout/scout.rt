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
import {Event, FileDropEvent, KeyStroke, Menu, PropertyChangeEvent, Tree, TreeCheckableStyle, TreeDisplayStyle, TreeNode, WidgetEventMap} from '../index';

export interface TreeAllChildNodesDeletedEvent<T = Tree> extends Event<T> {
  parentNode: TreeNode;
}

export interface TreeChildNodeOrderChangedEvent<T = Tree> extends Event<T> {
  parentNode: TreeNode;
}

export interface TreeDropEvent<T = Tree> extends Event<T>, FileDropEvent {
}

export interface TreeNodeActionEvent<T = Tree> extends Event<T> {
  node: TreeNode;
}

export interface TreeNodeChangedEvent<T = Tree> extends Event<T> {
  node: TreeNode;
}

export interface TreeNodeClickEvent<T = Tree> extends Event<T> {
  node: TreeNode;
}

export interface TreeNodeExpandedEvent<T = Tree> extends Event<T> {
  node: TreeNode;
  expanded: boolean;
  expandedLazy: boolean;
}

export interface TreeNodesCheckedEvent<T = Tree> extends Event<T> {
  nodes: TreeNode[];
}

export interface TreeNodesDeletedEvent<T = Tree> extends Event<T> {
  nodes: TreeNode[];
  parentNode: TreeNode;
}

export interface TreeNodesInsertedEvent<T = Tree> extends Event<T> {
  nodes: TreeNode[];
  parentNode: TreeNode;
}

export interface TreeNodesSelectedEvent<T = Tree> extends Event<T> {
  debounce: boolean;
}

export interface TreeNodesUpdatedEvent<T = Tree> extends Event<T> {
  nodes: TreeNode[];
}

export interface TreeEventMap extends WidgetEventMap {
  'allChildNodesDeleted': TreeAllChildNodesDeletedEvent;
  'childNodeOrderChanged': TreeChildNodeOrderChangedEvent;
  'drop': TreeDropEvent;
  'nodeAction': TreeNodeActionEvent;
  'nodeChanged': TreeNodeChangedEvent;
  'nodeClick': TreeNodeClickEvent;
  'nodeExpanded': TreeNodeExpandedEvent;
  'nodesChecked': TreeNodesCheckedEvent;
  'nodesDeleted': TreeNodesDeletedEvent;
  'nodesInserted': TreeNodesInsertedEvent;
  'nodesSelected': TreeNodesSelectedEvent;
  'nodesUpdated': TreeNodesUpdatedEvent;
  'propertyChange:breadcrumbTogglingThreshold': PropertyChangeEvent<number>;
  'propertyChange:checkable': PropertyChangeEvent<boolean>;
  'propertyChange:checkableStyle': PropertyChangeEvent<TreeCheckableStyle>;
  'propertyChange:displayStyle': PropertyChangeEvent<TreeDisplayStyle>;
  'propertyChange:dropMaximumSize': PropertyChangeEvent<number>;
  'propertyChange:dropType': PropertyChangeEvent<number>;
  'propertyChange:keyStrokes': PropertyChangeEvent<KeyStroke[]>;
  'propertyChange:menus': PropertyChangeEvent<Menu[]>;
  'propertyChange:textFilterEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:toggleBreadcrumbStyleEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:viewRangeSize': PropertyChangeEvent<number>;
}
