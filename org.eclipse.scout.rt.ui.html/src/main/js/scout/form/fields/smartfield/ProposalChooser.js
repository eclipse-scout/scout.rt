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

  this._$status = this.$container.appendDiv('status');

  // support for activeFilter
  if (this.activeFilter) {
    this._$activeFilter = this.$container.appendDiv('active-filter');
    this._appendOption(this._$activeFilter, 'UNDEFINED', 'Alle');
    this._appendOption(this._$activeFilter, 'TRUE', 'Aktive');
    this._appendOption(this._$activeFilter, 'FALSE', 'Inaktive');
  }
};

scout.ProposalChooser.prototype._renderProperties = function() {
  scout.ProposalChooser.parent.prototype._renderProperties.call(this);
  this._updateStatus();
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
};

scout.ProposalChooser.prototype._renderStatusVisible = function() {
  this._updateStatus();
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

scout.ProposalChooser.prototype._appendOption = function($parent, value, text) {
  var $radio = $parent.makeElement('<input>')
    .attr('type', 'radio')
    .attr('name', 'activeState')
    .attr('value', value)
    .change(this._onActiveFilterChanged.bind(this));
  if (this.activeFilter === value) {
    $radio.attr('checked', 'checked');
  }
  $parent
    .append($radio)
    .appendElement('<label>').text(text);
};

scout.ProposalChooser.prototype._onActiveFilterChanged = function(event) {
  var value = $(event.target).val();
  this._send('activeFilterChanged', {
    state: value
  });
};
