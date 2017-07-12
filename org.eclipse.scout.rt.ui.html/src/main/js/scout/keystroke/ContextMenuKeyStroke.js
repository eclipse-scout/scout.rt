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
scout.ContextMenuKeyStroke = function(field, contextFunction, bindObject) {
  scout.ContextMenuKeyStroke.parent.call(this);
  this._contextFunction = contextFunction;
  this._bindObject = bindObject || this;

  this.field = field;
  this.renderingHints.render = false;

  this.which = [scout.keys.SELECT]; // = "Menu" key
  this.ctrl = false;
  this.shift = false;
  this.stopPropagation = true;
};
scout.inherits(scout.ContextMenuKeyStroke, scout.KeyStroke);

/**
 * @override KeyStroke.js
 */
scout.ContextMenuKeyStroke.prototype.handle = function(event) {
  this._contextFunction.call(this._bindObject, event);
};
