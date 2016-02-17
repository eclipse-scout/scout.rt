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
  this.fields = [];
  this.menus = [];
  this.staticMenus = [];
  this._addAdapterProperties(['fields', 'menus']);
  this.$body;
  this._$groupBoxTitle;

  this.controls = [];
  this.systemButtons = [];
  this.customButtons = [];
  this.processButtons = [];
};
scout.inherits(scout.GroupBox, scout.CompositeField);

scout.GroupBox.prototype._init = function(model) {
  scout.GroupBox.parent.prototype._init.call(this, model);
  this.menuBar = scout.create('MenuBar', {
    parent: this,
    menuOrder: new scout.GroupBoxMenuItemsOrder()
  });
  if (this.mainBox) {
    this.menuBar.large();
  }
};

/**
 * @override ModelAdapter.js
 */
scout.GroupBox.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.GroupBox.parent.prototype._initKeyStrokeContext.call(this, this.keyStrokeContext);
  this.keyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  this.keyStrokeContext.$bindTarget = this._keyStrokeBindTarget.bind(this);
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
  var htmlBody, i,
    env = scout.HtmlEnvironment;

  this.addContainer($parent, this.mainBox ? 'root-group-box' : 'group-box', this._createLayout());
  if (this.mainBox) {
    this.htmlComp.layoutData = null;
  }

  this._$groupBoxTitle = this.$container.appendDiv('group-box-title');
  this.addLabel();
  this.addStatus();
  if (this.menuBar.position === 'top') {
    this.menuBar.render(this.$container);
    // move after title
    this.menuBar.$container.appendTo(this.$container);
  }
  this.$body = this.$container.appendDiv('group-box-body');
  htmlBody = new scout.HtmlComponent(this.$body, this.session);
  htmlBody.setLayout(new scout.LogicalGridLayout(env.formColumnGap, env.formRowGap));
  if (this.scrollable) {
    scout.scrollbars.install(this.$body, {
      parent: this,
      axis: 'y'
    });
  }
  this._prepareFields();
  this.controls.forEach(function(control) {
    control.render(this.$body);
  }, this);
  // FIXME awe: andere lösung finden für das hier
  // only render when 2nd argument is undefined or matches this.position
  //  if (whenPosition !== undefined && this.position !== whenPosition) {
  //    return;
  //  }
  if (this.menuBar.position === 'bottom') {
    this.menuBar.render(this.$container);
  }
};

scout.GroupBox.prototype._renderProperties = function() {
  scout.GroupBox.parent.prototype._renderProperties.call(this);

  this._renderBorderVisible(this.borderVisible);
  this._renderExpandable(this.expandable);
  this._renderExpanded(this.expanded);
};

scout.GroupBox.prototype._createLayout = function() {
  return new scout.GroupBoxLayout(this);
};

scout.GroupBox.prototype.addLabel = function() {
  if (this.$label) {
    return;
  }
  this.$label = this._$groupBoxTitle.appendSpan();
};

scout.GroupBox.prototype._renderLabel = function() {
  this.$label.textOrNbsp(this.label);
};

scout.GroupBox.prototype._remove = function() {
  scout.GroupBox.parent.prototype._remove.call(this);
  if (this.menuBar) {
    this.menuBar.remove();
  }
  if (this.scrollable) {
    scout.scrollbars.uninstall(this.$body);
  }
};

scout.GroupBox.prototype._prepareFields = function() {
  this.controls = [];
  this.systemButtons = [];
  this.customButtons = [];
  this.processButtons = [];

  var i, field, res;
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
        var tabMnemonic = this._getMnemonic(field.tabItems[k]);
        if (tabMnemonic) {
          this.keyStrokeContext.registerKeyStroke(new scout.TabItemMnemonicKeyStroke(tabMnemonic, field.tabItems[k]));
        }
      }
    } else {
      this.controls.push(field);
    }
  }
};

scout.GroupBox.prototype._registerButtonKeyStrokes = function(button) {
  var mnemonic = this._getMnemonic(button);
  if (mnemonic) {
    this.keyStrokeContext.registerKeyStroke(new scout.ButtonMnemonicKeyStroke(mnemonic, button));
  }
  if (button.keyStrokes) {
    button.keyStrokes.forEach(function(keyStroke) {
      this.keyStrokeContext.registerKeyStroke(keyStroke);
    }, this);
  }
};

scout.GroupBox.prototype._getMnemonic = function(field) {
  return scout.strings.getMnemonic(field.label);
};

/**
 * @override
 */
scout.GroupBox.prototype.getFields = function() {
  return this.controls;
};

scout.GroupBox.prototype._renderBorderVisible = function(borderVisible) {
  if (this.borderDecoration === 'auto') {
    borderVisible = this._computeBorderVisible(borderVisible);
  }

  if (!borderVisible) {
    this.$body.addClass('y-padding-invisible');
  }
};

//Don't include in renderProperties, it is not necessary to execute it initially because renderBorderVisible is executed already
scout.GroupBox.prototype._renderBorderDecoration = function() {
  this._renderBorderVisible(this.borderVisible);
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

scout.GroupBox.prototype._renderExpandable = function(expandable) {
  var $control = this._$groupBoxTitle.children('.group-box-control');

  if (expandable) {
    if ($control.length === 0) {
      // Create control if necessary
      $control = this.$container.makeDiv('group-box-control')
        .on('click', this._onGroupBoxControlClick.bind(this))
        .prependTo(this._$groupBoxTitle);
    }
    this._$groupBoxTitle
      .addClass('expandable')
      .on('click.group-box-control', this._onGroupBoxControlClick.bind(this));
  } else {
    $control.remove();
    this._$groupBoxTitle
      .removeClass('expandable')
      .off('.group-box-control');
  }
};

scout.GroupBox.prototype._renderExpanded = function(expanded) {
  this.$container.toggleClass('collapsed', !expanded);
  if (this.borderDecoration === 'line') {
    this.$container.toggleClass('with-line', !expanded);
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
 * @override
 */
scout.GroupBox.prototype._renderLabelVisible = function(visible) {
  this._$groupBoxTitle.setVisible(visible && this.label && !this.mainBox);
};

scout.GroupBox.prototype._renderMenus = function() {
  var menu,
    menus = this.menus,
    menuItems = this.staticMenus.concat(menus);

  // create a menu-adapter for each process button
  this.processButtons.forEach(function(button) {
    menu = scout.create('ButtonAdapterMenu',
      scout.ButtonAdapterMenu.adaptButtonProperties(button, {
        parent: this,
        button: button
      }));
    menuItems.push(menu);
  }, this);

  // register keystrokes on root group-box
  menuItems.forEach(function(menuItem) {
    this.keyStrokeContext.registerKeyStroke(menuItem);
    this._registerButtonKeyStrokes(menuItem);
  }, this);

  this.menuBar.updateItems(menuItems);
};

scout.GroupBox.prototype._removeMenus = function(menus) {
  menus.forEach(function(menu) {
    menu.remove();
  });
};

scout.GroupBox.prototype.setStaticMenus = function(staticMenus) {
  this.staticMenus = staticMenus;
  if (this.rendered) {
    this._renderMenus();
  }
};

scout.GroupBox.prototype._onGroupBoxControlClick = function(event) {
  if (this.expandable) {
    this.setGroupBoxExpanded(!this.expanded);
  }
  $.suppressEvent(event); // otherwise, the event would be triggered twice sometimes (by group-box-control and group-box-title)
};

scout.GroupBox.prototype.setGroupBoxExpanded = function(expanded) {
  if (this.expanded !== expanded) {
    this.expanded = expanded;
    this._send('expanded', {
      expanded: expanded
    });
  }
  if (this.rendered) {
    this._renderExpanded(expanded);
  }
};
