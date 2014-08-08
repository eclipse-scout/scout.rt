scout.DesktopNavigation = function(desktop, $parent) {
  this.desktop = desktop;
  this.session = desktop.session;
  this.outline = desktop.outline;

  this._$navigation = $parent.appendDiv('DesktopNavigation');
  this.$div = this._$navigation;

  // TODO AWE: (search) naming mit CGU/CRU besprechen DesktopMenu VS DesktopMenubar
  // letzteres evtl. besser DesktopBenchMenubar nennen?

  // TODO AWE: unschön: das hier sollte alles in einer render methode passieren
  // und nicht im CTOR

  // TODO AWE/CGU: naming der IDs/Classes überarbeiten: momentan funktioniert
  // der tree nur, wenn er in einem #DesktopTree Element drin ist. Darum habe ich das
  // top-level element der navigation renamed und den "#DesktopTree" in den tab-body
  // verschoben.

  this.$tabHeader;
  this.$tabContainer;
  this.$queryField;
  this.$activeOutline;
  this.$activeTab;
  this.$outlinesMenu;

  //  create tab container
  var outlineTab = new scout.TabAndBody(
    this._createOutlinesTabHead(),
    this._createOutlinesTabBody());
  outlineTab.$head.on('click', function() { this._setActiveTab(outlineTab); }.bind(this));

  var searchTab = new scout.TabAndBody(
      this._createSearchTabHead(),
      this._createSearchTabBody());
  searchTab.$head.on('click', function() { this._setActiveTab(searchTab); }.bind(this));

  this.$tabHeader = this._$navigation.appendDiv('DesktopTreeTabHeader');
  this.$tabHeader.append(outlineTab.$head);
  this.$tabHeader.append(searchTab.$head);

  this.$tabContainer = this._$navigation.appendDiv('DesktopTreeTabContainer');
  this.$tabContainer.append(outlineTab.$body);

  this.activeTab = outlineTab;
};

scout.TabAndBody = function($head, $body) {
  this.$head = $head;
  this.$body = $body;
};

scout.TabAndBody.prototype.getOutlineParent = function() {
  return this.$body;
};

scout.DesktopNavigation.prototype._getSearchOutline = function() {
  // TODO AWE: (search) get real search outline.
  return {
    'query': 'Suchen',
    'icon': 'S'
  };
};

scout.DesktopNavigation.prototype._createOutlinesTabHead = function() {
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
  var $tab = $('<div class="outlines-tab-head active-tab-head"></div>');
  $tab.append($button);
  $tab.append(this.$activeOutline);
  $tab.append(this.$outlinesMenu);
  return $tab;
};

scout.DesktopNavigation.prototype._setActiveTab = function(tab) {
  if (this.activeTab === tab) {
    return;
  }
  var oldTab = this.activeTab;
  oldTab.$head.removeClass('active-tab-head');
  oldTab.$body.detach();
  tab.$head.addClass('active-tab-head');
  this.$tabContainer.append(tab.$body);
  this.activeTab = tab;
};

scout.DesktopNavigation.prototype._createOutlinesTabBody = function() {
  var $body = $('<div class="tab-body" id="DesktopTree"></div>');
  return $body;
};

scout.DesktopNavigation.prototype._showOutlinesMenu = function() {
  var visible = this.$outlinesMenu.css('display') === 'none';
  this.$outlinesMenu.css('display', visible ? 'auto' : 'none');
};

scout.DesktopNavigation.prototype.onOutlineChanged = function(outline) {
  this.$activeOutline.html(outline.title);
};

scout.DesktopNavigation.prototype._createSearchTabHead = function() {
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

scout.DesktopNavigation.prototype._createSearchTabBody = function() {
  var $body = $('<div class="tab-body"></div>');
  this.$searchStatus = $('<div class="search-status"></div>');
  $body.append(this.$searchStatus);
  // TODO AWE: (search) hier den tree für die suchergebnisse hinzufügen
  return $body;
};

scout.DesktopNavigation.prototype._search = function() {
  var query = this.$queryField.val();
  this.session.send('search', this.desktop.id, {
    'query': query
  });
};

scout.DesktopNavigation.prototype.renderOutline = function() {
  this.outline.render(this.activeTab.getOutlineParent());
  this.$activeOutline.html(this.outline.title);
  this._addVerticalSplitter(this._$navigation);
};

scout.DesktopNavigation.prototype.onSearchPerformed = function(event) {
  this.$searchStatus.html(event.status);
};

scout.DesktopNavigation.prototype.onOutlineChanged = function(outline) {
  this.outline.remove();
  this.outline = outline;
  this.$activeOutline.html(outline.title);
  this.outline.render(this.activeTab.getOutlineParent());
  if (this._$splitter) {
    this._$splitter.appendTo(this._$navigation); //move after tree, otherwise tree overlays splitter after outline change
  }
};

scout.DesktopNavigation.prototype._addVerticalSplitter = function($div) {
  this._$splitter = $div.appendDiv(undefined, 'splitter-vertical')
    .on('mousedown', '', resize);

  var that = this;

  function resize() {
    var w;

    $('body').addClass('col-resize')
      .on('mousemove', '', resizeMove)
      .one('mouseup', '', resizeEnd);

    function resizeMove(event) {
      w = event.pageX + 11;

      $div.width(w);
      $div.next().width('calc(100% - ' + (w + 80) + 'px)')
        .css('left', w);

      if (w <= 180) {
        that.outline.doBreadCrumb(true);
      } else {
        that.outline.doBreadCrumb(false);
      }

    }

    function resizeEnd() {
      $('body').off('mousemove')
        .removeClass('col-resize');

      if (w < 180) {
        w = 180;
        $div.animateAVCSD('width', w, null,
            function(i){$div.next().css('width', 'calc(100% - ' + (i + 80) + 'px)'); });
        $div.next().animateAVCSD('left', w);
      }
    }
    return false;
  }
};

