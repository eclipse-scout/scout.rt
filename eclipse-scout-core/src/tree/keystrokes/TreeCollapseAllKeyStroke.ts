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
import {AbstractTreeNavigationKeyStroke, HAlign, keys, Tree} from '../../index';
import {TreeEventCurrentNode} from './AbstractTreeNavigationKeyStroke';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export default class TreeCollapseAllKeyStroke extends AbstractTreeNavigationKeyStroke {

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

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement> & TreeEventCurrentNode) {
    this.field.collapseAll();
    if (this.field.visibleNodesFlat.length > 0) {
      this.selectNodesAndReveal(this.field.visibleNodesFlat[0]);
    }
  }
}
