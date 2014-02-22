//local log function
var log = console.log.bind(console);

// extend jqueray
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
  session = function Session (sessionPartId) {
  }();

  
  // interface to outside
  this.add = function add (sessionPartId) {
    // create widget ...
  }($div);
  
  
  // each 'class' in one file
  DesktopTable = function Desktop () {
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
  DesktopTable.prototpye.laterUsage = function () {
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
      sessionPartId = '' + portletPartId + '.' + tabId,
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
