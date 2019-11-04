/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ResponsiveHandler, ResponsiveManager} from '../index';

export default class DesktopResponsiveHandler extends ResponsiveHandler {

  constructor() {
    super();

    this.compactThreshold = 500;
    this.allowedStates = [ResponsiveManager.ResponsiveState.NORMAL, ResponsiveManager.ResponsiveState.COMPACT];
  }

  init(model) {
    super.init(model);

    this._registerTransformation('navigationVisible', this._transformNavigationVisible);
    this._enableTransformation(ResponsiveManager.ResponsiveState.COMPACT, 'navigationVisible');
  }

  /* --- TRANSFORMATIONS ------------------------------------------------------------- */

  _transformNavigationVisible(widget, apply) {
    if (apply) {
      this._storeFieldProperty(widget, 'navigationVisible', widget.navigationVisible);
      widget.setNavigationVisible(false);
    } else {
      if (this._hasFieldProperty(widget, 'navigationVisible')) {
        widget.setNavigationVisible(this._getFieldProperty(widget, 'navigationVisible'));
      }
    }
  }
}
