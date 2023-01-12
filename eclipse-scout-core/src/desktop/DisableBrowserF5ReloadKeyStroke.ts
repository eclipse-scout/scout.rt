/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, Desktop, keys, KeyStroke} from '../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

/**
 * Global key stroke on the desktop that prevents 'leaking' of the F5 keystroke to the browser.
 *
 * F5 is used in Scout applications to reload table pages or invoke application-specific logic.
 * If the application does not consume the key, some browsers would reload the page. This can
 * be confusing and annoying for the user. For example when the user presses F5 on a table
 * page to reload the data, but the table is covered with a glass pane (e.g. busy indicator
 * is active because of slow network connection), the browser would simply reload the page
 * (i.e. create a new UiSession) instead of updating the data from the database.
 *
 * To reload the page, the general key stroke 'Ctrl-R' ('Command-R' on Macintosh, respectively)
 * should be used instead.
 */
export class DisableBrowserF5ReloadKeyStroke extends KeyStroke {
  declare field: Desktop;

  constructor(desktop: Desktop) {
    super();
    this.field = desktop;

    this.which = [keys.F5];
    this.preventDefault = true;
    this.keyStrokeFirePolicy = Action.KeyStrokeFirePolicy.ALWAYS; // ignore glass panes
    this.renderingHints.render = false;
    this.inheritAccessibility = false;
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    // NOP
  }
}
