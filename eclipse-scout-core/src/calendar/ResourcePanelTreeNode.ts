/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {ResourcePanelTreeNodeModel, TreeNode} from '../index';

export class ResourcePanelTreeNode extends TreeNode implements ResourcePanelTreeNodeModel {
  declare model: ResourcePanelTreeNodeModel;

  resourceId: string;
  parentId: string;

  constructor() {
    super();

    this.resourceId = null;
    this.parentId = null;
  }
}
