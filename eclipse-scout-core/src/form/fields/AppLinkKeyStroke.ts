/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, ScoutKeyboardEvent, Widget} from '../../index';
import $ from 'jquery';

export class AppLinkKeyStroke extends KeyStroke {

  appLinkTriggerFunction: AppLinkTriggerFunction;

  constructor(field: Widget, appLinkTriggerFunction: AppLinkTriggerFunction) {
    super();
    this.field = field;
    this.appLinkTriggerFunction = appLinkTriggerFunction;

    this.which = [keys.SPACE, keys.ENTER];
    this.renderingHints.render = false;
    this.inheritAccessibility = false; // Links cannot be disabled, mouse handlers work as well
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
