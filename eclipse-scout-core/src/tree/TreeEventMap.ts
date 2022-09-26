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
import {Event, KeyStroke, Menu, PropertyChangeEvent, Tree, TreeNode, WidgetEventMap} from '../index';
import {TreeCheckableStyle, TreeDisplayStyle} from './Tree';
import {FileDropEvent} from '../util/dragAndDrop';

export interface TreeAllChildNodesDeletedEvent<T extends Tree = Tree> extends Event<T> {
  parentNode: TreeNode;
}

export interface TreeChildNodeOrderChangedEvent<T extends Tree = Tree> extends Event<T> {
  parentNode: TreeNode;
}

export interface TreeDropEvent<T extends Tree = Tree> extends Event<T>, FileDropEvent {
}

export interface TreeNodeActionEvent<T extends Tree = Tree> extends Event<T> {
  node: TreeNode;
}

export interface TreeNodeChangedEvent<T extends Tree = Tree> extends Event<T> {
  node: TreeNode;
}

export interface TreeNodeClickEvent<T extends Tree = Tree> extends Event<T> {
  node: TreeNode;
}

export interface TreeNodeExpandedEvent<T extends Tree = Tree> extends Event<T> {
  node: TreeNode;
  expanded: boolean;
  expandedLazy: boolean;
}

export interface TreeNodesCheckedEvent<T extends Tree = Tree> extends Event<T> {
  nodes: TreeNode[];
}

export interface TreeNodesDeletedEvent<T extends Tree = Tree> extends Event<T> {
  nodes: TreeNode[];
  parentNode: TreeNode;
}

export interface TreeNodesInsertedEvent<T extends Tree = Tree> extends Event<T> {
  nodes: TreeNode[];
  parentNode: TreeNode;
}

export interface TreeNodesSelectedEvent<T extends Tree = Tree> extends Event<T> {
  debounce: boolean;
}

export interface TreeNodesUpdatedEvent<T extends Tree = Tree> extends Event<T> {
  nodes: TreeNode[];
}

export default interface TreeEventMap extends WidgetEventMap {
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
  'propertyChange:breadcrumbTogglingThreshold': PropertyChangeEvent<number, Tree>;
  'propertyChange:checkable': PropertyChangeEvent<boolean, Tree>;
  'propertyChange:checkableStyle': PropertyChangeEvent<TreeCheckableStyle, Tree>;
  'propertyChange:displayStyle': PropertyChangeEvent<TreeDisplayStyle, Tree>;
  'propertyChange:dropMaximumSize': PropertyChangeEvent<number, Tree>;
  'propertyChange:dropType': PropertyChangeEvent<number, Tree>;
  'propertyChange:keyStrokes': PropertyChangeEvent<KeyStroke[], Tree>;
  'propertyChange:menus': PropertyChangeEvent<Menu[], Tree>;
  'propertyChange:textFilterEnabled': PropertyChangeEvent<boolean, Tree>;
  'propertyChange:toggleBreadcrumbStyleEnabled': PropertyChangeEvent<boolean, Tree>;
  'propertyChange:viewRangeSize': PropertyChangeEvent<number, Tree>;
}
