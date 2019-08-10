/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.TableRowTileMapping = function() {
  scout.TableRowTileMapping.parent.call(this);
  this.tableRow = null;
  this.tile = null;
  this._addWidgetProperties(['tile']);
};
scout.inherits(scout.TableRowTileMapping, scout.Widget);
