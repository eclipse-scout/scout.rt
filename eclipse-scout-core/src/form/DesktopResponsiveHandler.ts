/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Desktop, DesktopResponsiveHandlerModel, InitModelOf, ResponsiveHandler, ResponsiveManager} from '../index';

export class DesktopResponsiveHandler extends ResponsiveHandler implements DesktopResponsiveHandlerModel {
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
