/*
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {keys, KeyStroke} from '../../index';
import $ from 'jquery';

export default class AppLinkKeyStroke extends KeyStroke {

  constructor(field, appLinkTriggerFunction) {
    super();
    this.field = field;
    this.appLinkTriggerFunction = appLinkTriggerFunction;

    this.which = [keys.SPACE];
    this.renderingHints.render = false;
  }

  /**
   * @override KeyStroke.js
   */
  _accept(event) {
    let accepted = super._accept(event);
    return accepted && $(event.target).hasClass('app-link');
  }

  /**
   * @override KeyStroke.js
   */
  handle(event) {
    this.appLinkTriggerFunction.call(this.field, event);
  }
}
