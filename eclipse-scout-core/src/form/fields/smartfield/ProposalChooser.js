/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {HtmlComponent, objects, ProposalChooserLayout, ProposalField, scout, SmartField, Status, Table, Widget} from '../../../index';
import $ from 'jquery';

export default class ProposalChooser extends Widget {

  constructor() {
    super();

    this.$container = null;
    this.$status = null;
    this.activeFilterGroup = null;
    this.htmlComp = null;
    this._updateStatusTimeout = null;
    this.status = null;
    this.statusVisible = true;
    this.touch = false;
  }

  _init(model) {
    super._init(model);

    // If smartField is not explicitly provided by model, use smartField instance
    // from parent (which is usually the SmartFieldPopup)
    if (!model.smartField) {
      this.smartField = this.parent.smartField;
    }

    this.model = this._createModel();
  }

  _createModel($parent) {
    throw new Error('_createModel() not implemented');
  }

  setLookupResult(result) {
    throw new Error('setLookupResult() not implemented');
  }

  selectFirstLookupRow() {
    throw new Error('selectFirstLookupRow() not implemented');
  }

  clearSelection() {
    throw new Error('clearSelection() not implemented');
  }

  clearLookupRows() {
    throw new Error('clearLookupRows() not implemented');
  }

  /**
   * @param row TableRow or TreeNode (both have the same properties)
   */
  triggerLookupRowSelected(row) {
    row = row || this.selectedRow();
    if (!row || !row.enabled) {
      return;
    }
    this.trigger('lookupRowSelected', {
      lookupRow: row.lookupRow
    });
  }

  /**
   * Implement this function to get the selected row or node from the proposal chooser.
   * The implementation depends on the widget used by the chooser (Table or Tree).
   */
  selectedRow() {
    throw new Error('selectedRow() not implemented');
  }

  _render() {
    this.$container = this.$parent
      .appendDiv('proposal-chooser')
      .toggleClass('touch', this.touch);
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new ProposalChooserLayout(this));

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
        statusVisible: false,
        gridColumnCount: SmartField.ACTIVE_FILTER_VALUES.length
      });

      // add radio buttons
      SmartField.ACTIVE_FILTER_VALUES.forEach(function(value, index) {
        this._insertActiveFilterButton(value, index);
      }, this);

      this.activeFilterGroup.render();
      this.activeFilterGroup.$container.addClass('active-filter');
      this.activeFilterGroup.removeMandatoryIndicator();
    }
  }

  _renderModel() {
    this.model.render();

    // Make sure container never gets the focus, but looks focused
    this.model.$container.setTabbable(false);
    this.model.$container.addClass('focused');
  }

  _renderProperties() {
    super._renderProperties();
    this._updateStatus();
  }

  /**
   * Delegates an event (e.g. keyup, keydown) to the model.$container of this instance,
   * calling the JQuery trigger method.
   */
  delegateEvent(event) {
    event.originalEvent.smartFieldEvent = true;
    this.model.$container.trigger(event);
  }

  delegateKeyEvent(event) {
    this.model.$container.trigger(event);
  }

  _renderStatus() {
    this._updateStatus();
  }

  _renderStatusVisible() {
    this._updateStatus();
  }

  _computeStatusVisible() {
    return !!(this.statusVisible && this.status);
  }

  _updateStatus() {
    // Note: the UI has a special way to deal with the status. When the UI is rendered
    // we do NOT render an OK status, even when it is set on the model. The status
    // "Search proposals..." is set to severity OK. That status is only displayed, when
    // it is still there after 250 ms. Usually a smart-field lookup is fast, so the user
    // never sees the status message. However: it would be better if the status on the
    // (Java-)model would implement the behavior described above, but
    // this would require a timer thread, so it is easier to implement that in the UI.
    // Status with other severities than OK are displayed immediately.
    clearTimeout(this._updateStatusTimeout);
    if (objects.optProperty(this.status, 'severity') === Status.Severity.OK) {
      // compute statusVisible 250 ms later (status can change in the meantime)
      this._updateStatusTimeout = setTimeout(
        this._updateStatusImpl.bind(this), 250);
    } else {
      this._updateStatusImpl();
    }
  }

  _updateStatusImpl() {
    if (!this.rendered && !this.rendering) {
      return;
    }

    let
      oldVisible = this.$status.isVisible(),
      oldMessage = this.$status.text(),
      visible = this._computeStatusVisible();

    if (oldVisible === visible &&
      oldMessage === objects.optProperty(this.status, 'message')) {
      return;
    }

    $.log.isDebugEnabled() && $.log.debug('(ProposalChooser#_updateStatusImpl) $status.visible=' + visible);
    this.$status.setVisible(visible);
    if (this.status) {
      this._setStatusMessage(this.status.message);
    } else {
      this.$status.text('');
    }
    this.htmlComp.invalidateLayoutTree();
  }

  /**
   * Replaces an ellipsis (...) at the end of the message-text with a CSS animation.
   */
  _setStatusMessage(message) {
    Status.animateStatusMessage(this.$status, message);
  }

  _insertActiveFilterButton(value, index) {
    let radio = scout.create('RadioButton', {
      parent: this.activeFilterGroup,
      label: this._activeFilterLabel(index),
      radioValue: SmartField.ACTIVE_FILTER_VALUES[index],
      selected: this.smartField.activeFilter === value,
      focusWhenSelected: false,
      gridDataHints: {
        useUiWidth: true
      }
    });

    radio.on('propertyChange', event => {
      if (event.propertyName === 'selected' && event.newValue === true) {
        this.trigger('activeFilterSelected', {
          activeFilter: event.source.radioValue
        });
      }
    });

    this.activeFilterGroup.insertButton(radio);
  }

  setVirtual(virtual) {
    if (this.model instanceof Table) {
      this.model.setVirtual(virtual);
    }
  }

  setStatus(status) {
    this.setProperty('status', status);
  }

  setBusy(busy) {
    this.model.setProperty('loading', busy);
    this.model.setProperty('enabled', !busy);
  }

  _activeFilterLabel(index) {
    return this.smartField.activeFilterLabels[index];
  }

  /**
   * Override this function to implement update scrollbar behavior.
   */
  updateScrollbars() {
    this.model.updateScrollbars();
  }

  _isProposal() {
    return this.smartField instanceof ProposalField;
  }

  _selectProposal(result, proposals) {
    if (this._isProposal()) {
      return; // no pre-selection when field is a proposal field
    }
    if (result.byAll) {
      this.trySelectCurrentValue();
    } else if (proposals.length === 1) {
      this.selectFirstLookupRow();
    }
  }
}
