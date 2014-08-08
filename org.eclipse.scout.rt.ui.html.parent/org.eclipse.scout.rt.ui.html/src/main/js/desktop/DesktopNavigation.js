scout.DesktopNavigation = function(desktop) {
  this.desktop = desktop;
  this.session = desktop.session;
  this.outline = desktop.outline;

  this.$navigation;
  this.$header;
  this.$container;

  this.activeTab;
  this.$activeOutline;
<<<<<<< Upstream, based on branch 'develop' of ssh://cru@git.bsiag.com:29418/tools/eclipse.scout
  this.$activeTab;
  this.$outlinesMenu;

  //  create tab container
  var outlineTab = new scout.TabAndBody(
    'outlines',
    this._createOutlinesTabHead(),
    this._createOutlinesTabBody());
  outlineTab.$head.on('click', function() { this._setActiveTab(outlineTab); }.bind(this));
  outlineTab.$outlineParent = outlineTab.$body;

  var searchTab = new scout.TabAndBody(
      'search',
      this._createSearchTabHead(),
      this._createSearchTabBody());
  searchTab.$head.on('click', function() { this._setActiveTab(searchTab); }.bind(this));
  searchTab.$outlineParent = searchTab.$body.find('#DesktopTree');

  this.$tabHeader = this._$navigation.appendDiv('DesktopTreeTabHeader');
  this.$tabHeader.append(outlineTab.$head);
  this.$tabHeader.append(searchTab.$head);

  this.$tabContainer = this._$navigation.appendDiv('DesktopTreeTabContainer');
  this.$tabContainer.append(outlineTab.$body);

  this.activeTab = outlineTab;
=======
>>>>>>> 9a0c84e html ui : first step to new design
};

<<<<<<< Upstream, based on branch 'develop' of ssh://cru@git.bsiag.com:29418/tools/eclipse.scout
scout.TabAndBody = function(tabId, $head, $body) {
  this.tabId = tabId;
  this.$head = $head;
  this.$body = $body;
  this.$outlineParent;
=======
scout.DesktopNavigation.prototype.render = function($parent) {
  // create main element
  this.$navigation = $parent.appendDIV('desktop-navigation');

  //  create outline tabs
  var outlineTab = new scout.TabAndContent(this._createOutlinesTab());
  outlineTab.$tab.on('click', function() { this._setActiveTab(outlineTab); }.bind(this));

  //  create search tabs
  var searchTab = new scout.TabAndContent(this._createSearchTab());
  searchTab.$tab.on('click', function() { this._setActiveTab(searchTab); }.bind(this));

  this.$header = this.$navigation.appendDIV('navigation-header');
  this.$header.append(outlineTab.$tab);
  this.$header.append(searchTab.$tab);

  this.$container = this.$navigation.appendDIV('navigation-container');
  this._setActiveTab(outlineTab);

  this._addSplitter(this.$navigation);
};

scout.TabAndContent = function($tab, $content) {
  this.$tab = $tab;
  this.$storage = null;
>>>>>>> 9a0c84e html ui : first step to new design
};

// outline tab creation

scout.DesktopNavigation.prototype._createOutlinesTab = function() {
  // create title of active outline
  var $activeOutline = $.makeDIV('navigation-tab-outline-title');

  // create menu
  var $outlinesMenu = $.makeDIV('navigation-tab-outline-menu');
  for (var i = 0; i < this.desktop.viewButtons.length; i++) {
    var $item = $.makeDIV('outline-menu-item')
      .on('click',  function() {
        $button.removeClass('tab-menu-open');
        $outlinesMenu.hide();
      });

    this.desktop.viewButtons[i].render($item);
    $outlinesMenu.append($item);
  }

  // create button
  var $button = $.makeDIV('navigation-tab-outline-button')
    .on('click', function() {
      $button.toggleClass('tab-menu-open');
      $outlinesMenu.toggle();
    });

  // create tab
  var $tab = $.makeDIV('navigation-tab-outline')
    .append($button)
    .append($activeOutline)
    .append($outlinesMenu);

  // save and return
  this.$activeOutline = $activeOutline;
  return $tab;
};

<<<<<<< Upstream, based on branch 'develop' of ssh://cru@git.bsiag.com:29418/tools/eclipse.scout
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
  this.session.send('desktopTabClicked', this.desktop.id, {'tabId' : tab.tabId });
};
=======
scout.DesktopNavigation.prototype._createSearchTab = function() {
  // create query field
  var $queryField = $('<input class="navigation-tab-search-field">').val('Suchbegriff');
>>>>>>> 9a0c84e html ui : first step to new design

<<<<<<< Upstream, based on branch 'develop' of ssh://cru@git.bsiag.com:29418/tools/eclipse.scout
scout.DesktopNavigation.prototype._createOutlinesTabBody = function() {
  return $('<div class="tab-body" id="DesktopTree"></div>');
};
=======
  // create button
  var $button = $.makeDIV('navigation-tab-search-button')
    .on('click',  function() {
    });
>>>>>>> 9a0c84e html ui : first step to new design

<<<<<<< Upstream, based on branch 'develop' of ssh://cru@git.bsiag.com:29418/tools/eclipse.scout
scout.DesktopNavigation.prototype._showOutlinesMenu = function() {
  var visible = this.$outlinesMenu.css('display') === 'none';
  this.$outlinesMenu.css('display', visible ? 'auto' : 'none');
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
=======
  // create tab
  var $tab = $.makeDIV('navigation-tab-search')
    .append(this.$queryField).append($queryField)
    .append(this.$queryField).append($button);

  // return
>>>>>>> 9a0c84e html ui : first step to new design
  return $tab;
<<<<<<< Upstream, based on branch 'develop' of ssh://cru@git.bsiag.com:29418/tools/eclipse.scout
};

scout.DesktopNavigation.prototype._createSearchTabBody = function() {
  var $body = $('<div class="tab-body"></div>');
  var $outline = $('<div class="tab-body" id="DesktopTree"></div>');
  this.$searchStatus = $('<div class="search-status"></div>');
  $body.
    append(this.$searchStatus).
    append($outline);
  return $body;
=======
>>>>>>> 9a0c84e html ui : first step to new design
};

scout.DesktopNavigation.prototype._search = function() {
  var query = this.$queryField.val();
  this.session.send('search', this.desktop.id, {
    'query': query
  });
};

<<<<<<< Upstream, based on branch 'develop' of ssh://cru@git.bsiag.com:29418/tools/eclipse.scout
scout.DesktopNavigation.prototype.renderOutline = function() {
  this.outline.render(this.activeTab.$outlineParent);
  this.$activeOutline.html(this.outline.title);
  this._addVerticalSplitter(this._$navigation);
=======
// tab state and container handling

scout.DesktopNavigation.prototype._setActiveTab = function(newTab) {
  var oldTab = this.activeTab;
  if (oldTab === newTab) {
    return;
  }

  if (oldTab) {
    oldTab.$tab.removeClass('tab-active');
    oldTab.$storage = this.$container.children();
    this.$container.children().detach();
    this.$container.append(newTab.$storage);
  }

  newTab.$tab.addClass('tab-active');
  this.activeTab = newTab;
>>>>>>> 9a0c84e html ui : first step to new design
};

// event handling

scout.DesktopNavigation.prototype.onOutlineChanged = function(outline) {
  this.outline.remove();
  this.outline = outline;
<<<<<<< Upstream, based on branch 'develop' of ssh://cru@git.bsiag.com:29418/tools/eclipse.scout
  this.outline.render(this.activeTab.$outlineParent);
  this.$activeOutline.html(outline.title);
  if (this._$splitter) {
    this._$splitter.appendTo(this._$navigation); //move after tree, otherwise tree overlays splitter after outline change
  }
=======
  this.outline.render(this.$container);
  this.$activeOutline.html(this.outline.title);
>>>>>>> 9a0c84e html ui : first step to new design
};

scout.DesktopNavigation.prototype.onSearchPerformed = function(event) {
  this.$container.empty().appendDIV('search-status', event.status);
};


//vertical splitter

scout.DesktopNavigation.prototype._addSplitter = function($navigation) {
this._$splitter = $navigation.appendDIV('navigation-splitter-vertical')
 .on('mousedown', '', resize);

var that = this;

function resize() {
 var w;

 $('body').addClass('col-resize')
   .on('mousemove', '', resizeMove)
   .one('mouseup', '', resizeEnd);

 function resizeMove(event) {
   w = event.pageX;

   $navigation.width(w);
   $navigation.nextAll().css('left', w);

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
     $navigation.animateAVCSD('width', w);
     $navigation.nextAll().animateAVCSD('left', w);
   }
 }
 return false;
}
};
