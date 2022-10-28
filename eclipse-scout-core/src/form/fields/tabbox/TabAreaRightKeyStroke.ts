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
import {keys, KeyStroke, TabArea} from '../../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export default class TabAreaRightKeyStroke extends KeyStroke {
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

  override handle(event: KeyboardEventBase) {
    this.field.selectNextTab(true);
    this.field.selectedTab.$container.addClass('keyboard-navigation');
  }
}
