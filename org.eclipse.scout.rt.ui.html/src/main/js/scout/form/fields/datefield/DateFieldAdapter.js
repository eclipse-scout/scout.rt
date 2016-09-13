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
scout.DateFieldAdapter = function() {
  scout.DateFieldAdapter.parent.call(this);
  this.enabledWhenOffline = true;
};
scout.inherits(scout.DateFieldAdapter, scout.ValueFieldAdapter);

scout.DateFieldAdapter.prototype._initProperties = function(model) {
  if (model.errorStatus) {
    model._modelErrorStatus = new scout.Status(model.errorStatus);
  } else {
    model._modelErrorStatus = null;
  }
};

scout.DateFieldAdapter.prototype._syncErrorStatus = function(errorStatus) {
  var widgetErrorStatus = this.widget.errorStatus;

  // copy UI only properties to error status from server
  if (errorStatus && widgetErrorStatus) {
    errorStatus.invalidTime = widgetErrorStatus.invalidTime;
    errorStatus.invalidDate = widgetErrorStatus.invalidDate;
  }

  // set error status and additional model error status on widget (stores error status from server)
  this.widget.setErrorStatus(errorStatus); // info: this setter ensures that errorStatus object is converted to scout.Status
  this.widget._modelErrorStatus = scout.Status.clone(this.widget.errorStatus);
};
