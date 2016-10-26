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
scout.TableField = function() {
  scout.TableField.parent.call(this);
  this._addAdapterProperties(['table']);
};
scout.inherits(scout.TableField, scout.FormField);

scout.TableField.prototype._init = function(model) {
  scout.TableField.parent.prototype._init.call(this, model);

  this._delegatePropertyChange('enabled');
  this._delegatePropertyChange('disabledStyle');
};

scout.TableField.prototype._delegatePropertyChange = function(propertyName) {
  this.on('propertyChange', function(event) {
    if (event.newProperties.hasOwnProperty(propertyName)) {
      this.table.setProperty(propertyName, event.newProperties[propertyName]);
    }
  }.bind(this));
};

scout.TableField.prototype._render = function($parent) {
  this.addContainer($parent, 'table-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addStatus();
  this._renderTable();
};

scout.TableField.prototype.setTable = function(table) {
  this.setProperty('table', table);
};

scout.TableField.prototype._renderTable = function() {
  if (this.table) {
    this.table.render(this.$container);
    this.addField(this.table.$container);
  }
};

scout.TableField.prototype._removeTable = function() {
  if (this.table) {
    this.table.remove();
  }
  this._removeField();
};

