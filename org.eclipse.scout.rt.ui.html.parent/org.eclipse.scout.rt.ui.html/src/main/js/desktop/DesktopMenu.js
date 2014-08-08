// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.DesktopMenu = function(desktop, $parent) {

  // TODO AWE: (search) naming mit CGU/CRU besprechen DesktopMenu VS DesktopMenubar
  // letzteres evtl. besser DesktopBenchMenubar nennen?

  // TODO AWE: (search) DesktopMenu.js mit DesktopNavigation.js mergen

  this.desktop = desktop;
  this.session = desktop.session;

  this.$tabHeader;
  this.$tabContainer;
  this.$queryField;
  this.$activeOutline;
  this.$outlinesMenu;

  //  create container
  this.$tabHeader = $parent.appendDiv('DesktopTreeTabHeader');
  this.$tabHeader.append(this._createOutlinesTabHead());
  this.$tabHeader.append(this._createSearchTabHead());

  this.$tabContainer = $parent.appendDiv('DesktopTreeTabContainer');
  this.$tabContainer.append(this._createSearchTabBody());
};

scout.DesktopMenu.prototype._getSearchOutline = function() {
  // TODO AWE: (search) get real search outline.
  return {
    'query': 'Suchen',
    'icon': 'S'
  };
};

scout.DesktopMenu.prototype._createOutlinesTabHead = function() {
  // button
  var $button = $('<button>M</button>');
  $button.on('click', function() { this._showOutlinesMenu(); }.bind(this));
  // active outline
  this.$activeOutline = $('<div class="active-outline"></div>');
  this.$activeOutline.html(this.desktop.outline.text);
  // menu
  this.$outlinesMenu = $('<ul class="outlines-menu"></ul>');
  this.$outlinesMenu.css('display', 'none');
  for (var i = 0; i < this.desktop.viewButtons.length; i++) {
    var $li = $('<li></li>');
    this.desktop.viewButtons[i].render($li);
    this.$outlinesMenu.append($li);
  }
  // tab
  var $tab = $('<div class="outlines-tab-head"></div>');
  $tab.append($button);
  $tab.append(this.$activeOutline);
  $tab.append(this.$outlinesMenu);
  return $tab;
};

scout.DesktopMenu.prototype._createOutlinesTabBody = function() {
  // TODO AWE: impl. _createOutlinesTabBody
};

scout.DesktopMenu.prototype._showOutlinesMenu = function() {
  var visible = this.$outlinesMenu.css('display') === 'none';
  this.$outlinesMenu.css('display', visible ? 'auto' : 'none');
};

scout.DesktopMenu.prototype.onOutlineChanged = function(outline) {
  this.$activeOutline.html(outline.title);
};

scout.DesktopMenu.prototype._createSearchTabHead = function() {
  var searchOutline = this._getSearchOutline();
  var $tab = $('<div class="search-tab-head"></div>');
  this.$queryField = $('<input type="text" />');
  this.$queryField.val(searchOutline.query);
  var $button = $('<button>');
  $button.
    html(searchOutline.icon).
    on('click', function() { this._search(); }.bind(this));
  $tab.append(this.$queryField).append($button);
  return $tab;
};

scout.DesktopMenu.prototype._createSearchTabBody = function() {
  var $body = $('<div class="search-tab-body"></div>');
  this.$searchStatus = $('<div class="search-status"></div>');
  $body.append(this.$searchStatus);
  // TODO AWE: (search) hier den tree für die suchergebnisse hinzufügen
  return $body;
};

scout.DesktopMenu.prototype._search = function() {
  var query = this.$queryField.val();
  this.session.send('search', this.desktop.id, {
    'query': query
  });
};

scout.DesktopMenu.prototype.onSearchPerformed = function(event) {
  this.$searchStatus.html(event.status);
};

