/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {KeyStrokeContext, Outline, ScoutKeyboardEvent} from '../../../index';

export class OutlineKeyStrokeContext extends KeyStrokeContext {
  protected _outline: Outline;

  constructor(outline: Outline) {
    super();
    this._outline = outline;
  }

  /**
   * @returns true if this event is handled by this context, and if so sets the propagation flags accordingly.
   */
  override accept(event: ScoutKeyboardEvent): boolean {
    return !this._outline.inBackground && !this.isFormMenuOpen() && super.accept(event);
  }

  isFormMenuOpen(): boolean {
    let menus = this._outline.session.desktop.menus;
    return menus.some(menu => menu.popup && menu.popup.$container && menu.popup.$container.isAttached());
  }
}
