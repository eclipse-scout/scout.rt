/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Device, Event, EventHandler, FieldStatus, FormField, HtmlComponent, HtmlEnvironment, InitModelOf, PropertyChangeEvent, scout, SomeRequired, Status, strings, TabEventMap, TabItem, TabModel, tooltips, Widget} from '../../../index';

export class Tab extends Widget implements TabModel {
  declare model: TabModel;
  declare initModel: SomeRequired<this['model'], 'parent' | 'tabItem'>;
  declare eventMap: TabEventMap;
  declare self: Tab;

  label: string;
  subLabel: string;
  selected: boolean;
  overflown: boolean;
  marked: boolean;
  errorStatus: Status;
  tooltipText: string;
  fieldStatus: FieldStatus;
  tabItem: TabItem;
  tabbable: boolean;
  $title: JQuery;
  $label: JQuery;
  $subLabel: JQuery;
  protected _preventTabSelection: boolean;
  protected _tabPropertyChangeHandler: EventHandler<PropertyChangeEvent>;
  protected _statusMouseDownHandler: EventHandler;
  protected _desktopPropertyChangeHandler: EventHandler<PropertyChangeEvent>;

  constructor() {
    super();

    this.label = null;
    this.subLabel = null;
    this.selected = false;
    this.overflown = false;
    this.marked = false;
    this.errorStatus = null;
    this.tooltipText = null;
    this.fieldStatus = null;
    this.tabItem = null;
    this.tabbable = false;

    this.$label = null;
    this.$subLabel = null;
    this.$title = null;
    this._preventTabSelection = false;
    this._tabPropertyChangeHandler = this._onTabPropertyChange.bind(this);
    this._statusMouseDownHandler = this._onStatusMouseDown.bind(this);
    this._desktopPropertyChangeHandler = this._onDesktopPropertyChange.bind(this);
  }

  protected override _init(options: InitModelOf<this>) {
    super._init(options);
    this.visible = this.tabItem.visible;
    this.label = this.tabItem.label;
    this.subLabel = this.tabItem.subLabel;
    this.cssClass = this.tabItem.cssClass;
    this.marked = this.tabItem.marked;
    this.errorStatus = this.tabItem.errorStatus;
    this.tooltipText = this.tabItem.tooltipText;

    this.fieldStatus = scout.create(FieldStatus, {
      parent: this,
      visible: false
    });
    this.fieldStatus.on('statusMouseDown', this._statusMouseDownHandler);

    this.tabItem.on('propertyChange', this._tabPropertyChangeHandler);
  }

  protected override _destroy() {
    super._destroy();
    this.tabItem.off('propertyChange', this._tabPropertyChangeHandler);
    this.fieldStatus.off('statusMouseDown', this._statusMouseDownHandler);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('tab-item');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.$title = this.$container.appendDiv('title');
    this.$label = this.$title.appendDiv('label');
    tooltips.installForEllipsis(this.$label, {
      parent: this
    });

    this.$subLabel = this.$title.appendDiv('sub-label');
    tooltips.installForEllipsis(this.$subLabel, {
      parent: this
    });

    this.fieldStatus.render();
    this.fieldStatus.$container.cssWidth(HtmlEnvironment.get().fieldStatusWidth);

    this.$container.on('mousedown', this._onTabMouseDown.bind(this));
    this.session.desktop.on('propertyChange', this._desktopPropertyChangeHandler);
  }

  protected override _remove() {
    this.session.desktop.off('propertyChange', this._desktopPropertyChangeHandler);
    super._remove();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderVisible();
    this._renderLabel();
    this._renderSubLabel();
    this._renderTabbable();
    this._renderSelected();
    this._renderMarked();
    this._renderOverflown();
    this._renderTooltipText();
    this._renderErrorStatus();
  }

  protected override _renderVisible() {
    super._renderVisible();
    this._updateStatus();
  }

  setLabel(label: string) {
    this.setProperty('label', label);
  }

  protected _renderLabel() {
    this.$label.textOrNbsp(this.label);
    this.$label.attr('data-text', this.label);
    this.invalidateLayoutTree();
  }

  setSubLabel(subLabel: string) {
    this.setProperty('subLabel', subLabel);
  }

  protected _renderSubLabel() {
    this.$subLabel.textOrNbsp(this.subLabel);
    this.invalidateLayoutTree();
  }

  setTooltipText(tooltipText: string) {
    this.setProperty('tooltipText', tooltipText);
  }

  protected _renderTooltipText() {
    this.$container.toggleClass('has-tooltip', strings.hasText(this.tooltipText));
    this._updateStatus();
  }

  setErrorStatus(errorStatus: Status) {
    this.setProperty('errorStatus', errorStatus);
  }

  protected _renderErrorStatus() {
    let hasStatus = !!this.errorStatus,
      statusClass = hasStatus ? 'has-' + this.errorStatus.cssClass() : '';
    this._updateErrorStatusClasses(statusClass);
    this._updateStatus();
  }

  protected _updateErrorStatusClasses(statusClass: string) {
    this.$container.removeClass(FormField.SEVERITY_CSS_CLASSES);
    if (statusClass) {
      this.$container.addClass(statusClass);
    }
  }

  protected _updateStatus() {
    let visible = this._computeVisible(),
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
      status = scout.create(Status, {
        message: this.tooltipText,
        severity: Status.Severity.INFO
      });
    }
    this.fieldStatus.update(status, null, autoRemove, initialShow);
  }

  protected _computeVisible(): boolean {
    return this.visible && !this.overflown && (!!this.errorStatus || strings.hasText(this.tooltipText));
  }

  setTabbable(tabbable: boolean) {
    this.setProperty('tabbable', tabbable);
  }

  protected _renderTabbable() {
    this.$container.setTabbable(this.tabbable && !Device.get().supportsOnlyTouch());
  }

  select() {
    this.setSelected(true);
  }

  setSelected(selected: boolean) {
    this.setProperty('selected', selected);
  }

  protected _renderSelected() {
    this.$container.select(this.selected);
    this.$container.setTabbable(this.selected && !Device.get().supportsOnlyTouch());
  }

  setMarked(marked: boolean) {
    this.setProperty('marked', marked);
  }

  protected _renderMarked() {
    this.$container.toggleClass('marked', this.marked);
  }

  setOverflown(overflown: boolean) {
    this.setProperty('overflown', overflown);
  }

  protected _renderOverflown() {
    this.$container.toggleClass('overflown', this.overflown);
    this._updateStatus();
  }

  protected _onDesktopPropertyChange(event: PropertyChangeEvent) {
    // switching from or to the dense mode requires clearing of the tab's htmlComponent prefSize cache.
    if (event.propertyName === 'dense') {
      this.invalidateLayout();
    }
  }

  protected _onTabMouseDown(event: JQuery.MouseDownEvent) {
    if (this._preventTabSelection) {
      this._preventTabSelection = false;
      return;
    }

    // ensure to focus the selected tab before selecting the new tab.
    // The selection of a tab will remove the content of the previous selected tab.
    // Problem: If the previous is the focus owner the focus will be transferred to body what ends in a scroll top.
    this.setTabbable(true);
    this.$container.focus();

    this.select();

    // When the tab is clicked the user wants to execute the action and not see the tooltip
    if (this.$label) {
      tooltips.cancel(this.$label);
      tooltips.close(this.$label);
    }
    if (this.$subLabel) {
      tooltips.cancel(this.$subLabel);
      tooltips.close(this.$subLabel);
    }
  }

  protected _onStatusMouseDown(event: Event) {
    // Prevent switching tabs when status gets clicked
    // Don't use event.preventDefault, otherwise other mouse listener (like tooltip mouse down) will not be executed as well
    this._preventTabSelection = true;
    // Prevent focusing the tab
    event.preventDefault();
  }

  protected _onTabPropertyChange(event: PropertyChangeEvent) {
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
    // Note: If you add a property here, also add it to _init()
  }
}
