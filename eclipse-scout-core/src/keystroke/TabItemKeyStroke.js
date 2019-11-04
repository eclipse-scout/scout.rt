/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {HAlign, KeyStroke} from '../index';

export default class TabItemKeyStroke extends KeyStroke {

  constructor(keyStroke, field) {
    super();
    this.field = field;
    this.parseAndSetKeyStroke(keyStroke);

    this.renderingHints.offset = 16;
    this.renderingHints.hAlign = HAlign.RIGHT;
    this.renderingHints.$drawingArea = function($drawingArea, event) {
      return this.field.$tabContainer;
    }.bind(this);
  }

  /**
   * @override KeyStroke.js
   */
  handle(event) {
    this.field.parent.setSelectedTab(this.field);
  }
}
