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
import {Action, Button, Event, FormLifecycleMenu, scout} from '../../index';
import {InitModelOf} from '../../scout';

export default class ResetMenu extends FormLifecycleMenu {

  constructor() {
    super();

    this.actionStyle = Action.ActionStyle.BUTTON;
    this.systemType = Button.SystemType.RESET;
    this.preventDoubleClick = true;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.text = scout.nvl(this.text, this.session.text('ResetButton'));
  }

  protected override _doAction() {
    let form = this.getForm();
    let event = new Event();
    this.trigger('action', event);
    if (!event.defaultPrevented) {
      form.reset();
    }
  }
}
