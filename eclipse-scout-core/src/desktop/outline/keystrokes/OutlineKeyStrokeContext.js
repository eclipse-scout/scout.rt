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
import {KeyStrokeContext} from '../../../index';

export default class OutlineKeyStrokeContext extends KeyStrokeContext {

  constructor(outline) {
    super();
    this._outline = outline;
  }

  /**
   * Returns true if this event is handled by this context, and if so sets the propagation flags accordingly.
   */
  accept(event) {
    return !this._outline.inBackground && !this.isFormMenuOpen() && super.accept(event);
  }

  isFormMenuOpen() {
    let menus = this._outline.session.desktop.menus;
    return menus.some(menu => {
      return menu.popup && menu.popup.$container && menu.popup.$container.isAttached();
    }, this);
  }
}
