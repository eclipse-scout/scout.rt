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
scout.TableInfoSelectionTooltip = function() {
  scout.TableInfoSelectionTooltip.parent.call(this);
};
scout.inherits(scout.TableInfoSelectionTooltip, scout.Tooltip);

scout.TableInfoSelectionTooltip.prototype._init = function(options) {
  scout.TableInfoSelectionTooltip.parent.prototype._init.call(this, options);

  this.tableFooter = options.tableFooter;
};

scout.TableInfoSelectionTooltip.prototype._renderText = function() {
  var table = this.tableFooter.table,
    numRowsSelected = table.selectedRows.length;

  this.$content.appendSpan().text(this.session.text('ui.NumRowsSelected', this.tableFooter.computeCountInfo(numRowsSelected)));
  this.$content.appendBr();
  this.$content.appendSpan('link')
    .text(this.session.text('ui.SelectNone'))
    .on('click', this._onSelectNoneClick.bind(this));
  this.$content.appendBr();
  this.$content.appendSpan('link')
    .text(this.session.text('ui.SelectAll'))
    .on('click', this._onSelectAllClick.bind(this));
};

scout.TableInfoSelectionTooltip.prototype._onSelectNoneClick = function() {
  this.tableFooter.table.deselectAll();
  this.destroy();
};

scout.TableInfoSelectionTooltip.prototype._onSelectAllClick = function() {
  this.tableFooter.table.selectAll();
  this.destroy();
};
