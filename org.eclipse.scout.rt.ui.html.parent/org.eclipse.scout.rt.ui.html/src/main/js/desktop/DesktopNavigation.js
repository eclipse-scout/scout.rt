scout.DesktopNavigation = function(desktop) {
  this.desktop = desktop;
  this.session = desktop.session;

  this.$navigation;
  this.$header;
  this.$container;

  this.activeTab;
  this.$outlineTitle;
};

scout.DesktopNavigation.prototype.render = function($parent) {
  // create main element
  this.$navigation = $parent.appendDIV('desktop-navigation');
  this.$header = this.$navigation.appendDIV('navigation-header');

  //  create outline tabs
  var outlineTab = new scout.DesktopNavigation.TabAndContent(this._createOutlinesTab());
  outlineTab.$tab.on('click', function() { this._setActiveTab(outlineTab); }.bind(this));

  //  create search tabs
  var searchTab = new scout.DesktopNavigation.TabAndContent(this._createSearchTab());
  searchTab.$tab.on('click', function() { this._setActiveTab(searchTab); }.bind(this));

  this.$container = this.$navigation.appendDIV('navigation-container');
  this._setActiveTab(outlineTab);

  this._addSplitter();
};

scout.DesktopNavigation.TabAndContent = function($tab) {
  this.$tab = $tab;
  this.$storage = null;
};

// outline tab creation

scout.DesktopNavigation.prototype._createOutlinesTab = function() {
  var that = this,
    doNotOpen = false;

  // create tab
  var $tab = this.$header.appendDIV('navigation-tab-outline');

  // create button
  var $menuButton = $tab.appendDIV('navigation-tab-outline-button')
    .on('click', openMenu);

  // create menu
  var $outlinesMenu = $tab.appendDIV('navigation-tab-outline-menu');
  for (var i = 0; i < this.desktop.viewButtons.length; i++) {
    this.desktop.viewButtons[i].render($outlinesMenu);
  }

  // create title of active outline
  var $outlineTitle = $tab.appendDIV('navigation-tab-outline-title');
  $outlineTitle.click(function() {
    that.outline.clearSelection();
    that.outline.collapseAll();
  });

  // save and return
  this.$outlineTitle = $outlineTitle;
  return $tab;

  function openMenu() {
    if ($tab.hasClass('tab-active') && !doNotOpen) {
      that.$header.addClass('tab-menu-open');
      $(document).one('mousedown.remove keydown.remove', closeMenu);
    } else {
      doNotOpen = false;
    }
  }

  function closeMenu(event) {
    // in case of click on 'open menu button': close, but do not reopen
    if ($(event.target).is($menuButton)) {
      doNotOpen = true;
    }

    // in case of menu item was clicked: remove class after handling click event
    if ($outlinesMenu.has($(event.target)).length === 0) {
      that.$header.removeClass('tab-menu-open');
    }

    $(document).off('.remove');
  }
};

scout.DesktopNavigation.prototype._createSearchTab = function() {
  // create tab
  var $tab = this.$header.appendDIV('navigation-tab-search');

  // create field
  var $queryField = $('<input class="navigation-tab-search-field">').val('Suchbegriff');
  $tab.append($queryField);

  // create button
  $tab.appendDIV('navigation-tab-search-button')
    .on('click',  function() {
        this.session.send('search', this.desktop.id, { 'query': $queryField.val() });
    }.bind(this));

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

  // TODO cru: save in model?
  // this.session.send('desktopTabClicked', this.desktop.id, {'tabId' : tab.tabId });
};

// event handling

scout.DesktopNavigation.prototype.onOutlineChanged = function(outline) {
  if (this.outline === outline) {
    this.$header.removeClass('tab-menu-open');
    return;
  }

  if (this.outline) {
    this.outline.remove();
  }

  this.outline = outline;
  this.outline.render(this.$container);
  this.$outlineTitle.html(this.outline.title);
  this.$header.removeClass('tab-menu-open');
};

scout.DesktopNavigation.prototype.onSearchPerformed = function(event) {
  this.$container.empty().appendDIV('search-status', event.status);
};

//vertical splitter

scout.DesktopNavigation.prototype._addSplitter = function() {
  this._$splitter = this.$navigation.appendDIV('navigation-splitter-vertical')
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

     that.$navigation.width(w);
     that.desktop.$bar.css('left', w);
     that.desktop.$bench.css('left', w);

     if (w <= WIDTH_BREADCRUMB) {
       if (!that.$navigation.hasClass('navigation-breadcrumb')) {
         that.$navigation.addClass('navigation-breadcrumb');
         that.outline.setBreadcrumb(true);
       }
     } else {
       that.$navigation.removeClass('navigation-breadcrumb');
       that.outline.setBreadcrumb(false);
     }

     scout.Scrollbar2.update(that.outline._$viewport);
   }

   function resizeEnd() {
     $('body').off('mousemove')
       .removeClass('col-resize');

     if (w < WIDTH_BREADCRUMB) {
       that.$navigation.animateAVCSD('width', WIDTH_BREADCRUMB);
       that.desktop.$bar.animateAVCSD('left', WIDTH_BREADCRUMB);
       that.desktop.$bench.animateAVCSD('left', WIDTH_BREADCRUMB);
     }

     that.desktop.onResize();
   }

   return false;
  }
};

/**
 * Called by DesktopViewButton.js
 */
scout.DesktopNavigation.prototype.onOutlinePropertyChange = function(event) {
  for (var propertyName in event.properties) {
    if (propertyName === "text") {
      this.$outlineTitle.text(event.properties[propertyName]);
    }
  }
};
