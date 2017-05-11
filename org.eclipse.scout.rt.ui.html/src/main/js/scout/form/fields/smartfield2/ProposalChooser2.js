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
scout.ProposalChooser2 = function() { // FIXME [awe] 7.0 - SF2: merge with SmartField2Popup?
  scout.ProposalChooser2.parent.call(this);

  this.$container = null;
  this.$status = null;
  this.$activeFilter = null;
  this.htmlComp = null;
  this._updateStatusTimeout = null;
  this.status = null;
  this.statusVisible = true;
};
scout.inherits(scout.ProposalChooser2, scout.Widget);

scout.ProposalChooser2.prototype._init = function(model) {
  scout.ProposalChooser2.parent.prototype._init.call(this, model);

  this.model = this._createModel();
};

scout.ProposalChooser2.prototype._createModel = function($parent) {
  throw new Error('_createModel() not implemented');
};

scout.ProposalChooser2.prototype.setLookupRows = function(lookupRows) {
  throw new Error('setLookupRows() not implemented');
};

scout.ProposalChooser2.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('proposal-chooser');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.ProposalChooser2Layout(this));

  this._renderModel();

  // status
  this.$status = this.$container
    .appendDiv('status')
    .setVisible(false);

  // active filter
  if (this._smartField().activeFilterEnabled) {
    this.activeFilterGroup = scout.create('RadioButtonGroup', {
      parent: this,
      labelVisible: false,
      statusVisible: false
    });

    // add radio buttons
    scout.SmartField2.ACTIVE_FILTER_VALUES.forEach(function(value, index) {
      this._renderActiveFilterButton(value, index);
    }, this);

    this.activeFilterGroup.render(this.$container);
    this.activeFilterGroup.$container.addClass('active-filter');
    this.activeFilterGroup.removeMandatoryIndicator();
  }
};

scout.ProposalChooser2.prototype._renderModel = function() {
  this.model.render(this.$container);
  if (this.model instanceof scout.Tree) { // FIXME [awe] 7.0 - SF2: move this to TreeProposalChooser
    // disable focus on field container
    this.model._onNodeControlMouseDownDoFocus = function() {};
  }
};

scout.ProposalChooser2.prototype._renderProperties = function() {
  scout.ProposalChooser2.parent.prototype._renderProperties.call(this);
  this._updateStatus();
  this.htmlComp.revalidateLayout();
};

/**
 * Delegates an event (e.g. keyup, keydown) to the model.$container of this instance,
 * calling the JQuery trigger method.
 */
scout.ProposalChooser2.prototype.delegateEvent = function(event) {
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
      selected: this._smartField().activeFilter === value,
      focusWhenSelected: false,
      gridData: {
        x: index,
        y: 1,
        useUiWidth: true
      }
    });

  radio.on('propertyChange', function(event) {
    if (event.name === 'selected' && event.newValue === true) {
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

scout.ProposalChooser2.prototype.setLookupResult = function(result) {
  this.setLookupRows(result.lookupRows);
  var status = this._computeStatus(result);
  this.setStatus(status);
};

scout.ProposalChooser2.prototype._computeStatus = function(result) {
  if (result.lookupFailed) {
    return scout.Status.error({
      message: '%%%Unbekannter Fehler%%%'
    });
  }

  var rows = result.lookupRows;
  if (rows.length === 0) {
    // FIXME [awe] 7.0 - SF2: distinct between search for '*' and search for other
    return scout.Status.warn({
      message: this.session.text('SmartFieldCannotComplete', result.searchText)
    });
  }

  var maxRows = this._smartField().browseMaxRowCount;
  if (rows.length > maxRows) {
    return scout.Status.info({
      message: this.session.text('SmartFieldMoreThanXRows', maxRows)
    });
  }

  return null;
};

scout.ProposalChooser2.prototype._smartField = function() {
  return this.parent._smartField();
};

scout.ProposalChooser2.prototype._activeFilterLabel = function(index) {
  return this._smartField().activeFilterLabels[index];
};
