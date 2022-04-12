/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, Button, ButtonAdapterMenu, CompositeField, fields, Form, FormField, GroupBoxGridConfig, GroupBoxLayout, GroupBoxMenuItemsOrder, HAlign, HtmlComponent, LogicalGridData, LogicalGridLayout, LogicalGridLayoutConfig, MenuBar, ResponsiveManager, scout, SplitBox, strings, TabBox, TabItemKeyStroke, tooltips, WrappedFormField} from '../../../index';
import $ from 'jquery';

export default class GroupBox extends CompositeField {

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
    // set to null to enable conditional default
    // -> it will be set to true if it is a mainbox unless it was explicitly set to false
    this.scrollable = null;
    this.expandable = false;
    this.expanded = true;
    this.logicalGrid = scout.create('scout.VerticalSmartGrid');
    this.gridColumnCount = 2;
    this.gridDataHints.useUiHeight = true;
    this.gridDataHints.w = FormField.FULL_WIDTH;
    this.controls = [];
    this.systemButtons = [];
    this.customButtons = [];
    this.processButtons = [];
    this.processMenus = [];
    this.staticMenus = [];
    this.selectionKeystroke = null;
    this.responsive = null;

    this.$header = null;
    this.$body = null;
    this.$title = null;
    this.$subLabel = null;
    this._statusPositionOrig = null;
  }

  static BorderDecoration = {
    AUTO: 'auto',
    EMPTY: 'empty',
    LINE: 'line'
  };

  static MenuBarPosition = {
    AUTO: 'auto',
    TOP: 'top',
    BOTTOM: 'bottom',
    TITLE: 'title'
  };

  _init(model) {
    super._init(model);
    this.resolveConsts([{
      property: 'menuBarPosition',
      constType: GroupBox.MenuBarPosition
    }]);
    this._setBodyLayoutConfig(this.bodyLayoutConfig);
    this.menuBar = scout.create('MenuBar', {
      parent: this,
      menuOrder: new GroupBoxMenuItemsOrder(),
      ellipsisPosition: this.menuBarEllipsisPosition
    });
    this.menuBar.on('propertyChange:visible', () => this._updateMenuBarStyle());
    this._setFields(this.fields);
    this._setMainBox(this.mainBox);
    this._updateMenuBar();

    ResponsiveManager.get().registerHandler(this, scout.create('GroupBoxResponsiveHandler', {
      widget: this
    }));

    this._setResponsive(this.responsive);
  }

  _destroy() {
    ResponsiveManager.get().unregisterHandler(this);
    super._destroy();
  }

  /**
   * @override
   */
  getFields() {
    return this.fields;
  }

  insertField(field, index) {
    let newFields = this.fields.slice();
    index = scout.nvl(index, this.fields.length);
    newFields.splice(index, 0, field);
    this.setFields(newFields);
  }

  insertFieldBefore(field, sibling) {
    scout.assertParameter('sibling', sibling);
    let index = this.fields.indexOf(sibling);
    this.insertField(field, index);
  }

  insertFieldAfter(field, sibling) {
    scout.assertParameter('sibling', sibling);
    let index = this.fields.indexOf(sibling) + 1;
    this.insertField(field, index);
  }

  deleteField(field) {
    let newFields = this.fields.slice(),
      index = this.fields.indexOf(field);
    if (index < 0) {
      return;
    }
    newFields.splice(index, 1);
    this.setFields(newFields);
  }

  setFields(fields) {
    this.setProperty('fields', fields);
  }

  _setFields(fields) {
    this._setProperty('fields', fields);
    this._prepareFields();
  }

  _renderFields(fields) {
    this._renderExpanded();
    this.invalidateLogicalGrid(true);
  }

  /**
   * @override
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    this.keyStrokeContext.invokeAcceptInputOnActiveValueField = true;
    this.keyStrokeContext.$bindTarget = this._keyStrokeBindTarget.bind(this);
  }

  /**
   * @override FormField.js
   */
  _setKeyStrokes(keyStrokes) {
    keyStrokes = arrays.ensure(keyStrokes);

    let groupBoxRenderingHints = {
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

    keyStrokes
      .forEach(keyStroke => {
        keyStroke.actionKeyStroke.renderingHints = $.extend({}, keyStroke.actionKeyStroke.renderingHints, groupBoxRenderingHints);
      }, this);

    super._setKeyStrokes(keyStrokes);
  }

  /**
   * Returns a $container used as a bind target for the key-stroke context of the group-box.
   * By default this function returns the container of the form, or when group-box is has no
   * form as a parent the container of the group-box.
   */
  _keyStrokeBindTarget() {
    let form = this.getForm();
    if (form) {
      // keystrokes on a group-box have form scope
      return form.$container;
    }
    return this.$container;
  }

  _render() {
    this.addContainer(this.$parent, this.mainBox ? 'root-group-box' : 'group-box');

    this.$header = this.$container.appendDiv('group-box-header');
    HtmlComponent.install(this.$header, this.session); // Complete layout chain for elements inside header (e.g. allow top status to invalidate layout when visibility changes)
    this.$title = this.$header.appendDiv('title');
    this.$borderBottom = this.$header.appendDiv('bottom-border');
    this.addLabel();
    this.addSubLabel();
    this.addStatus();
    this.$body = this.$container.appendDiv('group-box-body');
    this.htmlBody = HtmlComponent.install(this.$body, this.session);
    this.htmlBody.setLayout(this._createBodyLayout());
  }

  _remove() {
    this._removeSubLabel();
    super._remove();
  }

  _renderProperties() {
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

  _createLayout() {
    return new GroupBoxLayout(this);
  }

  _createBodyLayout() {
    return new LogicalGridLayout(this, this.bodyLayoutConfig);
  }

  setBodyLayoutConfig(bodyLayoutConfig) {
    this.setProperty('bodyLayoutConfig', bodyLayoutConfig);
  }

  _setBodyLayoutConfig(bodyLayoutConfig) {
    if (!bodyLayoutConfig) {
      bodyLayoutConfig = new LogicalGridLayoutConfig();
    }
    this._setProperty('bodyLayoutConfig', LogicalGridLayoutConfig.ensure(bodyLayoutConfig));
  }

  _renderBodyLayoutConfig() {
    let oldMinWidth = this.htmlBody.layout.minWidth;
    this.bodyLayoutConfig.applyToLayout(this.htmlBody.layout);
    if (oldMinWidth !== this.bodyLayoutConfig.minWidth) {
      this._renderScrollable();
    }
    if (this.rendered) {
      this.htmlBody.invalidateLayoutTree();
    }
  }

  /**
   * Redraws the group box body by removing and rerendering every control.
   * This may be necessary if a field does not support a dynamic property change and therefore needs to be redrawn completely to reflect the change.
   */
  rerenderControls() {
    this._removeControls();
    this._renderControls();
    this.htmlBody.invalidateLayoutTree();
  }

  _removeControls() {
    this.controls.forEach(control => {
      control.remove();
    }, this);
  }

  _renderControls() {
    this.controls.forEach(function(control) {
      if (!control.rendered) {
        control.render(this.$body);
        // set each children layout data to logical grid data
        control.setLayoutData(new LogicalGridData(control));
      }
    }, this);
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

  _removeSubLabel() {
    if (!this.$subLabel) {
      return;
    }
    tooltips.uninstall(this.$subLabel);
    this.$subLabel.remove();
    this.$subLabel = null;
  }

  setSubLabel(subLabel) {
    this.setProperty('subLabel', subLabel);
  }

  _renderSubLabel() {
    this.$subLabel.setVisible(strings.hasText(this.subLabel));
    this.$subLabel.textOrNbsp(this.subLabel);
    this.$container.toggleClass('has-sub-label', this.$subLabel.isVisible());
    this.invalidateLayoutTree();
  }

  setScrollable(scrollable) {
    this.setProperty('scrollable', scrollable);
  }

  _renderScrollable() {
    this._uninstallScrollbars();

    // horizontal (x-axis) scrollbar is only installed when minWidth is > 0
    if (this.scrollable) {
      this._installScrollbars({
        axis: ((this.bodyLayoutConfig.minWidth > 0) ? 'both' : 'y')
      });
    } else if (this.bodyLayoutConfig.minWidth > 0) {
      this._installScrollbars({
        axis: 'x'
      });
    }
  }

  /**
   * @override
   */
  get$Scrollable() {
    return this.$body;
  }

  _onScroll() {
    super._onScroll();
    this._updateScrollShadow();
  }

  _updateScrollShadow() {
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

  setMainBox(mainBox) {
    this.setProperty('mainBox', mainBox);
  }

  _setMainBox(mainBox) {
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

  addLabel() {
    if (this.$label) {
      return;
    }
    this.$label = this.$title.appendDiv('label');
    tooltips.installForEllipsis(this.$label, {
      parent: this
    });
  }

  _renderLabel() {
    this.$label.textOrNbsp(this.label);
    if (this.rendered) {
      this._renderLabelVisible();
    }
  }

  addStatus() {
    super.addStatus();
    this._updateStatusPosition();
  }

  _renderStatusPosition() {
    this._updateStatusPosition();
  }

  _updateStatusPosition() {
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

  setNotification(notification) {
    this.setProperty('notification', notification);
  }

  _renderNotification() {
    if (!this.notification) {
      this.invalidateLayoutTree();
      return;
    }
    this.notification.render();
    this.notification.$container.insertBefore(this.$body);
    this.invalidateLayoutTree();
  }

  _prepareFields() {
    this.processButtons.forEach(this._unregisterButtonKeyStrokes.bind(this));

    this.controls = [];
    this.systemButtons = [];
    this.customButtons = [];
    this.processButtons = [];
    this.processMenus = [];

    let i, field;
    for (i = 0; i < this.fields.length; i++) {
      field = this.fields[i];
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
    this.processMenus = this.processButtons.map(function(button) {
      return scout.create('ButtonAdapterMenu',
        ButtonAdapterMenu.adaptButtonProperties(button, {
          parent: this,
          menubar: this.menuBar,
          button: button,
          // initially defaultMenu should only be set if defaultButton is set to true, false should not be mapped as the default defaultMenu = null setting
          // would be overridden if this default null setting is overridden MenuBar.prototype.updateDefaultMenu would not consider these entries anymore
          defaultMenu: button.defaultButton ? true : null
        }));
    }, this);
    this.registerKeyStrokes(this.processMenus);
  }

  _unregisterButtonKeyStrokes(button) {
    if (button.keyStrokes) {
      button.keyStrokes.forEach(function(keyStroke) {
        this.keyStrokeContext.unregisterKeyStroke(keyStroke);
      }, this);
    }
  }

  _registerButtonKeyStrokes(button) {
    if (button.keyStrokes) {
      button.keyStrokes.forEach(function(keyStroke) {
        this.keyStrokeContext.registerKeyStroke(keyStroke);
      }, this);
    }
  }

  setBorderVisible(borderVisible) {
    this.setProperty('borderVisible', borderVisible);
  }

  _renderBorderVisible() {
    let borderVisible = this.borderVisible;
    if (this.borderDecoration === GroupBox.BorderDecoration.AUTO) {
      borderVisible = this._computeBorderVisible(borderVisible);
    }

    this.$body.toggleClass('y-padding-invisible', !borderVisible);
    this.invalidateLayoutTree();
  }

  setBorderDecoration(borderDecoration) {
    this.setProperty('borderDecoration', borderDecoration);
  }

  // Don't include in renderProperties, it is not necessary to execute it initially because renderBorderVisible is executed already
  _renderBorderDecoration() {
    this._renderBorderVisible();
  }

  getContextMenuItems(onlyVisible = true) {
    if (this.menuBarVisible) {
      return [];
    }
    return super.getContextMenuItems(onlyVisible);
  }

  setMenuBarVisible(visible) {
    this.setProperty('menuBarVisible', visible);
  }

  _setMenuBarVisible(visible) {
    this._setProperty('menuBarVisible', visible);
    this._updateMenuBar();
  }

  _renderMenuBarVisible() {
    if (this.menuBarVisible) {
      this._renderMenuBar();
    } else {
      this.menuBar.remove();
    }
    this._updateMenus();
    this.invalidateLayoutTree();
  }

  _renderMenuBar() {
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

  setMenuBarPosition(menuBarPosition) {
    this.setProperty('menuBarPosition', menuBarPosition);
  }

  _renderMenuBarPosition() {
    let position = this.menuBarPosition;
    if (position === GroupBox.MenuBarPosition.AUTO) {
      position = GroupBox.MenuBarPosition.TOP;
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

  setMenuBarEllipsisPosition(menuBarEllipsisPosition) {
    this.setProperty('menuBarEllipsisPosition', menuBarEllipsisPosition);
    this.menuBar.setEllipsisPosition(menuBarEllipsisPosition);
  }

  _renderMenuBarEllipsisPosition() {
    this.menuBar.reorderMenus();
    if (this.rendered) {
      this.menuBar.remove();
      this._renderMenuBarVisible();
    }
  }

  _updateMenuBarStyle() {
    if (this.rendered) {
      this._renderMenuBarStyle();
    }
  }

  _renderMenuBarStyle() {
    let visible = this.menuBar.visible;
    let hasTitleMenuBar = this.menuBarPosition === GroupBox.MenuBarPosition.TITLE;
    this.$header.toggleClass('has-menubar', visible && hasTitleMenuBar);
    this.$container.toggleClass('menubar-position-top', visible && !hasTitleMenuBar && this.menuBar.position === MenuBar.Position.TOP);
    this.$container.toggleClass('menubar-position-bottom', visible && !hasTitleMenuBar && this.menuBar.position === MenuBar.Position.BOTTOM);
  }

  /**
   *
   * @returns {boolean} false if it is the mainbox. Or if the groupbox contains exactly one tablefield which has an invisible label
   */
  _computeBorderVisible(borderVisible) {
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

  setExpandable(expandable) {
    this.setProperty('expandable', expandable);
  }

  _renderExpandable() {
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

  setExpanded(expanded) {
    this.setProperty('expanded', expanded);
  }

  _renderExpanded() {
    this.$container.toggleClass('collapsed', !this.expanded);

    // Group boxes have set "useUiHeight=true" by default. When a group box is collapsed, it should not
    // stretched vertically (no "weight Y"). However, because "weightY" is -1 by default, a calculated value
    // is assigned (LogicalGridData._inheritWeightY()) that is based on the group boxes height. In collapsed
    // state, this height would be wrong. Therefore, we manually assign "weightY=0" to collapsed group boxes
    // to prevent them from beeing stretched.
    if (this.expanded) {
      // If group box was previously collapsed, restore original "weightY" griaData value
      if (this._collapsedWeightY !== undefined) {
        this.gridData.weightY = this._collapsedWeightY;
        delete this._collapsedWeightY;
      }
      // Update inner layout (e.g. menubar)
      this.invalidateLayout();
      this._renderControls();
    } else {
      // If group box has a weight different than 0, we set it to zero and back up the old value
      if (this.gridData.weightY !== 0) {
        this._collapsedWeightY = this.gridData.weightY;
        this.gridData.weightY = 0;
      }
    }

    this.invalidateLayoutTree();
  }

  setGridColumnCount(gridColumnCount) {
    this.setProperty('gridColumnCount', gridColumnCount);
    this.invalidateLogicalGrid();
  }

  /**
   * @override
   */
  invalidateLogicalGrid(invalidateLayout) {
    super.invalidateLogicalGrid(false);
    if (scout.nvl(invalidateLayout, true) && this.rendered) {
      this.htmlBody.invalidateLayoutTree();
    }
  }

  /**
   * @override
   */
  _setLogicalGrid(logicalGrid) {
    super._setLogicalGrid(logicalGrid);
    if (this.logicalGrid) {
      this.logicalGrid.setGridConfig(new GroupBoxGridConfig());
    }
  }

  /**
   * @override FormField.js
   */
  _renderLabelVisible(labelVisible) {
    this.$header.setVisible(this._computeTitleVisible(labelVisible));
    this._updateFieldStatus();
    if (this.menuBarPosition === GroupBox.MenuBarPosition.TITLE) {
      this.invalidateLayoutTree();
    }
  }

  _computeTitleVisible(labelVisible) {
    labelVisible = scout.nvl(labelVisible, this.labelVisible);
    return !!(labelVisible && this.label && !this.mainBox);
  }

  /**
   * @override FormField.js
   *
   * Only show the group box status if title is visible.
   */
  _computeStatusVisible() {
    return super._computeStatusVisible() && this._computeTitleVisible();
  }

  _setMenus(menus) {
    super._setMenus(menus);

    if (this.menuBar) {
      // updateMenuBar is required because menuBar is not created yet when synMenus is called initially
      this._updateMenuBar();
    }
  }

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

  _removeMenus() {
    // menubar takes care about removal
  }

  setStaticMenus(staticMenus) {
    this.setProperty('staticMenus', staticMenus);
    this._updateMenuBar();
  }

  _onControlClick(event) {
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

  setResponsive(responsive) {
    this.setProperty('responsive', responsive);
  }

  _setResponsive(responsive) {
    this._setProperty('responsive', responsive);

    if (!this.initialized) {
      return;
    }
    if (this.responsive) {
      ResponsiveManager.get().reset(this, true);
    } else {
      ResponsiveManager.get().reset(this, true);
      if (this.responsive === null) {
        let parent = this.findParent(parent => {
          return parent instanceof GroupBox && parent.responsive;
        });
        ResponsiveManager.get().reset(parent, true);
      }
    }
    this.invalidateLayoutTree();
  }

  clone(model, options) {
    let clone = super.clone(model);
    this._deepCloneProperties(clone, ['fields'], options);
    clone._prepareFields();
    return clone;
  }
}
