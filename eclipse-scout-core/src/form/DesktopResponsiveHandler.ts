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
import {Desktop, DesktopResponsiveHandlerModel, ResponsiveHandler, ResponsiveManager} from '../index';
import {InitModelOf} from '../scout';

export default class DesktopResponsiveHandler extends ResponsiveHandler implements DesktopResponsiveHandlerModel {
  declare model: DesktopResponsiveHandlerModel;
  declare widget: Desktop;

  constructor() {
    super();

    this.compactThreshold = 500;
    this.allowedStates = [ResponsiveManager.ResponsiveState.NORMAL, ResponsiveManager.ResponsiveState.COMPACT];
  }

  override init(model: InitModelOf<this>) {
    super.init(model);

    this._registerTransformation('navigationVisible', this._transformNavigationVisible);
    this._enableTransformation(ResponsiveManager.ResponsiveState.COMPACT, 'navigationVisible');
  }

  /* --- TRANSFORMATIONS ------------------------------------------------------------- */

  protected _transformNavigationVisible(widget: Desktop, apply: boolean) {
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
