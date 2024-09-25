/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, TabArea} from '../../../index';

export class TabAreaRightKeyStroke extends KeyStroke {
  declare field: TabArea;

  constructor(tabArea: TabArea) {
    super();
    this.field = tabArea;
    this.which = [keys.RIGHT];
    this.renderingHints.render = false;
    this.stopPropagation = true;
    this.keyStrokeMode = KeyStroke.Mode.DOWN;
    this.inheritAccessibility = false;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.selectNextTab(true);
    this.field.selectedTab.$container.addClass('keyboard-navigation');
  }
}
