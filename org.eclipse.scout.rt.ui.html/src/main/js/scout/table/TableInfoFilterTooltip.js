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
scout.TableInfoFilterTooltip = function() {
  scout.TableInfoFilterTooltip.parent.call(this);
};
scout.inherits(scout.TableInfoFilterTooltip, scout.Tooltip);

scout.TableInfoFilterTooltip.prototype._init = function(options) {
  scout.TableInfoFilterTooltip.parent.prototype._init.call(this, options);

  this.tableFooter = options.tableFooter;
};

scout.TableInfoFilterTooltip.prototype._renderText = function() {
  var table = this.tableFooter.table,
    numRowsFiltered = table.filteredRows().length,
    filteredBy = table.filteredBy().join(', '); // filteredBy() returns an array

  this.$content.appendSpan()
    .text(this.session.text('ui.NumRowsFilteredBy', this.tableFooter.computeCountInfo(numRowsFiltered), filteredBy));
  this.$content.appendBr();
  this.$content.appendSpan('link')
    .text(this.session.text('ui.RemoveFilter'))
    .on('click', this._onRemoveFilterClick.bind(this));
};

scout.TableInfoFilterTooltip.prototype._onRemoveFilterClick = function() {
  this.tableFooter.table.resetFilter();
  this.destroy();
};
