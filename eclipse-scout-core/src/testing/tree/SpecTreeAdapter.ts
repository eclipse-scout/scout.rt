/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {TreeAdapter} from '../../index';

export class SpecTreeAdapter extends TreeAdapter {
  override _onNodesSelected(nodeIds: string[]) {
    super._onNodesSelected(nodeIds);
  }

  override _onNodesChecked(nodes: { id: string; checked: boolean }[]) {
    super._onNodesChecked(nodes);
  }

  override _onNodeExpanded(nodeId: string, event: { expanded: boolean; expandedLazy: boolean; recursive?: boolean }) {
    super._onNodeExpanded(nodeId, event);
  }
}
