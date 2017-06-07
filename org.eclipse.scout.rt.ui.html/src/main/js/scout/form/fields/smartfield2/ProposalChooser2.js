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
scout.ProposalChooser2 = function() {
  scout.ProposalChooser2.parent.call(this);

  this.$container = null;
  this.$status = null;
  this.$activeFilter = null;
  this.htmlComp = null;
  this._updateStatusTimeout = null;
  this.status = null;
  this.statusVisible = true;
  this.animateRemoval = true;
};
scout.inherits(scout.ProposalChooser2, scout.Popup);

scout.ProposalChooser2.prototype._init = function(model) {
  model.withFocusContext = false;
  scout.ProposalChooser2.parent.prototype._init.call(this, model);

  this.smartField = this.parent;
  this.model = this._createModel();
  this.setLookupResult(model.lookupResult);
  this.setStatus(model.status);
};

scout.ProposalChooser2.prototype._createLayout = function() {
  if (this.smartField.variant === scout.SmartField2.DisplayStyle.DROPDOWN) {
    return new scout.DropdownPopupLayout(this, this);
  } else {
    return new scout.ProposalChooser2Layout(this, this);
  }
};

scout.ProposalChooser2.prototype._createModel = function($parent) {
  throw new Error('_createModel() not implemented');
};

scout.ProposalChooser2.prototype.setLookupResult = function(result) {
  throw new Error('setLookupResult() not implemented');
};

scout.ProposalChooser2.prototype.selectFirstLookupRow = function() {
  throw new Error('selectFirstLookupRow() not implemented');
};

scout.ProposalChooser2.prototype.triggerLookupRowSelected = function() {
  throw new Error('triggerLookupRowSelected() not implemented');
};

scout.ProposalChooser2.prototype._render = function() {
  var cssClass = this.smartField.cssClassName() + '-popup';
  scout.ProposalChooser2.parent.prototype._render.call(this);
  this.$container
    .addClass(cssClass)
    .addClass('proposal-chooser')
    .on('mousedown', this._onContainerMouseDown.bind(this));

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
    scout.SmartField2.ACTIVE_FILTER_VALUES.forEach(function(value, index) {
      this._renderActiveFilterButton(value, index);
    }, this);

    this.activeFilterGroup.render();
    this.activeFilterGroup.$container.addClass('active-filter');
    this.activeFilterGroup.removeMandatoryIndicator();
  }
};

scout.ProposalChooser2.prototype._renderModel = function() {
  this.model.render();
};

scout.ProposalChooser2.prototype._renderProperties = function() {
  scout.ProposalChooser2.parent.prototype._renderProperties.call(this);
  this._updateStatus();
  this.htmlComp.revalidateLayout();
};

scout.ProposalChooser2.prototype.setStatusLookupInProgress = function(status) {
  this.setStatus(scout.Status.ok({
    message: this.session.text('searchingProposals')
  }));
};

/**
 * Delegates an event (e.g. keyup, keydown) to the model.$container of this instance,
 * calling the JQuery trigger method.
 */
scout.ProposalChooser2.prototype.delegateEvent = function(event) {
  event.originalEvent.smartFieldEvent = true;
  this.model.$container.trigger(event);
};

scout.ProposalChooser2.prototype.delegateKeyEvent = function(event) {
  event.originalEvent.smartFieldEvent = true;
  this.model.$container.trigger(event);
};

scout.ProposalChooser2.prototype._renderStatus = function() {
  this._updateStatus();
};

scout.ProposalChooser2.prototype._renderStatusVisible = function() {
  this._updateStatus();
};

scout.ProposalChooser2.prototype._computeStatusVisible = function() {
  return !!(this.statusVisible && this.status);
};

scout.ProposalChooser2.prototype._updateStatus = function() {
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

scout.ProposalChooser2.prototype._updateStatusImpl = function() {
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
scout.ProposalChooser2.prototype._setStatusMessage = function(message) {
  scout.Status.animateStatusMessage(this.$status, message);
};

scout.ProposalChooser2.prototype._renderActiveFilterButton = function(value, index) {
  var radio = scout.create('RadioButton', {
      parent: this.activeFilterGroup,
      label: this._activeFilterLabel(index),
      radioValue: scout.SmartField2.ACTIVE_FILTER_VALUES[index],
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


scout.ProposalChooser2.prototype.setVirtual = function(virtual) {
  if (this.model instanceof scout.Table) {
    this.model.setVirtual(virtual);
  }
};

scout.ProposalChooser2.prototype.setStatus = function(status) {
  this.setProperty('status', status);
};

scout.ProposalChooser2.prototype.setBusy = function(busy) {
  this.model.setProperty('loading', busy);
  this.model.setProperty('enabled', !busy);
};

scout.ProposalChooser2.prototype._activeFilterLabel = function(index) {
  return this.smartField.activeFilterLabels[index];
};

/**
 * This event handler is called before the mousedown handler on the _document_ is triggered
 * This allows us to prevent the default, which is important for the CellEditorPopup which
 * should stay open when the SmartField popup is closed. It also prevents the focus blur
 * event on the SmartField input-field.
 */
scout.ProposalChooser2.prototype._onContainerMouseDown = function(event) {
  // when user clicks on proposal popup with table or tree (prevent default,
  // so input-field does not lose the focus, popup will be closed by the
  // proposal chooser impl.
  return false;
};
