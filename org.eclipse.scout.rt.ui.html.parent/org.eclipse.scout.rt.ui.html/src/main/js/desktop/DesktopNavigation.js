scout.DesktopNavigation = function(desktop) {
  this.desktop = desktop;
  this.session = desktop.session;
  this.outline = desktop.outline;

  this.$navigation;
  this.$header;
  this.$container;

  this.activeTab;
  this.$activeOutline;
};

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

scout.DesktopNavigation.prototype._createSearchTab = function() {
  // create field
  var $queryField = $('<input class="navigation-tab-search-field">').val('Suchbegriff');

  // create button
  var $button = $.makeDIV('navigation-tab-search-button')
    .on('click',  function() {
        this.session.send('search', this.desktop.id, { 'query': $queryField.val() });
    }.bind(this));


  // create tab
  var $tab = $.makeDIV('navigation-tab-search')
    .append(this.$queryField).append($queryField)
    .append(this.$queryField).append($button);

  // return
  return $tab;
};

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

  // this.session.send('desktopTabClicked', this.desktop.id, {'tabId' : tab.tabId });
};

// event handling

scout.DesktopNavigation.prototype.onOutlineChanged = function(outline) {
  this.outline.remove();
  this.outline = outline;
  this.outline.render(this.$container);
  this.$activeOutline.html(this.outline.title);
};

scout.DesktopNavigation.prototype.onSearchPerformed = function(event) {
  this.$container.empty().appendDIV('search-status', event.status);
};

//vertical splitter

scout.DesktopNavigation.prototype._addSplitter = function($navigation) {
  this._$splitter = $navigation.appendDIV('navigation-splitter-vertical')
   .on('mousedown', '', resize);

  var WIDTH_BREADCRUMB = 190;

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

     if (w <= WIDTH_BREADCRUMB) {
       $navigation.addClass('navigation-breadcrumb');
     } else {
       $navigation.removeClass('navigation-breadcrumb');
     }

     that.outline.scrollbar.initThumb();
   }

   function resizeEnd() {
     $('body').off('mousemove')
       .removeClass('col-resize');

     if (w < WIDTH_BREADCRUMB) {
       $navigation.animateAVCSD('width', WIDTH_BREADCRUMB);
       $navigation.nextAll().animateAVCSD('left', WIDTH_BREADCRUMB);
     }
   }
   return false;
  }
};
