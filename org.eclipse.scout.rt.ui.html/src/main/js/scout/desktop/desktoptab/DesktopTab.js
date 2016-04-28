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
scout.DesktopTab = function() {
  scout.DesktopTab.parent.call(this);

  this.view;

  this._propertyChangeListener = function(event) {
    if (scout.arrays.containsAny(event.changedProperties, ['title'])) {
      this.setTitle(this.view.title);
    } else if (scout.arrays.containsAny(event.changedProperties, ['subTitle'])) {
      this.setSubTitle(this.view.subTitle);
    } else if (scout.arrays.containsAny(event.changedProperties, ['iconId'])) {
      this.setIconId(this.view.iconId);
    } else if (scout.arrays.containsAny(event.changedProperties, ['cssClass'])) {
      this._cssClassUpdated(event.newProperties.cssClass, event.oldProperties.cssClass);
    }
  }.bind(this);

  // FIXME awe: problem ist, dass Widegt#remove prüft ob rendered ist
  // im bench mode ist der DesktopTab nicht gerendet, _remove wird
  // darum nicht aufgerufen und das 'remove event vom tab nie getriggert
  this._removeListener = this._onViewRemoved.bind(this);

  this._addEventSupport();
};

scout.inherits(scout.DesktopTab, scout.SimpleTab);

scout.DesktopTab.prototype._init = function(options) {
  this.view = options.view;
  options.title = this.view.title;
  options.subTitle = this.view.subTitle;
  options.iconId = this.view.iconId;

  scout.DesktopTab.parent.prototype._init.call(this, options);


  this._installListeners();
};

scout.DesktopTab.prototype._installListeners = function() {
  this.view.on('propertyChange', this._propertyChangeListener);
  this.view.on('remove', this._removeListener);
};

scout.DesktopTab.prototype._uninstallListeners = function() {
  this.view.off('propertyChange', this._propertyChangeListener);
  this.view.off('remove', this._removeListener);
};

scout.DesktopTab.prototype._cssClassUpdated = function(cssClass, oldCssClass) {
  if (!this.$container) {
    return;
  }
  this.$container.removeClass(oldCssClass);
  this.$container.addClass(cssClass);
};

/**
 * We cannot not bind the 'remove' event of the view to the remove function
 * of the this tab, because in bench-mode we the tab is never rendered
 * and thus the _remove function is never called.
 */
scout.DesktopTab.prototype._onViewRemoved = function() {
  this._uninstallListeners();
  if (this.rendered) {
    this.remove();
  } else {
    this.trigger('remove');
  }
};
