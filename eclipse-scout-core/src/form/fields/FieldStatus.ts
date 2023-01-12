/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  arrays, ContextMenuPopup, EventHandler, FieldStatusEventMap, FieldStatusModel, FormField, FormFieldStatusPosition, HierarchyChangeEvent, HtmlComponent, Menu, PropertyChangeEvent, scout, Status, StatusOrModel, strings, Tooltip, Widget
} from '../../index';

export class FieldStatus extends Widget implements FieldStatusModel {
  declare model: FieldStatusModel;
  declare eventMap: FieldStatusEventMap;
  declare self: FieldStatus;

  autoRemove: boolean;
  status: Status;
  position: FormFieldStatusPosition;
  menus: Menu[];
  tooltip: Tooltip;
  contextMenu: ContextMenuPopup;
  updating: boolean;

  protected _parents: Widget[];
  protected _parentPropertyChangeListener: EventHandler<PropertyChangeEvent<any, Widget>>;
  protected _parentHierarchyChangeListener: EventHandler<HierarchyChangeEvent>;

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

  protected override _render() {
    this.$container = this.$parent.appendSpan('status')
      .on('mousedown', this._onStatusMouseDown.bind(this));
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
  }

  protected override _remove() {
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

  protected override _renderProperties() {
    super._renderProperties();
    this._renderPosition();
  }

  update(status: StatusOrModel, menus: Menu | Menu[], autoRemove: boolean, showStatus?: boolean) {
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

  setStatus(status: StatusOrModel) {
    this.setProperty('status', status);
  }

  protected _setStatus(status: StatusOrModel) {
    status = Status.ensure(status);
    this._setProperty('status', status);
  }

  protected _renderStatus() {
    if (!this.updating) {
      this._updatePopup();
    }
  }

  setPosition(position: FormFieldStatusPosition) {
    this.setProperty('position', position);
  }

  protected _renderPosition() {
    this.$container.toggleClass('top', this.position === FormField.StatusPosition.TOP);
    this.invalidateLayoutTree();
  }

  protected override _renderVisible() {
    super._renderVisible();
    if (!this.visible) {
      this.hidePopup();
    }
  }

  setMenus(menus: Menu | Menu[]) {
    this.setProperty('menus', arrays.ensure(menus));
  }

  protected _renderMenus() {
    if (!this.updating) {
      this._updatePopup();
    }
  }

  setAutoRemove(autoRemove: boolean) {
    this.setProperty('autoRemove', autoRemove);
  }

  protected _renderAutoRemove() {
    if (!this.updating) {
      this._updatePopup();
    }
  }

  hideTooltip() {
    if (this.tooltip) {
      let event = this.trigger('hideTooltip');
      if (!event.defaultPrevented) {
        this.tooltip.destroy();
        this._removeParentListeners();
      }
    }
  }

  protected _updatePopup(showStatus?: boolean) {
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

  protected _requiresTooltip(): boolean {
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
    let event = this.trigger('showTooltip');
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
      this.tooltip = scout.create(Tooltip, {
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

    this.contextMenu = scout.create(ContextMenuPopup, {
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

  protected _onStatusMouseDown(event: JQuery.MouseDownEvent) {
    let statusDownEvent = this.trigger('statusMouseDown', event);
    if (!statusDownEvent.defaultPrevented) {
      this.togglePopup();
    }
  }

  protected _updateTooltipVisibility(parent: Widget) {
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

  protected _onParentHierarchyChange(event: HierarchyChangeEvent) {
    // If the parent of a widget we're listening to changes, we must re-check the parent hierarchy
    // and re-install the property change listener
    this._updateParentListeners();
  }

  protected _onParentPropertyChange(event: PropertyChangeEvent<any, Widget>) {
    if ('visible' === event.propertyName) {
      this._updateTooltipVisibility(event.source);
    }
  }

  protected _removeParentListeners() {
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
  protected _updateParentListeners() {
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
