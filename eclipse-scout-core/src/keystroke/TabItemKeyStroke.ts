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
    this.renderingHints.render = () => {
      let tab = this.field.getTab();
      return tab && tab.rendered;
    };
    this.renderingHints.$drawingArea = ($drawingArea, event) => this.field.getTab().$container;
    this.inheritAccessibility = false;
  }

  /**
   * @override
   */
  handle(event) {
    this.field.select();
  }
}
