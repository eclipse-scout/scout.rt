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
var CalendarSpecHelper = function(session) {
  this.session = session;
};

CalendarSpecHelper.prototype.createCalendar = function (model) {
  var calendar = new scout.Calendar();
  calendar.init(model);
  return calendar;
};

CalendarSpecHelper.prototype.createSimpleModel = function() {
  var c = createSimpleModel('Calendar', this.session);
  c.selectedDate = "2016-07-20 14:09:28.556";
  c.startHour = 6;
  c.displayMode = scout.Calendar.DisplayMode.DAY;
  return c;
};

CalendarSpecHelper.prototype.createCompoment = function(options, cal) {
  var component = new scout.CalendarComponent();
  var model = this.createCompomentModel(options);
  component.init(model);
  component.parent = cal;
  return component;
};

CalendarSpecHelper.prototype.createCompomentModel = function(options) {
  var c = createSimpleModel('CalendarComponent', this.session);
  for (var prop in options) {
    c[prop] = options[prop];
  }
  return c;
};

