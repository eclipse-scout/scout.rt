/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HAlign, KeyStroke, ScoutKeyboardEvent, TabItem} from '../../../index';

export class TabItemKeyStroke extends KeyStroke {
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

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.select();
  }
}
