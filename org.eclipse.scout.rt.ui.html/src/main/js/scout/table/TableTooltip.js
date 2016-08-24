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
scout.TableTooltip = function() {
  scout.TableTooltip.parent.call(this);
};
scout.inherits(scout.TableTooltip, scout.Tooltip);

scout.TableTooltip.prototype._init = function(options) {
  scout.TableTooltip.parent.prototype._init.call(this, options);

  this.table = options.table;
};

scout.TableTooltip.prototype._render = function($parent) {
  scout.TableTooltip.parent.prototype._render.call(this, $parent);

  this._rowOrderChangedFunc = function(event) {
    if (event.animating) {
      // row is only set while animating
      if (event.row === this.row) {
        this.position();
      }
    } else {
      this.position();
    }
  }.bind(this);
  this.table.on('rowOrderChanged', this._rowOrderChangedFunc);
};

scout.TableTooltip.prototype._remove = function() {
  scout.TableTooltip.parent.prototype._remove.call(this);
  this.table.off('rowOrderChanged', this._rowOrderChangedFunc);
};
