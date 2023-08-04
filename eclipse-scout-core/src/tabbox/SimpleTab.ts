/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, Event, EventHandler, GlassPaneContribution, InitModelOf, PropertyChangeEvent, SimpleTabEventMap, SimpleTabModel, Status, strings, tooltips, Widget} from '../index';

export type DisplayViewId = 'N' | 'NE' | 'E' | 'SE' | 'S' | 'SW' | 'W' | 'NW' | 'C' | 'OUTLINE' | 'OUTLINE_SELECTOR' | 'PAGE_DETAIL' | 'PAGE_SEARCH' | 'PAGE_TABLE';

export class SimpleTab<TView extends SimpleTabView = SimpleTabView> extends Widget implements SimpleTabModel<TView> {
  declare model: SimpleTabModel<TView>;
  declare eventMap: SimpleTabEventMap<TView>;
  declare self: SimpleTab<any>;

  view: TView;
  title: string;
  subTitle: string;
  iconId: string;
  closable: boolean;
  saveNeeded: boolean;
  saveNeededVisible: boolean;
  status: Status;
  selected: boolean;
  $title: JQuery;
  $subTitle: JQuery;
  $iconContainer: JQuery;
  $statusContainer: JQuery;
  $close: JQuery;
  $titleLine: JQuery;
  $saveNeeded: JQuery;
  $statusIcons: JQuery[];

  protected _statusContainerUsageCounter: number;
  protected _viewPropertyChangeListener: EventHandler<PropertyChangeEvent>;
  protected _viewRemoveListener: EventHandler<Event<TView>>;
  protected _glassPaneContribution: GlassPaneContribution;

  constructor() {
    super();

    this.view = null;
    this.title = null;
    this.subTitle = null;
    this.iconId = null;
    this.cssClass = null;
    this.closable = false;
    this.saveNeeded = false;
    this.saveNeededVisible = false;
    this.status = null;
    this.selected = false;

    this.$title = null;
    this.$subTitle = null;
    this.$iconContainer = null;
    this.$statusContainer = null;
    this.$statusIcons = [];

    this._statusContainerUsageCounter = 0;
    this._viewPropertyChangeListener = this._onViewPropertyChange.bind(this);
    this._viewRemoveListener = this._onViewRemove.bind(this);
    this._glassPaneContribution = element => {
      if (!this.$close) {
        return null;
      }
      // glass pane will be added as direct child which does not prevent clicks and hover effects -> glasspane-parent marker needed
      this.$close.addClass('glasspane-parent');
      return this.$close;
    };
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this.view = model.view;

    this.title = (this.view ? this.view.title : model.title);
    this.subTitle = (this.view ? this.view.subTitle : model.subTitle);
    this.iconId = (this.view ? this.view.iconId : model.iconId);
    this.cssClass = (this.view ? this.view.cssClass : model.cssClass);
    this.closable = (this.view ? this.view.closable : model.closable);
    this.saveNeeded = (this.view ? this.view.saveNeeded : model.saveNeeded);
    this.saveNeededVisible = (this.view ? this.view.saveNeededVisible : model.saveNeededVisible);
    this.status = (this.view ? this.view.status : model.status);

    if (this.view) {
      this._installViewListeners();
      this.view.addGlassPaneContribution(this._glassPaneContribution);
    }
  }

  renderAfter($parent: JQuery, sibling?: Widget) {
    this.render($parent);
    if (sibling) {
      this.$container.insertAfter(sibling.$container);
    }
  }

  protected override _render() {
    this.$container = this.$parent.prependDiv('simple-tab');
    this.$container.on('mousedown', this._onMouseDown.bind(this));
    this.$titleLine = this.$container.appendDiv('title-line');
    this.$iconContainer = this.$titleLine.appendDiv('icon-container');
    this.$title = this.$titleLine.appendDiv('title');
    tooltips.installForEllipsis(this.$title, {
      parent: this
    });
    this.$statusContainer = this.$titleLine.appendDiv('status-container');
    this.$subTitle = this.$container.appendDiv('sub-title');
    tooltips.installForEllipsis(this.$subTitle, {
      parent: this
    });
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderTitle();
    this._renderSubTitle();
    this._renderIconId();
    this._renderCssClass();
    this._renderClosable();
    this._renderSaveNeeded();
    this._renderStatus();
    this._renderSelected();
  }

  protected override _remove() {
    this.$close = null;
    super._remove();
  }

  setTitle(title: string) {
    this.setProperty('title', title);
  }

  protected _renderTitle() {
    if (this.title || this.subTitle) { // $title is always needed if subtitle is not empty
      this.$title.textOrNbsp(this.title);
    }
  }

  setSubTitle(subTitle: string) {
    this.setProperty('subTitle', subTitle);
  }

  protected _renderSubTitle() {
    if (this.subTitle) {
      if (!this.title) {
        this._renderTitle();
      }
      this.$subTitle.textOrNbsp(this.subTitle);
    } else {
      if (!this.title) {
        this._renderTitle();
      }
    }
  }

  setIconId(iconId: string) {
    this.setProperty('iconId', iconId);
  }

  protected _renderIconId() {
    this.$iconContainer.icon(this.iconId);
  }

  setClosable(closable: boolean) {
    this.setProperty('closable', closable);
  }

  protected _renderClosable() {
    if (this.closable) {
      if (this.$close) {
        return;
      }
      this.$container.addClass('closable');
      this.$close = this.$container.appendDiv('closer')
        .on('click', this._onClose.bind(this));
      aria.role(this.$close, 'button');
      aria.label(this.$close, this.session.text('ui.Close'));
    } else {
      if (!this.$close) {
        return;
      }
      this.$container.removeClass('closable');
      this.$close.remove();
      this.$close = null;
    }
  }

  setSaveNeededVisible(saveNeededVisible: boolean) {
    if (this.saveNeededVisible === saveNeededVisible) {
      return;
    }
    this._setProperty('saveNeededVisible', saveNeededVisible);
    if (this.rendered) {
      this._renderSaveNeeded();
    }
  }

  setSaveNeeded(saveNeeded: boolean) {
    if (this.saveNeeded === saveNeeded) {
      return;
    }
    this._setProperty('saveNeeded', saveNeeded);
    if (this.rendered) {
      this._renderSaveNeeded();
    }
  }

  protected _renderSaveNeeded() {
    if (this.saveNeeded && this.saveNeededVisible) {
      this.$container.addClass('save-needed');
      if (this.$saveNeeded) {
        return;
      }
      this.$saveNeeded = this.$statusContainer.prependDiv('status save-needer');
      this._statusContainerUsageCounter++;
    } else {
      if (!this.$saveNeeded) {
        return;
      }
      this.$container.removeClass('save-needed');
      this.$saveNeeded.remove();
      this.$saveNeeded = null;
      this._statusContainerUsageCounter--;
    }
  }

  setStatus(status: Status) {
    this.setProperty('status', status);
  }

  protected _renderStatus() {
    this._statusContainerUsageCounter -= (this.$statusIcons.length === 0 ? 0 : 1);

    this.$statusIcons.forEach($statusIcon => $statusIcon.remove());
    this.$statusIcons = [];

    if (this.status) {
      this.status.asFlatList().forEach(status => {
        if (!status || (!status.iconId && !status.message)) {
          return;
        }
        if (status.iconId) {
          let $statusIcon = this.$statusContainer.appendIcon(status.iconId, 'status');
          if (status.cssClass()) {
            $statusIcon.addClass(status.cssClass());
          }
          this.$statusIcons.push($statusIcon);
        }
        if (status.message) {
          let $statusMessage = this.$statusContainer.appendSpan('status message');
          if (status.cssClass()) {
            $statusMessage.addClass(status.cssClass());
          }
          let $text = $statusMessage.appendSpan('text', status.message);
          tooltips.installForEllipsis($text, {
            parent: this
          });
          this.$statusIcons.push($statusMessage);
        }
      });
    }
    this._statusContainerUsageCounter += (this.$statusIcons.length === 0 ? 0 : 1);
    this.$container.toggleClass('has-status', this._statusContainerUsageCounter > 0);
  }

  select() {
    this.setSelected(true);
  }

  deselect() {
    this.setSelected(false);
  }

  setSelected(selected: boolean) {
    this.setProperty('selected', selected);
  }

  protected _renderSelected() {
    this.$container.toggleClass('selected', this.selected);
    aria.role(this.$titleLine, this.selected ? 'heading' : null);
    aria.level(this.$titleLine, this.selected ? 1 : null);
  }

  protected _onMouseDown(event: JQuery.MouseDownEvent) {
    if (this.$close && this.$close.isOrHas(event.target)) {
      return;
    }

    this.trigger('click');

    // When the tab is clicked the user wants to execute the action and not see the tooltip
    if (this.$title) {
      tooltips.cancel(this.$title);
      tooltips.close(this.$title);
    }
    if (this.$subTitle) {
      tooltips.cancel(this.$subTitle);
      tooltips.close(this.$subTitle);
    }
    event.preventDefault();
  }

  protected _onClose(event: JQuery.ClickEvent) {
    if (this.view) {
      this.view.abort();
    }
  }

  getMenuText(): string {
    return strings.join(' \u2013 ', this.title, this.subTitle);
  }

  protected _installViewListeners() {
    this.view.on('propertyChange', this._viewPropertyChangeListener);
    this.view.on('remove', this._viewRemoveListener);
  }

  protected _uninstallViewListeners() {
    this.view.off('propertyChange', this._viewPropertyChangeListener);
    this.view.off('remove', this._viewRemoveListener);
  }

  protected _onViewPropertyChange(event: PropertyChangeEvent) {
    if (event.propertyName === 'title') {
      this.setTitle(this.view.title);
    } else if (event.propertyName === 'subTitle') {
      this.setSubTitle(this.view.subTitle);
    } else if (event.propertyName === 'iconId') {
      this.setIconId(this.view.iconId);
    } else if (event.propertyName === 'cssClass') {
      this.setCssClass(event.newValue);
    } else if (event.propertyName === 'saveNeeded') {
      this.setSaveNeeded(event.newValue);
    } else if (event.propertyName === 'saveNeededVisible') {
      this.setSaveNeededVisible(event.newValue);
    } else if (event.propertyName === 'closable') {
      this.setClosable(event.newValue);
    } else if (event.propertyName === 'status') {
      this.setStatus(event.newValue);
    }
  }

  /**
   * We cannot not bind the 'remove' event of the view to the remove function
   * of this tab, because in bench-mode the tab is never rendered
   * and thus the _remove function is never called.
   */
  protected _onViewRemove(event: Event<TView>) {
    this._uninstallViewListeners();
    if (this.rendered) {
      this.remove();
    } else {
      this.trigger('remove');
    }
  }
}

export interface SimpleTabView extends Widget {
  title?: string;
  subTitle?: string;
  iconId?: string;
  closable?: boolean;
  saveNeeded?: boolean;
  saveNeededVisible?: boolean;
  status?: Status;
  displayViewId?: DisplayViewId;
  abort?: () => void;
}
