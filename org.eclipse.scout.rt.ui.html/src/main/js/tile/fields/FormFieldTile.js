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
import {WidgetTile} from '../../index';
import {BrowserField} from '../../index';
import {Tile} from '../../index';

export default class FormFieldTile extends WidgetTile {

constructor() {
  super();
  this.displayStyle = FormFieldTile.DisplayStyle.DASHBOARD;
}


static DisplayStyle = {
  DEFAULT: Tile.DEFAULT,
  PLAIN: Tile.PLAIN,
  DASHBOARD: 'dashboard'
};

_renderProperties() {
  super._renderProperties();
  this._renderFieldLabelVisible();
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

_onFieldPropertyChange(event) {
  if (event.propertyName === 'labelVisible' || event.propertyName === 'errorStatus') {
    if (this.rendered) {
      this._renderFieldLabelVisible();
    }
  }
}
}
