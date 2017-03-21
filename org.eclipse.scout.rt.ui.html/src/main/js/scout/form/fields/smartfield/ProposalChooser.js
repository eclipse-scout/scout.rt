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
  this.$status;
  this.$activeFilter;
  this.htmlComp;

  this._updateStatusTimeout;
};
scout.inherits(scout.ProposalChooser, scout.Widget);

/**
 * @see IContentAssistField#getActiveFilterLabels() - should have the same order.
 */
scout.ProposalChooser.ACTIVE_FILTER_VALUES = ['UNDEFINED', 'FALSE', 'TRUE'];

scout.ProposalChooser.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('proposal-chooser');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.ProposalChooserLayout(this));
  this.model.render(this.$container);
  if (this.model instanceof scout.Tree) {
    // disable focus on field container
    this.model._onNodeControlMouseDownDoFocus = function() {};
  }

  // status
  this.$status = this.$container
    .appendDiv('status')
    .setVisible(false);

  // active filter
  if (this.activeFilter) {
    this.activeFilterGroup = scout.create('RadioButtonGroup', {
      parent: this,
      labelVisible: false,
      statusVisible: false
    });

    // add radio buttons
    scout.ProposalChooser.ACTIVE_FILTER_VALUES.forEach(function(value, index) {
      this._renderButton(value, index);
    }, this);

    this.activeFilterGroup.render(this.$container);
    this.activeFilterGroup.$container.addClass('active-filter');
    this.activeFilterGroup.removeMandatoryIndicator();
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
};

scout.ProposalChooser.prototype._renderStatusVisible = function() {
  this._updateStatus();
};

scout.ProposalChooser.prototype._computeStatusVisible = function() {
  return !!(this.statusVisible && this.status);
};

scout.ProposalChooser.prototype._updateStatus = function() {
  // Note: the UI has a special way to deal with the status. When the UI is rendered
  // we do NOT render an OK status, even when it is set on the model. The status
  // "Search proposals..." is set to severity OK. That status is only displayed, when
  // it is still there after 250 ms. Usually a smart-field lookup is fast, so the user
  // never sees the status message. However: it would be better if the status on the
  // (Java-)model would implement the behavior described above, but
  // this would require a timer thread, so it is easier to implement that in the UI.
  // Status with other severities than OK are displayed immediately.
  clearTimeout(this._updateStatusTimeout);
  if (scout.objects.optProperty(this.status, 'severity') === scout.Status.Severity.OK) {
    // compute statusVisible 250 ms later (status can change in the meantime)
    this._updateStatusTimeout = setTimeout(
        this._updateStatusImpl.bind(this), 250);
  } else {
    this._updateStatusImpl();
  }
};

scout.ProposalChooser.prototype._updateStatusImpl = function() {
  var
    oldVisible = this.$status.isVisible(),
    oldMessage = this.$status.text(),
    visible = this._computeStatusVisible();

  if (oldVisible === visible &&
      oldMessage === scout.objects.optProperty(this.status, 'message')) {
    return;
  }

  $.log.debug('_updateStatus statusVisible=' + visible);
  this.$status.setVisible(visible);
  if (this.status) {
    this._setStatusMessage(this.status.message);
  } else {
    this.$status.text('');
  }
  this.htmlComp.invalidateLayoutTree();
};

/**
 * Replaces an ellipsis (...) at the end of the message-text with a CSS animation.
 */
scout.ProposalChooser.prototype._setStatusMessage = function(message) {
  scout.Status.animateStatusMessage(this.$status, message);
};

scout.ProposalChooser.prototype._renderButton = function(value, index) {
  var radio = scout.create('RadioButton', {
      parent: this.activeFilterGroup,
      label: this.activeFilterLabels[index],
      radioValue: scout.ProposalChooser.ACTIVE_FILTER_VALUES[index],
      selected: this.activeFilter === value,
      focusWhenSelected: false,
      gridData: {
        x: index,
        y: 1,
        useUiWidth: true
      }
    });

  radio.on('propertyChange', function(event) {
    if (event.changedProperties.indexOf('selected') !== -1 && event.newProperties.selected === true) {
      this._onActiveFilterChanged(event.source.radioValue);
    }
  }.bind(this));

  this.activeFilterGroup.addButton(radio);
};

scout.ProposalChooser.prototype._onActiveFilterChanged = function(radioValue) {
  this._send('activeFilterChanged', {
    state: radioValue
  });
};

scout.ProposalChooser.prototype.setVirtual = function(virtual) {
  if (this.model instanceof scout.Table) {
    this.model.setVirtual(virtual);
  }
};

scout.ProposalChooser.prototype.setBusy = function(busy) {
  this.model.setProperty('loading', busy);
  this.model.setProperty('enabled', !busy);
};
