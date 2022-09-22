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

export interface TreeAllChildNodesDeletedEvent extends Event<Tree> {
  parentNode: TreeNode;
}

export interface TreeChildNodeOrderChangedEvent extends Event<Tree> {
  parentNode: TreeNode;
}

export interface TreeDropEvent extends Event<Tree>, FileDropEvent {
}

export interface TreeNodeActionEvent extends Event<Tree> {
  node: TreeNode;
}

export interface TreeNodeChangedEvent extends Event<Tree> {
  node: TreeNode;
}

export interface TreeNodeClickEvent extends Event<Tree> {
  node: TreeNode;
}

export interface TreeNodeExpandedEvent extends Event<Tree> {
  node: TreeNode;
  expanded: boolean;
  expandedLazy: boolean;
}

export interface TreeNodesCheckedEvent extends Event<Tree> {
  nodes: TreeNode[];
}

export interface TreeNodesDeletedEvent extends Event<Tree> {
  nodes: TreeNode[];
  parentNode: TreeNode;
}

export interface TreeNodesInsertedEvent extends Event<Tree> {
  nodes: TreeNode[];
  parentNode: TreeNode;
}

export interface TreeNodesSelectedEvent extends Event<Tree> {
  debounce: boolean;
}

export interface TreeNodesUpdatedEvent extends Event<Tree> {
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
