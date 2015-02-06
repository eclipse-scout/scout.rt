scout.TableOrganizeMenu = function() {
  scout.TableOrganizeMenu.parent.call(this);
};

scout.inherits(scout.TableOrganizeMenu, scout.Menu);

// FIXME CGU: finalize ;)
scout.TableOrganizeMenu.prototype._onMenuClicked = function(event) {
  // basic frame
  var popup = new scout.PopupMenuItem($(event.target));
  popup.render();
  popup.addClassToBody('table-menu-organize');

  // organize elements
  popup.appendToBody(this._createSlotSave(popup));
  popup.appendToBody(this._createSlotLoad(popup));
  popup.appendToBody(this._createFixedColumn(popup));
  this._setFixedColumn(2, null);

  // finalize design
  popup.alignTo();
};

scout.TableOrganizeMenu.prototype._createSlotSave = function(popup) {
  // main element
  var $group = $.makeDiv('group');

  // add slots
  $group.appendDiv('text', 'Spalteneinstellungen speichern');
  $group.appendDiv('slot', 'Slot 1').data('id', 1);
  $group.appendDiv('slot', 'Slot 2').data('id', 2);
  $group.appendDiv('slot', 'Slot 3').data('id', 3);

  // event handling for slots
  $('.slot', $group)
    .click(function() {
        if ($(event.target).hasClass('rename-active')) {
          $.suppressEvent(event);
        } else {
          popup.remove();
          this.session.send('saveColumns', this.parent.id, $(event.target).data('id'));
        }
      }.bind(this))
    .on(scout.menus.CLOSING_EVENTS, $.suppressEvent);

  // button toggels rename mode
  $group.appendDiv('rename')
    .html('Slots umbenennen')
    .click(function() {
      if ($(event.target).hasClass('rename-active')) {
          $('.slot', $group)
            .attr('contentEditable', false)
            .removeClass('rename-active');
          $(event.target)
            .html('Slots umbenennen')
            .removeClass('rename-active');
          $('.slot', $group).each(function (index){
            $('.slot', $group.parent()).eq(index + 3).text($(this).text());
          });
        } else {
          $('.slot', $group)
            .attr('contentEditable', true)
            .addClass('rename-active');
          $(event.target)
            .html('Namen speichern')
            .addClass('rename-active');
        }
        return false;
      }.bind(this))
    .on(scout.menus.CLOSING_EVENTS, $.suppressEvent);

  return $group;
};


scout.TableOrganizeMenu.prototype._createSlotLoad = function(popup) {
  // main element
  var $group = $.makeDiv('group');

  // add slots
  $group.appendDiv('text', 'Spalteneinstellungen wiederherstellen');
  $group.appendDiv('slot', 'Slot 1').data('id', 1);
  $group.appendDiv('slot empty', 'Slot 2').data('id', 2);
  $group.appendDiv('slot empty', 'Slot 3').data('id', 3);
  $group.appendDiv('slot', 'BSI CRM').data('id', 0);

  //event handling for slots
  $('.slot', $group)
    .click(function() {
      popup.remove();
      this.session.send('loadColumns', this.parent.id, $(event.target).data('id'));
    }.bind(this))
    .one(scout.menus.CLOSING_EVENTS, $.suppressEvent);

  return $group;
};

scout.TableOrganizeMenu.prototype._createFixedColumn = function() {
  // main element
  var $group = $.makeDiv('group');

  // add elements
  $group.appendDiv('text', 'Spalten fixieren');
  $group.appendDiv('fixed');

  $group.appendDiv('minus')
    .click(function() {
      this._setFixedColumn(null, -1);
    }.bind(this))
    .on(scout.menus.CLOSING_EVENTS, $.suppressEvent);

  $group.appendDiv('plus')
    .click(function() {
      this._setFixedColumn(null, 1);
     }.bind(this))
    .on(scout.menus.CLOSING_EVENTS, $.suppressEvent);

  // temp. solution to show the old column organize dialog
  $group.appendDiv('slot', 'Organize')
    .click(function() {
      this.session.send(this.id, 'clicked');
    }.bind(this))
    .on(scout.menus.CLOSING_EVENTS, $.suppressEvent);

  return $group;
};

scout.TableOrganizeMenu.prototype._setFixedColumn = function(value, diff) {
  // basics
  var $fixed = $('.fixed', this.$body),
    newValue;

  // adds diff or sets to value
  if (value) {
    newValue = value;
  }
  else if (diff) {
    newValue = $fixed.text();
    newValue = isNaN(newValue) ? 0 : parseFloat(newValue);
    newValue = Math.max(newValue + diff, 0);
  }

  // set correct text
  if (newValue === 0) {
    $fixed.text("ohne");
  } else {
    $fixed.text(newValue);
  }
};
