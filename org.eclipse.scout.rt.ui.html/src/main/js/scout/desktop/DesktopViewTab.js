scout.DesktopViewTab = function(view, $bench, session) {
  scout.DesktopViewTab.parent.call(this);
  this.init(session);

  this._view = view;
  this._$bench = $bench;
  this._mouseListener;

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

  // FIXME AWE: problem ist, dass Widegt#remove prüft ob rendered ist
  // im bench mode ist der DesktopViewTab nicht gerendet, _remove wird
  // darum nicht aufgerufen und das 'remove event vom tab nie getriggert
  this._removeListener = this._onViewRemoved.bind(this);

  this.addChild(this._view);
  this._addEventSupport();
  this._installListeners();
};
scout.inherits(scout.DesktopViewTab, scout.Widget);

scout.DesktopViewTab.prototype._installListeners = function() {
  this._view.on('propertyChange', this._propertyChangeListener);
  this._view.on('remove', this._removeListener);
};

scout.DesktopViewTab.prototype._uninstallListeners = function() {
  this._view.off('propertyChange', this._propertyChangeListener);
  this._view.off('remove', this._removeListener);
};

scout.DesktopViewTab.prototype._render = function($parent) {
// FIXME NBU: animation causes selenium tests to fail - commented out until these problems have been solved.
  var parentTab = this.session.desktop.viewTabsController.viewTab(this._view.parent);
  if (parentTab) {
    this.$container = parentTab.$container.afterDiv('desktop-view-tab').addClass('hidden');
  } else {
    this.$container = $parent.prependDiv('desktop-view-tab').addClass('hidden');
  }
  this._mouseListener = this._onMouseDown.bind(this);
  this.$container.on('mousedown', this._mouseListener);
  var $wrapper = this.$container.wrapAll('<div class="animationWrapper">').parent().copyCss(this.$container, 'display border-right');
  var w = $wrapper.width();
  var removeContainer = function() {
    $(this).replaceWith($(this).contents());
  };
  $wrapper.css('width', 0)
    .animateAVCSD('width', w, removeContainer, false, 750);
  this._$title = this.$container.appendDiv('title');
  this._$subTitle = this.$container.appendDiv('sub-title');
  this._titlesUpdated();
  this._cssClassUpdated(this._view.cssClass, null);
};

scout.DesktopViewTab.prototype._remove = function() {
  this.$container.off('mousedown', this._mouseListener);
  var removeContainer = function() {
    this.$container.remove();
    this.$container = null;
  }.bind(this);
  this.$container
  .animateAVCSD('width', 0, removeContainer, false, 200);
};

scout.DesktopViewTab.prototype._renderView = function($parent) {
  if (this._view.rendered) {
    throw new Error('view already rendered');
  }
  this._view.render(this._$bench);
  this._view.htmlComp.validateLayout();
  this._view.htmlComp.validateRoot = true;
  this._view.rendered = true;
};

scout.DesktopViewTab.prototype.select = function() {
  this._cssSelect(true);
  if (this._view.rendered) {
    this._view.attach();
  } else {
    this._renderView();
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
    if (title) {
      $titleElement.text(title).setVisible(true);
    } else {
      $titleElement.setVisible(false);
    }
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

scout.DesktopViewTab.prototype.onResize = function() {
  this._view.onResize();
};

scout.DesktopViewTab.prototype.getMenuText = function() {
  var text = this._view.title;
  if (this._view.subTitle) {
    text += ' (' + this._view.subTitle + ')';
  }
  return text;
};
