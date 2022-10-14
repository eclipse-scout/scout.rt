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
import {keys, KeyStroke, ScoutKeyboardEvent, Widget} from '../../index';
import $ from 'jquery';

export default class AppLinkKeyStroke extends KeyStroke {

  appLinkTriggerFunction: AppLinkTriggerFunction;

  constructor(field: Widget, appLinkTriggerFunction: AppLinkTriggerFunction) {
    super();
    this.field = field;
    this.appLinkTriggerFunction = appLinkTriggerFunction;

    this.which = [keys.SPACE];
    this.renderingHints.render = false;
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    let accepted = super._accept(event);
    return accepted && $(event.target).hasClass('app-link');
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.appLinkTriggerFunction.call(this.field, event);
  }
}

export type AppLinkTriggerFunction = (this: Widget, event: JQuery.KeyboardEventBase) => void;
