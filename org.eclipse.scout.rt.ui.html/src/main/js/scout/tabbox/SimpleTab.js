/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.SimpleTab = function() {
  scout.SimpleTab.parent.call(this);

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

  // Order: $statusContainer, $iconContainer, $title, $subTitle
  // - Status container needs to be the first element because it is "float: right".
  // - Icon container is "float: left" , must be before title.
  this.$title;
  this.$subTitle;
  this.$iconContainer;
  this.$statusContainer;

  this._statusContainerUsageCounter = 0;
  this._statusIconDivs = [];

  this._viewPropertyChangeListener = this._onViewPropertyChange.bind(this);
  this._viewRemoveListener = this._onViewRemove.bind(this);
};
scout.inherits(scout.SimpleTab, scout.Widget);

scout.SimpleTab.prototype._init = function(model) {
  scout.SimpleTab.parent.prototype._init.call(this, model);

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
  }
};

scout.SimpleTab.prototype.renderAfter = function($parent, sibling) {
  this.render($parent);
  if (sibling) {
    this.$container.insertAfter(sibling.$container);
  }
};

scout.SimpleTab.prototype._render = function() {
  this.$container = this.$parent.prependDiv('simple-tab');
  this.$container.on('mousedown', this._onMouseDown.bind(this));
};

scout.SimpleTab.prototype._renderProperties = function() {
  scout.SimpleTab.parent.prototype._renderProperties.call(this);
  this._renderTitle();
  this._renderSubTitle();
  this._renderIconId();
  this._renderCssClass();
  this._renderClosable();
  this._renderSaveNeeded();
  this._renderStatus();
  this._renderSelected();
};

scout.SimpleTab.prototype._remove = function() {
  this._remove$Title();
  this._remove$SubTitle();
  this._remove$IconContainer();
  this._remove$StatusContainer();
  this.$close = null;
  scout.SimpleTab.parent.prototype._remove.call(this);
};

scout.SimpleTab.prototype._getOrCreate$Title = function() {
  if (this.$title) {
    return this.$title;
  }
  this.$title = this.$container.makeDiv('title');
  scout.tooltips.installForEllipsis(this.$title, {
    parent: this
  });
  if (this.$subTitle) {
    this.$title.insertBefore(this.$subTitle);
  } else if (this.$iconContainer) {
    this.$title.insertAfter(this.$iconContainer);
  } else if (this.$statusContainer) {
    this.$title.insertAfter(this.$statusContainer);
  } else {
    this.$title.appendTo(this.$container);
  }
  return this.$title;
};

scout.SimpleTab.prototype._getOrCreate$SubTitle = function() {
  if (this.$subTitle) {
    return this.$subTitle;
  }
  this.$subTitle = this.$container.makeDiv('sub-title');
  scout.tooltips.installForEllipsis(this.$subTitle, {
    parent: this
  });
  if (this.$title) {
    this.$subTitle.insertAfter(this.$title);
  } else if (this.$iconContainer) {
    this.$subTitle.insertAfter(this.$iconContainer);
  } else if (this.$statusContainer) {
    this.$subTitle.insertAfter(this.$statusContainer);
  } else {
    this.$subTitle.appendTo(this.$container);
  }
  return this.$subTitle;
};

scout.SimpleTab.prototype._getOrCreate$IconContainer = function() {
  if (this.$iconContainer) {
    return this.$iconContainer;
  }
  this.$iconContainer = this.$container.makeDiv('icon-container');
  if (this.$title) {
    this.$iconContainer.insertBefore(this.$title);
  } else if (this.$subTitle) {
    this.$iconContainer.insertBefore(this.$subTitle);
  } else if (this.$statusContainer) {
    this.$iconContainer.insertAfter(this.$statusContainer);
  } else {
    this.$iconContainer.appendTo(this.$container);
  }
  return this.$iconContainer;
};

scout.SimpleTab.prototype._getOrCreate$StatusContainer = function() {
  if (this.$statusContainer) {
    return this.$statusContainer;
  }
  // Prepend because of "float: right"
  this.$statusContainer = this.$container.prependDiv('status-container');
  return this.$statusContainer;
};

scout.SimpleTab.prototype._remove$Title = function() {
  if (this.$title) {
    scout.tooltips.uninstall(this.$title);
    this.$title.remove();
    this.$title = null;
  }
};

scout.SimpleTab.prototype._remove$SubTitle = function() {
  if (this.$subTitle) {
    scout.tooltips.uninstall(this.$subTitle);
    this.$subTitle.remove();
    this.$subTitle = null;
  }
};

scout.SimpleTab.prototype._remove$IconContainer = function() {
  if (this.$iconContainer) {
    this.$iconContainer.remove();
    this.$iconContainer = null;
  }
};

scout.SimpleTab.prototype._remove$StatusContainer = function() {
  if (this.$statusContainer) {
    this.$statusContainer.remove();
    this.$statusContainer = null;
  }
};

scout.SimpleTab.prototype.setTitle = function(title) {
  this.setProperty('title', title);
};

scout.SimpleTab.prototype._renderTitle = function() {
  if (this.title || this.subTitle) { // $title is always needed if subtitle is not empty
    this._getOrCreate$Title().textOrNbsp(this.title);
  } else {
    this._remove$Title();
  }
};

scout.SimpleTab.prototype.setSubTitle = function(subTitle) {
  this.setProperty('subTitle', subTitle);
};

scout.SimpleTab.prototype._renderSubTitle = function() {
  if (this.subTitle) {
    if (!this.title) {
      this._renderTitle();
    }
    this._getOrCreate$SubTitle().textOrNbsp(this.subTitle);
  } else {
    if (!this.title) {
      this._renderTitle();
    }
    this._remove$SubTitle();
  }
};

scout.SimpleTab.prototype.setIconId = function(iconId) {
  this.setProperty('iconId', iconId);
};

scout.SimpleTab.prototype._renderIconId = function(iconId) {
  if (this.iconId) {
    this._getOrCreate$IconContainer().icon(this.iconId);
  } else {
    this._remove$IconContainer();
  }
};

scout.SimpleTab.prototype.setCssClass = function(cssClass) {
  this.setProperty('cssClass', cssClass);
};

scout.SimpleTab.prototype._renderCssClass = function() {
  this.$container.addClass(this.cssClass);
};

scout.SimpleTab.prototype._removeCssClass = function() {
  this.$container.removeClass(this.cssClass);
};

scout.SimpleTab.prototype.setClosable = function(closable) {
  this.setProperty('closable', closable);
};

scout.SimpleTab.prototype._renderClosable = function() {
  if (this.closable) {
    if (this.$close) {
      return;
    }
    this.$container.addClass('closable');
    this.$close = this._getOrCreate$StatusContainer().appendDiv('status closer')
      .on('click', this._onClose.bind(this));
    this._statusContainerUsageCounter++;
  } else {
    if (!this.$close) {
      return;
    }
    this.$container.removeClass('closable');
    this.$close.remove();
    this.$close = null;
    this._statusContainerUsageCounter--;
    if (this._statusContainerUsageCounter === 0) {
      this._remove$StatusContainer();
    }
  }
};

scout.SimpleTab.prototype.setSaveNeededVisible = function(saveNeededVisible) {
  if (this.saveNeededVisible === saveNeededVisible) {
    return;
  }
  this._setProperty('saveNeededVisible', saveNeededVisible);
  if (this.rendered) {
    this._renderSaveNeeded();
  }
};

scout.SimpleTab.prototype.setSaveNeeded = function(saveNeeded) {
  if (this.saveNeeded === saveNeeded) {
    return;
  }
  this._setProperty('saveNeeded', saveNeeded);
  if (this.rendered) {
    this._renderSaveNeeded();
  }
};

scout.SimpleTab.prototype._renderSaveNeeded = function() {
  if (this.saveNeeded && this.saveNeededVisible) {
    this.$container.addClass('save-needed');
    if (this.$saveNeeded) {
      return;
    }
    this.$saveNeeded = this._getOrCreate$StatusContainer().prependDiv('status save-needer');
    this._statusContainerUsageCounter++;
  } else {
    if (!this.$saveNeeded) {
      return;
    }
    this.$container.removeClass('save-needed');
    this.$saveNeeded.remove();
    this.$saveNeeded = null;
    this._statusContainerUsageCounter--;
    if (this._statusContainerUsageCounter === 0) {
      this._remove$StatusContainer();
    }
  }
};

scout.SimpleTab.prototype.setStatus = function(status) {
  this.setProperty('status', status);
};

scout.SimpleTab.prototype._renderStatus = function() {
  this._statusContainerUsageCounter -= (this._statusIconDivs.length === 0 ? 0 : 1);

  this._statusIconDivs.forEach(function($statusIcon) {
    $statusIcon.remove();
  }.bind(this));
  this._statusIconDivs = [];

  if (this.status) {
    this.status.asFlatList().forEach(function(status) {
      if (!status || !status.iconId) {
        return;
      }
      var $statusIcon = this._getOrCreate$StatusContainer().appendIcon(status.iconId, 'status');
      if (status.cssClass()) {
        $statusIcon.addClass(status.cssClass());
      }
      this._statusIconDivs.push($statusIcon);
    }.bind(this));
  }

  this._statusContainerUsageCounter += (this._statusIconDivs.length === 0 ? 0 : 1);
  if (this._statusContainerUsageCounter === 0) {
    this._remove$StatusContainer();
  }
};

scout.SimpleTab.prototype.select = function() {
  this.setSelected(true);
};

scout.SimpleTab.prototype.deselect = function() {
  this.setSelected(false);
};

scout.SimpleTab.prototype.setSelected = function(selected) {
  this.setProperty('selected', selected);
};

scout.SimpleTab.prototype._renderSelected = function() {
  this.$container.select(this.selected);
};

scout.SimpleTab.prototype._onMouseDown = function(event) {
  this.trigger('click');
};

scout.SimpleTab.prototype._onClose = function() {
  if (this.view) {
    this.view.abort();
  }
};

scout.SimpleTab.prototype.getMenuText = function() {
  return scout.strings.join(' ', this.title, scout.strings.box('(', this.subTitle, ')'));
};

scout.SimpleTab.prototype._installViewListeners = function() {
  this.view.on('propertyChange', this._viewPropertyChangeListener);
  this.view.on('remove', this._viewRemoveListener);
};

scout.SimpleTab.prototype._uninstallViewListeners = function() {
  this.view.off('propertyChange', this._viewPropertyChangeListener);
  this.view.off('remove', this._viewRemoveListener);
};

scout.SimpleTab.prototype._onViewPropertyChange = function(event) {
  if (event.propertyName === 'title') {
    this.setTitle(this.view.title);
  }
  else if (event.propertyName === 'subTitle') {
    this.setSubTitle(this.view.subTitle);
  }
  else if (event.propertyName === 'iconId') {
    this.setIconId(this.view.iconId);
  }
  else if (event.propertyName === 'cssClass') {
    this.setCssClass(event.newValue);
  }
  else if (event.propertyName === 'saveNeeded') {
    this.setSaveNeeded(event.newValue);
  }
  else if (event.propertyName === 'saveNeededVisible') {
    this.setSaveNeededVisible(event.newValue);
  }
  else if (event.propertyName === 'closable') {
    this.setClosable(event.newValue);
  }
  else if (event.propertyName === 'status') {
    this.setStatus(event.newValue);
  }
};

/**
 * We cannot not bind the 'remove' event of the view to the remove function
 * of the this tab, because in bench-mode the tab is never rendered
 * and thus the _remove function is never called.
 */
scout.SimpleTab.prototype._onViewRemove = function() {
  this._uninstallViewListeners();
  if (this.rendered) {
    this.remove();
  } else {
    this.trigger('remove'); // FIXME STUDIO why is this needed?
  }
};
