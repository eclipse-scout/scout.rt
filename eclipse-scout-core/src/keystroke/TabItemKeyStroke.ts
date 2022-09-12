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
import {HAlign, KeyStroke, ScoutKeyboardEvent, TabItem} from '../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export default class TabItemKeyStroke extends KeyStroke {
  declare field: TabItem;

  constructor(keyStroke: string, field: TabItem) {
    super();
    this.field = field;
    this.parseAndSetKeyStroke(keyStroke);

    this.renderingHints.offset = 16;
    this.renderingHints.hAlign = HAlign.RIGHT;
    this.renderingHints.render = () => {
      let tab = this.field.getTab();
      return tab && tab.rendered;
    };
    this.renderingHints.$drawingArea = ($drawingArea: JQuery, event: ScoutKeyboardEvent) => this.field.getTab().$container;
    this.inheritAccessibility = false;
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    this.field.select();
  }
}
