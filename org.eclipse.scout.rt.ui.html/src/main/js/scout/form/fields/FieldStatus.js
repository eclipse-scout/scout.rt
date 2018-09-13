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
scout.FieldStatus = function() {
  scout.FieldStatus.parent.call(this);
  this.tooltip;
  this.contextMenu;
  this.status;
  this.updating = false;
  this.autoRemove = true;
};
scout.inherits(scout.FieldStatus, scout.Widget);

scout.FieldStatus.StatusPosition = {
  DEFAULT: 'default',
  TOP: 'top'
};

scout.FieldStatus.prototype._render = function() {
  this.$container = this.$parent.appendSpan('status')
    .on('mousedown', this._onStatusMouseDown.bind(this));
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
};

scout.FieldStatus.prototype._remove = function() {
  scout.FieldStatus.parent.prototype._remove.call(this);
  if (this.tooltip) {
    this.tooltip.destroy();
    this.tooltip = null;
  }
  if (this.contextMenu) {
    this.contextMenu.destroy();
    this.contextMenu = null;
  }
};

scout.FieldStatus.prototype._renderProperties = function() {
  scout.FieldStatus.parent.prototype._renderProperties.call(this);
  this._renderPosition();
};

scout.FieldStatus.prototype.update = function(status, menus, autoRemove, showStatus) {
  this.updating = true;
  this.setStatus(status);
  this.setMenus(menus);
  this.setAutoRemove(autoRemove);
  this.updating = false;
  this._updatePopup(showStatus);
};

scout.FieldStatus.prototype.clearStatus = function() {
  this.setStatus(null);
};

scout.FieldStatus.prototype.setStatus = function(status) {
  this.setProperty('status', status);
};

scout.FieldStatus.prototype._setStatus = function(status) {
  status = scout.Status.ensure(status);
  this._setProperty('status', status);
};

scout.FieldStatus.prototype._renderStatus = function() {
  if (!this.updating) {
    this._updatePopup();
  }
};

scout.FieldStatus.prototype.setAutoRemove = function(autoRemove) {
  this.setProperty('autoRemove', autoRemove);
};

scout.FieldStatus.prototype._renderAutoRemove = function(autoRemove) {
  if (!this.updating) {
    this._updatePopup();
  }
};

scout.FieldStatus.prototype.setPosition = function(position) {
  this.setProperty('position', position);
};

scout.FieldStatus.prototype._renderPosition = function() {
  this.$container.toggleClass('top', this.position === scout.FieldStatus.StatusPosition.TOP);
  this.invalidateLayoutTree();
};

scout.FieldStatus.prototype._renderVisible = function() {
  scout.FieldStatus.parent.prototype._renderVisible.call(this);
  if (!this.visible) {
    this.hidePopup();
  }
};

scout.FieldStatus.prototype.setMenus = function(menus) {
  this.setProperty('menus', scout.arrays.ensure(menus));
};

scout.FieldStatus.prototype._renderMenus = function() {
  if (!this.updating) {
    this._updatePopup();
  }
};

scout.FieldStatus.prototype.setAutoRemove = function(autoRemove) {
  this.setProperty('autoRemove', autoRemove);
};

scout.FieldStatus.prototype._renderAutoRemove = function() {
  if (!this.updating) {
    this._updatePopup();
  }
};

scout.FieldStatus.prototype.hideTooltip = function() {
  var event = new scout.Event();
  if (this.tooltip) {
    this.trigger('hideTooltip', event);
    if (!event.defaultPrevented) {
      this.tooltip.destroy();
      this.tooltip = null;
    }
  }
};

scout.FieldStatus.prototype._updatePopup = function(showStatus) {
  if (!this.status) {
    this.hideTooltip();
  }
  if (scout.arrays.empty(this.menus)) {
    this.hideContextMenu();
  }
  if (showStatus === true) {
    this.showTooltip();
  } else if (showStatus === false) {
    this.hideTooltip();
  }

};

scout.FieldStatus.prototype.showTooltip = function() {
  if (!this.status) {
    return;
  }
  var event = new scout.Event();
  this.trigger('showTooltip', event);
  if (event.defaultPrevented) {
    return;
  }

  this.hideContextMenu();
  if (this.tooltip && this.tooltip.autoRemove !== this.autoRemove) {
    // close
    this.hideTooltip();
  }
  if (this.tooltip) {
    // update existing tooltip
    this.tooltip.setText(this.status.message);
    this.tooltip.setSeverity(this.status.severity);
    this.tooltip.setMenus(this.menus);
  } else {
    this.tooltip = scout.create('Tooltip', {
      parent: this,
      $anchor: this.$container,
      text: this.status.message,
      severity: this.status.severity,
      autoRemove: this.autoRemove,
      menus: this.menus
    });
    this.tooltip.one('destroy', function() {
      this.hideTooltip();
    }.bind(this));
    this.tooltip.render();
  }
};

scout.FieldStatus.prototype.hideContextMenu = function() {
  if (this.contextMenu) {
    this.contextMenu.destroy();
    this.contextMenu = null;
  }
};

scout.FieldStatus.prototype.showContextMenu = function() {
  if (scout.arrays.empty(this.menus)) {
    // at least one menu item must be visible
    return;
  }
  // close both contextMenu and status tooltip
  this.hidePopup();

  this.contextMenu = scout.create('ContextMenuPopup', {
    parent: this,
    $anchor: this.$container,
    menuItems: this.menus,
    cloneMenuItems: false,
    closeOnAnchorMouseDown: false
  });
  this.contextMenu.one('destroy', function() {
    this.hideContextMenu();
  }.bind(this));
  this.contextMenu.open();
};

scout.FieldStatus.prototype.hidePopup = function() {
  this.hideTooltip();
  this.hideContextMenu();
};

scout.FieldStatus.prototype.togglePopup = function() {
  if (this.status) {
    // ensure context menu closed
    this.hideContextMenu();
    if (this.tooltip) {
      this.hideTooltip();
    } else {
      this.showTooltip();
    }
    return;
  }
  if (!scout.arrays.empty(this.menus)) {
    this.hideTooltip();
    var func = function func(event) {
      if (!this.rendered || !this.attached) { // check needed because function is called asynchronously
        return;
      }
      // Toggle menu
      if (this.contextMenu) {
        this.hideContextMenu();
      } else {
        this.showContextMenu();
      }
    }.bind(this);

    this.session.onRequestsDone(func, event);

  } else {
    // close all
    this.hidePopup();
  }
};

scout.FieldStatus.prototype._onStatusMouseDown = function(event) {
  this.trigger('statusMouseDown', event);
  if (!event.defaultPrevented) {
    this.togglePopup();
  }
};
