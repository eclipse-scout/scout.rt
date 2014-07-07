scout.TableControl = function() {
  scout.TableControl.parent.call(this);
  this.table;
  this.form;
  this.$controlButton;
  this._addAdapterProperties('form');
};

scout.inherits(scout.TableControl, scout.ModelAdapter);

/**
 * This function actually renders the content of the control, not the control (icon) itself.
 */
scout.TableControl.prototype._render = function($parent) {
  this.form.render($parent);
  //Set container to make it removed by ModelAdapter.remove()
  this.$container = this.form.$container;
};

/**
 * api for table footer
 */
scout.TableControl.prototype.toggle = function() {
  this._setSelected(!this.$controlButton.isSelected());
};

scout.TableControl.prototype._setForm = function(form) {
  this.renderContent();
};

/**
 * Returns true if the table control may be displayed (opened).
 */
scout.TableControl.prototype.isContentAvailable = function() {
  return this.form;
};

scout.TableControl.prototype.renderContent = function(form) {
  if (!this.isContentAvailable() || !this.table.isRendered()) {
    return;
  }

  if (!this.isRendered()) {
    this.render(this.table.footer.$controlContainer);
  }

  //FIXME CGU opening should be controllable. Check current implementation of table page: is search form always opened automatically on activation?
  if (!this.table.footer.open) {
    this.table.footer.openTableControl();
  }
};

scout.TableControl.prototype._setSelected = function(selected) {
  if (selected == this.$controlButton.isSelected()) {
    return;
  }

  var previouslySelectedControl = this.table.footer.selectedControl;
  if (selected) {
    this.table.footer.selectedControl = this;

    if (!this.$controlButton.isSelected()) {
      this.$controlButton.select(true);
    }

    if (previouslySelectedControl) {
      previouslySelectedControl._setSelected(false);
    }

    this.renderContent();

  } else {
    this.$controlButton.select(false);

    //When clicking on the already selected control, close the pane
    if (previouslySelectedControl === this) {
      //The control gets removed after the close operation
      this.table.footer.closeTableControl(this);
      this.table.footer.selectedControl = null;
    } else {
      this.remove();
    }
  }

  if (!this.session.processingEvents) {
    this.session.send('selected', this.id);
  }
};

scout.TableControl.prototype._setEnabled = function(enabled) {
  if (!this.table.isRendered()) {
    return;
  }

  var $control = this.$controlButton,
    that = this;

  if (enabled) {
    $control.data('label', that.label)
      .removeClass('disabled')
      .hover(onControlHoverIn, onControlHoverOut)
      .click(onControlClicked);
  } else {
    $control.addClass('disabled')
      .off('mouseenter mouseleave')
      .off('click');
  }

  function onControlHoverIn(event) {
    that.table.footer._updateControlLabel($(event.target));
  }

  function onControlHoverOut(event) {
    that.table.footer._resetControlLabel($(event.target));
  }

  function onControlClicked(event) {
    that.toggle();
  }
};

scout.TableControl.prototype.goOffline = function() {
  scout.TableControl.parent.prototype.goOffline.call(this);

  if (!this.isContentAvailable()) {
    this._setEnabled(false);
  }
};

scout.TableControl.prototype.goOnline = function() {
  scout.TableControl.parent.prototype.goOnline.call(this);

  if (!this.isContentAvailable() && this.enabled) {
    this._setEnabled(true);
  }
};
