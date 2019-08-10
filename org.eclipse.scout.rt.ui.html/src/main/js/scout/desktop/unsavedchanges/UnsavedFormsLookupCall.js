/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.UnsavedFormsLookupCall = function() {
  scout.UnsavedFormsLookupCall.parent.call(this);

  this.unsavedForms = [];
};
scout.inherits(scout.UnsavedFormsLookupCall, scout.StaticLookupCall);

scout.UnsavedFormsLookupCall.prototype._data = function() {
  return this.unsavedForms.map(function(form) {
    var text = scout.UnsavedFormChangesForm.getFormDisplayName(form);
    return [form, text];
  });
};
