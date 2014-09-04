scout.TableOrganizeMenu = function() {
  scout.TableOrganizeMenu.parent.call(this);
};

scout.inherits(scout.TableOrganizeMenu, scout.Menu);

scout.TableOrganizeMenu.prototype._onMenuClicked = function(event) {

  if (this.$container.data('menu-open')) {
    scout.menus.removeContextMenuContainer(this.$menuContainer, this.$container, true);
    this.$container.data('menu-open', false);
  }
  else {
    //FIXME CGU Same code as in Menu.js, improve
    var right = parseFloat(this.parent.$container[0].offsetWidth) - parseFloat(this.$container.position().left) -  parseFloat(this.$container[0].offsetWidth),
      top = this.$container.height() - 7;
    this.$menuContainer = scout.menus.createContextMenuContainer(this.parent.$container, this.$container, undefined, right, top, false, true);
    this._renderContent(this.$menuContainer);
  }
};

scout.TableOrganizeMenu.prototype._renderContent = function($parent) {
  var $content = $parent.appendDIV('table-menu-organize');
  var $clicked = this.$container;

  $('<button>')
    .text('Spalten zurücksetzen')
    .click(onClick.bind(this))
    .one('mousedown.contextMenu', $.suppressEvent) //TODO event list to suppress is defined in createOrRemoveContextMenuContainer
    .appendTo($content);

  function onClick() {
    var table = this.parent;
    scout.menus.removeContextMenuContainer(this.$menuContainer, this.$container);
    this.session.send('resetColumns', table.id);
  }
};

