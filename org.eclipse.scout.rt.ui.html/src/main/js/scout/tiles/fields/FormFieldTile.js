/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.FormFieldTile = function() {
  scout.FormFieldTile.parent.call(this);
};
scout.inherits(scout.FormFieldTile, scout.WidgetTile);

scout.FormFieldTile.prototype._renderProperties = function() {
  scout.FormFieldTile.parent.prototype._renderProperties.call(this);
  this._renderFieldLabelVisible();
};

scout.FormFieldTile.prototype._renderFieldLabelVisible = function() {
  // Special handling for browser field (remove padding when label is invisible)
  if (this.tileWidget instanceof scout.BrowserField) {
    this.tileWidget.$container.toggleClass('no-padding', !this.tileWidget.labelVisible && !this.tileWidget.errorStatus);
  }
};

scout.FormFieldTile.prototype._onFieldPropertyChange = function(event) {
  if (event.propertyName === 'labelVisible' || event.propertyName === 'errorStatus') {
    if (this.rendered) {
      this._renderFieldLabelVisible();
    }
  }
};
