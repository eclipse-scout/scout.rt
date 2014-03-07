//local log function
var log = console.log.bind(console);

Function.prototype.that = function (context) {that = context; return this.bind(context);}

// extend jquery
(function ($) {

  $.fn.doIt = function (a, v) {
    return this.each(function () {
      this.doEach(a, v);
    });
  };

  // ...

}(jQuery));

// scout namespace and object

Scout = function (sessionPartId) {

  // session handling, singleton
  session = new function Session (sessionPartId) {
    var server = 'localhost';
    this.id = sessionPartId;

    this.fetch () {
      log(server, this.id)
    };
  }();

  // interface to outside
  this.add = function add ($div) {
    // create widget ...
    // use session

    // append classes dynamiacly
    // if (typeof this.MySuperField === 'undefined') {
    // $.getScript('XXX.js');
    // DesktopTable.prototype.newFunc() {};
    // this.MySuperField = function ()....
    // }

  };


  // each 'class' in one file
  DesktopTable = function () {
    // 1. instance variables
    this.publicVar = publicVar;
    this._privateVar = privateVar;

     // 2. construction code
    var $div =..;

     // 3. named functions used for construction or event handling
    var that = this;
    function onClick () {
      // direct access to 'variable'
      log(variable);

      that._privateFunc();

      $div.xy;

      // direct access to session
      session.fetch(that._privateVar);
    }

  };

  //instance functions
  DesktopTable.prototype._privateFunc = function () {

  };

  DesktopTable.prototype.publicFunc = function () {
      // access to 'this._privateVar'
      log(this._privateVar);
      // direct access to session
      session.fetch(this._privateVar);
      this._privateFunc();
      this.publicVar ="abc";
  };

  // next class, next file
  // ...
}

// scan index and start scout
$(document).ready(function () {
  var tabId = '' + new Date().getTime(),
    scouts = {};

  $('.scout').each(function () {
    var portletPartId = $(this).data('partid') || '0',
      sessionPartId = [portletPartId, tabId].join('.'),
      scout;

    if (scouts[sessionPartId]) {
      scout = new Scout(sessionPartId);
      scouts[sessionPartId] = scout;
    } else {
      scout = scouts[sessionPartId];
    }

    scout.add($(this));
  });
});




//----------------- new -----------------------
