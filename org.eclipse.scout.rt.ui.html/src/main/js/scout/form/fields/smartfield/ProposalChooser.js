/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
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

  this.$container = null;
  this.$status = null;
  this.activeFilterGroup = null;
  this.htmlComp = null;
  this._updateStatusTimeout = null;
  this.status = null;
  this.statusVisible = true;
  this.touch = false;
};
scout.inherits(scout.ProposalChooser, scout.Widget);

scout.ProposalChooser.prototype._init = function(model) {
  scout.ProposalChooser.parent.prototype._init.call(this, model);

  // If smartField is not explicitly provided by model, use smartField instance
  // from parent (which is usually the SmartFieldPopup)
  if (!model.smartField) {
    this.smartField = this.parent.smartField;
  }

  this.model = this._createModel();
};

scout.ProposalChooser.prototype._createModel = function($parent) {
  throw new Error('_createModel() not implemented');
};

scout.ProposalChooser.prototype.setLookupResult = function(result) {
  throw new Error('setLookupResult() not implemented');
};

scout.ProposalChooser.prototype.selectFirstLookupRow = function() {
  throw new Error('selectFirstLookupRow() not implemented');
};

scout.ProposalChooser.prototype.clearSelection = function() {
  throw new Error('clearSelection() not implemented');
};

scout.ProposalChooser.prototype.clearLookupRows = function() {
  throw new Error('clearLookupRows() not implemented');
};

scout.ProposalChooser.prototype.triggerLookupRowSelected = function() {
  throw new Error('triggerLookupRowSelected() not implemented');
};

scout.ProposalChooser.prototype._render = function() {
  this.$container = this.$parent
    .appendDiv('proposal-chooser')
    .toggleClass('touch', this.touch);
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.ProposalChooserLayout(this));

  this._renderModel();

  // status
  this.$status = this.$container
    .appendDiv('status')
    .setVisible(false);

  // active filter
  if (this.smartField.activeFilterEnabled) {
    this.activeFilterGroup = scout.create('RadioButtonGroup', {
      parent: this,
      labelVisible: false,
      statusVisible: false
    });

    // add radio buttons
    scout.SmartField.ACTIVE_FILTER_VALUES.forEach(function(value, index) {
      this._renderActiveFilterButton(value, index);
    }, this);

    this.activeFilterGroup.render();
    this.activeFilterGroup.$container.addClass('active-filter');
    this.activeFilterGroup.removeMandatoryIndicator();
  }
};

scout.ProposalChooser.prototype._renderModel = function() {
  this.model.render();
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

scout.ProposalChooser.prototype.delegateKeyEvent = function(event) {
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
  if (!this.rendered && !this.rendering) {
    return;
  }

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

scout.ProposalChooser.prototype._renderActiveFilterButton = function(value, index) {
  var radio = scout.create('RadioButton', {
      parent: this.activeFilterGroup,
      label: this._activeFilterLabel(index),
      radioValue: scout.SmartField.ACTIVE_FILTER_VALUES[index],
      selected: this.smartField.activeFilter === value,
      focusWhenSelected: false,
      gridData: {
        x: index,
        y: 1,
        useUiWidth: true
      }
    });

  radio.on('propertyChange', function(event) {
    if (event.propertyName === 'selected' && event.newValue === true) {
      this.trigger('activeFilterSelected', {
        activeFilter: event.source.radioValue
      });
    }
  }.bind(this));

  this.activeFilterGroup.addButton(radio);
};

scout.ProposalChooser.prototype.setVirtual = function(virtual) {
  if (this.model instanceof scout.Table) {
    this.model.setVirtual(virtual);
  }
};

scout.ProposalChooser.prototype.setStatus = function(status) {
  this.setProperty('status', status);
};

scout.ProposalChooser.prototype.setBusy = function(busy) {
  this.model.setProperty('loading', busy);
  this.model.setProperty('enabled', !busy);
};

scout.ProposalChooser.prototype._activeFilterLabel = function(index) {
  return this.smartField.activeFilterLabels[index];
};

/**
 * Override this function to implement update scrollbar behavior.
 */
scout.ProposalChooser.prototype.updateScrollbars = function() {
  this.model.updateScrollbars();
};

scout.ProposalChooser.prototype._isProposal = function() {
  return this.smartField instanceof scout.ProposalField;
};

scout.ProposalChooser.prototype._selectProposal = function(result, proposals) {
  if (this._isProposal()) {
    return; // no pre-selection when field is a proposal field
  }
  if (result.byAll) {
    this.trySelectCurrentValue();
  } else if (proposals.length === 1) {
    this.selectFirstLookupRow();
  }
};
