/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import {DateField, DatePickerTouchPopup, dates, fields, scout, Status} from '../../src/index';

describe('DatePickerTouchPopup', () => {

  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  function createDatePickerTouchPopup(touchField) {
    return scout.create(DatePickerTouchPopup, {
      parent: session.desktop,
      field: touchField
    });
  }

  describe('acceptInput', () => {

    /**
     * Note: in case the field has a ParsingFailedStatus, the value should not be set.
     * #277301
     */
    it('Sets the value on the field, even if the field has a (model) error', () => {
      let dateTimeField = scout.create(DateField, {
        parent: session.desktop,
        hasTime: true,
        hasDate: true
      });
      dateTimeField.addErrorStatus(Status.error('model error'));
      dateTimeField.render();

      let popup = createDatePickerTouchPopup(dateTimeField);
      popup.open();
      fields.valOrText(popup._field.$dateField, '02.10.2020');
      fields.valOrText(popup._field.$timeField, '13:30');
      popup._field.acceptInput();

      let expectedDate = dates.parseJsonDate('2020-10-02 13:30:00.000');
      expect(dateTimeField.displayText).toBe('02.10.2020\n13:30');
      expect(dateTimeField.value).toEqual(expectedDate);
    });

  });

});
