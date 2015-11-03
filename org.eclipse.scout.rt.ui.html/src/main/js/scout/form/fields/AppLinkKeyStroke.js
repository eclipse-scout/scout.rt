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
scout.AppLinkKeyStroke = function(field, appLinkTriggerFunction) {
  scout.AppLinkKeyStroke.parent.call(this);
  this.field = field;
  this.appLinkTriggerFunction = appLinkTriggerFunction;

  this.which = [scout.keys.SPACE];
  this.renderingHints.render = false;
};
scout.inherits(scout.AppLinkKeyStroke, scout.KeyStroke);

/**
 * @override KeyStroke.js
 */
scout.AppLinkKeyStroke.prototype._accept = function(event) {
  var accepted = scout.AppLinkKeyStroke.parent.prototype._accept.call(this, event);
  return accepted && $(event.target).hasClass('app-link');
};

/**
 * @override KeyStroke.js
 */
scout.AppLinkKeyStroke.prototype.handle = function(event) {
  this.appLinkTriggerFunction.call(this.field, event);
};
