/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  AbstractLayout, Action, aria, arrays, Button, ButtonAdapterMenu, CloneOptions, CompositeField, EnumObject, fields, Form, FormField, FormFieldStatusPosition, GroupBoxEventMap, GroupBoxGridConfig, GroupBoxLayout, GroupBoxMenuItemsOrder,
  GroupBoxModel, GroupBoxResponsiveHandler, HAlign, HtmlComponent, InitModelOf, KeyStrokeRenderingHints, LogicalGrid, LogicalGridData, LogicalGridLayout, LogicalGridLayoutConfig, Menu, MenuBar, MenuBarEllipsisPosition, Notification,
  ObjectOrChildModel, ObjectOrModel, ObjectUuidProvider, ResponsiveManager, scout, SplitBox, strings, TabBox, TabItemKeyStroke, tooltips, VerticalSmartGrid, WrappedFormField
} from '../../../index';
import $ from 'jquery';

export class GroupBox extends CompositeField implements GroupBoxModel {
  declare model: GroupBoxModel;
  declare eventMap: GroupBoxEventMap;
  declare self: GroupBox;

  fields: FormField[];
  menuBarVisible: boolean;
  menuBarPosition: GroupBoxMenuBarPosition;
  menuBarEllipsisPosition: MenuBarEllipsisPosition;
  notification: Notification;
  bodyLayoutConfig: LogicalGridLayoutConfig;
  borderDecoration: GroupBoxBorderDecoration;
  borderVisible: boolean;
  mainBox: boolean;
  menuBar: MenuBar;
  subLabel: string;
  scrollable: boolean;
  expandable: boolean;
  expanded: boolean;
  gridColumnCount: number;
  controls: FormField[];
  systemButtons: Button[];
  customButtons: Button[];
  processButtons: Button[];
  processMenus: Menu[];
  staticMenus: Menu[];
  responsive: boolean;
  responsiveHandler?: GroupBoxResponsiveHandler;
  htmlBody: HtmlComponent;
  $header: JQuery;
  $body: JQuery;
  $title: JQuery;
  $borderBottom: JQuery;
  $subLabel: JQuery;

  protected _statusPositionOrig: FormFieldStatusPosition;
  protected _collapsedWeightY: number;

  constructor() {
    super();
    this._addWidgetProperties(['fields', 'notification', 'staticMenus']);
    this._addCloneProperties(['menuBarVisible', 'bodyLayoutConfig', 'borderDecoration', 'borderVisible', 'expandable', 'expanded', 'gridColumnCount', 'scrollable', 'subLabel']);

    this.fields = [];
    this.menuBarVisible = true;
    this.menuBarPosition = GroupBox.MenuBarPosition.AUTO;
    this.menuBarEllipsisPosition = MenuBar.EllipsisPosition.RIGHT;
    this.notification = null;
    this.bodyLayoutConfig = null;
    this.borderDecoration = GroupBox.BorderDecoration.AUTO;
    this.borderVisible = true;
    this.mainBox = false;
    this.scrollable = null; // set to null to enable conditional default  -> it will be set to true if it is a MainBox unless it was explicitly set to false
    this.expandable = false;
    this.expanded = true;
    this.logicalGrid = scout.create(VerticalSmartGrid);
    this.gridColumnCount = 2;
    this.gridDataHints.useUiHeight = true;
    this.gridDataHints.w = FormField.FULL_WIDTH;
    this.controls = [];
    this.systemButtons = [];
    this.customButtons = [];
    this.processButtons = [];
    this.processMenus = [];
    this.staticMenus = [];
    this.responsive = null;
    this.$header = null;
    this.$body = null;
    this.$title = null;
    this.$subLabel = null;
    this._statusPositionOrig = null;
  }

  static BorderDecoration = {
    /**
     * Makes {@link borderVisible} to be computed automatically which means top and bottom paddings are invisible if it is a {@link mainBox}.
     * @see _computeBorderVisible
     */
    AUTO: 'auto',
    /**
     * The top and bottom paddings are always visible, even if it is a {@link mainBox}.
     */
    EMPTY: 'empty',
    /**
     * Currently has no effect.
     */
    LINE: 'line'
  } as const;

  static MenuBarPosition = {
    AUTO: 'auto',
    TOP: 'top',
    BOTTOM: 'bottom',
    TITLE: 'title'
  } as const;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.resolveConsts([{
      property: 'menuBarPosition',
      constType: GroupBox.MenuBarPosition
    }]);
    this.resolveTextKeys(['subLabel']);
    this._setBodyLayoutConfig(this.bodyLayoutConfig);
    this.menuBar = scout.create(MenuBar, {
      parent: this,
      menuOrder: new GroupBoxMenuItemsOrder(),
      ellipsisPosition: this.menuBarEllipsisPosition
    });
    this.menuBar.on('propertyChange:visible', () => this._updateMenuBarStyle());
    this._setFields(this.fields);
    this._setMainBox(this.mainBox);
    this._updateMenuBar();

    ResponsiveManager.get().registerHandler(this, scout.create(GroupBoxResponsiveHandler, {
      widget: this
    }));

    this._setResponsive(this.responsive);
  }

  protected override _destroy() {
    ResponsiveManager.get().unregisterHandler(this);
    super._destroy();
  }

  getFields(): FormField[] {
    return this.fields;
  }

  insertField(field: ObjectOrChildModel<FormField>, index?: number) {
    let newFields = this.fields.slice() as ObjectOrChildModel<FormField>[];
    index = scout.nvl(index, this.fields.length);
    newFields.splice(index, 0, field);
    this.setFields(newFields);
  }

  insertFieldBefore(field: ObjectOrChildModel<FormField>, sibling: FormField) {
    scout.assertParameter('sibling', sibling);
    let index = this.fields.indexOf(sibling);
    this.insertField(field, index);
  }

  insertFieldAfter(field: ObjectOrChildModel<FormField>, sibling: FormField) {
    scout.assertParameter('sibling', sibling);
    let index = this.fields.indexOf(sibling) + 1;
    this.insertField(field, index);
  }

  deleteField(field: FormField) {
    let index = this.fields.indexOf(field);
    if (index < 0) {
      return;
    }
    let newFields = this.fields.slice();
    newFields.splice(index, 1);
    this.setFields(newFields);
  }

  /** @see GroupBoxModel.fields */
  setFields(fields: ObjectOrChildModel<FormField>[]) {
    this.setProperty('fields', fields);
  }

  protected _setFields(fields: FormField[]) {
    this._setProperty('fields', fields);
    this._prepareFields();
  }

  protected _renderFields(fields: FormField[]) {
    this._renderExpanded();
    this.invalidateLogicalGrid(true);
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    this.keyStrokeContext.invokeAcceptInputOnActiveValueField = true;
    this.keyStrokeContext.$bindTarget = this._keyStrokeBindTarget.bind(this);
  }

  protected override _setKeyStrokes(keyStrokes: Action | Action[]) {
    keyStrokes = arrays.ensure(keyStrokes);

    let groupBoxRenderingHints: KeyStrokeRenderingHints = {
      render: () => true,
      offset: 0,
      hAlign: HAlign.RIGHT,
      $drawingArea: ($drawingArea, event) => {
        if (this.$header && this.$header.isVisible()) {
          return this.$header;
        }
        return this.$body;
      }
    };

    keyStrokes.forEach(keyStroke => {
      keyStroke.actionKeyStroke.renderingHints = $.extend({}, keyStroke.actionKeyStroke.renderingHints, groupBoxRenderingHints);
    });

    super._setKeyStrokes(keyStrokes);
  }

  /**
   * Returns a $container used as a bind target for the keystroke context of the group-box.
   * By default, this function returns the container of the form, or when group-box is has no
   * form as a parent the container of the group-box.
   */
  protected _keyStrokeBindTarget(): JQuery {
    let form = this.getForm();
    if (form) {
      // keystrokes on a group-box have form scope
      return form.$container;
    }
    return this.$container;
  }

  protected override _render() {
    this.addContainer(this.$parent, this.mainBox ? 'root-group-box' : 'group-box');

    this.$header = this.$container.appendDiv('group-box-header');
    HtmlComponent.install(this.$header, this.session); // Complete layout chain for elements inside header (e.g. allow top status to invalidate layout when visibility changes)
    this.$title = this.$header.appendDiv('title');
    this.$borderBottom = this.$header.appendDiv('bottom-border');
    this.addLabel();
    this.addSubLabel();
    this.addStatus();
    this.$body = this.$container.appendDiv('group-box-body');
    aria.role(this.$body, 'group');
    this.htmlBody = HtmlComponent.install(this.$body, this.session);
    this.htmlBody.setLayout(this._createBodyLayout());
  }

  protected override _remove() {
    this._removeSubLabel();
    super._remove();
  }

  protected override _renderProperties() {
    this._renderScrollable(); // Need to be before renderExpanded in order to have the scrollbars when the fields are rendered. The status tooltips require a scrollable parent to move when scrolling.
    this._renderExpanded(); // Need to be before renderVisible is executed, otherwise controls might be rendered if group box is invisible which breaks some widgets (e.g. Tree and Table)
    super._renderProperties();

    this._renderBodyLayoutConfig();
    this._renderNotification();
    this._renderBorderVisible();
    this._renderExpandable();
    this._renderMenuBarPosition();
    this._renderMenuBarEllipsisPosition();
    this._renderMenuBarVisible();
    this._renderSubLabel();
  }

  protected override _createLayout(): AbstractLayout {
    return new GroupBoxLayout(this);
  }

  protected _createBodyLayout(): LogicalGridLayout {
    return new LogicalGridLayout(this, this.bodyLayoutConfig);
  }

  setBodyLayoutConfig(bodyLayoutConfig: ObjectOrModel<LogicalGridLayoutConfig>) {
    this.setProperty('bodyLayoutConfig', bodyLayoutConfig);
  }

  protected _setBodyLayoutConfig(bodyLayoutConfig: ObjectOrModel<LogicalGridLayoutConfig>) {
    this._setProperty('bodyLayoutConfig', LogicalGridLayoutConfig.ensure(bodyLayoutConfig || {}));
    LogicalGridLayoutConfig.initHtmlEnvChangeHandler(this, () => this.bodyLayoutConfig, layoutConfig => this.setBodyLayoutConfig(layoutConfig));
  }

  protected _renderBodyLayoutConfig() {
    let layout = this.htmlBody.layout as LogicalGridLayout;
    let oldMinWidth = layout.minWidth;
    this.bodyLayoutConfig.applyToLayout(layout);
    if (oldMinWidth !== this.bodyLayoutConfig.minWidth) {
      this._renderScrollable();
    }
    if (this.rendered) {
      this.htmlBody.invalidateLayoutTree();
    }
  }

  /**
   * Redraws the group box body by removing and re-rendering every control.
   * This may be necessary if a field does not support a dynamic property change and therefore needs to be redrawn completely to reflect the change.
   */
  rerenderControls() {
    this._removeControls();
    this._renderControls();
    this.htmlBody.invalidateLayoutTree();
  }

  protected _removeControls() {
    this.controls.forEach(control => control.remove());
  }

  protected _renderControls() {
    this.controls.forEach(control => {
      if (!control.rendered) {
        control.render(this.$body);
        // set each children layout data to logical grid data
        control.setLayoutData(new LogicalGridData(control));
      }
    });
  }

  addSubLabel() {
    if (this.$subLabel) {
      return;
    }
    this.$subLabel = this.$title.appendDiv('sub-label');
    tooltips.installForEllipsis(this.$subLabel, {
      parent: this
    });
  }

  protected _removeSubLabel() {
    if (!this.$subLabel) {
      return;
    }
    tooltips.uninstall(this.$subLabel);
    this.$subLabel.remove();
    this.$subLabel = null;
  }

  /** @see GroupBoxModel.subLabel */
  setSubLabel(subLabel: string) {
    this.setProperty('subLabel', subLabel);
  }

  protected _renderSubLabel() {
    this.$subLabel.setVisible(strings.hasText(this.subLabel));
    this.$subLabel.textOrNbsp(this.subLabel);
    this.$container.toggleClass('has-sub-label', this.$subLabel.isVisible());
    this.invalidateLayoutTree();
  }

  /** @see GroupBoxModel.scrollable */
  setScrollable(scrollable: boolean) {
    this.setProperty('scrollable', scrollable);
  }

  protected _renderScrollable() {
    this._uninstallScrollbars();

    // horizontal (x-axis) scrollbar is only installed when minWidth is > 0
    if (this.scrollable) {
      this._installScrollbars({
        axis: this.bodyLayoutConfig.minWidth > 0 ? 'both' : 'y'
      });
    } else if (this.bodyLayoutConfig.minWidth > 0) {
      this._installScrollbars({
        axis: 'x'
      });
    }
  }

  override get$Scrollable(): JQuery {
    return this.$body;
  }

  protected override _onScroll(event: JQuery.ScrollEvent) {
    super._onScroll(event);
    this._updateScrollShadow();
  }

  protected _updateScrollShadow() {
    if (this.mainBox || !this.rendered) {
      // No need to do anything if it's the mainBox because header is invisible and the menu bar already takes the full width
      return;
    }
    let hasScrollShadowTop = this.hasScrollShadow('top');
    let hasScrollShadowBottom = this.hasScrollShadow('bottom');
    let oldHasScrollShadowTop = this.$container.hasClass('has-scroll-shadow-top');
    let oldHasScrollShadowBottom = this.$container.hasClass('has-scroll-shadow-bottom');
    let hasMenubarTop = this.$container.hasClass('menubar-position-top');
    let hasMenubarBottom = this.$container.hasClass('menubar-position-bottom');
    let headerVisible = this.$header.isVisible();
    this.$container.toggleClass('has-scroll-shadow-top', hasScrollShadowTop);
    this.$container.toggleClass('has-scroll-shadow-bottom', hasScrollShadowBottom);
    if ((headerVisible || hasMenubarTop) && oldHasScrollShadowTop !== hasScrollShadowTop
      || hasMenubarBottom && oldHasScrollShadowBottom !== hasScrollShadowBottom) {
      this.invalidateLayoutTree(false);
    }

    // Enlarge header line if there is a shadow, but don't do it if there is a menubar on top
    fields.adjustStatusPositionForScrollShadow(this, () => hasScrollShadowTop && headerVisible && !hasMenubarTop);
  }

  setMainBox(mainBox: boolean) {
    this.setProperty('mainBox', mainBox);
  }

  protected _setMainBox(mainBox: boolean) {
    this._setProperty('mainBox', mainBox);
    if (this.mainBox) {
      this.menuBar.setCssClass('main-menubar');
      if (this.scrollable === null) {
        this.setScrollable(true);
      }
      if (this.responsive === null) {
        this.setResponsive(true);
      }
    }
  }

  override addLabel() {
    if (this.$label) {
      return;
    }
    this.$label = this.$title.appendDiv('label');
    if (this._computeTitleVisible()) { // add it as a heading if its not the invisible main box
      aria.linkElementWithHeader(this.$container, this.$label);
    }
    tooltips.installForEllipsis(this.$label, {
      parent: this
    });
  }

  protected override _renderLabel() {
    this.$label.textOrNbsp(this.label);

    if (this._computeTitleVisible()) {
      aria.linkElementWithLabel(this.$body, this.$label); // label linked with body so group navigation announces the group when entering it
    }

    if (this.rendered) {
      this._renderLabelVisible();
    }
  }

  override addStatus() {
    super.addStatus();
    this._updateStatusPosition();
  }

  protected override _renderStatusPosition() {
    this._updateStatusPosition();
  }

  protected _updateStatusPosition() {
    if (!this.fieldStatus) {
      return;
    }
    if (this.statusPosition === FormField.StatusPosition.TOP) {
      // move into header
      this.$status.appendTo(this.$header);
    } else {
      this.$status.appendTo(this.$container);
    }
    this.invalidateLayoutTree();
  }

  /** @see GroupBoxModel.notification */
  setNotification(notification: ObjectOrChildModel<Notification>) {
    this.setProperty('notification', notification);
  }

  protected _renderNotification() {
    if (!this.notification) {
      this.invalidateLayoutTree();
      return;
    }
    this.notification.render();
    this.notification.$container.insertBefore(this.$body);
    this.invalidateLayoutTree();
  }

  /** @internal */
  _prepareFields() {
    this.processButtons.forEach(this._unregisterButtonKeyStrokes.bind(this));

    this.controls = [];
    this.systemButtons = [];
    this.customButtons = [];
    this.processButtons = [];
    this.processMenus = [];

    for (let i = 0; i < this.fields.length; i++) {
      let field = this.fields[i];
      if (field instanceof Button) {
        if (field.processButton) {
          this.processButtons.push(field);
          if (field.systemType !== Button.SystemType.NONE) {
            this.systemButtons.push(field);
          } else {
            this.customButtons.push(field);
          }
        } else {
          this.controls.push(field);
          this._registerButtonKeyStrokes(field);
        }
      } else if (field instanceof TabBox) {
        this.controls.push(field);
        for (let k = 0; k < field.tabItems.length; k++) {
          if (field.tabItems[k].selectionKeystroke) {
            this.keyStrokeContext.registerKeyStroke(new TabItemKeyStroke(field.tabItems[k].selectionKeystroke, field.tabItems[k]));
          }
        }
      } else {
        this.controls.push(field);
      }
    }

    // Create menu for each process button
    this.processMenus = this.processButtons.map(button => {
      let model = ButtonAdapterMenu.adaptButtonProperties(button, {
        parent: this,
        menubar: this.menuBar,
        button: button,
        // initially defaultMenu should only be set if defaultButton is set to true, false should not be mapped as the default defaultMenu = null setting
        // would be overridden if this default null setting is overridden MenuBar.prototype.updateDefaultMenu would not consider these entries anymore
        defaultMenu: button.defaultButton ? true : null
      });
      return scout.create(ButtonAdapterMenu, model);
    });
    this.registerKeyStrokes(this.processMenus);
  }

  protected _unregisterButtonKeyStrokes(button: Button) {
    if (button.keyStrokes) {
      button.keyStrokes.forEach(keyStroke => this.keyStrokeContext.unregisterKeyStroke(keyStroke));
    }
  }

  protected _registerButtonKeyStrokes(button: Button) {
    if (button.keyStrokes) {
      button.keyStrokes.forEach(keyStroke => this.keyStrokeContext.registerKeyStroke(keyStroke));
    }
  }

  /** @see GroupBoxModel.borderVisible */
  setBorderVisible(borderVisible: boolean) {
    this.setProperty('borderVisible', borderVisible);
  }

  protected _renderBorderVisible() {
    let borderVisible = this.borderVisible;
    if (this.borderDecoration === GroupBox.BorderDecoration.AUTO) {
      borderVisible = this._computeBorderVisible(borderVisible);
    }

    this.$body.toggleClass('y-padding-invisible', !borderVisible);
    this.invalidateLayoutTree();
  }

  /** @see GroupBoxModel.borderDecoration */
  setBorderDecoration(borderDecoration: GroupBoxBorderDecoration) {
    this.setProperty('borderDecoration', borderDecoration);
  }

  // Don't include in renderProperties, it is not necessary to execute it initially because renderBorderVisible is executed already
  protected _renderBorderDecoration() {
    this._renderBorderVisible();
  }

  override getContextMenuItems(onlyVisible = true): Menu[] {
    if (this.menuBarVisible) {
      return [];
    }
    return super.getContextMenuItems(onlyVisible);
  }

  /** @see GroupBoxModel.menuBarVisible */
  setMenuBarVisible(visible: boolean) {
    this.setProperty('menuBarVisible', visible);
  }

  protected _setMenuBarVisible(visible: boolean) {
    this._setProperty('menuBarVisible', visible);
    this._updateMenuBar();
  }

  protected _renderMenuBarVisible() {
    if (this.menuBarVisible) {
      this._renderMenuBar();
    } else {
      this.menuBar.remove();
    }
    this._updateMenus();
    this.invalidateLayoutTree();
  }

  protected _renderMenuBar() {
    this.menuBar.render();
    if (this.menuBarPosition === GroupBox.MenuBarPosition.TITLE) {
      // move right of title
      let $control = this.$header.children('.group-box-control');
      if ($control.length > 0) {
        this.menuBar.$container.insertAfter($control);
      } else {
        this.menuBar.$container.insertAfter(this.$title);
      }
    } else if (this.menuBar.position === MenuBar.Position.TOP) {
      // move below header
      this.menuBar.$container.insertAfter(this.$header);
    }
  }

  /** @see GroupBoxModel.menuBarPosition */
  setMenuBarPosition(menuBarPosition: GroupBoxMenuBarPosition) {
    this.setProperty('menuBarPosition', menuBarPosition);
  }

  protected _renderMenuBarPosition() {
    let position = this.menuBarPosition;
    if (position === GroupBox.MenuBarPosition.AUTO) {
      position = GroupBox.MenuBarPosition.TOP;
      if (this.mainBox) {
        let form = this.getForm();
        if (form && form.isDialog()) {
          position = GroupBox.MenuBarPosition.BOTTOM;
        }
      }
    }

    if (position === GroupBox.MenuBarPosition.BOTTOM) {
      this.menuBar.setPosition(MenuBar.Position.BOTTOM);
    } else { // top + title
      this.menuBar.setPosition(MenuBar.Position.TOP);
    }
    this._renderMenuBarStyle();

    if (this.rendered) {
      this.menuBar.remove();
      this._renderMenuBarVisible();
    }
  }

  /** @see GroupBoxModel.menuBarEllipsisPosition */
  setMenuBarEllipsisPosition(menuBarEllipsisPosition: MenuBarEllipsisPosition) {
    this.setProperty('menuBarEllipsisPosition', menuBarEllipsisPosition);
    this.menuBar.setEllipsisPosition(menuBarEllipsisPosition);
  }

  protected _renderMenuBarEllipsisPosition() {
    this.menuBar.reorderMenus();
    if (this.rendered) {
      this.menuBar.remove();
      this._renderMenuBarVisible();
    }
  }

  protected _updateMenuBarStyle() {
    if (this.rendered) {
      this._renderMenuBarStyle();
    }
  }

  protected _renderMenuBarStyle() {
    let visible = this.menuBar.visible;
    let hasTitleMenuBar = this.menuBarPosition === GroupBox.MenuBarPosition.TITLE;
    this.$header.toggleClass('has-menubar', visible && hasTitleMenuBar);
    this.$container.toggleClass('menubar-position-top', visible && !hasTitleMenuBar && this.menuBar.position === MenuBar.Position.TOP);
    this.$container.toggleClass('menubar-position-bottom', visible && !hasTitleMenuBar && this.menuBar.position === MenuBar.Position.BOTTOM);
  }

  protected _computeBorderVisible(borderVisible: boolean): boolean {
    if (this.mainBox) {
      borderVisible = false;
    } else if (this.parent instanceof GroupBox &&
      this.parent.parent instanceof Form &&
      this.parent.parent.parent instanceof WrappedFormField &&
      this.parent.parent.parent.parent instanceof SplitBox &&
      this.parent.getFields().length === 1) {
      // Special case for wizard: wrapped form in split box with a single group box
      borderVisible = false;
    }
    return borderVisible;
  }

  /** @see GroupBoxModel.expandable */
  setExpandable(expandable: boolean) {
    this.setProperty('expandable', expandable);
  }

  protected _renderExpandable() {
    let expandable = this.expandable;
    let $control = this.$header.children('.group-box-control');

    if (expandable) {
      if ($control.length === 0) {
        // Create control if necessary
        this.$container.makeDiv('group-box-control')
          .on('click', this._onControlClick.bind(this))
          .insertAfter(this.$title);
      }
      this.$header
        .addClass('expandable')
        .on('click.group-box-control', this._onControlClick.bind(this));
    } else {
      $control.remove();
      this.$header
        .removeClass('expandable')
        .off('.group-box-control');
    }
    this.invalidateLayoutTree();
  }

  /** @see GroupBoxModel.expanded */
  setExpanded(expanded: boolean) {
    this.setProperty('expanded', expanded);
  }

  protected _renderExpanded() {
    this.$container.toggleClass('collapsed', !this.expanded);

    // Group boxes have set "useUiHeight=true" by default. When a group box is collapsed, it should not
    // stretch vertically (no "weight Y"). However, because "weightY" is -1 by default, a calculated value
    // is assigned (LogicalGridData._inheritWeightY()) that is based on the group boxes height. In collapsed
    // state, this height would be wrong. Therefore, we manually assign "weightY=0" to collapsed group boxes
    // to prevent them from being stretched.
    if (this.expanded) {
      // If group box was previously collapsed, restore original "weightY" gridData value
      if (this._collapsedWeightY !== undefined) {
        this.gridData.weightY = this._collapsedWeightY;
        delete this._collapsedWeightY;
      }
      // Update inner layout (e.g. menubar)
      this.invalidateLayout();
      this._renderControls();
    } else {
      // If group box has a weight different from 0, we set it to zero and back up the old value
      if (this.gridData.weightY !== 0) {
        this._collapsedWeightY = this.gridData.weightY;
        this.gridData.weightY = 0;
      }
    }

    this.invalidateLayoutTree();
  }

  /** @see GroupBoxModel.gridColumnCount */
  setGridColumnCount(gridColumnCount: number) {
    this.setProperty('gridColumnCount', gridColumnCount);
    this.invalidateLogicalGrid();
  }

  override invalidateLogicalGrid(invalidateLayout?: boolean) {
    super.invalidateLogicalGrid(false);
    if (scout.nvl(invalidateLayout, true) && this.rendered) {
      this.htmlBody.invalidateLayoutTree();
    }
  }

  protected override _setLogicalGrid(logicalGrid: LogicalGrid | string) {
    super._setLogicalGrid(logicalGrid);
    if (this.logicalGrid) {
      this.logicalGrid.setGridConfig(new GroupBoxGridConfig());
    }
  }

  protected override _renderLabelVisible() {
    this.$header.setVisible(this._computeTitleVisible());
    this._updateFieldStatus();
    if (this.menuBarPosition === GroupBox.MenuBarPosition.TITLE) {
      this.invalidateLayoutTree();
    }
  }

  protected _computeTitleVisible(labelVisible?: boolean): boolean {
    labelVisible = scout.nvl(labelVisible, this.labelVisible);
    return !!(labelVisible && this.label && !this.mainBox);
  }

  /**
   * Only show the group box status if title is visible.
   */
  protected override _computeStatusVisible(): boolean {
    return super._computeStatusVisible() && this._computeTitleVisible();
  }

  protected override _setMenus(menus: Menu | Menu[]) {
    super._setMenus(menus);

    if (this.menuBar) {
      // updateMenuBar is required because menuBar is not created yet when synMenus is called initially
      this._updateMenuBar();
    }
  }

  /** @internal */
  _updateMenuBar() {
    if (!this.menuBarVisible) {
      // Do not update menuBar while it is invisible, the menus may now be managed by another widget.
      // -> this makes sure the parent is not accidentally set to the group box, the other widget should remain responsible
      this.menuBar.setMenuItems([]);
      return;
    }
    let menus = this.staticMenus
      .concat(this.processMenus)
      .concat(this.menus);

    this.menuBar.setMenuItems(menus);
  }

  protected _removeMenus() {
    // menubar takes care of removal
  }

  setStaticMenus(staticMenus: ObjectOrChildModel<Menu>[]) {
    this.setProperty('staticMenus', staticMenus);
    this._updateMenuBar();
  }

  protected _onControlClick(event: JQuery.ClickEvent) {
    if (!this.expandable) {
      return;
    }
    const target = scout.widget(event.target);
    if (this.menuBarPosition === GroupBox.MenuBarPosition.TITLE && this.menuBar.has(target)) {
      // If the position of the menubar is set to title and a menu has been clicked, then the event must not be handled
      return;
    }

    this.setExpanded(!this.expanded);
    $.suppressEvent(event); // otherwise, the event would be triggered twice sometimes (by group-box-control and group-box-title)
  }

  /** @see GroupBoxModel.responsive */
  setResponsive(responsive: boolean) {
    this.setProperty('responsive', responsive);
  }

  protected _setResponsive(responsive: boolean) {
    this._setProperty('responsive', responsive);

    if (!this.initialized) {
      return;
    }
    if (this.responsive) {
      ResponsiveManager.get().reset(this, true);
    } else {
      ResponsiveManager.get().reset(this, true);
      if (this.responsive === null) {
        let parent = this.findParent(parent => parent instanceof GroupBox && parent.responsive) as GroupBox;
        ResponsiveManager.get().reset(parent, true);
      }
    }
    this.invalidateLayoutTree();
  }

  override clone(model: GroupBoxModel, options?: CloneOptions): this {
    let clone = super.clone(model) as GroupBox;
    this._deepCloneProperties(clone, ['fields'], options);
    clone._prepareFields();
    return clone as this;
  }
}

export type GroupBoxBorderDecoration = EnumObject<typeof GroupBox.BorderDecoration>;
export type GroupBoxMenuBarPosition = EnumObject<typeof GroupBox.MenuBarPosition>;

ObjectUuidProvider.UuidPathSkipWidgets.add(GroupBox);
