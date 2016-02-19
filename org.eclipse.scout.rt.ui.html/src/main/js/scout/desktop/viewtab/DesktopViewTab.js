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
scout.DesktopViewTab = function() {
  scout.DesktopViewTab.parent.call(this);

  this._view;
  this._$bench;
  this._mouseListener;
  this.viewTabController;

  // Container for the _Tab_ (not for the view).
  this.$container;

  this._propertyChangeListener = function(event) {
    if (scout.arrays.containsAny(event.changedProperties, ['title', 'subTitle', 'iconId'])) {
      this._titlesUpdated();
    }
    if (scout.arrays.containsAny(event.changedProperties, ['cssClass'])) {
      this._cssClassUpdated(event.newProperties.cssClass, event.oldProperties.cssClass);
    }
  }.bind(this);

  // FIXME awe: problem ist, dass Widegt#remove prüft ob rendered ist
  // im bench mode ist der DesktopViewTab nicht gerendet, _remove wird
  // darum nicht aufgerufen und das 'remove event vom tab nie getriggert
  this._removeListener = this._onViewRemoved.bind(this);

  this._addEventSupport();
};
scout.inherits(scout.DesktopViewTab, scout.Widget);

scout.DesktopViewTab.prototype._init = function(options) {
  scout.DesktopViewTab.parent.prototype._init.call(this, options);
  this._view = options.view;
  this._view.setParent(this);
  this.viewTabController = options.viewTabController;
  this._$bench = options.$bench;

  this._installListeners();
};

scout.DesktopViewTab.prototype._installListeners = function() {
  this._view.on('propertyChange', this._propertyChangeListener);
  this._view.on('remove', this._removeListener);
};

scout.DesktopViewTab.prototype._uninstallListeners = function() {
  this._view.off('propertyChange', this._propertyChangeListener);
  this._view.off('remove', this._removeListener);
};

scout.DesktopViewTab.prototype._render = function($parent) {
  var position = this.viewTabController.viewTabs().indexOf(this);
  if (position === 0) {
    this.$container = $parent.prependDiv('desktop-view-tab');
  } else if (position > 0) {
    var previousTab = this.viewTabController.viewTabs()[position - 1];
    this.$container = previousTab.$container.afterDiv('desktop-view-tab');

  }
  this._mouseListener = this._onMouseDown.bind(this);
  this.$container.on('mousedown', this._mouseListener);
  this._$title = this.$container.appendDiv('title');
  this._$subTitle = this.$container.appendDiv('sub-title');
  this._titlesUpdated();
  this._cssClassUpdated(this._view.cssClass, null);
};

scout.DesktopViewTab.prototype._renderView = function($parent) {
  if (this._view.rendered) {
    throw new Error('view already rendered');
  }
  this._view.render(this._$bench);
  this._view.$container.addClass('view');
  this._view.validateRoot = true;
  this._view.invalidateLayoutTree(false);
  // Layout immediate to prevent 'laggy' form visualization,
  // but not initially while desktop gets rendered because it will be done at the end anyway
  if (this.rendered) {
    this._view.validateLayoutTree();
  }
};

scout.DesktopViewTab.prototype.select = function() {
  this._cssSelect(true);
  if (this._view.rendered) {
    this._view.attach();
  } else {
    this._renderView();
    if (this.session.desktop._outlineContent !== this._view) {
      // Notify model that this form is active
      this.session.desktop._setFormActivated(this._view);
    }
  }
};

scout.DesktopViewTab.prototype._cssSelect = function(selected) {
  if (this.$container) {
    this.$container.select(selected);
  }
};

scout.DesktopViewTab.prototype.deselect = function() {
  if (this._view.rendered) {
    this._view.detach();
  }
  this._cssSelect(false);
};

scout.DesktopViewTab.prototype._onMouseDown = function(event) {
  this.events.trigger('tabClicked', this);
};

scout.DesktopViewTab.prototype._titlesUpdated = function() {
  if (!this.$container) {
    return;
  }

  // Titles
  setTitle(this._$title, this._view.title);
  setTitle(this._$subTitle, this._view.subTitle);

  // Icon
  this.$container.icon(this._view.iconId);

  // ----- Helper functions -----

  function setTitle($titleElement, title) {
    $titleElement.textOrNbsp(title);
  }
};

scout.DesktopViewTab.prototype._cssClassUpdated = function(cssClass, oldCssClass) {
  if (!this.$container) {
    return;
  }
  this.$container.removeClass(oldCssClass);
  this.$container.addClass(cssClass);
};

/**
 * We cannot not bind the 'remove' event of the view to the remove function
 * of the this tab, because in bench-mode we the tab is never rendered
 * and thus the _remove function is never called. However, we must still
 * trigger the 'remove' event because the ViewTabsController depends on it.
 */
scout.DesktopViewTab.prototype._onViewRemoved = function() {
  this._uninstallListeners();
  if (this.rendered) {
    this.remove();
  } else {
    this._trigger('remove');
  }
};

scout.DesktopViewTab.prototype.getMenuText = function() {
  var text = this._view.title;
  if (this._view.subTitle) {
    text += ' (' + this._view.subTitle + ')';
  }
  return text;
};
