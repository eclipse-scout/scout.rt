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
scout.WizardProgressFieldAdapter = function() {
  scout.WizardProgressFieldAdapter.parent.call(this);
};
scout.inherits(scout.WizardProgressFieldAdapter, scout.FormFieldAdapter);

scout.WizardProgressFieldAdapter.prototype._onWidgetStepAction = function(event) {
  this._send('doStepAction', {
    stepIndex: event.stepIndex
  });
};

scout.WizardProgressFieldAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'stepAction') {
    this._onWidgetStepAction(event);
  } else {
    scout.WizardProgressFieldAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};
