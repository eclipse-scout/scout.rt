scout.TableControl = function() {
  scout.TableControl.parent.call(this);
  this._addAdapterProperties('form');
  this.table;
  this.form;
  this.$controlButton;
};

scout.inherits(scout.TableControl, scout.ModelAdapter);

scout.TableControl.prototype._render = function($parent) {
  this.form.render($parent);
  // Set container to make it removed by ModelAdapter.remove()
  this.$container = this.form.$container;
};

/**
 * api for table footer
 */
scout.TableControl.prototype.toggle = function() {
  if (!this.selected) {
    if (!this.$controlButton.hasClass('selected')) {
      this.$controlButton.addClass('selected');
    }
  } else {
    if (this.table.footer.selectedControl !== this) {
      this.remove();
    }
    this.$controlButton.removeClass('selected');
  }

  this.session.send('selected', this.id);
};

scout.TableControl.prototype._setForm = function(form) {
  this.form = this.session.getOrCreateModelAdapter(form, this);
};

scout.TableControl.prototype._setSelected = function(selected) {
  var previouslySelectedControl = this.table.footer.selectedControl;
  if (selected) {
    this.table.footer.selectedControl = this;

    if (!this.$controlButton.hasClass('selected')) {
      this.$controlButton.addClass('selected');
    }

    // if no control is selected, the control pane is closed -> open it
    if (!previouslySelectedControl) {
      this.table.footer.openTableControl();
    } else {
      //Deselect the previously selected control
      previouslySelectedControl.toggle();
    }

    this.render(this.table.footer.$controlContainer);

  } else {
    this.$controlButton.removeClass('selected');

    //When clicking on the already selected control, close the pane
    if (previouslySelectedControl === this) {
      //The control gets removed after the close operation
      this.table.footer.closeTableControl(this);
      this.table.footer.selectedControl = null;
    }
    else {
      this.remove();
    }
  }
};

scout.TableControl.prototype._setEnabled = function(enabled) {
  this.table.footer.setControlEnabled(this);
};
