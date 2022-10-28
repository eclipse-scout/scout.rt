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
