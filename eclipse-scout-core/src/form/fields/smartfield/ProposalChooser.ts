/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  HtmlComponent, InitModelOf, LookupRow, objects, ProposalChooserEventMap, ProposalChooserLayout, ProposalChooserModel, ProposalField, RadioButton, RadioButtonGroup, scout, ScoutKeyboardEvent, SmartField, SmartFieldLookupResult,
  SmartFieldPopup, Status, StatusOrModel, Table, Widget
} from '../../../index';
import $ from 'jquery';

export abstract class ProposalChooser<TValue, TContent extends ProposalChooserContent, TContentRow extends ProposalChooserContentRow<TValue>> extends Widget implements ProposalChooserModel<TValue> {
  declare model: ProposalChooserModel<TValue>;
  declare eventMap: ProposalChooserEventMap<TValue, TContent, TContentRow>;
  declare self: ProposalChooser<any, any, any>;
  declare parent: SmartFieldPopup<TValue>;

  smartField: SmartField<TValue>;
  activeFilterGroup: RadioButtonGroup<string>;
  status: StatusOrModel;
  statusVisible: boolean;
  touch: boolean;
  content: TContent;
  $status: JQuery;
  protected _updateStatusTimeout: number;

  constructor() {
    super();

    this.activeFilterGroup = null;
    this.status = null;
    this.statusVisible = true;
    this.touch = false;

    this.$status = null;

    this._updateStatusTimeout = null;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    // If smartField is not explicitly provided by model, use smartField instance
    // from parent (which is usually the SmartFieldPopup)
    if (!model.smartField) {
      this.smartField = this.parent.smartField;
    }

    this.content = this._createContent();
  }

  protected abstract _createContent(): TContent;

  /**
   * Creates a layout resetter that is used by the {@link ProposalChooserLayout}.
   */
  protected abstract _createLayoutResetter(): ProposalChooserLayoutResetter;

  abstract setLookupResult(result: SmartFieldLookupResult<TValue>);

  abstract selectFirstLookupRow();

  abstract clearSelection();

  abstract clearLookupRows();

  /**
   * @param row TableRow or TreeNode (both have the same properties)
   */
  triggerLookupRowSelected(row?: TContentRow) {
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
  abstract selectedRow(): TContentRow;

  abstract getSelectedLookupRow(): LookupRow<TValue>;

  protected override _render() {
    this.$container = this.$parent
      .appendDiv('proposal-chooser')
      .toggleClass('touch', this.touch);
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new ProposalChooserLayout(this, this._createLayoutResetter()));

    this._renderContent();

    // status
    this.$status = this.$container
      .appendDiv('status')
      .setVisible(false);

    // active filter
    if (this.smartField.activeFilterEnabled) {
      this.activeFilterGroup = scout.create(RadioButtonGroup, {
        parent: this,
        labelVisible: false,
        statusVisible: false,
        gridColumnCount: SmartField.ACTIVE_FILTER_VALUES.length
      }) as RadioButtonGroup<string>;

      // add radio buttons
      SmartField.ACTIVE_FILTER_VALUES.forEach(function(value, index) {
        this._insertActiveFilterButton(value, index);
      }, this);

      this.activeFilterGroup.render();
      this.activeFilterGroup.$container.addClass('active-filter');
      this.activeFilterGroup.removeMandatoryIndicator();
    }
  }

  protected _renderContent() {
    this.content.render();

    // Make sure container never gets the focus, but looks focused
    this.content.$container.setTabbable(false);
    this.content.$container.addClass('focused');
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._updateStatus();
  }

  /**
   * Delegates an event (e.g. keyup, keydown) to the content.$container of this instance,
   * calling the JQuery trigger method.
   */
  delegateEvent(event: ScoutKeyboardEvent & JQuery.Event) {
    event.originalEvent.smartFieldEvent = true;
    this.content.$container.trigger(event);
  }

  delegateKeyEvent(event: ScoutKeyboardEvent & JQuery.Event) {
    this.content.$container.trigger(event);
  }

  protected _renderStatus() {
    this._updateStatus();
  }

  protected _renderStatusVisible() {
    this._updateStatus();
  }

  protected _computeStatusVisible(): boolean {
    return !!(this.statusVisible && this.status);
  }

  protected _updateStatus() {
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

  protected _updateStatusImpl() {
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
      this.smartField.$screenReaderStatus?.appendSpan()
        .addClass('text')
        .addClass('sr-proposal-chooser-status')
        .text(this.status.message);
    } else {
      this.$status.text('');
      this.smartField.$screenReaderStatus?.children('.sr-proposal-chooser-status').remove();
    }
    this.htmlComp.invalidateLayoutTree();
  }

  /**
   * Replaces an ellipsis (...) at the end of the message-text with a CSS animation.
   */
  protected _setStatusMessage(message: string) {
    Status.animateStatusMessage(this.$status, message);
  }

  protected _insertActiveFilterButton(value: string, index: number) {
    let radio = scout.create(RadioButton, {
      parent: this.activeFilterGroup,
      label: this._activeFilterLabel(index),
      radioValue: SmartField.ACTIVE_FILTER_VALUES[index],
      selected: this.smartField.activeFilter === value,
      focusWhenSelected: false,
      gridDataHints: {
        useUiWidth: true
      }
    }) as RadioButton<string>;

    radio.on('propertyChange', event => {
      if (event.propertyName === 'selected' && event.newValue === true) {
        this.trigger('activeFilterSelected', {
          activeFilter: event.source.radioValue
        });
      }
    });

    this.activeFilterGroup.insertButton(radio);
  }

  setVirtual(virtual: boolean) {
    if (this.content instanceof Table) {
      this.content.setVirtual(virtual);
    }
  }

  setStatus(status: StatusOrModel) {
    this.setProperty('status', status);
  }

  setBusy(busy: boolean) {
    this.content.setProperty('loading', busy);
    this.content.setProperty('enabled', !busy);
  }

  protected _activeFilterLabel(index: number): string {
    return this.smartField.activeFilterLabels[index];
  }

  /**
   * Override this function to implement update scrollbar behavior.
   */
  updateScrollbars() {
    this.content.updateScrollbars();
  }

  protected _isProposal(): boolean {
    return this.smartField instanceof ProposalField;
  }

  protected _selectProposal(result: SmartFieldLookupResult<TValue>, proposals: TContentRow[]) {
    if (this._isProposal()) {
      return; // no pre-selection when field is a proposal field
    }
    if (result.byAll) {
      this.trySelectCurrentValue();
    } else if (proposals.length === 1) {
      this.selectFirstLookupRow();
    }
  }

  abstract trySelectCurrentValue();
}

export interface ProposalChooserLayoutResetter {
  cssSelector: string;

  modifyDom(): void;

  restoreDom(): void;
}

export interface ProposalChooserContent extends Widget {
  updateScrollbars(): void;
}

export interface ProposalChooserContentRow<TValue> {
  enabled: boolean;
  lookupRow: LookupRow<TValue>;
}
