/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, Event, FormField, HtmlComponent, scout, Status, strings, Widget} from '../../index';

export default class FieldStatus extends Widget {

  constructor() {
    super();
    this.tooltip = null;
    this.contextMenu = null;
    this.status = null;
    this.updating = false;
    this.autoRemove = true;
    this.position = FormField.StatusPosition.DEFAULT;

    this._parents = [];
    this._parentPropertyChangeListener = this._onParentPropertyChange.bind(this);
    this._parentHierarchyChangeListener = this._onParentHierarchyChange.bind(this);
  }

  _render() {
    this.$container = this.$parent.appendSpan('status')
      .on('mousedown', this._onStatusMouseDown.bind(this));
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
  }

  _remove() {
    super._remove();
    if (this.tooltip) {
      this.tooltip.destroy();
      this.tooltip = null;
    }
    if (this.contextMenu) {
      this.contextMenu.destroy();
      this.contextMenu = null;
    }
    this._removeParentListeners();
  }

  _renderProperties() {
    super._renderProperties();
    this._renderPosition();
  }

  update(status, menus, autoRemove, showStatus) {
    this.updating = true;
    this.setStatus(status);
    this.setMenus(menus);
    this.setAutoRemove(autoRemove);
    this.updating = false;
    this._updatePopup(showStatus);
  }

  clearStatus() {
    this.setStatus(null);
  }

  setStatus(status) {
    this.setProperty('status', status);
  }

  _setStatus(status) {
    status = Status.ensure(status);
    this._setProperty('status', status);
  }

  _renderStatus() {
    if (!this.updating) {
      this._updatePopup();
    }
  }

  setPosition(position) {
    this.setProperty('position', position);
  }

  _renderPosition() {
    this.$container.toggleClass('top', this.position === FormField.StatusPosition.TOP);
    this.invalidateLayoutTree();
  }

  _renderVisible() {
    super._renderVisible();
    if (!this.visible) {
      this.hidePopup();
    }
  }

  setMenus(menus) {
    this.setProperty('menus', arrays.ensure(menus));
  }

  _renderMenus() {
    if (!this.updating) {
      this._updatePopup();
    }
  }

  setAutoRemove(autoRemove) {
    this.setProperty('autoRemove', autoRemove);
  }

  _renderAutoRemove() {
    if (!this.updating) {
      this._updatePopup();
    }
  }

  hideTooltip() {
    let event = new Event();
    if (this.tooltip) {
      this.trigger('hideTooltip', event);
      if (!event.defaultPrevented) {
        this.tooltip.destroy();
        this._removeParentListeners();
      }
    }
  }

  _updatePopup(showStatus) {
    if (!this._requiresTooltip()) {
      this.hideTooltip();
    }
    if (arrays.empty(this.menus)) {
      this.hideContextMenu();
    }
    if (showStatus === true) {
      this.showTooltip();
    } else if (showStatus === false) {
      this.hideTooltip();
    }
  }

  _requiresTooltip() {
    if (!this.status || !this.rendered) {
      return false;
    }
    if (arrays.empty(this.menus) && !strings.hasText(this.status.message)) {
      return false;
    }
    return true;
  }

  showTooltip() {
    if (!this._requiresTooltip()) {
      return;
    }
    let event = new Event();
    this.trigger('showTooltip', event);
    if (event.defaultPrevented) {
      return;
    }

    this._updateParentListeners();
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
      this.tooltip.render();
      this.$container.addClass('selected');
      this.tooltip.one('destroy', () => {
        this.tooltip = null;
        if (this.$container) {
          this.$container.removeClass('selected');
        }
      });
    }
  }

  hideContextMenu() {
    if (this.contextMenu) {
      this.contextMenu.close();
    }
  }

  showContextMenu() {
    if (arrays.empty(this.menus)) {
      // at least one menu item must be visible
      return;
    }
    // close both contextMenu and status tooltip
    this.hidePopup();

    // Context menu must be removed immediately before it can be opened because cloneMenuItems is false
    if (this.contextMenu && this.contextMenu.isRemovalPending()) {
      this.contextMenu.removeImmediately();
    }

    this.contextMenu = scout.create('ContextMenuPopup', {
      parent: this,
      $anchor: this.$container,
      menuItems: this.menus,
      cloneMenuItems: false,
      closeOnAnchorMouseDown: false
    });
    this.contextMenu.open();
    this.$container.addClass('selected');
    this.contextMenu.one('destroy', () => {
      this.contextMenu = null;
      if (this.$container) {
        this.$container.removeClass('selected');
      }
    });
  }

  hidePopup() {
    this.hideTooltip();
    this.hideContextMenu();
  }

  togglePopup() {
    if (this.status) {
      // ensure context menu closed
      this.hideContextMenu();
      this.toggleTooltip();
      return;
    }
    if (!arrays.empty(this.menus)) {
      this.hideTooltip();
      this.session.onRequestsDone(() => {
        if (!this.rendered) { // check needed because function is called asynchronously
          return;
        }
        this.toggleContextMenu();
      });

    } else {
      // close all
      this.hidePopup();
    }
  }

  toggleTooltip() {
    if (this.tooltip) {
      this.hideTooltip();
    } else {
      this.showTooltip();
    }
  }

  toggleContextMenu() {
    if (this.contextMenu) {
      this.hideContextMenu();
    } else {
      this.showContextMenu();
    }
  }

  _onStatusMouseDown(event) {
    this.trigger('statusMouseDown', event);
    if (!event.defaultPrevented) {
      this.togglePopup();
    }
  }

  _updateTooltipVisibility(parent) {
    if (this.isEveryParentVisible()) {
      /* We must use a timeout here, because the propertyChange event for the visible property
       * is triggered before the _renderVisible() function is called. Which means the DOM is still
       * invisible, thus the tooltip cannot be rendered. Because of the timeout we must double-check
       * the state of the FieldStatus, because it could have been removed in the meantime.
       */
      setTimeout(() => {
        if (!this.rendered || !this.isEveryParentVisible()) {
          return;
        }
        if (this.tooltip && !this.tooltip.rendered) {
          this.tooltip.render();
        }
      });
    } else {
      if (this.tooltip && this.tooltip.rendered) {
        this.tooltip.remove();
      }
    }
  }

  _onParentHierarchyChange(event) {
    // If the parent of a widget we're listening to changes, we must re-check the parent hierarchy
    // and re-install the property change listener
    this._updateParentListeners();
  }

  _onParentPropertyChange(event) {
    if ('visible' === event.propertyName) {
      this._updateTooltipVisibility(event.source);
    }
  }

  _removeParentListeners() {
    this._parents.forEach(parent => {
      parent.off('hierarchyChange', this._parentHierarchyChangeListener);
      parent.off('propertyChange', this._parentPropertyChangeListener);
    });
    this._parents = [];
  }

  /**
   * Adds a property change listener to every parent of the field status. We keep a list of all parents because
   * we need to remove the listeners later, also when the parent hierarchy has changed.
   */
  _updateParentListeners() {
    this._removeParentListeners();
    let parent = this.parent;
    while (parent) {
      parent.on('hierarchyChange', this._parentHierarchyChangeListener);
      parent.on('propertyChange', this._parentPropertyChangeListener);
      this._parents.push(parent);
      parent = parent.parent;
    }
  }
}
