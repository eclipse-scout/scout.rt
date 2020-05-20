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
import {Action, Button, Event, Menu, scout} from '../../index';

export default class SaveMenu extends Menu {

  constructor() {
    super();

    this.actionStyle = Action.ActionStyle.BUTTON;
    this.keyStroke = 'enter';
    this.systemType = Button.SystemType.SAVE;
    this.preventDoubleClick = true;
  }

  _init(model) {
    super._init(model);
    this.text = scout.nvl(this.text, this.session.text('SaveButton'));
  }

  _doAction() {
    let form = this.getForm();
    let event = new Event();
    this.trigger('action', event);
    if (!event.defaultPrevented) {
      form.save();
    }
  }
}
