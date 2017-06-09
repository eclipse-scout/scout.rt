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
scout.SimpleTab = function() {
  scout.SimpleTab.parent.call(this);

  this.title;
  this.subTitle;
  this.iconId;
  this.status;

  this._mouseListener;

  // Container for the _Tab_ (not for the view).
  this.$container;
  this.$status = [];
};
scout.inherits(scout.SimpleTab, scout.Widget);

scout.SimpleTab.prototype._init = function(options) {
  scout.SimpleTab.parent.prototype._init.call(this, options);
  this.title = options.title;
  this.subTitle = options.subTitle;
  this.iconId = options.iconId;
  this.closable = options.closable;
  this.saveNeeded = options.saveNeeded;
  this.saveNeededVisible = options.saveNeededVisible;
  this.closable = options.closable;
  this.status = options.status;
  this.selected = false;
  this.cssClass = options.cssClass;
};

scout.SimpleTab.prototype.renderAfter = function($parent, sibling) {
  this.render($parent);
  if (sibling) {
    this.$container.insertAfter(sibling.$container);
  }
};

scout.SimpleTab.prototype._render = function() {
  this.$container = this.$parent.prependDiv('simple-tab');
  this._mouseListener = this._onMouseDown.bind(this);
  this.$container.on('mousedown', this._mouseListener);
  this.$statusContainer = this.$container.appendDiv('status-container');
  this.$icon = this.$container.appendDiv('icon-container');
  this._$title = this.$container.appendDiv('title');
  this._$subTitle = this.$container.appendDiv('sub-title');

  this._renderTitle();
  this._renderSubTitle();
  this._renderSelection();
  this._renderClosable();
  this._renderSaveNeeded();
  this._renderIconId();
  this._renderStatus();
  this._renderCssClass();
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
    this.$close = this.$statusContainer.appendDiv('status closer')
      .on('click', this._onClose.bind(this));
  } else {
    if (!this.$close) {
      return;
    }
    this.$container.removeClass('closable');
    this.$close.remove();
    this.$close = null;
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
    if (this.$close) {
      this.$saveNeeded = this.$close.beforeDiv('status save-needer');
    } else {
      this.$saveNeeded = this.$statusContainer
        .appendDiv('status save-needer');
    }
  } else {
    this.$container.removeClass('save-needed');
    if (!this.$saveNeeded) {
      return;
    }
    this.$saveNeeded.remove();
    this.$saveNeeded = null;
  }
};

scout.SimpleTab.prototype.setStatus = function(status) {
  this.setProperty('status', status);
};

scout.SimpleTab.prototype._renderStatus = function() {
  this.$status.forEach(function($sts) {
    $sts.remove();
  }.bind(this));
  this.$status = [];
  if (this.status) {
    var statusList = this.status.asFlatList();
    var $sibling = null;
    statusList.forEach(function(sts) {
      $sibling = _renderSingleStatus.call(this, sts, $sibling);
      if ($sibling) {
        this.$status.push($sibling);
      }

    }.bind(this));
  }

  // private function
  function _renderSingleStatus(status, $sibling) {
    if (status && status.iconId) {

      var $statusIcon = this.$statusContainer.appendIcon(status.iconId, 'status');
      if (status.cssClass()) {
        $statusIcon.addClass(status.cssClass());
      }
      if ($sibling) {
        $sibling.after($statusIcon);
      } else {
        $statusIcon.prependTo(this.$statusContainer);
      }
      return $statusIcon;
    } else {
      return $sibling;
    }
  }

};

scout.SimpleTab.prototype.select = function() {
  this.selected = true;
  this._renderSelection();
};

scout.SimpleTab.prototype._renderSelection = function() {
  if (this.$container) {
    if (this.$container.select() === this.selected) {
      return;
    }
    this.$container.select(this.selected);
  }
};

scout.SimpleTab.prototype.deselect = function() {
  this.selected = false;
  this._renderSelection();
};

scout.SimpleTab.prototype._onMouseDown = function(event) {
  this.trigger('click');
};

scout.SimpleTab.prototype._onClose = function() {};

scout.SimpleTab.prototype.setTitle = function(title) {
  this.setProperty('title', title);
};

scout.SimpleTab.prototype._renderTitle = function() {
  this._$title.textOrNbsp(this.title);
};

scout.SimpleTab.prototype.setSubTitle = function(subTitle) {
  this.setProperty('subTitle', subTitle);
};

scout.SimpleTab.prototype._renderSubTitle = function() {
  this._$subTitle.textOrNbsp(this.subTitle);
};

scout.SimpleTab.prototype.setIconId = function(iconId) {
  this.setProperty('iconId', iconId);
};

scout.SimpleTab.prototype._renderIconId = function(iconId) {
  this.$icon.icon(this.iconId);
};

scout.SimpleTab.prototype._removeCssClass = function() {
  this.$container.removeClass(this.cssClass);
};

scout.SimpleTab.prototype.setCssClass = function(cssClass) {
  this.setProperty('cssClass', cssClass);
};

scout.SimpleTab.prototype._renderCssClass = function() {
  this.$container.addClass(this.cssClass);
};

scout.SimpleTab.prototype.getMenuText = function() {
  var text = this.title;
  if (this.subTitle) {
    text += ' (' + this.subTitle + ')';
  }
  return text;
};
