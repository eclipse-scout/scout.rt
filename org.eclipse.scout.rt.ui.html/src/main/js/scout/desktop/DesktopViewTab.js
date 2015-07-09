scout.DesktopViewTab = function(view, $bench) {
  scout.DesktopViewTab.parent.call(this);

  this._view = view;
  this._$bench = $bench;

  /**
   * Container for the _Tab_ (not for the view).
   */
  this.$container;
  this.$viewContainer;
  this._viewAttached = false;

  /**
   * This property stores the detached DOM, when the tab is not active (=not visible).
   */
  this._$detachedDom;

  this._propertyChangeListener = function(event) {
    if (scout.helpers.isOneOf(event.changedProperties, 'title', 'subTitle')) {
      this._titlesUpdated();
    }
  }.bind(this);

  // XXX AWE: problem ist, dass Widegt#remove prüft ob rendered ist
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
  this.$container = $parent.appendDiv('desktop-view-tab')
    .on('mousedown', this._onMouseDown.bind(this));
  this._$title = this.$container.appendDiv('title').text(this._view.title);
  this._$subTitle = this.$container.appendDiv('sub-title').text(this._view.subTitle);
};

scout.DesktopViewTab.prototype._renderView = function($parent) {
  if (this._view.rendered) {
    throw new Error('view already rendered');
  }
  this._view.render(this._$bench);
  this._view.htmlComp.validateLayout();
  this._view.htmlComp.validateRoot = true;
  this._view.rendered = true;
  this._viewAttached = true;
};

scout.DesktopViewTab.prototype.select = function() {
  this._cssSelect(true);
  if (!this._view.rendered) {
    this._renderView();
  }
  if (!this._viewAttached) {
    var $viewContainer = this._view.$container;
    this._$bench.append($viewContainer);
    this._afterAttach();

    // If the parent has been resized while the content was not visible, the content has the wrong size -> update
    var htmlComp = scout.HtmlComponent.get($viewContainer);
    var htmlParent = htmlComp.getParent();
    htmlComp.setSize(htmlParent.getSize());
    this._viewAttached = true;
  }
};

scout.DesktopViewTab.prototype._cssSelect = function(selected) {
  if (this.$container) {
    this.$container.select(selected);
  }
};

scout.DesktopViewTab.prototype.deselect = function() {
  if (this._view.rendered) {
    var $viewContainer = this._view.$container;
    this._beforeDetach();
    $viewContainer.detach();
    this._viewAttached = false;
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

  setTitle(this._$title, this._view.title);
  setTitle(this._$subTitle, this._view.subTitle);

  function setTitle($titleElement, title) {
    if (title) {
      $titleElement.text(title).setVisible(true);
    } else {
      $titleElement.setVisible(false);
    }
  }
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

/**
 * Method invoked just after attaching the view's content to the DOM.
 */
scout.DesktopViewTab.prototype._afterAttach = function() {
  // Delegate to DetachHelper.
  this._view.session.detachHelper.afterAttach(this._view.$container);

  // Restore keystrokes.
  if (this._view.keyStrokeAdapter) {
    scout.keyStrokeManager.installAdapter(this._view.$container, this._view.keyStrokeAdapter);
  }

  // Restore dialogs and message boxes, not views.
  this._view._formController.renderDialogs();
  this._view._messageBoxController.render();
  this._view._fileChooserController.render();
};

/**
 * Method invoked just before detaching the view's content from DOM.
 */
scout.DesktopViewTab.prototype._beforeDetach = function() {
  // Uninstall keystrokes.
  if (scout.keyStrokeManager.isAdapterInstalled(this._view.keyStrokeAdapter)) {
    scout.keyStrokeManager.uninstallAdapter(this._view.keyStrokeAdapter);
  }

  // Remove dialogs and message boxes, not views.
  this._view._formController.removeDialogs();
  this._view._messageBoxController.remove();
  this._view._fileChooserController.remove();

  // Delegate to DetachHelper.
  this._view.session.detachHelper.beforeDetach(this._view.$container);
};
