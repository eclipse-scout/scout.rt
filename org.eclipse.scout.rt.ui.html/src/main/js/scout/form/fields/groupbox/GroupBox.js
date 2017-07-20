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
scout.GroupBox = function() {
  scout.GroupBox.parent.call(this);
  this._addAdapterProperties(['fields', 'notification', 'staticMenus']);

  this.fields = [];
  this.menus = [];
  this.menuBarVisible = true;
  this.notification;
  this.borderDecoration = 'auto';
  this.borderVisible = true;
  this.mainBox = false;
  this.scrollable = false;
  this.expandable = false;
  this.expanded = true;
  this.gridColumnCount = 2;
  this.gridDataHints.useUiHeight = true;
  this.gridDataHints.w = scout.FormField.FULL_WIDTH;
  this.controls = [];
  this.systemButtons = [];
  this.customButtons = [];
  this.processButtons = [];
  this.processMenus = [];
  this.staticMenus = [];
  this.minWidthInPixel = 0;

  this.$body;
  this.$title;
};
scout.inherits(scout.GroupBox, scout.CompositeField);

scout.GroupBox.prototype._init = function(model) {
  scout.GroupBox.parent.prototype._init.call(this, model);
  this._setFields(this.fields);
  this.menuBar = scout.create('MenuBar', {
    parent: this,
    menuOrder: new scout.GroupBoxMenuItemsOrder()
  });
  if (this.mainBox) {
    this.menuBar.large();
  }
  this._updateMenuBar();
};

scout.GroupBox.prototype._setFields = function(fields) {
  this._setProperty('fields', fields);
  this._prepareFields();
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
    hAlign: scout.hAlign.RIGHT,
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

scout.GroupBox.prototype._render = function($parent) {
  var htmlBody,
    env = scout.HtmlEnvironment;

  this.addContainer($parent, this.mainBox ? 'root-group-box' : 'group-box', this._createLayout());

  this.$title = this.$container.appendDiv('group-box-title');
  this.addLabel();
  this.addStatus();
  this.$body = this.$container.appendDiv('group-box-body');
  htmlBody = scout.HtmlComponent.install(this.$body, this.session);
  htmlBody.setLayout(new scout.LogicalGridLayout(env.formColumnGap, env.formRowGap, this.minWidthInPixel));

  this._installScrollbars();
};

scout.GroupBox.prototype._remove = function() {
  if (this.scrollable) {
    scout.scrollbars.uninstall(this.$body, this.session);
  }
  scout.GroupBox.parent.prototype._remove.call(this);
};

scout.GroupBox.prototype._renderProperties = function() {
  this._renderExpanded(); // Need to be before renderVisible is executed, otherwise controls might be rendered if group box is invisible which breaks some widgets (e.g. Tree and Table)
  scout.GroupBox.parent.prototype._renderProperties.call(this);

  this._renderNotification();
  this._renderBorderVisible();
  this._renderExpandable();
  this._renderMenuBarVisible();
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

scout.GroupBox.prototype._createLayout = function() {
  return new scout.GroupBoxLayout(this);
};

scout.GroupBox.prototype._installScrollbars = function() {
  scout.scrollbars.uninstall(this.$body, this.session);

  // horizontal (x-axis) scrollbar is only installed when minWidthInPixel is > 0
  if (this.scrollable) {
    scout.scrollbars.install(this.$body, {
      parent: this,
      axis: ((this.minWidthInPixel > 0) ? 'both' : 'y')
    });
  } else if (this.minWidthInPixel > 0) {
    scout.scrollbars.install(this.$body, {
      parent: this,
      axis: 'x'
    });
  }
};

scout.GroupBox.prototype.addLabel = function() {
  if (this.$label) {
    return;
  }
  this.$label = this.$title.appendSpan('label');
};

scout.GroupBox.prototype._renderLabel = function() {
  this.$label.textOrNbsp(this.label);
};

scout.GroupBox.prototype._renderStatusPosition = function() {
  if (this.statusPosition === scout.FormField.STATUS_POSITION_TOP) {
    // move into title
    this.$status.appendTo(this.$title);
  } else {
    this.$status.appendTo(this.$container);
  }
  this.invalidateLayoutTree();
};

scout.GroupBox.prototype._renderNotification = function() {
  if (!this.notification) {
    return;
  }
  this.notification.render(this.$container);
  this.notification.$container.insertBefore(this.$body);
};

scout.GroupBox.prototype._prepareFields = function() {
  this.unregisterKeyStrokes(this.processButtons);

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
  this.processButtons.forEach(function(button) {
    var menu = scout.create('ButtonAdapterMenu',
      scout.ButtonAdapterMenu.adaptButtonProperties(button, {
        parent: this,
        button: button
      }));
    this.processMenus.push(menu);
  }, this);
  this.registerKeyStrokes(this.processMenus);
};

scout.GroupBox.prototype._registerButtonKeyStrokes = function(button) {
  if (button.keyStrokes) {
    button.keyStrokes.forEach(function(keyStroke) {
      this.keyStrokeContext.registerKeyStroke(keyStroke);
    }, this);
  }
};

/**
 * @override
 */
scout.GroupBox.prototype.getFields = function() {
  return this.fields;
};

scout.GroupBox.prototype._renderBorderVisible = function() {
  var borderVisible = this.borderVisible;
  if (this.borderDecoration === 'auto') {
    borderVisible = this._computeBorderVisible(borderVisible);
  }

  if (!borderVisible) {
    this.$body.addClass('y-padding-invisible');
  }
};

// Don't include in renderProperties, it is not necessary to execute it initially because renderBorderVisible is executed already
scout.GroupBox.prototype._renderBorderDecoration = function() {
  this._renderBorderVisible();
};

scout.GroupBox.prototype.setMenuBarVisible = function(visible) {
  this.setProperty('menuBarVisible', visible);
};

scout.GroupBox.prototype._renderMenuBarVisible = function() {
  if (this.menuBarVisible) {
    this._renderMenuBar();
  } else {
    this.menuBar.remove();
  }
  this.invalidateLayoutTree();
};

scout.GroupBox.prototype._renderMenuBar = function() {
  this.menuBar.render(this.$container);
  if (this.menuBar.position === 'top') {
    // move after title
    this.menuBar.$container.insertAfter(this.$title);
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

scout.GroupBox.prototype._renderExpandable = function() {
  var expandable = this.expandable;
  var $control = this.$title.children('.group-box-control');

  if (expandable) {
    if ($control.length === 0) {
      // Create control if necessary
      $control = this.$container.makeDiv('group-box-control')
        .on('click', this._onControlClick.bind(this))
        .prependTo(this.$title);
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

scout.GroupBox.prototype._renderExpanded = function() {
  this.$container.toggleClass('collapsed', !this.expanded);
  if (this.borderDecoration === 'line') {
    this.$container.toggleClass('with-line', !this.expanded);
  }

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

/**
 * @override FormField.js
 */
scout.GroupBox.prototype._renderLabelVisible = function(labelVisible) {
  this.$title.setVisible(this._computeTitleVisible(labelVisible));
  this._updateStatusVisible();
};

scout.GroupBox.prototype._computeTitleVisible = function(labelVisible) {
  labelVisible = scout.nvl(labelVisible, this.labelVisible);
  return !!(labelVisible && this.label && !this.mainBox);
};

/**
 * @override FormField.js
 */
scout.GroupBox.prototype._updateStatusVisible = function() {
  this._renderStatusVisible();
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
  }
  $.suppressEvent(event); // otherwise, the event would be triggered twice sometimes (by group-box-control and group-box-title)
};

scout.GroupBox.prototype.setExpanded = function(expanded) {
  this.setProperty('expanded', expanded);
};
