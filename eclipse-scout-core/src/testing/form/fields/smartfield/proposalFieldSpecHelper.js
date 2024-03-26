/*
 * Copyright (c) 2010-2024 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import {ProposalField} from '../../../../index';

export async function testProposalFieldInputs(field, inputs, touchMode, callbacks) {
  field.touchMode = touchMode;
  field.render();

  callbacks = $.extend({
    beforeInput: (input) => {
    },
    afterInput: (input) => {
    },
    afterSelectLookupRow: (text, lookupRow) => {
    },
    afterAcceptCustomText: (text) => {
    }
  }, callbacks);

  for (const inputOrText of inputs) {
    const input = ensureInput(inputOrText);
    const {text, lookup} = input;

    callbacks.beforeInput(input);

    if (lookup) {
      const lookupRow = await selectLookupRow(field, text);
      callbacks.afterSelectLookupRow(text, lookupRow);
    } else {
      await acceptCustomText(field, text);
      callbacks.afterAcceptCustomText(text);
    }

    callbacks.afterInput(input);
  }
}

export function ensureInput(inputOrText) {
  return typeof inputOrText === 'string' ? {text: inputOrText} : inputOrText;
}

export async function acceptCustomText(field, text) {
  if (field.touchMode) {
    // touchMode opens a popup with a field and a done-menu
    const popup = await openPopup(field);
    popup._field.$field.val(text);
    popup._field.acceptInput();
    popup.doneAction.doAction();
  } else {
    field.$field.val(text);
    field.acceptInput();
  }
}

export async function selectLookupRow(field, text) {
  // find row for text
  const popup = await openPopup(field);
  const proposalChooser = field.touchMode ? popup._widget : popup.proposalChooser;
  const table = proposalChooser.model;
  const row = table.rows.find(r => r.cells[0].text === text);

  // trigger row mousedown and mouseup
  row.$row.triggerMouseDown();
  row.$row.triggerMouseUp();

  return row.lookupRow;
}

export async function openPopup(field) {
  field.$field.focus();
  await field.openPopup(true);
  const popup = field.popup;
  popup.animateRemoval = false;
  return popup;
}

export function createSpecProposalField() {
  return new SpecProposalField();
}

class SpecProposalField extends ProposalField {
  acceptInput(sync) {
    this._acceptInputEnabled = true; // accept all inputs, no need for a timeout
    return super.acceptInput(sync);
  }
}

export default {
  testProposalFieldInputs,
  ensureInput,
  acceptCustomText,
  selectLookupRow,
  openPopup,
  createSpecProposalField
}
