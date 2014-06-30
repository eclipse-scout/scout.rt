//FIXME keystrokes should consider focused fields. Maybe better to attach those keystrokes directly to the field? What with focused table?
scout.KeystrokeManager = function() {

  this._adapters = []; // FIXME BSH Check if we really need this

  // alt and f1-help
  // FIXME BSH Alt-Key is a bad choice on IE, see http://superuser.com/questions/470064/is-it-possible-to-disable-the-alt-key-as-it-is-used-for-default-windows-things
  $(document).keydown(function(event) {
    if (event.which == 18) {
      removeKeyBox();
      drawKeyBox();
    }
  });

  $(document).keyup(function(event) {
    if (event.which == 18) {
      removeKeyBox();
      // FIXME BSH This cannot work... See http://stackoverflow.com/questions/6220300/jquery-keyup-how-do-i-prevent-the-default-behavior-of-the-arrow-up-down-an
      return false;
    }
  });

  $(window).blur(function() {
    removeKeyBox();
  });

  var that = this;

  function removeKeyBox() {
    $('.key-box').remove();

    var i, adapter;
    for (i = 0; i < that._adapters.length; i++) {
      adapter = that._adapters[i];
      if (adapter.removeKeyBox) {
        adapter.removeKeyBox();
      }
    }
  }

  function drawKeyBox() {
    var i, adapter;
    for (i = 0; i < that._adapters.length; i++) {
      adapter = that._adapters[i];
      if (adapter.drawKeyBox) {
        adapter.drawKeyBox();
      }
    }
  }
};

scout.KeystrokeManager.prototype.installAdapter = function($element, adapter) {
  if (!$element) {
    throw 'missing argument \'$element\'';
  }
  if (!adapter) {
    throw 'missing argument \'adapter\'';
  }

  var controller = function(event) {
    var i, handler, accepted;
    var bubbleUp = true;

    if (!adapter.handlers) {
      return;
    }
    for (i = 0; i < adapter.handlers.length; i++) {
      handler = adapter.handlers[i];
      accepted = false;
      if (handler.accept && handler.accept(event)) {
        bubbleUp = bubbleUp && handler.handle(event);
        if (handler.removeKeyBox) {
          adapter.removeKeyBox();
        }
      }
    }
    if (!bubbleUp) {
      $element.find('.key-box').remove();
      event.stopPropagation();
    }
  };

  this._adapters.push(adapter);
  adapter.$target = $element;
  adapter.controller = controller;
  $element.keydown(controller);
};

scout.KeystrokeManager.prototype.uninstallAdapter = function(adapter) {
  if (!adapter) {
    throw 'missing argument \'adapter\'';
  }

  scout.arrays.remove(this._adapters, adapter);
  if (adapter.$target) {
    adapter.$target.off('keydown', undefined, adapter.controller);
  }
};

//Singleton
scout.keystrokeManager = new scout.KeystrokeManager();
