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
scout.Accordion = function() {
  scout.Accordion.parent.call(this);
  this.comparator = null;
  this.collapseStyle = null;
  this.exclusiveExpand = true;
  this.groups = [];
  this.scrollable = true;

  this.$container = null;
  this.htmlComp = null;
  this._addWidgetProperties(['groups']);
  this._groupPropertyChangeHandler = this._onGroupPropertyChange.bind(this);
};
scout.inherits(scout.Accordion, scout.Widget);

scout.Accordion.prototype._init = function(model) {
  scout.Accordion.parent.prototype._init.call(this, model);
  this._initGroups(this.groups);
  this._setExclusiveExpand(this.exclusiveExpand);
};

/**
 * @override
 */
scout.Accordion.prototype._createLoadingSupport = function() {
  return new scout.LoadingSupport({
    widget: this
  });
};

scout.Accordion.prototype._render = function() {
  this.$container = this.$parent.appendDiv('accordion');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(this._createLayout());
};

scout.Accordion.prototype._createLayout = function() {
  return new scout.RowLayout();
};

scout.Accordion.prototype._renderProperties = function() {
  scout.Accordion.parent.prototype._renderProperties.call(this);
  this._renderScrollable();
  this._renderGroups();
};

scout.Accordion.prototype.insertGroup = function(group) {
  this.insertGroups([group]);
};

scout.Accordion.prototype.insertGroups = function(groupsToInsert) {
  groupsToInsert = scout.arrays.ensure(groupsToInsert);
  this.setGroups(this.groups.concat(groupsToInsert));
};

scout.Accordion.prototype.deleteGroup = function(group) {
  this.deleteGroups([group]);
};

scout.Accordion.prototype.deleteGroups = function(groupsToDelete) {
  groupsToDelete = scout.arrays.ensure(groupsToDelete);
  var groups = this.groups.slice();
  scout.arrays.removeAll(groups, groupsToDelete);
  this.setGroups(groups);
};

scout.Accordion.prototype.deleteAllGroups = function() {
  this.setGroups([]);
};

scout.Accordion.prototype._initGroups = function(groups) {
  this.groups.forEach(function(group) {
    this._initGroup(group);
  }, this);
};

scout.Accordion.prototype.setGroups = function(groups) {
  groups = scout.arrays.ensure(groups);
  if (scout.objects.equals(this.groups, groups)) {
    return;
  }

  // Ensure given groups are real groups (of type scout.Group)
  groups = this._createChildren(groups);

  // Only delete those which are not in the new array
  // Only insert those which are not already there
  var groupsToDelete = scout.arrays.diff(this.groups, groups);
  var groupsToInsert = scout.arrays.diff(groups, this.groups);
  this._deleteGroups(groupsToDelete);
  this._insertGroups(groupsToInsert);
  this._sort(groups);
  this._updateGroupOrder(groups);
  this._setProperty('groups', groups);

  if (groupsToInsert.length > 0) {
    this._updateExclusiveExpand();
  }
  if (this.rendered) {
    this._updateFirstLastMarker();
    this.invalidateLayoutTree();
  }
};

scout.Accordion.prototype._insertGroups = function(groups) {
  groups.forEach(function(group) {
    this._insertGroup(group);
  }, this);
};

scout.Accordion.prototype._insertGroup = function(group) {
  this._initGroup(group);
  if (this.rendered) {
    this._renderGroup(group);
  }
};

scout.Accordion.prototype._initGroup = function(group) {
  group.setParent(this);
  group.on('propertyChange', this._groupPropertyChangeHandler);

  // Copy properties from accordion to new group. If the properties are not set yet, copy them from the group to the accordion
  // This gives the possibility to either define the properties on the accordion or on the group initially
  if (this.collapseStyle !== null) {
    group.setCollapseStyle(this.collapseStyle);
  }
  this.setProperty('collapseStyle', group.collapseStyle);
};

scout.Accordion.prototype._renderGroup = function(group) {
  group.render();
};

scout.Accordion.prototype._deleteGroups = function(groups) {
  groups.forEach(function(group) {
    this._deleteGroup(group);
  }, this);
};

scout.Accordion.prototype._deleteGroup = function(group) {
  group.off('propertyChange', this._groupPropertyChangeHandler);
  if (group.owner === this) {
    group.destroy();
  } else if (this.rendered) {
    group.remove();
  }
};

scout.Accordion.prototype._renderGroups = function() {
  this.groups.forEach(function(group) {
    this._renderGroup(group);
  }, this);
  this._updateFirstLastMarker();
  this.invalidateLayoutTree();
};

scout.Accordion.prototype.setComparator = function(comparator) {
  if (this.comparator === comparator) {
    return;
  }
  this.comparator = comparator;
};

scout.Accordion.prototype.sort = function() {
  var groups = this.groups.slice();
  this._sort(groups);
  this._updateGroupOrder(groups);
  this._setProperty('groups', groups);
};

scout.Accordion.prototype._sort = function(groups) {
  if (this.comparator === null) {
    return;
  }
  groups.sort(this.comparator);
};

scout.Accordion.prototype._updateGroupOrder = function(groups) {
  if (!this.rendered) {
    return;
  }
  // Loop through the the groups and move every html element to the end of the container
  // Only move if the order is different to the old order
  var different = false;
  groups.forEach(function(group, i) {
    if (this.groups[i] !== group || different) {
      // Start ordering as soon as the order of the array starts to differ
      different = true;
      group.$container.appendTo(this.$container);
    }
  }, this);
};

scout.Accordion.prototype._updateFirstLastMarker = function() {
  scout.widgets.updateFirstLastMarker(this.groups);
};

scout.Accordion.prototype.setScrollable = function(scrollable) {
  this.setProperty('scrollable', scrollable);
};

scout.Accordion.prototype._renderScrollable = function() {
  if (this.scrollable) {
    this._installScrollbars({
      axis: 'y'
    });
  } else {
    this._uninstallScrollbars();
  }
  this.$container.toggleClass('scrollable', this.scrollable);
  this.invalidateLayoutTree();
};

/**
 * @override
 */
scout.Accordion.prototype.getFocusableElement = function() {
  var group = scout.widgets.findFirstFocusableWidget(this.groups, this);
  if (group) {
    return group.getFocusableElement();
  }
  return null;
};

scout.Accordion.prototype.setExclusiveExpand = function(exclusiveExpand) {
  this.setProperty('exclusiveExpand', exclusiveExpand);
};

scout.Accordion.prototype._setExclusiveExpand = function(exclusiveExpand) {
  this._setProperty('exclusiveExpand', exclusiveExpand);
  this._updateExclusiveExpand();
};

scout.Accordion.prototype._updateExclusiveExpand = function() {
  if (!this.exclusiveExpand) {
    return;
  }
  var expandedGroup = scout.arrays.find(this.groups, function(group) {
    return group.visible && !group.collapsed;
  });
  this._collapseOthers(expandedGroup);
};

scout.Accordion.prototype.setCollapseStyle = function(collapseStyle) {
  this.groups.forEach(function(group) {
    group.setCollapseStyle(collapseStyle);
  });
  this.setProperty('collapseStyle', collapseStyle);
};

scout.Accordion.prototype._collapseOthers = function(expandedGroup) {
  if (!expandedGroup || !expandedGroup.collapsible) {
    return;
  }
  this.groups.forEach(function(group) {
    if (group !== expandedGroup && group.collapsible) {
      group.setCollapsed(true);
    }
  });
};

scout.Accordion.prototype._onGroupPropertyChange = function(event) {
  if (event.propertyName === 'collapsed') {
    this._onGroupCollapsedChange(event);
  } else if (event.propertyName === 'visible') {
    this._updateFirstLastMarker();
  }
};

scout.Accordion.prototype._onGroupCollapsedChange = function(event) {
  if (!event.newValue && this.exclusiveExpand) {
    this._collapseOthers(event.source);
  }
};
