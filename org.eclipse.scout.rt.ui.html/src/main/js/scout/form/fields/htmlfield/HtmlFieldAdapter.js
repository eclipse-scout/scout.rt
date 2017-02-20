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
scout.HtmlFieldAdapter = function() {
  scout.HtmlFieldAdapter.parent.call(this);
};
scout.inherits(scout.HtmlFieldAdapter, scout.ValueFieldAdapter);

scout.HtmlFieldAdapter.prototype._initProperties = function(model) {
  if (model.scrollToEnd !== undefined) {
    // ignore pseudo property initially (to prevent the function StringField#scrollToEnd() to be replaced)
    delete model.scrollToEnd;
  }
};

scout.HtmlFieldAdapter.prototype._syncScrollToEnd = function() {
  this.widget.scrollToEnd();
};

scout.HtmlFieldAdapter.prototype._onWidgetAppLinkAction = function(event) {
  this._send('appLinkAction', {
    ref: event.ref
  });
};

scout.HtmlFieldAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'appLinkAction') {
    this._onWidgetAppLinkAction(event);
  } else {
    scout.HtmlFieldAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};
