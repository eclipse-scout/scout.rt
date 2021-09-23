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
import {strings, tooltips, Widget} from '../index';

export default class SimpleTab extends Widget {

  constructor() {
    super();

    // optional
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

    this._statusContainerUsageCounter = 0;
    this._statusIconDivs = [];

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

  _init(model) {
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

  renderAfter($parent, sibling) {
    this.render($parent);
    if (sibling) {
      this.$container.insertAfter(sibling.$container);
    }
  }

  _render() {
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

  _renderProperties() {
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

  _remove() {
    this.$close = null;
    super._remove();
  }

  setTitle(title) {
    this.setProperty('title', title);
  }

  _renderTitle() {
    if (this.title || this.subTitle) { // $title is always needed if subtitle is not empty
      this.$title.textOrNbsp(this.title);
    }
  }

  setSubTitle(subTitle) {
    this.setProperty('subTitle', subTitle);
  }

  _renderSubTitle() {
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

  setIconId(iconId) {
    this.setProperty('iconId', iconId);
  }

  _renderIconId(iconId) {
    this.$iconContainer.icon(this.iconId);
  }

  setClosable(closable) {
    this.setProperty('closable', closable);
  }

  _renderClosable() {
    if (this.closable) {
      if (this.$close) {
        return;
      }
      this.$container.addClass('closable');
      this.$close = this.$container.appendDiv('closer')
        .on('click', this._onClose.bind(this));
    } else {
      if (!this.$close) {
        return;
      }
      this.$container.removeClass('closable');
      this.$close.remove();
      this.$close = null;
    }
  }

  setSaveNeededVisible(saveNeededVisible) {
    if (this.saveNeededVisible === saveNeededVisible) {
      return;
    }
    this._setProperty('saveNeededVisible', saveNeededVisible);
    if (this.rendered) {
      this._renderSaveNeeded();
    }
  }

  setSaveNeeded(saveNeeded) {
    if (this.saveNeeded === saveNeeded) {
      return;
    }
    this._setProperty('saveNeeded', saveNeeded);
    if (this.rendered) {
      this._renderSaveNeeded();
    }
  }

  _renderSaveNeeded() {
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

  setStatus(status) {
    this.setProperty('status', status);
  }

  _renderStatus() {
    this._statusContainerUsageCounter -= (this._statusIconDivs.length === 0 ? 0 : 1);

    this._statusIconDivs.forEach($statusIcon => {
      $statusIcon.remove();
    });
    this._statusIconDivs = [];

    if (this.status) {
      this.status.asFlatList().forEach(status => {
        if (!status || !status.iconId) {
          return;
        }
        let $statusIcon = this.$statusContainer.appendIcon(status.iconId, 'status');
        if (status.cssClass()) {
          $statusIcon.addClass(status.cssClass());
        }
        this._statusIconDivs.push($statusIcon);
      });
    }
    this._statusContainerUsageCounter += (this._statusIconDivs.length === 0 ? 0 : 1);
    this.$container.toggleClass('has-status', this._statusContainerUsageCounter > 0);
  }

  select() {
    this.setSelected(true);
  }

  deselect() {
    this.setSelected(false);
  }

  setSelected(selected) {
    this.setProperty('selected', selected);
  }

  _renderSelected() {
    this.$container.toggleClass('selected', this.selected);
  }

  _onMouseDown(event) {
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

  _onClose() {
    if (this.view) {
      this.view.abort();
    }
  }

  getMenuText() {
    return strings.join(' \u2013 ', this.title, this.subTitle);
  }

  _installViewListeners() {
    this.view.on('propertyChange', this._viewPropertyChangeListener);
    this.view.on('remove', this._viewRemoveListener);
  }

  _uninstallViewListeners() {
    this.view.off('propertyChange', this._viewPropertyChangeListener);
    this.view.off('remove', this._viewRemoveListener);
  }

  _onViewPropertyChange(event) {
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
   * of the this tab, because in bench-mode the tab is never rendered
   * and thus the _remove function is never called.
   */
  _onViewRemove() {
    this._uninstallViewListeners();
    if (this.rendered) {
      this.remove();
    } else {
      this.trigger('remove');
    }
  }
}
