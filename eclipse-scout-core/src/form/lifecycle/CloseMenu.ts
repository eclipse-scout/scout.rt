/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, Button, Event, FormLifecycleMenu, InitModelOf, scout} from '../../index';

export class CloseMenu extends FormLifecycleMenu {

  constructor() {
    super();

    this.actionStyle = Action.ActionStyle.BUTTON;
    this.keyStroke = 'esc';
    this.systemType = Button.SystemType.CLOSE;
    this.inheritAccessibility = false;
    this.preventDoubleClick = true;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.text = scout.nvl(this.text, this.session.text('CloseButton'));
  }

  protected override _doAction() {
    let form = this.getForm();
    let event = new Event();
    this.trigger('action', event);
    if (!event.defaultPrevented) {
      form.close();
    }
  }
}
