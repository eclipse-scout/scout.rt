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

/**
 * @override
 */
scout.DateFieldAdapter.prototype._initProperties = function(model) {
  if (model.errorStatus) {
    model._modelErrorStatus = new scout.Status(model.errorStatus);
  } else {
    model._modelErrorStatus = null;
  }
};

/**
 * @override
 */
scout.DateFieldAdapter.prototype._orderPropertyNamesOnSync = function(newProperties) {
  return Object.keys(newProperties).sort(function(a, b) {
    if (a === 'hasDate' || a === 'hasTime') {
      // make sure hasDate and hasTime are always set before displayText,
      // otherwise toggling hasDate and hasTime dynamically won't work because renderDisplayText would try to write the time into the date field
      return -1;
    }
    return 1;
  });
};

scout.DateFieldAdapter.prototype._syncErrorStatus = function(errorStatus) {
  // always store the error status from the server in a separate property
  if (errorStatus) {
    this.widget._modelErrorStatus = new scout.Status(errorStatus);
  } else {
    this.widget._modelErrorStatus = null;
  }
  // if UI has already an error, don't overwrite it with the error status from the server
  if (!this.widget._hasUiErrorStatus()) {
    // info: this setter ensures that errorStatus object is converted to scout.Status
    this.widget.setErrorStatus(errorStatus);
  }
};
