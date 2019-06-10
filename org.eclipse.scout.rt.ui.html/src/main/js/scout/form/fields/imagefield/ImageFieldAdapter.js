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
scout.ImageFieldAdapter = function() {
  scout.ImageFieldAdapter.parent.call(this);
};
scout.inherits(scout.ImageFieldAdapter, scout.FormFieldAdapter);

scout.ImageFieldAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'fileUpload') {
    this._onFileUpload(event);
  } else {
    scout.ImageFieldAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};

scout.ImageFieldAdapter.prototype._onFileUpload = function(event) {
  var success = this.widget.fileInput.upload();
  if (!success) {
    this.widget.fileInput.clear();
  }
};
