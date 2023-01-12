/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AccordionLayout, AccordionModel, arrays, Comparator, EventHandler, Group, GroupCollapseStyle, HtmlComponent, InitModelOf, LoadingSupport, ObjectOrChildModel, objects, PropertyChangeEvent, Widget, widgets} from '../index';

export class Accordion extends Widget implements AccordionModel {
  declare model: AccordionModel;
  comparator: Comparator<Group>;
  collapseStyle: GroupCollapseStyle;
  exclusiveExpand: boolean;
  groups: Group[];
  scrollable: boolean;
  protected _groupPropertyChangeHandler: EventHandler<PropertyChangeEvent<any>>;

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

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._initGroups(this.groups);
    this._setExclusiveExpand(this.exclusiveExpand);
  }

  protected override _createLoadingSupport(): LoadingSupport {
    return new LoadingSupport({
      widget: this
    });
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('accordion');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(this._createLayout());
  }

  protected _createLayout(): AccordionLayout {
    return new AccordionLayout();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderScrollable();
    this._renderGroups();
  }

  insertGroup(group: ObjectOrChildModel<Group>) {
    this.insertGroups([group]);
  }

  insertGroups(groupsToInsert: ObjectOrChildModel<Group> | ObjectOrChildModel<Group>[]) {
    let additionalGroups = arrays.ensure(groupsToInsert);
    let existingGroups: ObjectOrChildModel<Group>[] = this.groups;
    this.setGroups(existingGroups.concat(additionalGroups));
  }

  deleteGroup(group: Group) {
    this.deleteGroups([group]);
  }

  deleteGroups(groupsToDelete: Group[] | Group) {
    groupsToDelete = arrays.ensure(groupsToDelete);
    let groups = this.groups.slice();
    arrays.removeAll(groups, groupsToDelete);
    this.setGroups(groups);
  }

  deleteAllGroups() {
    this.setGroups([]);
  }

  protected _initGroups(groups: Group[]) {
    this.groups.forEach(group => {
      this._initGroup(group);
    });
  }

  setGroups(groupsOrModels: ObjectOrChildModel<Group> | ObjectOrChildModel<Group>[]) {
    let groupsOrModelsArr = arrays.ensure(groupsOrModels);
    if (objects.equals(this.groups, groupsOrModels)) {
      return;
    }

    // Ensure given groups are real groups (of type Group)
    let groups = this._createChildren(groupsOrModelsArr);

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

  protected _insertGroups(groups: Group[]) {
    groups.forEach(group => {
      this._insertGroup(group);
    });
  }

  protected _insertGroup(group: Group) {
    this._initGroup(group);
    if (this.rendered) {
      this._renderGroup(group);
    }
  }

  protected _initGroup(group: Group) {
    group.setParent(this);
    group.on('propertyChange', this._groupPropertyChangeHandler);

    // Copy properties from accordion to new group. If the properties are not set yet, copy them from the group to the accordion
    // This gives the possibility to either define the properties on the accordion or on the group initially
    if (this.collapseStyle !== null) {
      group.setCollapseStyle(this.collapseStyle);
    }
    this.setProperty('collapseStyle', group.collapseStyle);
  }

  protected _renderGroup(group: Group) {
    group.render();
  }

  protected _deleteGroups(groups: Group[]) {
    groups.forEach(group => {
      this._deleteGroup(group);
    });
  }

  protected _deleteGroup(group: Group) {
    group.off('propertyChange', this._groupPropertyChangeHandler);
    if (group.owner === this) {
      group.destroy();
    } else if (this.rendered) {
      group.remove();
    }
  }

  protected _renderGroups() {
    this.groups.forEach(group => {
      this._renderGroup(group);
    });
    this._updateFirstLastMarker();
    this.invalidateLayoutTree();
  }

  /** @see AccordionModel.comparator */
  setComparator(comparator: Comparator<Group>) {
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

  protected _sort(groups: Group[]) {
    if (this.comparator === null) {
      return;
    }
    groups.sort(this.comparator);
  }

  protected _updateGroupOrder(groups: Group[]) {
    if (!this.rendered) {
      return;
    }
    // Loop through the groups and move every html element to the end of the container
    // Only move if the order is different to the old order
    let different = false;
    groups.forEach((group, i) => {
      if (this.groups[i] !== group || different) {
        // Start ordering as soon as the order of the array starts to differ
        different = true;
        group.$container.appendTo(this.$container);
      }
    });
  }

  protected _updateFirstLastMarker() {
    widgets.updateFirstLastMarker(this.groups);
  }

  setScrollable(scrollable: boolean) {
    this.setProperty('scrollable', scrollable);
  }

  protected _renderScrollable() {
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

  override getFocusableElement(): HTMLElement | JQuery {
    let group = widgets.findFirstFocusableWidget(this.groups, this);
    if (group) {
      return group.getFocusableElement();
    }
    return null;
  }

  /** @see AccordionModel.exclusiveExpand */
  setExclusiveExpand(exclusiveExpand: boolean) {
    this.setProperty('exclusiveExpand', exclusiveExpand);
  }

  protected _setExclusiveExpand(exclusiveExpand: boolean) {
    this._setProperty('exclusiveExpand', exclusiveExpand);
    this._updateExclusiveExpand();
  }

  protected _updateExclusiveExpand() {
    if (!this.exclusiveExpand) {
      return;
    }
    let expandedGroup = arrays.find(this.groups, group => {
      return group.visible && !group.collapsed;
    });
    this._collapseOthers(expandedGroup);
  }

  setCollapseStyle(collapseStyle: GroupCollapseStyle) {
    this.groups.forEach(group => {
      group.setCollapseStyle(collapseStyle);
    });
    this.setProperty('collapseStyle', collapseStyle);
  }

  protected _collapseOthers(expandedGroup: Group) {
    if (!expandedGroup || !expandedGroup.collapsible) {
      return;
    }
    this.groups.forEach(group => {
      if (group !== expandedGroup && group.collapsible) {
        group.setCollapsed(true);
      }
    });
  }

  protected _onGroupPropertyChange(event: PropertyChangeEvent<any>) {
    if (event.propertyName === 'collapsed') {
      this._onGroupCollapsedChange(event);
    } else if (event.propertyName === 'visible') {
      this._updateFirstLastMarker();
    }
  }

  protected _onGroupCollapsedChange(event: PropertyChangeEvent<boolean>) {
    if (!event.newValue && this.exclusiveExpand) {
      this._collapseOthers(event.source as Group);
    }
  }
}
