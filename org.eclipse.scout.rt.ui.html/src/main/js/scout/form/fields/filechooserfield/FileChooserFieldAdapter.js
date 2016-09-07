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
scout.FileChooserFieldAdapter = function() {
  scout.FileChooserFieldAdapter.parent.call(this);
};
scout.inherits(scout.FileChooserFieldAdapter, scout.ValueFieldAdapter);

scout.FileChooserFieldAdapter.prototype._onWidgetChooseFile = function(event) {
  this._send('chooseFile');
};

scout.FileChooserFieldAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'chooseFile') {
    this._onWidgetChooseFile(event);
  } else {
    scout.FileChooserFieldAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};
