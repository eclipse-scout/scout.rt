/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {BrowserField, Device, FormFieldLayout, Tile, WidgetTile} from '../../index';

export default class FormFieldTile extends WidgetTile {

  constructor() {
    super();
    this.displayStyle = FormFieldTile.DisplayStyle.DASHBOARD;
  }

  static DisplayStyle = {
    DEFAULT: Tile.DisplayStyle.DEFAULT,
    PLAIN: Tile.DisplayStyle.PLAIN,
    DASHBOARD: 'dashboard'
  };

  _renderProperties() {
    super._renderProperties();
    this._renderFieldLabelVisible();
    this._renderCompact();
  }

  _renderTileWidget() {
    super._renderTileWidget();
    if (this.displayStyle !== FormFieldTile.DisplayStyle.DASHBOARD) {
      return;
    }
    if (this.tileWidget && this.tileWidget.htmlComp && this.tileWidget.htmlComp.layout instanceof FormFieldLayout) {
      this.tileWidget.htmlComp.layout.statusWidth = 0;
    }
  }

  _renderDisplayStyle() {
    super._renderDisplayStyle();
    this.$container.toggleClass('dashboard', this.displayStyle === FormFieldTile.DisplayStyle.DASHBOARD);
  }

  _renderFieldLabelVisible() {
    if (this.displayStyle !== FormFieldTile.DisplayStyle.DASHBOARD) {
      return;
    }
    // Special handling for browser field (remove padding when label is invisible)
    if (this.tileWidget instanceof BrowserField) {
      this.tileWidget.$container.toggleClass('no-padding', !this.tileWidget.labelVisible && !this.tileWidget.errorStatus);
    }
  }

  _renderCompact() {
    this.$container.toggleClass('compact', Device.get().type === Device.Type.MOBILE);
  }

  _onFieldPropertyChange(event) {
    if (event.propertyName === 'labelVisible' || event.propertyName === 'errorStatus') {
      if (this.rendered) {
        this._renderFieldLabelVisible();
      }
    }
  }
}
