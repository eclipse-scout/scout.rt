/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, Event, InitModelOf, Menu, scout} from '../../index';

export class SearchMenu extends Menu {

  constructor() {
    super();

    this.actionStyle = Action.ActionStyle.BUTTON;
    this.keyStroke = 'enter';
    this.preventDoubleClick = true;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.text = scout.nvl(this.text, this.session.text('SearchButton'));
  }

  protected override _doAction() {
    const event = new Event();
    this.trigger('action', event);
    if (event.defaultPrevented) {
      return;
    }

    const form = this.getForm();
    form.validate().then(status => {
      if (status.isValid()) {
        form.trigger('search');
      }
    });
  }
}
