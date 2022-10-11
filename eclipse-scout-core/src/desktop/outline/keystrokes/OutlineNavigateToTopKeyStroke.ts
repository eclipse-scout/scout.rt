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
import {AbstractTreeNavigationKeyStroke, HAlign, keys, Outline} from '../../../index';
import {TreeEventCurrentNode} from '../../../tree/keystrokes/AbstractTreeNavigationKeyStroke';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export default class OutlineNavigateToTopKeyStroke extends AbstractTreeNavigationKeyStroke {
  declare field: Outline;

  constructor(tree: Outline, modifierBitMask: number) {
    super(tree, modifierBitMask);
    this.which = [keys.HOME];
    this.renderingHints.hAlign = HAlign.RIGHT;

    this.renderingHints.$drawingArea = ($drawingArea, event) => this.field.$title || this.field.$data;
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement> & TreeEventCurrentNode) {
    this.field.navigateToTop();
  }
}
