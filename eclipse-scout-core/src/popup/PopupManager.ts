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
import {arrays, Popup, PopupManagerModel, Widget} from '../index';

export default class PopupManager extends Widget implements PopupManagerModel {
  popups: Popup[];

  constructor() {
    super();
    this.popups = [];
    this._addWidgetProperties(['popups']);
    this._addPreserveOnPropertyChangeProperties(['popups']);
  }

  protected override _init(model: PopupManagerModel) {
    super._init(model);
    this.session.layoutValidator.schedulePostValidateFunction(() => {
      this._openPopups(this.popups);
    });
  }

  setPopups(popups: Popup[]) {
    this.setProperty('popups', popups);
  }

  protected _setPopups(popups: Popup[]) {
    this._openPopups(arrays.diff(popups, this.popups));
    this._destroyPopups(arrays.diff(this.popups, popups));
    this._setProperty('popups', popups);
    // re-parent popups, since PopupManager is not a real widget but only used to sync data
    this.popups.forEach(popup => {
      if (popup.parent instanceof PopupManager) {
        popup.setParent(this.session.desktop);
      }
    });
  }

  protected _openPopups(popups: Popup[]) {
    popups.forEach(popup => {
      popup.open(); // Let the popup itself determine the entry point (important for popup windows)
    });
  }

  protected _destroyPopups(popups: Popup[]) {
    popups.forEach(popup => popup.destroy());
  }
}
