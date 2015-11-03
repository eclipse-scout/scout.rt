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
scout.CellEditorCompleteEditKeyStroke = function(popup) {
  scout.CellEditorCompleteEditKeyStroke.parent.call(this);
  this.field = popup;
  this.which = [scout.keys.ENTER];
  this.stopPropagation = true;
};
scout.inherits(scout.CellEditorCompleteEditKeyStroke, scout.KeyStroke);

scout.CellEditorCompleteEditKeyStroke.prototype.handle = function(event) {
  this.field.completeEdit();
};
