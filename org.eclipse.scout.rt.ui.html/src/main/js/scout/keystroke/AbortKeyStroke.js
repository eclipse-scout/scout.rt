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
scout.AbortKeyStroke = function(field, $drawingArea) {
  scout.AbortKeyStroke.parent.call(this, field, $drawingArea);
};
scout.inherits(scout.AbortKeyStroke, scout.CloseKeyStroke);

scout.AbortKeyStroke.prototype.handle = function(event) {
  this.field.abort();
};
