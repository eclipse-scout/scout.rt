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
import {Session, Tree, TreeNode} from '../index';
import {ObjectModel} from '../scout';

export default interface TreeNodeModel extends ObjectModel<TreeNode, TreeNodeModel> {
  parent: Tree;
  checked?: boolean;
  childNodes?: (TreeNodeData | TreeNode)[];
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
}

export type TreeNodeData = Omit<TreeNodeModel, 'parent'>;
