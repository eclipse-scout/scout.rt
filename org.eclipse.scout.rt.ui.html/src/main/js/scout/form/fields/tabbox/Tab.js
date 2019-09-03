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
scout.Tab = function() {
  scout.Tab.parent.call(this);

  this.label = null;
  this.subLabel = null;
  this.selected = false;
  this.overflown = false;
  this._preventTabSelection = false;

  this.$label = null;
  this.$subLabel = null;
  this._tabPropertyChangeHandler = this._onTabPropertyChange.bind(this);
  this._statusMouseDownHandler = this._onStatusMouseDown.bind(this);
  this._desktopPropertyChangeHandler = this._onDesktopPropertyChange.bind(this);
};
scout.inherits(scout.Tab, scout.Widget);

scout.Tab.prototype._init = function(options) {
  scout.Tab.parent.prototype._init.call(this, options);
  this.visible = this.tabItem.visible;
  this.label = this.tabItem.label;
  this.subLabel = this.tabItem.subLabel;
  this.cssClass = this.tabItem.cssClass;
  this.marked = this.tabItem.marked;

  this.fieldStatus = scout.create('FieldStatus', {
    parent: this,
    visible: false
  });
  this.fieldStatus.on('statusMouseDown', this._statusMouseDownHandler);

  this.tabItem.on('propertyChange', this._tabPropertyChangeHandler);
};

scout.Tab.prototype._destroy = function() {
  scout.Tab.parent.prototype._destroy.call(this);
  this.tabItem.off('propertyChange', this._tabPropertyChangeHandler);
  this.fieldStatus.off('statusMouseDown', this._statusMouseDownHandler);
};

scout.Tab.prototype._render = function() {
  this.$container = this.$parent.appendDiv('tab-item');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.$title = this.$container.appendDiv('title');
  this.$label = this.$title.appendDiv('label');
  scout.tooltips.installForEllipsis(this.$label, {
    parent: this
  });

  this.$subLabel = this.$title.appendDiv('sub-label');
  scout.tooltips.installForEllipsis(this.$subLabel, {
    parent: this
  });

  this.fieldStatus.render();
  this.fieldStatus.$container.cssWidth(scout.htmlEnvironment.fieldStatusWidth);

  this.$container.on('mousedown', this._onTabMouseDown.bind(this));
  this.session.desktop.on('propertyChange', this._desktopPropertyChangeHandler);
};

scout.Tab.prototype._renderProperties = function() {
  scout.Tab.parent.prototype._renderProperties.call(this);
  this._renderVisible();
  this._renderLabel();
  this._renderSubLabel();
  this._renderTabbable();
  this._renderSelected();
  this._renderMarked();
  this._renderOverflown();
  this._renderTooltipText();
  this._renderErrorStatus();
};


scout.Tab.prototype._renderVisible = function() {
  scout.Tab.parent.prototype._renderVisible.call(this);
  this._updateStatus();
};

scout.Tab.prototype.setLabel = function(label) {
  this.setProperty('label', label);
};

scout.Tab.prototype._renderLabel = function(label) {
  this.$label.textOrNbsp(this.label);
  this.invalidateLayoutTree();
};

scout.Tab.prototype.setSubLabel = function(subLabel) {
  this.setProperty('subLabel', subLabel);
};

scout.Tab.prototype._renderSubLabel = function() {
  this.$subLabel.textOrNbsp(this.subLabel);
  this.invalidateLayoutTree();
};

scout.Tab.prototype.setTooltipText = function(tooltipText) {
  this.setProperty('tooltipText', tooltipText);
};
scout.Tab.prototype._renderTooltipText = function() {
  this.$container.toggleClass('has-tooltip', scout.strings.hasText(this.tooltipText));
  this._updateStatus();
};

scout.Tab.prototype.setErrorStatus = function(errorStatus) {
  this.setProperty('errorStatus', errorStatus);
};

scout.Tab.prototype._renderErrorStatus = function() {
  var hasStatus = !!this.errorStatus,
    statusClass = hasStatus ? 'has-' + this.errorStatus.cssClass() : '';
  this._updateErrorStatusClasses(statusClass, hasStatus);
  this._updateStatus();
};

scout.Tab.prototype._updateErrorStatusClasses = function(statusClass, hasStatus) {
  this.$container.removeClass(scout.FormField.SEVERITY_CSS_CLASSES);
  this.$container.addClass(statusClass, hasStatus);
};

scout.Tab.prototype._updateStatus = function() {
  var visible = this._computeVisible(),
    status = null,
    autoRemove = true,
    initialShow = false;
  this.fieldStatus.setVisible(visible);
  if (!visible) {
    return;
  }
  if (this.errorStatus) {
    status = this.errorStatus;
    autoRemove = !status.isError();
    initialShow = true;
  } else {
    status = scout.create('Status', {
      message: this.tooltipText,
      severity: scout.Status.Severity.OK
    });
  }
  this.fieldStatus.update(status, null, autoRemove, initialShow);
};

scout.Tab.prototype._computeVisible = function() {
  return this.visible && !this.overflown && (this.errorStatus || scout.strings.hasText(this.tooltipText));
};

scout.Tab.prototype.setTabbable = function(tabbable) {
  this.setProperty('tabbable', tabbable);
};

scout.Tab.prototype._renderTabbable = function() {
  this.$container.setTabbable(this.tabbable && !scout.device.supportsTouch());
};

scout.Tab.prototype.select = function() {
  this.setSelected(true);
};

scout.Tab.prototype.setSelected = function(selected) {
  this.setProperty('selected', selected);
};

scout.Tab.prototype._renderSelected = function() {
  this.$container.select(this.selected);
  this.$container.setTabbable(this.selected && !scout.device.supportsTouch());
};

scout.Tab.prototype.setMarked = function(marked) {
  this.setProperty('marked', marked);
};

scout.Tab.prototype._renderMarked = function(marked) {
  this.$container.toggleClass('marked', this.marked);
};

scout.Tab.prototype.setOverflown = function(overflown) {
  this.setProperty('overflown', overflown);
};

scout.Tab.prototype._renderOverflown = function() {
  this.$container.toggleClass('overflown', this.overflown);
  this._updateStatus();
};

scout.Tab.prototype._remove = function() {
  this.session.desktop.off('propertyChange', this._desktopPropertyChangeHandler);
  scout.Tab.parent.prototype._remove.call(this);
};

scout.Tab.prototype._onDesktopPropertyChange = function(event) {
  // switching from or to the dense mode requires clearing of the tab's htmlComponent prefSize cache.
  if (event.propertyName === 'dense') {
    this.invalidateLayout();
  }
};

scout.Tab.prototype._onTabMouseDown = function(event) {
  if (this._preventTabSelection) {
    this._preventTabSelection = false;
    return;
  }

  // ensure to focus the selected tab before selecting the new tab.
  // The selection of a tab will remove the content of the previous selected tab.
  // Problem: If the previous is the focus owner the focus will be transfered to body what ends in a scroll top.
  this.setTabbable(true);
  this.$container.focus();

  this.select();

  // When the tab is clicked the user wants to execute the action and not see the tooltip
  if (this.$label) {
    scout.tooltips.cancel(this.$label);
    scout.tooltips.close(this.$label);
  }
  if (this.$subLabel) {
    scout.tooltips.cancel(this.$subLabel);
    scout.tooltips.close(this.$subLabel);
  }
};

scout.Tab.prototype._onStatusMouseDown = function(event) {
  // Prevent switching tabs when status gets clicked
  // Don't use event.preventDefault, otherwise other mouse listener (like tooltip mouse down) will not be executed as well
  this._preventTabSelection = true;
  // Prevent focusing the tab
  event.preventDefault();
};

scout.Tab.prototype._onTabPropertyChange = function(event) {
  if (event.propertyName === 'visible') {
    this.setVisible(event.newValue);
  } else if (event.propertyName === 'label') {
    this.setLabel(this.tabItem.label);
  } else if (event.propertyName === 'subLabel') {
    this.setSubLabel(this.tabItem.subLabel);
  } else if (event.propertyName === 'cssClass') {
    this.setCssClass(this.tabItem.cssClass);
  } else if (event.propertyName === 'marked') {
    this.setMarked(this.tabItem.marked);
  } else if (event.propertyName === 'errorStatus') {
    this.setErrorStatus(event.newValue);
  } else if (event.propertyName === 'tooltipText') {
    this.setTooltipText(event.newValue);
  }
};
