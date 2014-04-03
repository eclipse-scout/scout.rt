// extend jquery -> in eigenes File jquery-scout.js
(function ($) {

  $.fn.doIt = function (a, v) {
    return this.each(function () {
      this.doEach(a, v);
    });
  };

  // ...
  $.log = console.log.bind(console);
}(jQuery));

// scout namespace
// protects $ and undefined from being redefined by another library
(function(scout, $, undefined) {

  //start file Session.js
  function Session ($entryPoint, sessionPartId) {

  }
  Session.prototype.init = function () {
  };
  Session.prototype.createField = function () {
    if(type == 'desktopTable') {
      new DesktopTable
    }
    else {
      call({}, scout[type]);


    }
  };
  //end file Session.js

  //start file DesktopTable.js
  function DesktopTable(session, model) {
  }
  DesktopTable.prototype.Func = function () {
  };
  //end file DesktopTable.js

  //start file numbers.js
  var numbers = {
    multiply : function sum(a,b) {},
    div : function sum(a,b) {}
  };
  //end file numbers.js

  // scout API
  scout.init = function() {
    var tabId = '' + new Date().getTime();
    $('.scout').each(function() {
      var portletPartId = $(this).data('partid') || '0',
        sessionPartId = [portletPartId, tabId].join('.');
      var session = new Session($(this), sessionPartId);
      session.init();
    });
  };

  scout.registerCustomComponent = function (name, component) {
    scout[name] = component;
  };

  scout.numbers = numbers;

}(window.scout = window.scout || {});

Walbusch = {};
Walbusch.MagicField = function (session) {

};

// scan index and start scout
$(document).ready(function () {
  window.scout.init();

  window.scout.registerComponent('magicField', Walbusch.MagicField);
});

//TODO discuss
Function.prototype.that = function (context) {that = context; return this.bind(context);}
