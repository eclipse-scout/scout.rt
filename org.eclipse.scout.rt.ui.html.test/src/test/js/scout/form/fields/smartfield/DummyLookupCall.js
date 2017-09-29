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
scout.DummyLookupCall = function() {
  scout.DummyLookupCall.parent.call(this);
  
  this.multiline = false;
  this.showText = true;
  this.setDelay(250);
};
scout.inherits(scout.DummyLookupCall, scout.StaticLookupCall);

scout.DummyLookupCall.prototype._data = function() {
  return [
    [1, line.call(this, 'Foo')],
    [2, line.call(this, 'Bar')],
    [3, line.call(this, 'Baz')]
  ];

  function line(text) {
    if (!this.showText) {
      return null;
    }
    if (this.multiline) {
      return '1:' + text + '\n2:' + text;
    } else {
      return text;
    }
  }
};
