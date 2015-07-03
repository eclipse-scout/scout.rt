scout.TableControl = function() {
  scout.TableControl.parent.call(this);
  this.tableFooter;
  this.form;
  this._addAdapterProperties('form');
  this.contentRendered = false;
  this.tableControlKeyStrokeAdapter;
};
scout.inherits(scout.TableControl, scout.Action);

scout.TableControl.prototype.init = function(model, session) {
  scout.TableControl.parent.prototype.init.call(this, model, session);
  this._syncForm(this.form);
};

scout.TableControl.prototype._render = function($parent) {
  var classes = 'control ';
  if (this.cssClass) {
    classes += this.cssClass;
  }
  this.$container = $parent.appendDiv(classes)
    .data('control', this);
};

scout.TableControl.prototype.remove = function() {
  this.removeContent();
  scout.TableControl.parent.prototype.remove.call(this);
};

scout.TableControl.prototype._renderContent = function($parent) {
  this.form.render($parent);

  // Tab box gets a special style if it is the first field in the root group box
  var rootGroupBox = this.form.rootGroupBox;
  if (rootGroupBox.fields[0] instanceof scout.TabBox) {
    rootGroupBox.fields[0].$container.addClass('in-table-control');
  }

  this.form.$container.height($parent.height());
  this.form.$container.width($parent.width());
  //FIXME CGU make this more easy to use
  this.form.htmlComp.pixelBasedSizing = true;
  this.form.htmlComp.validateRoot = true;
  this.form.htmlComp.revalidateLayout();
};

scout.TableControl.prototype._removeContent = function() {
  this.form.remove();
};

scout.TableControl.prototype.removeContent = function() {
  if (this.contentRendered) {
    this._removeContent();
    if (this.cssClass) {
      this.tableFooter.$controlContent.removeClass('control-content-' + this.cssClass);
    }
    this.contentRendered = false;
  }
};

/**
 * Renders the content if not already rendered.<br>
 * Opens the container if the container is not already open.<br>
 * Does nothing if the content is not available yet to -> don't open container if content is not rendered yet to prevent blank container or laggy opening.
 */
scout.TableControl.prototype.renderContent = function(animated) {
  if (!this.contentRendered && !this.isContentAvailable()) {
    return;
  }

  if (!this.tableFooter.open) {
    this.tableFooter.openControlContainer(animated);
  }

  if (!this.contentRendered) {
    if (this.cssClass) {
      this.tableFooter.$controlContent.addClass('control-content-' + this.cssClass);
    }
    this._renderContent(this.tableFooter.$controlContent);
    this.contentRendered = true;
    this.tableControlKeyStrokeAdapter = new scout.TableControlKeyStrokeAdapter(this);
    scout.keyStrokeManager.installAdapter(this.tableFooter.$controlContent, this.tableControlKeyStrokeAdapter);
    this.tableFooter.$controlContainer.installFocusContextAsync('auto', this.tableFooter._table.session.uiSessionId);
  }
};

scout.TableControl.prototype.onControlContainerClosed = function() {
  setTimeout(function() {
    this.tableFooter.$controlContainer.uninstallFocusContext(this.tableFooter._table.session.uiSessionId);
  }.bind(this));
  this.removeContent();
};

scout.TableControl.prototype._removeForm = function() {
  this.removeContent();
};

scout.TableControl.prototype._syncForm = function(form) {
  if (form) {
    form.rootGroupBox.menuBar.bottom();
  }
  this.form = form;
};

scout.TableControl.prototype._renderForm = function(form) {
  this.renderContent(false);
};

/**
 * Returns true if the table control may be displayed (opened).
 */
scout.TableControl.prototype.isContentAvailable = function() {
  return !!this.form;
};

scout.TableControl.prototype._onMouseDown = function() {
  this.toggle();
};

scout.TableControl.prototype.toggle = function() {
  if (this.tableFooter.selectedControl === this) {
    this.setSelected(false, true, true);
  } else {
    this.setSelected(true, true, true);
  }
};

scout.TableControl.prototype.setSelected = function(selected, closeWhenUnselected, animated) {
  if (selected === this.selected) {
    return;
  }

  if (this.tableFooter.selectedControl && this.tableFooter.selectedControl !== this) {
    this.tableFooter.selectedControl.setSelected(false, false, animated);
  }

  this.selected = selected;
  this.sendSelected(selected);
  this._renderSelected(this.selected, closeWhenUnselected, animated);
  var that = this;
  if (!selected) {
    scout.keyStrokeManager.uninstallAdapter(this.tableControlKeyStrokeAdapter);

  }
};

scout.TableControl.prototype._renderSelected = function(selected, closeWhenUnselected, animated) {
  closeWhenUnselected = closeWhenUnselected !== undefined ? closeWhenUnselected : true;

  this.$container.select(selected);

  if (selected) {
    this.renderContent(animated);
    this.tableFooter.selectedControl = this;
  } else {

    // Don't modify the state initially, only on property change events
    if (this.rendered) {

      if (closeWhenUnselected && this === this.tableFooter.selectedControl) {
        // Don't remove immediately, wait for the animation to finish (handled by onControlContainerClosed)
        this.tableFooter.closeControlContainer(this);
        this.tableFooter.selectedControl = null;
      } else {
        setTimeout(function() {
          this.tableFooter.$controlContainer.uninstallFocusContext(this.tableFooter._table.session.uiSessionId);
        }.bind(this));
        this.removeContent();
      }

    }
  }
};

scout.TableControl.prototype._renderEnabled = function(enabled) {
  if (enabled) {
    this.$container.on('mousedown', '', this._onMouseDown.bind(this));
  } else {
    this.$container.off('mousedown');
  }
  this.$container.setEnabled(enabled);
};

scout.TableControl.prototype._configureTooltip = function() {
  var options = scout.TableControl.parent.prototype._configureTooltip.call(this);
  options.cssClass = 'table-control';
  return options;
};

scout.TableControl.prototype._goOffline = function() {
  if (!this.isContentAvailable()) {
    this._renderEnabled(false);
  }
};

scout.TableControl.prototype._goOnline = function() {
  if (!this.isContentAvailable() && this.enabled) {
    this._renderEnabled(true);
  }
};

scout.TableControl.prototype.onResize = function() {
  if (this.form && this.form.rendered) {
    this.form.onResize();
  }
};

scout.TableControl.prototype._drawKeyBox = function($container) {
  if (this.rendered) {

    if (!this.drawHint || !this.keyStroke) {
      return;
    }
    var keyBoxText = scout.codesToKeys[this.keyStrokeKeyPart];
    scout.keyStrokeBox.drawSingleKeyBoxItem(6, keyBoxText, this.$container, this.ctrl, this.alt, this.shift);

  }
};

scout.TableControl.prototype.handle = function(event) {
  this.toggle();
  if (this.preventDefaultOnEvent) {
    event.preventDefault();
  }
};
