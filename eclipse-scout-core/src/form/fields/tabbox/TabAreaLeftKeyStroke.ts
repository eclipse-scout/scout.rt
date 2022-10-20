/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {keys, KeyStroke} from '../../../index';

export default class TabAreaLeftKeyStroke extends KeyStroke {

  // noinspection DuplicatedCode
  constructor(tabArea) {
    super();
    this.field = tabArea;
    this.which = [keys.LEFT];
    this.renderingHints.render = false;
    this.stopPropagation = true;
    this.keyStrokeMode = KeyStroke.Mode.DOWN;
    this.inheritAccessibility = false;
  }

  handle(event) {
    this.field.selectPreviousTab(true);
    this.field.selectedTab.$container.addClass('keyboard-navigation');
  }
}
