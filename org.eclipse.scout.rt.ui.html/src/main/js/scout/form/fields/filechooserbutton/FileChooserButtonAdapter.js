/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.FileChooserButtonAdapter = function() {
  scout.FileChooserButtonAdapter.parent.call(this);
};
scout.inherits(scout.FileChooserButtonAdapter, scout.ValueFieldAdapter);

scout.FileChooserButtonAdapter.prototype._onWidgetPropertyChange = function(event) {
  scout.FileChooserButtonAdapter.parent.prototype._onWidgetPropertyChange.call(this, event);

  if (event.propertyName === 'value') {
    this._onValueChange(event);
  }
};

scout.FileChooserButtonAdapter.prototype._onValueChange = function(event) {
  var success = this.widget.fileInput.upload();
  if (!success) {
    this.widget.fileInput.clear();
  }
};
