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
scout.TableControlCloseKeyStroke = function(tableControl) {
  scout.TableControlCloseKeyStroke.parent.call(this);
  this.field = tableControl;
  this.which = [scout.keys.ESC];
  this.stopPropagation = true;
  this.renderingHints.render = false;
};
scout.inherits(scout.TableControlCloseKeyStroke, scout.KeyStroke);

/**
 * @override KeyStroke.js
 */
scout.TableControlCloseKeyStroke.prototype.handle = function(event) {
  this.field.toggle();
};
