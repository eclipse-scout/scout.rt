/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractTreeNavigationKeyStroke, HAlign, keys, Tree, TreeEventCurrentNode} from '../../index';

export class TreeCollapseAllKeyStroke extends AbstractTreeNavigationKeyStroke {

  constructor(tree: Tree, keyStrokeModifier: number) {
    super(tree, keyStrokeModifier);
    this.which = [keys.HOME];
    this.renderingHints.hAlign = HAlign.RIGHT;
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      if (this.field.visibleNodesFlat.length > 0) {
        return this.field.visibleNodesFlat[0].$node;
      }
    };
  }

  override handle(event: JQuery.KeyboardEventBase & TreeEventCurrentNode) {
    this.field.collapseAll();
    if (this.field.visibleNodesFlat.length > 0) {
      this.selectNodesAndReveal(this.field.visibleNodesFlat[0]);
    }
  }
}
