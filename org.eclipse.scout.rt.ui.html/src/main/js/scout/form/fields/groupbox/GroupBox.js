/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.GroupBox = function() {
  scout.GroupBox.parent.call(this);
  this._addWidgetProperties(['fields', 'notification', 'staticMenus']);
  this._addCloneProperties(['menuBarVisible', 'bodyLayoutConfig', 'borderDecoration', 'borderVisible', 'expandable', 'expanded', 'gridColumnCount', 'scrollable', 'subLabel']);

  this.fields = [];
  this.menus = [];
  this.menuBarVisible = true;
  this.menuBarPosition = scout.GroupBox.MenuBarPosition.AUTO;
  this.menuBarEllipsisPosition = scout.MenuBar.EllipsisPosition.RIGHT;
  this.notification = null;
  this.bodyLayoutConfig = null;
  this.borderDecoration = scout.GroupBox.BorderDecoration.AUTO;
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
  this.gridDataHints.w = scout.FormField.FULL_WIDTH;
  this.controls = [];
  this.systemButtons = [];
  this.customButtons = [];
  this.processButtons = [];
  this.processMenus = [];
  this.staticMenus = [];
  this.responsive = null;

  this.$body = null;
  this.$title = null;
  this.$subLabel = null;
};
scout.inherits(scout.GroupBox, scout.CompositeField);

scout.GroupBox.BorderDecoration = {
  AUTO: 'auto',
  EMPTY: 'empty',
  LINE: 'line'
};

scout.GroupBox.MenuBarPosition = {
  AUTO: 'auto',
  TOP: 'top',
  BOTTOM: 'bottom',
  TITLE: 'title'
};

scout.GroupBox.prototype._init = function(model) {
  scout.GroupBox.parent.prototype._init.call(this, model);
  this.resolveConsts([{
    property: 'menuBarPosition',
    constType: scout.GroupBox.MenuBarPosition
  }]);
  this._setBodyLayoutConfig(this.bodyLayoutConfig);
  this.menuBar = scout.create('MenuBar', {
    parent: this,
    menuOrder: new scout.GroupBoxMenuItemsOrder()
  });
  this._setFields(this.fields);
  this._setMainBox(this.mainBox);
  this._updateMenuBar();

  scout.responsiveManager.registerHandler(this, scout.create('GroupBoxResponsiveHandler', {
    widget: this
  }));

  this._setResponsive(this.responsive);
};

scout.GroupBox.prototype._destroy = function() {
  scout.responsiveManager.unregisterHandler(this);
  scout.GroupBox.parent.prototype._destroy.call(this);
};

/**
 * @override
 */
scout.GroupBox.prototype.getFields = function() {
  return this.fields;
};

scout.GroupBox.prototype.insertField = function(field, index) {
  var newFields = this.fields.slice();
  index = scout.nvl(index, this.fields.length);
  newFields.splice(index, 0, field);
  this.setFields(newFields);
};

scout.GroupBox.prototype.insertFieldBefore = function(field, sibling) {
  scout.assertParameter('sibling', sibling);
  var index = this.fields.indexOf(sibling);
  this.insertField(field, index);
};

scout.GroupBox.prototype.insertFieldAfter = function(field, sibling) {
  scout.assertParameter('sibling', sibling);
  var index = this.fields.indexOf(sibling) + 1;
  this.insertField(field, index);
};

scout.GroupBox.prototype.deleteField = function(field) {
  var newFields = this.fields.slice(),
    index = this.fields.indexOf(field);
  if (index < 0) {
    return;
  }
  newFields.splice(index, 1);
  this.setFields(newFields);
};

scout.GroupBox.prototype.setFields = function(fields) {
  this.setProperty('fields', fields);
};

scout.GroupBox.prototype._setFields = function(fields) {
  this._setProperty('fields', fields);
  this._prepareFields();
};

scout.GroupBox.prototype._renderFields = function(fields) {
  this._renderExpanded();
  this.invalidateLogicalGrid(true);
};

/**
 * @override
 */
scout.GroupBox.prototype._initKeyStrokeContext = function() {
  scout.GroupBox.parent.prototype._initKeyStrokeContext.call(this);
  this.keyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  this.keyStrokeContext.$bindTarget = this._keyStrokeBindTarget.bind(this);
};

/**
 * @override FormField.js
 */
scout.GroupBox.prototype._setKeyStrokes = function(keyStrokes) {
  keyStrokes = scout.arrays.ensure(keyStrokes);

  var groupBoxRenderingHints = {
    render: function() {
      return true;
    },
    offset: 0,
    hAlign: scout.HAlign.RIGHT,
    $drawingArea: function($drawingArea, event) {
      if (this.labelVisible) {
        return this.$title;
      } else {
        return this.$body;
      }
    }.bind(this)
  };

  keyStrokes
    .forEach(function(keyStroke) {
      keyStroke.actionKeyStroke.renderingHints = $.extend({}, keyStroke.actionKeyStroke.renderingHints, groupBoxRenderingHints);
    }, this);

  scout.GroupBox.parent.prototype._setKeyStrokes.call(this, keyStrokes);
};

/**
 * Returns a $container used as a bind target for the key-stroke context of the group-box.
 * By default this function returns the container of the form, or when group-box is has no
 * form as a parent the container of the group-box.
 */
scout.GroupBox.prototype._keyStrokeBindTarget = function() {
  var form = this.getForm();
  if (form) {
    // keystrokes on a group-box have form scope
    return form.$container;
  }
  return this.$container;
};

scout.GroupBox.prototype._render = function() {
  this.addContainer(this.$parent, this.mainBox ? 'root-group-box' : 'group-box');

  this.$title = this.$container.appendDiv('group-box-title');
  this.addLabel();
  this.addSubLabel();
  this.addStatus();
  this.$body = this.$container.appendDiv('group-box-body');
  this.htmlBody = scout.HtmlComponent.install(this.$body, this.session);
  this.htmlBody.setLayout(this._createBodyLayout());
};

scout.GroupBox.prototype._remove = function() {
  this._removeSubLabel();
  scout.GroupBox.parent.prototype._remove.call(this);
};

scout.GroupBox.prototype._renderProperties = function() {
  this._renderScrollable(); // Need to be before renderExpanded in order to have the scrollbars when the fields are rendered. The status tooltips require a scrollable parent to move when scrolling.
  this._renderExpanded(); // Need to be before renderVisible is executed, otherwise controls might be rendered if group box is invisible which breaks some widgets (e.g. Tree and Table)
  scout.GroupBox.parent.prototype._renderProperties.call(this);

  this._renderBodyLayoutConfig();
  this._renderNotification();
  this._renderBorderVisible();
  this._renderExpandable();
  this._renderMenuBarPosition();
  this._renderMenuBarEllipsisPosition();
  this._renderMenuBarVisible();
  this._renderSubLabel();
};

scout.GroupBox.prototype._createLayout = function() {
  return new scout.GroupBoxLayout(this);
};

scout.GroupBox.prototype._createBodyLayout = function() {
  return new scout.LogicalGridLayout(this, this.bodyLayoutConfig);
};

scout.GroupBox.prototype.setBodyLayoutConfig = function(bodyLayoutConfig) {
  this.setProperty('bodyLayoutConfig', bodyLayoutConfig);
};

scout.GroupBox.prototype._setBodyLayoutConfig = function(bodyLayoutConfig) {
  if (!bodyLayoutConfig) {
    bodyLayoutConfig = new scout.LogicalGridLayoutConfig();
  }
  this._setProperty('bodyLayoutConfig', scout.LogicalGridLayoutConfig.ensure(bodyLayoutConfig));
};

scout.GroupBox.prototype._renderBodyLayoutConfig = function() {
  var oldMinWidth = this.htmlBody.layout.minWidth;
  this.bodyLayoutConfig.applyToLayout(this.htmlBody.layout);
  if (oldMinWidth !== this.bodyLayoutConfig.minWidth) {
    this._renderScrollable();
  }
  if (this.rendered) {
    this.htmlBody.invalidateLayoutTree();
  }
};

/**
 * Redraws the group box body by removing and rerendering every control.
 * This may be necessary if a field does not support a dynamic property change and therefore needs to be redrawn completely to reflect the change.
 */
scout.GroupBox.prototype.rerenderControls = function() {
  this._removeControls();
  this._renderControls();
  this.htmlBody.invalidateLayoutTree();
};

scout.GroupBox.prototype._removeControls = function() {
  this.controls.forEach(function(control) {
    control.remove();
  }, this);
};

scout.GroupBox.prototype._renderControls = function() {
  this.controls.forEach(function(control) {
    if (!control.rendered) {
      control.render(this.$body);
      // set each children layout data to logical grid data
      control.setLayoutData(new scout.LogicalGridData(control));
    }
  }, this);
};

scout.GroupBox.prototype.addSubLabel = function() {
  if (this.$subLabel) {
    return;
  }
  this.$subLabel = this.$title.appendDiv('sub-label');
  scout.tooltips.installForEllipsis(this.$subLabel, {
    parent: this
  });
};

scout.GroupBox.prototype._removeSubLabel = function() {
  if (!this.$subLabel) {
    return;
  }
  scout.tooltips.uninstall(this.$subLabel);
  this.$subLabel.remove();
  this.$subLabel = null;
};

scout.GroupBox.prototype.setSubLabel = function(subLabel) {
  this.setProperty('subLabel', subLabel);
};

scout.GroupBox.prototype._renderSubLabel = function() {
  this.$subLabel.setVisible(scout.strings.hasText(this.subLabel));
  this.$subLabel.textOrNbsp(this.subLabel);
  this.$container.toggleClass('has-sub-label', this.$subLabel.isVisible());
  this.invalidateLayoutTree();
};

scout.GroupBox.prototype.setScrollable = function(scrollable) {
  this.setProperty('scrollable', scrollable);
};

scout.GroupBox.prototype._renderScrollable = function() {
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
};

/**
 * @override
 */
scout.GroupBox.prototype.get$Scrollable = function() {
  return this.$body;
};

scout.GroupBox.prototype.setMainBox = function(mainBox) {
  this.setProperty('mainBox', mainBox);
};

scout.GroupBox.prototype._setMainBox = function(mainBox) {
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
};

scout.GroupBox.prototype.addLabel = function() {
  if (this.$label) {
    return;
  }
  this.$label = this.$title.appendDiv('label');
  scout.tooltips.installForEllipsis(this.$label, {
    parent: this
  });
};

scout.GroupBox.prototype._renderLabel = function() {
  this.$label.textOrNbsp(this.label);
  if (this.rendered) {
    this._renderLabelVisible();
  }
};

scout.GroupBox.prototype.addStatus = function() {
  scout.GroupBox.parent.prototype.addStatus.call(this);
  this._updateStatusPosition();
};

scout.GroupBox.prototype._renderStatusPosition = function() {
  this._updateStatusPosition();
};

scout.GroupBox.prototype._updateStatusPosition = function() {
  if (!this.fieldStatus) {
    return;
  }
  if (this.statusPosition === scout.FormField.StatusPosition.TOP) {
    // move into title
    this.$status.appendTo(this.$title);
  } else {
    this.$status.appendTo(this.$container);
  }
  this.invalidateLayoutTree();
};

scout.GroupBox.prototype.setNotification = function(notification) {
  this.setProperty('notification', notification);
};

scout.GroupBox.prototype._renderNotification = function() {
  if (!this.notification) {
    this.invalidateLayoutTree();
    return;
  }
  this.notification.render();
  this.notification.$container.insertBefore(this.$body);
  this.invalidateLayoutTree();
};

scout.GroupBox.prototype._prepareFields = function() {
  this.processButtons.forEach(this._unregisterButtonKeyStrokes.bind(this));

  this.controls = [];
  this.systemButtons = [];
  this.customButtons = [];
  this.processButtons = [];
  this.processMenus = [];

  var i, field;
  for (i = 0; i < this.fields.length; i++) {
    field = this.fields[i];
    if (field instanceof scout.Button) {
      if (field.processButton) {
        this.processButtons.push(field);
        if (field.systemType !== scout.Button.SystemType.NONE) {
          this.systemButtons.push(field);
        } else {
          this.customButtons.push(field);
        }
      } else {
        this.controls.push(field);
        this._registerButtonKeyStrokes(field);
      }
    } else if (field instanceof scout.TabBox) {
      this.controls.push(field);
      for (var k = 0; k < field.tabItems.length; k++) {
        if (field.tabItems[k].selectionKeystroke) {
          this.keyStrokeContext.registerKeyStroke(new scout.TabItemKeyStroke(field.tabItems[k].selectionKeystroke, field.tabItems[k]));
        }
      }
    } else {
      this.controls.push(field);
    }
  }

  // Create menu for each process button
  this.processMenus = this.processButtons.map(function(button) {
    return scout.create('ButtonAdapterMenu',
      scout.ButtonAdapterMenu.adaptButtonProperties(button, {
        parent: this,
        menubar: this.menuBar,
        button: button,
        // initially defaultMenu should only be set if defaultButton is set to true, false should not be mapped as the default defaultMenu = null setting
        // would be overridden if this default null setting is overridden scout.MenuBar.prototype.updateDefaultMenu would not consider these entries anymore
        defaultMenu: button.defaultButton ? true : null
      }));
  }, this);
  this.registerKeyStrokes(this.processMenus);
};

scout.GroupBox.prototype._unregisterButtonKeyStrokes = function(button) {
  if (button.keyStrokes) {
    button.keyStrokes.forEach(function(keyStroke) {
      this.keyStrokeContext.unregisterKeyStroke(keyStroke);
    }, this);
  }
};

scout.GroupBox.prototype._registerButtonKeyStrokes = function(button) {
  if (button.keyStrokes) {
    button.keyStrokes.forEach(function(keyStroke) {
      this.keyStrokeContext.registerKeyStroke(keyStroke);
    }, this);
  }
};

scout.GroupBox.prototype.setBorderVisible = function(borderVisible) {
  this.setProperty('borderVisible', borderVisible);
};

scout.GroupBox.prototype._renderBorderVisible = function() {
  var borderVisible = this.borderVisible;
  if (this.borderDecoration === scout.GroupBox.BorderDecoration.AUTO) {
    borderVisible = this._computeBorderVisible(borderVisible);
  }

  this.$body.toggleClass('y-padding-invisible', !borderVisible);
  this.invalidateLayoutTree();
};

scout.GroupBox.prototype.setBorderDecoration = function(borderDecoration) {
  this.setProperty('borderDecoration', borderDecoration);
};

// Don't include in renderProperties, it is not necessary to execute it initially because renderBorderVisible is executed already
scout.GroupBox.prototype._renderBorderDecoration = function() {
  this._renderBorderVisible();
};

scout.GroupBox.prototype._getCurrentMenus = function() {
  if (this.menuBarVisible) {
    return [];
  }
  return scout.GroupBox.parent.prototype._getCurrentMenus.call(this);
};

scout.GroupBox.prototype.setMenuBarVisible = function(visible) {
  this.setProperty('menuBarVisible', visible);
};

scout.GroupBox.prototype._setMenuBarVisible = function(visible) {
  this._setProperty('menuBarVisible', visible);
  this._updateMenuBar();
};

scout.GroupBox.prototype._renderMenuBarVisible = function() {
  if (this.menuBarVisible) {
    this._renderMenuBar();
  } else {
    this.menuBar.remove();
  }
  this._updateMenus();
  this.invalidateLayoutTree();
};

scout.GroupBox.prototype._renderMenuBar = function() {
  this.menuBar.render();
  if (this.menuBarPosition === scout.GroupBox.MenuBarPosition.TITLE) {
    // move right of title
    this.menuBar.$container.insertAfter(this.$subLabel);
  } else if (this.menuBar.position === scout.MenuBar.Position.TOP) {
    // move below title
    this.menuBar.$container.insertAfter(this.$title);
  }
};

scout.GroupBox.prototype.setMenuBarPosition = function(menuBarPosition) {
  this.setProperty('menuBarPosition', menuBarPosition);
};

scout.GroupBox.prototype._renderMenuBarPosition = function() {
  var position = this.menuBarPosition;
  if (position === scout.GroupBox.MenuBarPosition.AUTO) {
    position = scout.GroupBox.MenuBarPosition.TOP;
  }

  var hasMenubar = position === scout.GroupBox.MenuBarPosition.TITLE;
  this.$title.toggleClass('has-menubar', hasMenubar);

  if (position === scout.GroupBox.MenuBarPosition.BOTTOM) {
    this.menuBar.setPosition(scout.MenuBar.Position.BOTTOM);
  } else { // top + title
    this.menuBar.setPosition(scout.MenuBar.Position.TOP);
  }

  if (this.rendered) {
    this.menuBar.remove();
    this._renderMenuBarVisible();
  }
};

scout.GroupBox.prototype.setMenuBarEllipsisPosition = function(menuBarEllipsisPosition) {
  this.setProperty('menuBarEllipsisPosition', menuBarEllipsisPosition);
  this.menuBar.setEllipsisPosition(menuBarEllipsisPosition);
};

scout.GroupBox.prototype._renderMenuBarEllipsisPosition = function() {
  this.menuBar.reorderMenus();
  if (this.rendered) {
    this.menuBar.remove();
    this._renderMenuBarVisible();
  }
};

/**
 *
 * @returns false if it is the mainbox. Or if the groupbox contains exactly one tablefield which has an invisible label
 */
scout.GroupBox.prototype._computeBorderVisible = function(borderVisible) {
  if (this.mainBox) {
    borderVisible = false;
  } else if (this.parent instanceof scout.GroupBox &&
    this.parent.parent instanceof scout.Form &&
    this.parent.parent.parent instanceof scout.WrappedFormField &&
    this.parent.parent.parent.parent instanceof scout.SplitBox &&
    this.parent.getFields().length === 1) {
    // Special case for wizard: wrapped form in split box with a single group box
    borderVisible = false;
  }
  return borderVisible;
};

scout.GroupBox.prototype.setExpandable = function(expandable) {
  this.setProperty('expandable', expandable);
};

scout.GroupBox.prototype._renderExpandable = function() {
  var expandable = this.expandable;
  var $control = this.$title.children('.group-box-control');

  if (expandable) {
    if ($control.length === 0) {
      // Create control if necessary
      this.$container.makeDiv('group-box-control')
        .on('click', this._onControlClick.bind(this))
        .insertAfter(this.$label);
    }
    this.$title
      .addClass('expandable')
      .on('click.group-box-control', this._onControlClick.bind(this));
  } else {
    $control.remove();
    this.$title
      .removeClass('expandable')
      .off('.group-box-control');
  }
};

scout.GroupBox.prototype.setExpanded = function(expanded) {
  this.setProperty('expanded', expanded);
};

scout.GroupBox.prototype._renderExpanded = function() {
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
};

scout.GroupBox.prototype.setGridColumnCount = function(gridColumnCount) {
  this.setProperty('gridColumnCount', gridColumnCount);
  this.invalidateLogicalGrid();
};

/**
 * @override
 */
scout.GroupBox.prototype.invalidateLogicalGrid = function(invalidateLayout) {
  scout.GroupBox.parent.prototype.invalidateLogicalGrid.call(this, false);
  if (scout.nvl(invalidateLayout, true) && this.rendered) {
    this.htmlBody.invalidateLayoutTree();
  }
};

/**
 * @override
 */
scout.GroupBox.prototype._setLogicalGrid = function(logicalGrid) {
  scout.GroupBox.parent.prototype._setLogicalGrid.call(this, logicalGrid);
  if (this.logicalGrid) {
    this.logicalGrid.setGridConfig(new scout.GroupBoxGridConfig());
  }
};

/**
 * @override FormField.js
 */
scout.GroupBox.prototype._renderLabelVisible = function(labelVisible) {
  this.$title.setVisible(this._computeTitleVisible(labelVisible));
  this._updateFieldStatus();
  if (this.menuBarPosition === scout.GroupBox.MenuBarPosition.TITLE) {
    this.revalidateLayout();
  }
};

scout.GroupBox.prototype._computeTitleVisible = function(labelVisible) {
  labelVisible = scout.nvl(labelVisible, this.labelVisible);
  return !!(labelVisible && this.label && !this.mainBox);
};

/**
 * @override FormField.js
 *
 * Only show the group box status if title is visible.
 */
scout.GroupBox.prototype._computeStatusVisible = function() {
  return scout.GroupBox.parent.prototype._computeStatusVisible.call(this) && this._computeTitleVisible();
};

scout.GroupBox.prototype._setMenus = function(menus) {
  scout.GroupBox.parent.prototype._setMenus.call(this, menus);

  if (this.menuBar) {
    // updateMenuBar is required because menuBar is not created yet when synMenus is called initially
    this._updateMenuBar();
  }
};

scout.GroupBox.prototype._updateMenuBar = function() {
  if (!this.menuBarVisible) {
    // Do not update menuBar while it is invisible, the menus may now be managed by another widget.
    // -> this makes sure the parent is not accidentally set to the group box, the other widget should remain responsible
    return;
  }
  var menus = this.staticMenus
    .concat(this.processMenus)
    .concat(this.menus);

  this.menuBar.setMenuItems(menus);
};

scout.GroupBox.prototype._removeMenus = function() {
  // menubar takes care about removal
};

scout.GroupBox.prototype.setStaticMenus = function(staticMenus) {
  this.setProperty('staticMenus', staticMenus);
  this._updateMenuBar();
};

scout.GroupBox.prototype._onControlClick = function(event) {
  if (this.expandable) {
    this.setExpanded(!this.expanded);
    // Prevent flickering when expanding the group box
    this.validateLayoutTree();
  }
  $.suppressEvent(event); // otherwise, the event would be triggered twice sometimes (by group-box-control and group-box-title)
};

scout.GroupBox.prototype.setResponsive = function(responsive) {
  this.setProperty('responsive', responsive);
};

scout.GroupBox.prototype._setResponsive = function(responsive) {
  this._setProperty('responsive', responsive);

  if (!this.initialized) {
    return;
  }
  if (this.responsive) {
    scout.responsiveManager.reset(this, true);
  } else {
    scout.responsiveManager.reset(this, true);
    if (this.responsive === null) {
      var parent = this.findParent(function(parent) {
        return parent instanceof scout.GroupBox && parent.responsive;
      });
      scout.responsiveManager.reset(parent, true);
    }
  }
  this.invalidateLayoutTree();
};

scout.GroupBox.prototype.clone = function(model, options) {
  var clone = scout.GroupBox.parent.prototype.clone.call(this, model);
  this._deepCloneProperties(clone, ['fields'], options);
  clone._prepareFields();
  return clone;
};
