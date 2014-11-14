scout.DesktopNavigation = function(desktop) {
  this.desktop = desktop;
  this.session = desktop.session;

  this.$navigation;
  this.$header;
  this.$container;

  this.activeTab;
  this.$outlineTitle;
  this.previousOutline;
};

scout.DesktopNavigation.prototype.render = function($parent) {
  // create main element
  this.$navigation = $parent.appendDIV('desktop-navigation');
  this.$header = this.$navigation.appendDIV('navigation-header');

  //  create outline tabs
  this.outlineTab = new scout.DesktopNavigation.TabAndContent(this._createOutlinesTab());
  this.outlineTab.$tab.on('click', function() {
    //Switch tab if search outline is selected. Otherwise outline menu gets opened
    if (this.desktop.outline === this.desktop.searchOutline) {
      if (!this.previousOutline) {
        //FIXME CGU passiert nach F5 in der Search outline. irgendwie fehlt eine initiale outline, aktueller Workaround: open Menu
//        this.previousOutline = this.desktop.initialOutline;
        this._openMenu();
      } else {
        this._selectTab(this.outlineTab, this.previousOutline);
      }
    }
  }.bind(this));

  //  create search tabs
  var searchTab = new scout.DesktopNavigation.TabAndContent(this._createSearchTab());
  searchTab.$tab.on('click', function() {
    this._selectTab(searchTab, this.desktop.searchOutline);
  }.bind(this));

  this.$container = this.$navigation.appendDIV('navigation-container');
  this._setActiveTab(this.outlineTab);

  this._addSplitter();
};

scout.DesktopNavigation.prototype._selectTab = function(tab, outline) {
   this.desktop.changeOutline(outline);
   this.session.send('outlineChanged', this.desktop.id, {'outlineId' : outline.id });
   this._setActiveTab(tab);
};

scout.DesktopNavigation.TabAndContent = function($tab) {
  this.$tab = $tab;
  this.$storage = null;
};

// outline tab creation

scout.DesktopNavigation.prototype._createOutlinesTab = function() {
  var that = this;

  // create tab
  var $tab = this.$header.appendDIV('navigation-tab-outline');

  // create button
  this.$menuButton = $tab.appendDIV('navigation-tab-outline-button')
    .on('mousedown', this._onMenuButtonClicked.bind(this));

  // create menu
  // TODO AWE: use Popup class here, maybe a new "head" style is required here
  // than we could use the same popup for the phone-form
  this.$outlinesMenu = $tab.appendDIV('navigation-tab-outline-menu');
  for (var i = 0; i < this.desktop.viewButtons.length; i++) {
    this.desktop.viewButtons[i].render(this.$outlinesMenu);
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
};

scout.DesktopNavigation.prototype._onMenuButtonClicked = function(event) {
  if (this.$header.hasClass('tab-menu-open')) {
    this._closeMenu();
  } else if (this.activeTab === this.outlineTab) {
    this._openMenu();
  }
};

scout.DesktopNavigation.prototype._openMenu = function() {
  this.$header.addClass('tab-menu-open');
  $(document).one('mousedown.remove keydown.remove', onCloseEvent.bind(this));

  function onCloseEvent(event) {
    if ($(event.target).is(this.$menuButton)) {
      return;
    }

    // close the menu if a menu item was clicked
    if (this.$outlinesMenu.has($(event.target)).length === 0) {
      this._closeMenu();
    }
  }
};

scout.DesktopNavigation.prototype._closeMenu = function() {
  this.$header.removeClass('tab-menu-open');
  $(document).off('.remove');
};

scout.DesktopNavigation.prototype._createSearchTab = function() {
  // create tab
  var $tab = this.$header.appendDIV('navigation-tab-search');

  // create field
  var $queryField = $('<input>')
    .addClass('navigation-tab-search-field')
    .placeholder(scout.texts.get('searchFor'))
    .appendTo($tab);

  // create button
  $tab.appendDIV('navigation-tab-search-button')
    .on('click', function() {
      this.session.send('search', this.desktop.id, {
        'query': $queryField.val()
      });
    }.bind(this));

  return $tab;
};

// tab state and container handling

scout.DesktopNavigation.prototype._setActiveTab = function(tab) {
  var oldTab = this.activeTab;
  if (oldTab === tab) {
    return;
  }

  if (oldTab) {
    oldTab.$tab.removeClass('tab-active');
    //FIXME CGU remove ?
//    oldTab.$storage = this.$container.children();
//    this.$container.children().detach();
//    this.$container.append(newTab.$storage);
  }

  tab.$tab.addClass('tab-active');
  this.activeTab = tab;
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

  //Remember previous outline when switching to search outline
  if (outline === this.desktop.searchOutline) {
    this.previousOutline = this.outline;
  }
  this.outline = outline;
  this.outline.render(this.$container);
  this.$outlineTitle.html(this.outline.title);
  this.$header.removeClass('tab-menu-open');
};

scout.DesktopNavigation.prototype.onSearchPerformed = function(event) {
//FIXME CGU search status in SearchOutline.js schieben?
//  this.$container.empty().appendDIV('search-status', event.status);
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
