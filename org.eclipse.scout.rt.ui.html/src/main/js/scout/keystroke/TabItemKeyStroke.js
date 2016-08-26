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
scout.TabItemKeyStroke = function(keyStroke, field) {
  scout.TabItemKeyStroke.parent.call(this);
  this.field = field;
  this.parseAndSetKeyStroke(keyStroke);

  this.renderingHints.offset = 16;
  this.renderingHints.hAlign = scout.hAlign.RIGHT;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.field.$tabContainer;
  }.bind(this);
};
scout.inherits(scout.TabItemKeyStroke, scout.KeyStroke);

/**
 * @override KeyStroke.js
 */
scout.TabItemKeyStroke.prototype.handle = function(event) {
  this.field.parent._syncSelectedTab(this.field);
};
