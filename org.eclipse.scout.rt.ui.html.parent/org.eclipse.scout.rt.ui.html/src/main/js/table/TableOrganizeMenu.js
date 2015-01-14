scout.TableOrganizeMenu = function() {
  scout.TableOrganizeMenu.parent.call(this);
};

scout.inherits(scout.TableOrganizeMenu, scout.Menu);

// FIXME CRU: implement table organize menu, copy-columns-width menu (used for Scout SDK)
scout.TableOrganizeMenu.prototype._onMenuClicked = function(event) {
  var popup = new scout.PopupMenuItem($(event.target));
  popup.render();
  popup.addClassToBody('table-menu-organize');

  popup.appendToBody(this._createSlotSave(popup));
  popup.appendToBody(this._createSlotLoad(popup));
  popup.appendToBody(this._createFixedColumn(popup));
  this._setFixedColumn(2, null);

  popup.alignTo();
};

scout.TableOrganizeMenu.prototype._createSlotSave = function(popup) {
  var $group;

  $group = $.makeDiv('group');

  $group.appendDiv('text', 'Spalteneinstellungen speichern');
  $group.appendDiv('slot', 'Slot 1').data('id', 1).attr('contentEditable', true);
  $group.appendDiv('slot', 'Slot 2').data('id', 2);
  $group.appendDiv('slot', 'Slot 3').data('id', 3);

  $('.slot', $group)
    .click(function() {
        $.suppressEvent(event);
        //popup.remove();
        this.session.send('saveColumns', this.parent.id, $(event.target).data('id'));

      }.bind(this))
    .one(scout.menus.CLOSING_EVENTS, $.suppressEvent)
    .mousedown(function() {
      $.suppressEvent(event);
    }.bind(this));

  $group.appendDiv('rename')
    .html('umbe-<br>nennen')
    .click(function() {

      if ($(event.target).hasClass('rename-active')) {
        $('.slot', $group).attr('contentEditable', false);
        $('.slot', $group).removeClass('rename-active');
        $('.rename', $group).removeClass('rename-active');
        $(event.target).html('umbe-<br>nennen');
      } else {
        $('.slot', $group).attr('contentEditable', true);
        $('.slot', $group).addClass('rename-active');
        $('.rename', $group).addClass('rename-active');
        $(event.target).html('Name<br>speichern');
      }

      return false;
    }.bind(this))
    .on(scout.menus.CLOSING_EVENTS, $.suppressEvent);

  return $group;
};


scout.TableOrganizeMenu.prototype._createSlotLoad = function(popup) {
  var $group;

  $group = $.makeDiv('group');

  $group.appendDiv('text', 'Spalteneinstellungen wiederherstellen');
  $group.appendDiv('slot', 'Slot 1').data('id', 1);
  $group.appendDiv('slot empty', 'Slot 2').data('id', 2);
  $group.appendDiv('slot empty', 'Slot 3').data('id', 3);
  $group.appendDiv('slot', 'BSI CRM').data('id', 0);

  $('.slot', $group)
    .click(function() {
        popup.remove();
        this.session.send('loadColumns', this.parent.id, $(event.target).data('id'));
      }.bind(this))
    .one(scout.menus.CLOSING_EVENTS, $.suppressEvent);

  return $group;
};

scout.TableOrganizeMenu.prototype._createFixedColumn = function() {
  var $group;

  $group = $.makeDiv('group');

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

  return $group;
};

scout.TableOrganizeMenu.prototype._setFixedColumn = function(value, diff) {
  var $fixed = $('.fixed', this.$body),
    newValue;

  if (value) {
    newValue = value;
  }
  else if (diff) {
    newValue = $fixed.text();
    newValue = isNaN(newValue) ? 0 : parseFloat(newValue);
    newValue = Math.max(newValue + diff, 0);
  }

  if (newValue === 0) {
    $fixed.text("ohne");
  } else {
    $fixed.text(newValue);
  }
};
