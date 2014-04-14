// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Desktop = function(session, $parent, model) {
  this.session = session;
  this.tree;
  this._$parent = $parent;
  this.session.widgetMap[model.id] = this;

  //  this.$entryPoint.addClass('desktop'); //FIXME desktop elements use ids, maybe better change to class to support multiple session divs with multiple desktops

  // create all 4 containers
  var view = new scout.DesktopViewButtonBar(this.session, $parent, model.viewButtons);
  var tool = new scout.DesktopToolButton(this.session, $parent, model.toolButtons);
  var tree = new scout.DesktopTreeContainer(this.session, $parent, model.outline);
  var bench = new scout.DesktopBench(this.session, $parent);

  this.tree = tree;
  this.tree.attachModel();

  this._bench = bench;

  // alt and f1-help
  $(window).keydown(function(event) {
    if (event.which == 18) {
      removeKeyBox();
      drawKeyBox();
    }
  });

  $(window).keyup(function(event) {
    if (event.which == 18) {
      removeKeyBox();
      return false;
    }
  });

  $(window).blur(function() {
    removeKeyBox();
  });

  // key handling
  var fKeys = {};
  if (tool) {
    $('.tool-item', tool.$div).each(function(i, e) {
      var shortcut = parseInt($(e).attr('data-shortcut').replace('F', ''), 10) + 111;
      fKeys[shortcut] = e;
    });
  }

  $('body').keydown(function(event) {
    // numbers: views
    if (event.which >= 49 && event.which <= 57) {
      $.log('view');
      $('.view-item', view.$div).eq(event.which - 49).click();
    }

    // filter
    if (event.which >= 65 && event.which <= 90) {
      $('#HeaderOrganize').click();

      // set focus
      var $input = $('#FilterInput'),
        length= $input.val().length;

      $input.focus();
      $input[0].setSelectionRange(length, length);
    }

    // function keys: tools
    if (fKeys[event.which]) {
      $(fKeys[event.which]).click();
      return false;
    }

    // left: up in tree
    if (event.which == 37) {
      $('.selected', tree.$div).prev().click();
      removeKeyBox();
      return false;
    }

    // right: down in tree
    if (event.which == 39) {
      $('.selected', tree.$div).next().click();
      removeKeyBox();
      return false;
    }

    // +/-: open and close tree
    if (event.which == 109 || event.which == 107) {
      $('.selected', tree.$div).children('.tree-item-control').click();
      removeKeyBox();
      return false;
    }

    // table handling
    // todo: make clicked row visible
    if ([38, 40, 36, 35, 33, 34].indexOf(event.which) > -1) {
      var $rowsAll = $('.table-row', bench.$div),
        $rowsSelected = $('.row-selected', bench.$div),
        $rowClick;

      // up: move up
      if (event.which == 38) {
        if ($rowsSelected.length > 0) {
          $rowClick = $rowsSelected.first().prev();
        } else {
          $rowClick = $rowsAll.last();
        }
      }

      // down: move down
      if (event.which == 40) {
        if ($rowsSelected.length > 0) {
          $rowClick = $rowsSelected.last().next();
        } else {
          $rowClick = $rowsAll.first();
        }
      }

      // home: top of table
      if (event.which == 36) {
        $rowClick = $rowsAll.first();
      }

      // end: bottom of table
      if (event.which == 35) {
        $rowClick = $rowsAll.last();
      }

      // pgup: jump up
      var $prev;
      if (event.which == 33) {
        if ($rowsSelected.length > 0) {
          $prev = $rowsSelected.first().prevAll();
          if ($prev.length > 10) {
            $rowClick = $prev.eq(10);
          } else {
            $rowClick = $rowsAll.first();
          }
        } else {
          $rowClick = $rowsAll.last();
        }
      }

      // pgdn: jump down
      if (event.which == 34) {
        if ($rowsSelected.length > 0) {
          $prev = $rowsSelected.last().nextAll();
          if ($prev.length > 10) {
            $rowClick = $prev.eq(10);
          } else {
            $rowClick = $rowsAll.last();
          }
        } else {
          $rowClick = $rowsAll.first();
        }
      }

      $rowClick.trigger('mousedown').trigger('mouseup');
    }
  });

  function removeKeyBox() {
    $('.key-box').remove();
    $('.tree-item-control').show();
  }

  function drawKeyBox() {
    // keys for views
    $('.view-item', view.$div).each(function(i, e) {
      if (i < 9) $(e).appendDiv('', 'key-box', i + 1);
    });

    // keys for tools
    if (tool) {
      $('.tool-item', tool.$div).each(function(i, e) {
        $(e).appendDiv('', 'key-box', $(e).attr('data-shortcut'));
      });
    }

    // keys for tree
    var $node = $('.selected', tree.$div),
      $prev = $node.prev(),
      $next = $node.next();

    if ($node.hasClass('can-expand')) {
      if ($node.hasClass('expanded')) {
        $node.appendDiv('', 'key-box large', '-');
      } else {
        $node.appendDiv('', 'key-box large', '+');
      }
      $node.children('.tree-item-control').hide();
    }

    if ($prev.length) {
      $prev.appendDiv('', 'key-box', '←');
      $prev.children('.tree-item-control').hide();
    }

    if ($next.length) {
      $next.appendDiv('', 'key-box', '→');
      $next.children('.tree-item-control').hide();
    }

    // keys for table
    $node = $('#TableData', bench.$div);
    if ($node.length) {
      $node.appendDiv('', 'key-box top3', 'Home');
      $node.appendDiv('', 'key-box top2', 'PgUp');
      $node.appendDiv('', 'key-box top1', '↑');
      $node.appendDiv('', 'key-box bottom1', '↓');
      $node.appendDiv('', 'key-box bottom2', 'PgDn');
      $node.appendDiv('', 'key-box bottom3', 'End');
    }

    // keys for header
    $node = $('#TableHeader', bench.$div);
    if ($node.length) {
      $node.prependDiv('', 'key-box char', 'a - z');
    }
  }
};

scout.Desktop.prototype.onModelPropertyChange = function() {};

scout.Desktop.prototype.onModelCreate = function(event) {
  if (event.objectType == "Outline") {
    this.tree.onOutlineCreated(event);
  } else if (event.objectType == "Form") {
    if (event.displayHint == "view") {
      //FIXME separate into View and Dialog which use Form?
      new scout.Form(this.session, this._bench.$container, event);
    } else if (event.displayHint == "dialog") {
      new scout.Form(this.session, this._$parent, event);
    } else {
      $.log("Form displayHint not handled: '" + event.displayHint + "'.");
    }
  } else {
    $.log("Widget creation not handled for object type '" + event.objectType + "'.");
  }
};

scout.Desktop.prototype.onModelAction = function(event) {
  if (event.type_ == 'outlineChanged') {
    this.tree.onOutlineChanged(event.outlineId);
  } else if (event.type_ == 'formRemoved') {
    var form = this.session.widgetMap[event.formId];
    if (form) {
      form.hide();
    }
  } else {
    $.log("Model event not handled. Widget: Desktop. Event: " + event.type_ + ".");
  }
};
