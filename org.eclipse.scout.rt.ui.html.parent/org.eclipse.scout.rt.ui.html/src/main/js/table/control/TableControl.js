scout.TableControl = function() {
  scout.TableControl.parent.call(this);
  this.table;
  this.form;
  this.$control;
  this._addAdapterProperties('form');
  this.contentRendered = false;
};
scout.inherits(scout.TableControl, scout.ModelAdapter);

scout.TableControl.prototype._render = function($parent) {
  var classes = 'control ';
  if (this.cssClass) {
    classes += this.cssClass;
  }

  this.$control = $parent.appendDiv('', classes)
    .data('control', this);
};

scout.TableControl.prototype._renderProperties = function() {
  scout.TableControl.parent.prototype._renderProperties.call(this);

  this._renderEnabled(this.enabled);
  this._renderSelected(this.selected);
};

scout.TableControl.prototype.remove = function() {
  this.removeContent();

  scout.TableControl.parent.prototype.remove.call(this);
};

scout.TableControl.prototype._renderContent = function($parent) {
  this.form.render($parent);
  this.form.$container.height(scout.TableFooter.CONTAINER_SIZE);
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
    this.contentRendered = false;
  }
};

scout.TableControl.prototype.renderContent = function() {
  if (this.contentRendered) {
    throw new Error('Already rendered');
  }

  if (!this.isContentAvailable()) {
    return;
  }

  //FIXME CGU opening should be controllable. Check current implementation of table page: is search form always opened automatically on activation?
  if (!this.tableFooter.open) {
    this.tableFooter.openTableControl();
  }

  this._renderContent(this.tableFooter.$controlContent);

  this.contentRendered = true;
};

scout.TableControl.prototype.onClosed = function() {
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

scout.TableControl.prototype.setSelected = function(selected) {
  this._renderSelected(selected);
  this.session.send('selected', this.id);
};

scout.TableControl.prototype._renderSelected = function(selected) {
  if (selected == this.$control.isSelected()) {
    return;
  }
  var previouslySelectedControl = this.tableFooter.selectedControl;
  if (selected) {
    this.tableFooter.selectedControl = this;

    if (!this.$control.isSelected()) {
      this.$control.select(true);
    }

    if (previouslySelectedControl) {
      previouslySelectedControl.setSelected(false);
    }

    this.renderContent();

  } else {
    this.$control.select(false);

    //When clicking on the already selected control, close the pane
    if (previouslySelectedControl === this) {
      //The control gets removed after the close operation
      this.tableFooter.closeTableControl(this);
      this.tableFooter.selectedControl = null;
    } else {
      this.removeContent();
    }
  }
};

scout.TableControl.prototype._renderEnabled = function(enabled) {
  var tooltip;

  if (enabled) {
    this.$control
      .removeClass('disabled')
      .hover(onControlHoverIn.bind(this), onControlHoverOut.bind(this))
      .click(onControlClicked.bind(this));
  } else {
    this.$control.addClass('disabled')
      .off('mouseenter mouseleave')
      .off('click');
  }

  function onControlHoverIn(event) {
    if (this.selected) {
      return;
    }

    tooltip = new scout.Tooltip({
      text: this.label,
      $origin: this.$control,
      arrowPosition: 50,
      arrowPositionUnit: '%'
    });
    tooltip.render();
  }

  function onControlHoverOut(event) {
    if (tooltip) {
      tooltip.remove();
      tooltip = null;
    }
  }

  function onControlClicked(event) {
    this.setSelected(!this.$control.isSelected());
  }
};

scout.TableControl.prototype.goOffline = function() {
  scout.TableControl.parent.prototype.goOffline.call(this);

  if (!this.isContentAvailable()) {
    this._renderEnabled(false);
  }
};

scout.TableControl.prototype.goOnline = function() {
  scout.TableControl.parent.prototype.goOnline.call(this);

  if (!this.isContentAvailable() && this.enabled) {
    this._renderEnabled(true);
  }
};


scout.TableControl.prototype.onResize = function() {
  if (this.form && this.form.rendered) {
    this.form.onResize();
  }
};
