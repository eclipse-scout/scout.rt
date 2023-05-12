/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {InitModelOf, ResourcesPanelTreeNodeModel, TreeNode} from '../index';

export class ResourcesPanelTreeNode extends TreeNode implements ResourcesPanelTreeNodeModel {
  declare model: ResourcesPanelTreeNodeModel;

  calendarId: number;

  constructor() {
    super();

    this.calendarId = null;
  }

  override init(model: InitModelOf<this>) {
    super.init(model);

    this.leaf = true;
  }
}
