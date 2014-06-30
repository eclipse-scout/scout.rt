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
  if (!this.isContentAvailable()) {
    return;
  }

  if (!this.isRendered()) {
    this.render(this.table.footer.$controlContainer);
  }

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
  this.table.footer.setControlEnabled(this);
};
