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
