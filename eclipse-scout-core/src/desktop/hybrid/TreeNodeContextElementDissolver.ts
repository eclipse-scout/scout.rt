/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HybridActionContextElement, HybridActionContextElementDissolver, Tree, TreeAdapter, TreeNode} from '../../index';

export class TreeNodeContextElementDissolver extends HybridActionContextElementDissolver {

  dissolve(contextElement: HybridActionContextElement): object {
    if (contextElement.widget instanceof Tree && contextElement.widget.modelAdapter instanceof TreeAdapter) {
      let widgetAdapterId = contextElement.widget.modelAdapter.id;
      let elementId = undefined;
      if (contextElement.element instanceof TreeNode) {
        elementId = contextElement.element.id;
      }
      return {
        widget: widgetAdapterId,
        element: elementId
      };
    }
    return null;
  }
}

HybridActionContextElementDissolver.register(TreeNodeContextElementDissolver);
