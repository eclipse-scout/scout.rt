/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.ProposalChooser = function() {
  scout.ProposalChooser.parent.call(this);
  this._addAdapterProperties(['model']);
  this.$container;
  this._$status;
  this._$activeFilter;
  this.htmlComp;
};
scout.inherits(scout.ProposalChooser, scout.ModelAdapter);

scout.ProposalChooser.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('proposal-chooser');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.ProposalChooserLayout(this));
  this.model.render(this.$container);
  if (this.model instanceof scout.Tree) {
    //disable focus on field container
    this.model._onNodeControlMouseDownDoFocus = function() {};
  }

  this._$status = this.$container.appendDiv('status');

  // support for activeFilter
  if (this.activeFilter) {
    this._$activeFilter = this.$container.appendDiv('active-filter');
    var group = scout.create('RadioButtonGroup', {
      parent: this
    });

    this._appendOption(group, 'UNDEFINED', this.session.text('ui.All'), true);
    this._appendOption(group, 'TRUE', this.session.text('ui.Active'), false);
    this._appendOption(group, 'FALSE', this.session.text('ui.Inactive'), false);

    group.render(this._$activeFilter);
  }
};

scout.ProposalChooser.prototype._renderProperties = function() {
  scout.ProposalChooser.parent.prototype._renderProperties.call(this);
  this._updateStatus();
  this.htmlComp.revalidateLayout();
};

/**
 * Delegates an event (e.g. keyup, keydown) to the model.$container of this instance,
 * calling the JQuery trigger method.
 */
scout.ProposalChooser.prototype.delegateEvent = function(event) {
  event.originalEvent.smartFieldEvent = true;
  this.model.$container.trigger(event);
};

scout.ProposalChooser.prototype._renderStatus = function() {
  this._updateStatus();
  this.htmlComp.revalidateLayout();
};

scout.ProposalChooser.prototype._renderStatusVisible = function() {
  this._updateStatus();
  this.htmlComp.revalidateLayout();
};

scout.ProposalChooser.prototype._updateStatus = function() {
  $.log.debug('_updateStatus status=' + this.status + ' statusVisible=' + this.statusVisible);
  this._$status.setVisible(this.statusVisible && this.status);
  if (this.status) {
    this._setStatusMessage(this.status.message);
  } else {
    this._$status.text('');
  }
};

/**
 * Replaces an ellipsis (...) at the end of the message-text with a CSS animation.
 */
scout.ProposalChooser.prototype._setStatusMessage = function(message) {
  scout.Status.animateStatusMessage(this._$status, message);
};

scout.ProposalChooser.prototype._appendOption = function(group, value, text, selected) {
  var radio = scout.create('RadioButton', {
      parent: group,
      label: text,
      radioValue: value,
      selected: selected
    }),
    that = this;
  radio._mouseDown = function(event) {
    this.select();
  };
  radio._send = function() {
    that._onActiveFilterChanged(this.radioValue);
  };
  group.formFields.push(radio);
};

scout.ProposalChooser.prototype._onActiveFilterChanged = function(radioValue) {
  this._send('activeFilterChanged', {
    state: radioValue
  });
};
