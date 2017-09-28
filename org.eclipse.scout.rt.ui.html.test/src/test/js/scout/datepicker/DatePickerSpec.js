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
describe('DatePicker', function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('showDate', function() {

    it('shows the month specified by the given date', function() {
      var picker = scout.create('DatePicker', {
        parent: session.desktop
      });
      picker.render();
      picker.showDate(scout.dates.create('2017-04-12'), false);
      expect(scout.dates.isSameMonth(picker.currentMonth.viewDate, scout.dates.create('2017-04-01'))).toBe(true);

      picker.showDate(scout.dates.create('2016-01-12'), false);
      expect(scout.dates.isSameMonth(picker.currentMonth.viewDate, scout.dates.create('2016-01-01'))).toBe(true);
    });

  });

});
