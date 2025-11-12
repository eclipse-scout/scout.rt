/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {keys, LookupRow, ProposalChooser, ProposalField, SmartFieldLookupResult, SmartFieldPopup, SmartFieldTouchPopup, Table} from '../../../../index';
import {JQueryTesting} from '../../../jquery-testing';

export const proposalFieldSpecHelper = {

  async testProposalFieldInputs(field: ProposalField, inputs: (ProposalFieldSpecHelperInput | string)[], touchMode?: boolean, callbacks?: ProposalFieldSpecHelperCallbacks, selectionStrategy?: LookupRowSelectionStrategy) {
    field.touchMode = touchMode;
    field.render();

    callbacks = $.extend({
      beforeInput: (input: ProposalFieldSpecHelperInput) => {
      },
      afterInput: (input: ProposalFieldSpecHelperInput) => {
      },
      afterSelectLookupRow: (text: string, lookupRow: LookupRow<string>) => {
      },
      afterAcceptCustomText: (text: string) => {
      }
    }, callbacks);

    for (const inputOrText of inputs) {
      const input = proposalFieldSpecHelper.ensureInput(inputOrText);
      const {text, lookup} = input;

      callbacks.beforeInput(input);

      if (lookup || selectionStrategy === LookupRowSelectionStrategy.EXACT_TEXT) {
        const lookupRow = await proposalFieldSpecHelper.selectLookupRow(field, text, selectionStrategy);
        callbacks.afterSelectLookupRow(text, lookupRow);
      } else {
        await proposalFieldSpecHelper.acceptCustomText(field, text);
        callbacks.afterAcceptCustomText(text);
      }

      callbacks.afterInput(input);
    }
  },

  ensureInput(inputOrText: ProposalFieldSpecHelperInput | string): ProposalFieldSpecHelperInput {
    return typeof inputOrText === 'string' ? {text: inputOrText} : inputOrText;
  },

  async acceptCustomText(field: ProposalField, text: string) {
    if (field.touchMode) {
      // touchMode opens a popup with a field and a done-menu
      const popup = await proposalFieldSpecHelper.openPopup(field) as SmartFieldTouchPopup<string>;
      popup._field.$field.val(text);
      popup._field.acceptInput();
      popup.doneAction.doAction();
    } else {
      field.$field.val(text);
      await field.acceptInput();
    }
  },

  async selectLookupRow(field: ProposalField, text: string, strategy = LookupRowSelectionStrategy.CLICK): Promise<LookupRow<string>> {
    // find row for text
    const popup = await proposalFieldSpecHelper.openPopup(field);
    const smartField = field.touchMode ? (popup as SpecSmartFieldTouchPopup<string>)._field : (popup as SmartFieldPopup<string>).smartField;
    const proposalChooser = field.touchMode ? (popup as SpecSmartFieldTouchPopup<string>)._widget : (popup as SmartFieldPopup<string>).proposalChooser;
    const table = proposalChooser.content as Table;
    const row = table.rows.find(r => r.cells[0].text === text);

    switch (strategy) {
      case LookupRowSelectionStrategy.CLICK: {
        JQueryTesting.triggerMouseDown(row.$row);
        JQueryTesting.triggerMouseUp(row.$row);
        break;
      }
      case LookupRowSelectionStrategy.ENTER: {
        table.selectRow(row);
        JQueryTesting.triggerKeyDownCapture(smartField.$field, keys.ENTER);
        break;
      }
      case LookupRowSelectionStrategy.EXACT_TEXT: {
        smartField.$field.val(text);
        let closePromise = popup.when('close');
        JQueryTesting.triggerKeyDownCapture(smartField.$field, keys.ENTER);
        await closePromise;
        return smartField.lookupRow;
      }
    }

    return row.lookupRow;
  },

  async openPopup(field: ProposalField): Promise<SmartFieldTouchPopup<string> | SmartFieldPopup<string>> {
    field.$field.focus();
    await field.openPopup(true);
    const popup = field.popup;
    popup.animateRemoval = false;
    return popup;
  }
};

export type ProposalFieldSpecHelperInput = {
  text: string;
  lookup?: boolean;
};

export type ProposalFieldSpecHelperCallbacks = {
  beforeInput?: (input: ProposalFieldSpecHelperInput) => void;
  afterInput?: (input: ProposalFieldSpecHelperInput) => void;
  afterSelectLookupRow?: (text: string, lookupRow: LookupRow<string>) => void;
  afterAcceptCustomText?: (text: string) => void;
};

export class SpecProposalField extends ProposalField {
  declare _userWasTyping: boolean;

  override _lookupByTextOrAllDone(result: SmartFieldLookupResult<string>) {
    super._lookupByTextOrAllDone(result);
  }

  override _getLastSearchText(): string {
    return super._getLastSearchText();
  }

  override acceptInput(sync?: boolean): JQuery.Promise<void> | void {
    this._acceptInputEnabled = true; // accept all inputs, no need for a timeout
    return super.acceptInput(sync);
  }
}

export class SpecSmartFieldTouchPopup<TValue> extends SmartFieldTouchPopup<TValue> {
  declare _widget: ProposalChooser<TValue, any, any>;
}

export enum LookupRowSelectionStrategy {
  CLICK, // The desired row is clicked on
  ENTER, // The desired row is selected using arrow keys and then enter is pressed
  EXACT_TEXT // The exact value is typed into the smart field. In combination with the flag lookupOnAcceptByText, the row is selected
}
