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

  this._addEventSupport();
};
scout.inherits(scout.SimpleTab, scout.Widget);

scout.SimpleTab.prototype._init = function(options) {
  scout.SimpleTab.parent.prototype._init.call(this, options);
  this.title = options.title;
  this.subTitle = options.subTitle;
  this.iconId = options.iconId;
  this.saveNeeded = options.saveNeeded;
  this.saveNeededVisible = options.saveNeededVisible;
  this.closable = options.closable;
  this.status = options.status;
  this.selected = false;
};

scout.SimpleTab.prototype.renderAfter = function($parent, sibling) {
  this.render($parent);
  if (sibling) {
    this.$container.insertAfter(sibling.$container);
  }
};

scout.SimpleTab.prototype._render = function($parent) {
  this.$container = $parent.prependDiv('simple-tab');
  this.$statusContainer = this.$container.appendDiv('status-container');

  this._mouseListener = this._onMouseDown.bind(this);
  this.$container.on('mousedown', this._mouseListener);
  this._$title = this.$container.appendDiv('title');
  this._$subTitle = this.$container.appendDiv('sub-title');

  this._titlesUpdated();
  this._renderSelection();
  this._renderClosable();
  this._renderSaveNeeded();
  this._renderIconId();
  this._renderStatus();
  this._cssClassUpdated(this.view.cssClass, null);
};

scout.SimpleTab.prototype.setClosable = function(closable) {

  if (this.closable === closable) {
    return;
  }
  this.closable = closable;
  if (this.rendered) {
    this._renderClosable();
  }
};

scout.SimpleTab.prototype._renderClosable = function() {
  this.$container.toggleClass('closable');
  if (this.closable) {
    if (this.$close) {
      return;
    }
    this.$close = this.$statusContainer.appendDiv('status closer')
      .on('click', this._onClose.bind(this));
  } else {
    if (!this.$close) {
      return;
    }
    this.$close.remove();
    this.$close = null;
  }
};

scout.SimpleTab.prototype.setSaveNeededVisible = function(saveNeededVisible) {
  if (this.saveNeededVisible === saveNeededVisible) {
    return;
  }
  this.saveNeededVisible = saveNeededVisible;
  if (this.rendered) {
    this._renderSaveNeeded();
  }
};

scout.SimpleTab.prototype.setSaveNeeded = function(saveNeeded) {
  if (this.saveNeeded === saveNeeded) {
    return;
  }
  this.saveNeeded = saveNeeded;
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
  if (this.status === status) {
    return;
  }
  this.status = status;
  if (this.rendered) {
    this._renderStatus();
  }
};

scout.SimpleTab.prototype._renderStatus = function() {
  this.$status.forEach(function($sts) {
    $sts.remove();
  }.bind(this));
  this.$status = [];
  var flatenStatus = scout.Form.flatenStatus(this.status);
  if (flatenStatus) {
    var $sibling = this.$icon;
    flatenStatus.forEach(function(sts) {
      $sibling = this._renderSingleStatus(sts, $sibling);
      if ($sibling) {
        this.$status.push($sibling);
      }

    }.bind(this));
  }

};

scout.SimpleTab.prototype._renderSingleStatus = function(status, $sibling) {
  if (status && status.iconId) {
    var $statusIcon = this.$statusContainer.appendIcon(status.iconId, 'status');
    if ($sibling) {
      $sibling.after($statusIcon);
      //      $statusIcon.after($sibling);
    } else {
      $statusIcon.prependTo(this.$statusContainer);
    }
    return $statusIcon;
  } else {
    return $sibling;
  }
};

scout.SimpleTab.prototype._renderSelection = function() {
  if (this.$container) {
    if (this.$container.select() === this.selected) {
      return;
    }
    this.$container.select(this.selected);
  }
};

scout.SimpleTab.prototype.select = function() {
  this.selected = true;
  this._renderSelection();
};

scout.SimpleTab.prototype.deselect = function() {
  this.selected = false;
  this._renderSelection();
};

scout.SimpleTab.prototype._onMouseDown = function(event) {
  this.trigger('tabClicked');
};

scout.SimpleTab.prototype._onClose = function() {};

scout.SimpleTab.prototype.setTitle = function(title) {
  if (this.title === title) {
    return;
  }
  this.title = title;
  this._titlesUpdated();
};

scout.SimpleTab.prototype.setSubTitle = function(subTitle) {
  if (this.subTitle === subTitle) {
    return;
  }
  this.subTitle = subTitle;
  this._titlesUpdated();
};

scout.SimpleTab.prototype.setIconId = function(iconId) {
  if (this.iconId === iconId) {
    return;
  }
  this.iconId = iconId;
  this._renderIconId();
};

scout.SimpleTab.prototype._renderIconId = function(iconId) {
  if (this.iconId) {
    if (this.$icon) {
      return;
    }
    this.$icon = this.$statusContainer.appendIcon(this.iconId, 'status');
    this.$icon.prependTo(this.$statusContainer);
  } else {
    if (!this.$icon) {
      return;
    }
    this.$icon.remove();
    this.$icon = null;
  }
};

scout.SimpleTab.prototype._titlesUpdated = function() {
  if (!this.$container) {
    return;
  }
  this._$title.textOrNbsp(this.title);
  this._$subTitle.textOrNbsp(this.subTitle);
};

scout.SimpleTab.prototype.getMenuText = function() {
  var text = this.title;
  if (this.subTitle) {
    text += ' (' + this.subTitle + ')';
  }
  return text;
};
