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
scout.TableInfoLoadTooltip = function() {
  scout.TableInfoLoadTooltip.parent.call(this);
};
scout.inherits(scout.TableInfoLoadTooltip, scout.Tooltip);

scout.TableInfoLoadTooltip.prototype._init = function(options) {
  scout.TableInfoLoadTooltip.parent.prototype._init.call(this, options);

  this.tableFooter = options.tableFooter;
};

scout.TableInfoLoadTooltip.prototype._renderText = function() {
  var table = this.tableFooter.table,
    numRows = table.rows.length;

  this.$content.appendSpan().text(this.session.text('ui.NumRowsLoaded', this.tableFooter.computeCountInfo(numRows)));
  this.$content.appendBr();
  this.$content.appendSpan('link')
    .text(this.session.text('ui.ReloadData'))
    .on('click', this._onReloadClick.bind(this));
};

scout.TableInfoLoadTooltip.prototype._onReloadClick = function() {
  this.tableFooter.table.reload();
  this.destroy();
};
