//local log function
var log = console.log.bind(console);

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
  DesktopTable = function DesktopTable () {
    // 1. construction code
    var variable;
    
    // 2. instance variables 
    this.variable = variable;
    return this
        
    // 3. named functions used for construction or event handling
    function onClick () {
      // direct access to 'variable'
      log(variable);
      
      // direct access to session
      session.fetch(variable);
    }
  }
  
  //instance functions
  DesktopTable.prototype.laterUsage = function () {
    // access to 'this.variable'
    log(this.variable);
    
    // direct access to session
    session.fetch(this.variable);
    }

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
