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

scout.TableControl.prototype.onModelPropertyChange = function(event) {
  // FIXME AWE: das hier muss dann mit der lösung im ModelAdapter gemacht werden!
  // dann kann das entfernt werden. Problem ist, dass die property (JSON) zuerst noch
  // in einen adapter konvertiert werden muss. Ohne den Hack würde die property auf
  // dem Adapter fälschlicherweise durch das JSON ersetzt.
  // [HACK]
  var hasForm = false;
  if (event.hasOwnProperty('form')) {
    this.form = this.session.getOrCreateModelAdapter(event.form, this);
    delete event.form;
    hasForm = true;
  }

  scout.TableControl.parent.prototype.onModelPropertyChange.call(this, event);

  if (hasForm) {
    this._setForm(this.form);
  }
  // [/HACK]
};
