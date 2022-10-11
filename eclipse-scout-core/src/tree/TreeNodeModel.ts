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
import {ObjectType} from '../ObjectFactory';
import {RefModel} from '../types';

export default interface TreeNodeModel {
  parent: Tree;
  objectType?: ObjectType<TreeNode, TreeNodeModel>;
  checked?: boolean;
  childNodes?: RefModel<TreeNodeModel>[] | TreeNode[];
  cssClass?: string;
  enabled?: boolean;
  expanded?: boolean;
  expandedLazy?: boolean;
  htmlEnabled?: boolean;
  iconId?: string;
  id?: string;
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
