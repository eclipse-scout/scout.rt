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
import {AccordionLayout, arrays, HtmlComponent, LoadingSupport, objects, Widget, widgets} from '../index';

export default class Accordion extends Widget {
  constructor() {
    super();
    this.comparator = null;
    this.collapseStyle = null;
    this.exclusiveExpand = true;
    this.groups = [];
    this.scrollable = true;

    this.$container = null;
    this.htmlComp = null;
    this._addWidgetProperties(['groups']);
    this._groupPropertyChangeHandler = this._onGroupPropertyChange.bind(this);
  }

  _init(model) {
    super._init(model);
    this._initGroups(this.groups);
    this._setExclusiveExpand(this.exclusiveExpand);
  }

  /**
   * @override
   */
  _createLoadingSupport() {
    return new LoadingSupport({
      widget: this
    });
  }

  _render() {
    this.$container = this.$parent.appendDiv('accordion');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(this._createLayout());
  }

  _createLayout() {
    return new AccordionLayout();
  }

  _renderProperties() {
    super._renderProperties();
    this._renderScrollable();
    this._renderGroups();
  }

  insertGroup(group) {
    this.insertGroups([group]);
  }

  insertGroups(groupsToInsert) {
    groupsToInsert = arrays.ensure(groupsToInsert);
    this.setGroups(this.groups.concat(groupsToInsert));
  }

  deleteGroup(group) {
    this.deleteGroups([group]);
  }

  deleteGroups(groupsToDelete) {
    groupsToDelete = arrays.ensure(groupsToDelete);
    let groups = this.groups.slice();
    arrays.removeAll(groups, groupsToDelete);
    this.setGroups(groups);
  }

  deleteAllGroups() {
    this.setGroups([]);
  }

  _initGroups(groups) {
    this.groups.forEach(function(group) {
      this._initGroup(group);
    }, this);
  }

  setGroups(groups) {
    groups = arrays.ensure(groups);
    if (objects.equals(this.groups, groups)) {
      return;
    }

    // Ensure given groups are real groups (of type Group)
    groups = this._createChildren(groups);

    // Only delete those which are not in the new array
    // Only insert those which are not already there
    let groupsToDelete = arrays.diff(this.groups, groups);
    let groupsToInsert = arrays.diff(groups, this.groups);
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
  }

  _insertGroups(groups) {
    groups.forEach(function(group) {
      this._insertGroup(group);
    }, this);
  }

  _insertGroup(group) {
    this._initGroup(group);
    if (this.rendered) {
      this._renderGroup(group);
    }
  }

  _initGroup(group) {
    group.setParent(this);
    group.on('propertyChange', this._groupPropertyChangeHandler);

    // Copy properties from accordion to new group. If the properties are not set yet, copy them from the group to the accordion
    // This gives the possibility to either define the properties on the accordion or on the group initially
    if (this.collapseStyle !== null) {
      group.setCollapseStyle(this.collapseStyle);
    }
    this.setProperty('collapseStyle', group.collapseStyle);
  }

  _renderGroup(group) {
    group.render();
  }

  _deleteGroups(groups) {
    groups.forEach(function(group) {
      this._deleteGroup(group);
    }, this);
  }

  _deleteGroup(group) {
    group.off('propertyChange', this._groupPropertyChangeHandler);
    if (group.owner === this) {
      group.destroy();
    } else if (this.rendered) {
      group.remove();
    }
  }

  _renderGroups() {
    this.groups.forEach(function(group) {
      this._renderGroup(group);
    }, this);
    this._updateFirstLastMarker();
    this.invalidateLayoutTree();
  }

  setComparator(comparator) {
    if (this.comparator === comparator) {
      return;
    }
    this.comparator = comparator;
  }

  sort() {
    let groups = this.groups.slice();
    this._sort(groups);
    this._updateGroupOrder(groups);
    this._setProperty('groups', groups);
  }

  _sort(groups) {
    if (this.comparator === null) {
      return;
    }
    groups.sort(this.comparator);
  }

  _updateGroupOrder(groups) {
    if (!this.rendered) {
      return;
    }
    // Loop through the the groups and move every html element to the end of the container
    // Only move if the order is different to the old order
    let different = false;
    groups.forEach(function(group, i) {
      if (this.groups[i] !== group || different) {
        // Start ordering as soon as the order of the array starts to differ
        different = true;
        group.$container.appendTo(this.$container);
      }
    }, this);
  }

  _updateFirstLastMarker() {
    widgets.updateFirstLastMarker(this.groups);
  }

  setScrollable(scrollable) {
    this.setProperty('scrollable', scrollable);
  }

  _renderScrollable() {
    if (this.scrollable) {
      this._installScrollbars({
        axis: 'y'
      });
    } else {
      this._uninstallScrollbars();
    }
    this.$container.toggleClass('scrollable', this.scrollable);
    this.invalidateLayoutTree();
  }

  /**
   * @override
   */
  getFocusableElement() {
    let group = widgets.findFirstFocusableWidget(this.groups, this);
    if (group) {
      return group.getFocusableElement();
    }
    return null;
  }

  setExclusiveExpand(exclusiveExpand) {
    this.setProperty('exclusiveExpand', exclusiveExpand);
  }

  _setExclusiveExpand(exclusiveExpand) {
    this._setProperty('exclusiveExpand', exclusiveExpand);
    this._updateExclusiveExpand();
  }

  _updateExclusiveExpand() {
    if (!this.exclusiveExpand) {
      return;
    }
    let expandedGroup = arrays.find(this.groups, group => {
      return group.visible && !group.collapsed;
    });
    this._collapseOthers(expandedGroup);
  }

  setCollapseStyle(collapseStyle) {
    this.groups.forEach(group => {
      group.setCollapseStyle(collapseStyle);
    });
    this.setProperty('collapseStyle', collapseStyle);
  }

  _collapseOthers(expandedGroup) {
    if (!expandedGroup || !expandedGroup.collapsible) {
      return;
    }
    this.groups.forEach(group => {
      if (group !== expandedGroup && group.collapsible) {
        group.setCollapsed(true);
      }
    });
  }

  _onGroupPropertyChange(event) {
    if (event.propertyName === 'collapsed') {
      this._onGroupCollapsedChange(event);
    } else if (event.propertyName === 'visible') {
      this._updateFirstLastMarker();
    }
  }

  _onGroupCollapsedChange(event) {
    if (!event.newValue && this.exclusiveExpand) {
      this._collapseOthers(event.source);
    }
  }
}
