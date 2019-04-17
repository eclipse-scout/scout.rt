import * as $ from 'jquery';

export default class Event {

  constructor(model) {
    this.defaultPrevented = false;
    $.extend(this, model);
  }

  preventDefault() {
    this.defaultPrevented = true;
  };

}
