/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DatePicker, dates, scout} from '../../src/index';

describe('DatePicker', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('showDate', () => {

    it('shows the month specified by the given date', () => {
      let picker = scout.create(DatePicker, {
        parent: session.desktop
      });
      picker.render();
      picker.showDate(dates.create('2017-04-12'), false);
      expect(dates.isSameMonth(picker.currentMonth.viewDate, dates.create('2017-04-01'))).toBe(true);

      picker.showDate(dates.create('2016-01-12'), false);
      expect(dates.isSameMonth(picker.currentMonth.viewDate, dates.create('2016-01-01'))).toBe(true);
    });

  });

});
