scout.TableControl = function() {
  scout.TableControl.parent.call(this);
  this.tableFooter;
  this.form;
  this._addAdapterProperties('form');
  this.contentRendered = false;
};
scout.inherits(scout.TableControl, scout.Action);

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
  this.form.menubarPosition = 'bottom';
  this.form.render($parent);
  this.form.$container.height($parent.height());
  this.form.$container.width($parent.width());
  //FIXME CGU make this more easy to use
  this.form.htmlComp.pixelBasedSizing = true;
  this.form.htmlComp.validateRoot = true;
  this.form.htmlComp.invalidate();
  this.form.htmlComp.layout();
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
scout.TableControl.prototype.renderContent = function() {
  if (!this.contentRendered && !this.isContentAvailable()) {
    return;
  }

  if (!this.tableFooter.open) {
    this.tableFooter.openControlContainer();
  }

  if (!this.contentRendered) {
    if (this.cssClass) {
      this.tableFooter.$controlContent.addClass('control-content-' + this.cssClass);
    }
    this._renderContent(this.tableFooter.$controlContent);
    this.contentRendered = true;
  }
};

scout.TableControl.prototype.onControlContainerClosed = function() {
  this.removeContent();
};

scout.TableControl.prototype._removeForm = function() {
  this.removeContent();
};

scout.TableControl.prototype._renderForm = function(form) {
  this.renderContent();
};

/**
 * Returns true if the table control may be displayed (opened).
 */
scout.TableControl.prototype.isContentAvailable = function() {
  return this.form;
};

scout.TableControl.prototype._onMouseDown = function() {
  this.toggle();
};

scout.TableControl.prototype.toggle = function() {
  if (this.tableFooter.selectedControl === this) {
    this.setSelected(false);
  } else {
    this.setSelected(true);
  }
};

scout.TableControl.prototype.setSelected = function(selected, closeWhenUnselected) {
  if (selected === this.selected) {
    return;
  }

  if (this.tableFooter.selectedControl && this.tableFooter.selectedControl !== this) {
    this.tableFooter.selectedControl.setSelected(false, false);
  }

  this.selected = selected;
  this.sendSelected(selected);
  this._renderSelected(this.selected, closeWhenUnselected);
};

scout.TableControl.prototype._renderSelected = function(selected, closeWhenUnselected) {
  closeWhenUnselected = closeWhenUnselected !== undefined ? closeWhenUnselected : true;

  this.$container.select(selected);

  if (selected) {
    this.renderContent();
    this.tableFooter.selectedControl = this;
  } else {

    //Don't modify the state initially
    if (this.rendered) {

      if (closeWhenUnselected) {
        //Don't remove immediately, wait for the animation to finish (handled by onControlContainerClosed)
        this.tableFooter.closeControlContainer(this);
      } else {
        this.removeContent();
      }

      this.tableFooter.selectedControl = null;
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
