/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TableRowDetail = function() {
  scout.TableRowDetail.parent.call(this);
  this.table;
  this.row;
};
scout.inherits(scout.TableRowDetail, scout.Widget);

scout.TableRowDetail.prototype._init = function(model) {
  scout.TableRowDetail.parent.prototype._init.call(this, model);
  this.table = model.table;
  this.row = model.row;
};

scout.TableRowDetail.prototype._render = function() {
  this.$container = this.$parent.appendDiv('table-row-detail');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this._renderRow();
};

scout.TableRowDetail.prototype._renderRow = function() {
  this.table.visibleColumns().forEach(function(column) {
    var name = column.text;
    var value = this.table.cellText(column, this.row);
    if (scout.strings.empty(value)) {
      return;
    }
    var $field = this.$container.appendDiv('table-row-detail-field');
    // TODO [7.0] cgu handle column without text or with icon, handle icon content, html content, bean content
    $field.appendSpan('table-row-detail-name').text(name + ': ');
    $field.appendSpan('table-row-detail-value').text(value);
  }, this);
};
