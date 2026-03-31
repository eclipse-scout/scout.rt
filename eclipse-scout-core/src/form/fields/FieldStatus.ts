/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  aria, arrays, ContextMenuPopup, Device, EventHandler, FieldStatusEventMap, FieldStatusExecKeyStroke, FieldStatusModel, FormField, FormFieldStatusPosition, HierarchyChangeEvent, HtmlComponent, KeyStrokeContext, Menu, PropertyChangeEvent,
  scout, Status, StatusOrModel, strings, Tooltip, Widget
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

  static SEVERITY_CSS_CLASSES = 'has-error has-warning has-info has-ok';

  constructor() {
    super();
    this.tooltip = null;
    this.contextMenu = null;
    this.status = null;
    this.updating = false;
    this.autoRemove = true;
    this.position = FormField.StatusPosition.DEFAULT;
    this.inheritAccessibility = false;
    this.preventInitialFocus = true;
    this.preventClickFocus = true;
    this.menus = [];

    this._parents = [];
    this._parentPropertyChangeListener = this._onParentPropertyChange.bind(this);
    this._parentHierarchyChangeListener = this._onParentHierarchyChange.bind(this);
  }

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    this.keyStrokeContext.registerKeyStroke(new FieldStatusExecKeyStroke(this));
  }

  protected override _render() {
    this.$container = this.$parent.appendSpan('status field-status')
      .on('mousedown', this._onStatusMouseDown.bind(this));
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    aria.role(this.$container, 'button');
    aria.hasPopup(this.$container, 'menu');
    aria.expanded(this.$container, false);
    this._updateVisibility();
    this._updateHasStatus();
    this.updateHasMenus();
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

  protected override _renderTabbable() {
    let hasMenus = !!this.menus.length;
    this.$container.setTabbable(hasMenus && this.enabledComputed && !Device.get().supportsOnlyTouch());
  }

  protected override _updateEnabledComputed(enabledComputed: boolean, enabledComputedForChildren?: boolean) {
    // The enabled state of the field status is irrelevant for its child menus -> always pass the state of the field
    // This is because the field status should be enabled even if the parent field is disabled (inheritAccessibility is false), but the actual menus should be disabled
    super._updateEnabledComputed(enabledComputed, this.parent.enabledComputed);
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
    this._updateAriaLabel();
    this._updateVisibility();
    this._updateHasStatus();
  }

  protected _updateVisibility() {
    let invisible = !this.menus.length && !this.status;
    if (invisible && this.isFocused()) {
      this.session.focusManager.focusNextTabbable(this.get$Focusable());
    }
    this.$container.toggleClass('invisible', invisible);
  }

  protected _updateAriaLabel() {
    let hasMenus = this.menus.length > 0;

    let label = this.session.text('ui.MoreInformation');
    if (hasMenus) {
      label = this.session.text('ui.MoreActions');
    } else if (this.status?.isWarning()) {
      label = this.session.text('ui.Warning');
    } else if (this.status?.isError()) {
      label = this.session.text('ui.ErrorMessage');
    }
    aria.label(this.$container, label);
  }

  protected _updateHasStatus() {
    FieldStatus.updateHasStatus(this.$container, this.status);
  }

  /**
   * Sets or removes the status severity css classes on the given `$container`.
   */
  static updateHasStatus($container: JQuery<HTMLElement>, status: Status) {
    $container.removeClass(FieldStatus.SEVERITY_CSS_CLASSES);
    if (!status) {
      return;
    }

    let classes = 'has-' + status.cssClass();
    $container.addClass(classes);
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
    this._updateAriaLabel();
    this._updateVisibility();
    this._renderTabbable();
    this.updateHasMenus();
  }

  /**
   * Sets or removes the css class `has-menus` on the given `$container` or on `this.$container` if no container is provided.
   */
  updateHasMenus($container?: JQuery) {
    $container = scout.nvl($container, this.$container);
    $container.toggleClass('has-menus', !!this.menus.length);
  }

  setAutoRemove(autoRemove: boolean) {
    this.setProperty('autoRemove', autoRemove);
  }

  protected _renderAutoRemove() {
    if (!this.updating) {
      this._updatePopup();
    }
  }

  /**
   * Closes the open tooltip.
   *
   * @param immediately true, to immediately close it without waiting for the remove animation.
   */
  hideTooltip(immediately = false) {
    if (!this.tooltip) {
      return;
    }

    let event = this.trigger('hideTooltip');
    if (!event.defaultPrevented) {
      this.tooltip.destroy();
      this._removeParentListeners();
      if (immediately) {
        this.tooltip.removeImmediately();
      }
    }
  }

  protected _updatePopup(showStatus?: boolean) {
    if (!this._requiresTooltip()) {
      this.hideTooltip();
    }
    if (!this.menus.length) {
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
    if (!this.menus.length && !strings.hasText(this.status.message)) {
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
    this.hideContextMenu(true);
    if (this.tooltip && this.tooltip.autoRemove !== this.autoRemove) {
      // Close tooltip if the autoRemove property changes, the other properties can be updated even if the tooltip is open.
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
      aria.role(this.tooltip.$content, 'alert');
      this.$container.addClass('selected');
      aria.expanded(this.$container, true);
      aria.linkElementWithControls(this.$container, this.tooltip.$container);
      this.recomputeEnabled(); // triggers _updateEnabledComputed
      this.tooltip.one('destroy', () => {
        this.tooltip = null;
        if (this.$container) {
          this.$container.removeClass('selected');
          aria.expanded(this.$container, false);
          aria.removeControls(this.$container);
        }
      });
    }
  }

  /**
   * Closes the open context menu.
   *
   * @param immediately true, to immediately close it without waiting for the remove animation.
   */
  hideContextMenu(immediately = false) {
    if (!this.contextMenu) {
      return;
    }

    this.contextMenu.close();
    if (immediately) {
      this.contextMenu.removeImmediately();
    }
  }

  showContextMenu() {
    if (arrays.empty(this.menus)) {
      // at least one menu item must be visible
      return;
    }

    this.hidePopup(true);

    this.contextMenu = scout.create(ContextMenuPopup, {
      parent: this,
      $anchor: this.$container,
      menuItems: this.menus,
      cloneMenuItems: false,
      closeOnAnchorMouseDown: false
    });
    this.contextMenu.open();
    this.$container.addClass('selected');
    aria.expanded(this.$container, true);
    aria.linkElementWithControls(this.$container, this.contextMenu.$container);
    this.recomputeEnabled(); // triggers _updateEnabledComputed
    this.contextMenu.one('destroy', () => {
      this.contextMenu = null;
      if (this.$container) {
        this.$container.removeClass('selected');
        aria.expanded(this.$container, false);
        aria.removeControls(this.$container);
      }
    });
  }

  /**
   * Closes the open popup (tooltip or context menu).
   *
   * @param immediately true, to immediately close it without waiting for the remove animation.
   */
  hidePopup(immediately = false) {
    this.hideTooltip(immediately);
    this.hideContextMenu(immediately);
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
      this.doAction();
    }
  }

  doAction() {
    if (!this.enabledComputed) {
      return;
    }

    this.togglePopup();

    // Ensure the user can use keyboard to select the menus inside the tooltip.
    // Ideally, a tooltip would always be a focus context if it had menus, but some status tooltips will be opened during field input.
    // In that case we do not want the tooltip to take the focus away from the input
    // -> Only do it when the user explicitly requested the opening of the tooltip.
    let withFocusContext = this.menus.length > 0;
    this.tooltip?.setWithFocusContext(withFocusContext);

    // Remove 'alert' role to prevent a screen reader from reading it twice if it gains focus
    if (withFocusContext) {
      aria.role(this.tooltip?.$content, null);
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
