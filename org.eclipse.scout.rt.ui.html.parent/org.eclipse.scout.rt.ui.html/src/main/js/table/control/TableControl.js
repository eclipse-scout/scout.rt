scout.TableControl = function() {
  scout.TableControl.parent.call(this);

  this.table;
  this.form;
};
scout.inherits(scout.TableControl, scout.ModelAdapter);

scout.TableControl.prototype.init = function(model, session) {
  scout.TableControl.parent.prototype.init.call(this, model, session);

  this.form = this.session.getOrCreateModelAdapter(model.form, this);
  this.$controlButton;
};

scout.TableControl.prototype._render = function($parent) {
  this.form.render($parent);

  //Set container to make it removed by ModelAdapter.remove()
  this.$container = this.form.$container;
};

/**
 * api for table footer
 */
scout.TableControl.prototype.toggle = function() {
  if (!this.model.selected) {
    if (!this.$controlButton.hasClass('selected')) {
      this.$controlButton.addClass('selected');
    }
  } else {
    if (this.table.footer.selectedControl !== this) {
      this.remove();
    }
    this.$controlButton.removeClass('selected');
  }

  this.session.send('selected', this.model.id);
};

scout.TableControl.prototype._setForm = function(form) {
  this.model.form = form;

  this.form = this.session.getOrCreateModelAdapter(form, this);
};

scout.TableControl.prototype._setSelected = function(selected) {
  this.model.selected = selected;

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
  this.model.enabled = enabled;

  this.table.footer.setControlEnabled(this);
};

scout.TableControl.prototype._onModelPropertyChange = function(event) {
  if (event.hasOwnProperty('form')) {
    this._setForm(event.form);
  }
  if (event.hasOwnProperty('selected')) {
    this._setSelected(event.selected);
  }
  if (event.hasOwnProperty('enabled')) {
    this._setEnabled(event.enabled);
  }
};
