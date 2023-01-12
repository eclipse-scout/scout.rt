/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractTreeNavigationKeyStroke, HAlign, keys, Outline, TreeEventCurrentNode} from '../../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export class OutlineNavigateToTopKeyStroke extends AbstractTreeNavigationKeyStroke {
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
