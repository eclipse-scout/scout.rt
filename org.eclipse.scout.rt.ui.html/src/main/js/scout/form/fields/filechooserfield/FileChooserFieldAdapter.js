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
scout.FileChooserFieldAdapter = function() {
  scout.FileChooserFieldAdapter.parent.call(this);
};
scout.inherits(scout.FileChooserFieldAdapter, scout.ValueFieldAdapter);

scout.FileChooserFieldAdapter.prototype._onWidgetPropertyChange = function(event) {
  scout.FileChooserFieldAdapter.parent.prototype._onWidgetPropertyChange.call(this, event);

  if (event.propertyName === 'value') {
    this._onValueChange(event);
  }
};

scout.FileChooserFieldAdapter.prototype._onValueChange = function(event) {
  var success = this.widget.fileInput.upload();
  if (!success) {
    this.widget.fileInput.clear();
  }
};


/**
 * @override
 */
scout.FileChooserFieldAdapter.prototype._syncDisplayText = function(displayText) {
  this.widget.setDisplayText(displayText);
  // When displayText comes from the server we must not call parseAndSetValue here.
};
