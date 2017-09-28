/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
var FocusManagerSpecHelper = function() {};

FocusManagerSpecHelper.prototype.handlersRegistered = function($comp) {
  var i,
    expectedHandlers = ['keydown', 'focusin', 'focusout', 'hide'],
    handlerCount = 0,
    events = $._data($comp[0], 'events'),
    expectedCount = expectedHandlers.length;
  if (events) {
    for (i = 0; i < expectedCount; i++) {
      if (events[expectedHandlers[i]]) {
        handlerCount++;
      }
    }
  }
  return handlerCount === expectedCount;
};

