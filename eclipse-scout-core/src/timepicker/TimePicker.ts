/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {dates, Device, HtmlComponent, scrollbars, TimePickerTouchPopup, Widget} from '../index';
import $ from 'jquery';

export default class TimePicker extends Widget {

  constructor() {
    super();

    // Preselected date can only be set if selectedDate is null. The preselected date is rendered differently, but
    // has no function otherwise. (It is used to indicate the day that will be selected when the user presses
    // the UP or DOWN key while no date is selected.)
    this.preselectedTime = null;
    this.selectedTime = null;
    this.resolution = null;
    this.$scrollable = null;
  }

  _init(options) {
    super._init(options);
    this.resolution = options.timeResolution;
  }

  _render() {
    this.$container = this.$parent
      .appendDiv('time-picker')
      .toggleClass('touch-only', Device.get().supportsOnlyTouch());
    this.$parent.appendDiv('time-picker-separator');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);

    this._renderTimeSelection();
    this._installScrollbars();
  }

  _renderTimeSelection() {
    let i,
      date = dates.trunc(new Date()),
      now = dates.ceil(new Date(), this.resolution),
      currentHours = 0,
      $hourRow,
      $time;
    let $box = this.$parent.makeDiv('day-table');
    for (i = 0; i < 24; i++) {
      // reset minutes always every hour line starts with 00
      date.setMinutes(0);
      currentHours = date.getHours();

      $hourRow = $box.appendDiv('hour-row');
      let $hour = $hourRow.appendDiv('cell  hours')
        .data('time', new Date(date))
        .on('click', this._onTimeClick.bind(this));
      if (now.getHours() === date.getHours()) {
        $hour.addClass('now');
      }
      $hour.appendSpan('text')
        .text(dates.format(date, this.session.locale, 'HH'));

      while (currentHours === date.getHours()) {
        $time = $hourRow.appendDiv('cell minutes')
          .data('time', new Date(date))
          .on('click', this._onTimeClick.bind(this));

        $time.appendSpan('text').text(dates.format(date, this.session.locale, 'mm'));
        if (dates.isSameTime(now, date)) {
          $time.addClass('now');
        }
        date.setMinutes(date.getMinutes() + this.resolution);
      }
    }
    $box.appendTo(this.$container);
    return $box;
  }

  /**
   * @override
   */
  _installScrollbars(options) {
    this._uninstallScrollbars();

    super._installScrollbars({
      axis: 'y'
    });
  }

  _scrollTo($scrollTo) {
    if (!$scrollTo) {
      return;
    }
    if (this.parent instanceof TimePickerTouchPopup) {
      // setTimeout seems to be necessary on ios
      setTimeout(() => {
        if (this.rendered) {
          scrollbars.scrollTo(this.$container, $scrollTo, 'center');
        }
      });
    } else {
      scrollbars.scrollTo(this.$container, $scrollTo, 'center');
    }
  }

  preselectTime(time) {
    if (time) {
      // Clear selection when a date is preselected
      this.setSelectedTime(null);
    }
    this.setPreselectedTime(time);
  }

  /**
   * @internal, use preselectDate to preselect a date
   */
  setPreselectedTime(preselectedTime) {
    this.setProperty('preselectedTime', preselectedTime);
  }

  _renderPreselectedTime() {
    let $scrollTo;
    this.$container.find('.cell').each((i, elem) => {
      let $time = $(elem),
        time = $time.data('time');
      $time.removeClass('preselected');
      if (this.preselectedTime) {
        if ($time.hasClass('hours') && this.preselectedTime.getHours() === time.getHours()) {
          $time.addClass('preselected');
          $scrollTo = $time;
        } else if ($time.hasClass('minutes') && dates.isSameTime(this.preselectedTime, time)) {
          $time.addClass('preselected');
          $scrollTo = $time;
        }
      }
    });
    this._scrollTo($scrollTo);
  }

  selectTime(time) {
    if (time) {
      // Clear selection when a date is preselected
      this.setPreselectedTime(null);
    }
    this.setSelectedTime(time);
  }

  /**
   * @internal, use selectDate to select a date
   */
  setSelectedTime(selectedTime) {
    this.setProperty('selectedTime', selectedTime);
  }

  _renderSelectedTime() {

    let $scrollTo;
    this.$container.find('.cell').each((i, elem) => {
      let $time = $(elem),
        time = $time.data('time');
      $time.removeClass('selected');
      if (this.selectedTime) {
        if ($time.hasClass('hours') && this.selectedTime.getHours() === time.getHours()) {
          $time.addClass('selected');
          $scrollTo = $time;
        } else if ($time.hasClass('minutes') && dates.isSameTime(this.selectedTime, time)) {
          $time.addClass('selected');
          $scrollTo = $time;
        }
      }
    });
    this._scrollTo($scrollTo);
  }

  shiftViewDate(years, months, days) {
    let date = this.viewDate;
    date = dates.shift(date, years, months, days);
    this.showDate(date);
  }

  shiftSelectedTime(hourUnits, minuteUnits, secondUnits) {
    let time = this.preselectedTime;
    if (this.selectedTime) {
      time = dates.shiftTime(this.selectedTime, hourUnits, minuteUnits * this.resolution, secondUnits);
    }
    if (!time) {
      return; // do nothing when no date was found
    }
    this.selectTime(this._snapToTimeGrid(time));
  }

  _snapToTimeGrid(time) {
    if (!time) {
      return time;
    }
    let min = time.getMinutes();
    min = (parseInt(min / this.resolution) * this.resolution);
    time.setMinutes(min);
    return time;

  }

  _onNavigationMouseDown(event) {
    let $target = $(event.currentTarget);
    let diff = $target.data('shift');
    this.shiftViewDate(0, diff, 0);
  }

  _onTimeClick(event) {
    let $target = $(event.currentTarget);
    let time = new Date($target.data('time'));
    this.selectTime(time);
    this.trigger('timeSelect', {
      time: time
    });
  }

  _onMouseWheel(event) {
    event = event.originalEvent || this.$container.window(true).event.originalEvent;
    let wheelData = event.wheelDelta ? event.wheelDelta / 10 : -event.detail * 3;
    let diff = (wheelData >= 0 ? -1 : 1);
    this.shiftViewDate(0, diff, 0);
    event.preventDefault();
  }
}
