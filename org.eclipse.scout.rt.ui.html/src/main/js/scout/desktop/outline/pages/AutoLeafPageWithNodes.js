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
scout.AutoLeafPageWithNodes = function() {
  scout.AutoLeafPageWithNodes.parent.call(this);

  this.leaf = true;
};
scout.inherits(scout.AutoLeafPageWithNodes, scout.Page);

/**
 * @override Page.js
 */
scout.AutoLeafPageWithNodes.prototype._init = function(model) {
  scout.assertParameter('row', model.row, scout.TableRow);
  scout.AutoLeafPageWithNodes.parent.prototype._init.call(this, model);
  this.text = this.row.cells[0];
};
