/*
 * Copyright (c) 2014-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Desktop, DesktopHeaderLayout, Form, HtmlComponent, scout, Widget} from '../../index';

export default class DesktopHeader extends Widget {

  constructor() {
    super();
    this.tabArea = null;
    this.toolBoxVisible = true;
    this.viewButtonBox = null;
    this.viewButtonBoxVisible = false;
    this._desktopPropertyChangeHandler = this._onDesktopPropertyChange.bind(this);
    this._desktopAnimationEndHandler = this._onDesktopAnimationEnd.bind(this);
    this._outlineContentMenuBarPropertyChangeHandler = this._onOutlineContentMenuBarPropertyChange.bind(this);
    this._viewButtonBoxPropertyChangeHandler = this._onViewButtonBoxPropertyChange.bind(this);
  }

  _init(model) {
    super._init(model);
    this.desktop = this.session.desktop;
    this.updateViewButtonBoxVisibility();
    this.tabArea = this._createTabArea();
  }

  _createTabArea() {
    return scout.create('SimpleTabArea', $.extend({
      parent: this
    }, this.tabArea));
  }

  _render() {
    this.$container = this.$parent.appendDiv('desktop-header');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new DesktopHeaderLayout(this));
    this._renderViewButtonBoxVisible();
    this._renderViewTabs();
    this._renderToolBoxVisible();
    this._renderLogoUrl();
    this._renderInBackground();
    this.desktop.on('propertyChange', this._desktopPropertyChangeHandler);
    this.desktop.on('animationEnd', this._desktopAnimationEndHandler);
    if (this.desktop.bench) {
      this.outlineContent = this.desktop.bench.outlineContent;
    }
    this._attachOutlineContentMenuBarHandler();
  }

  _remove() {
    this.desktop.off('propertyChange', this._desktopPropertyChangeHandler);
    this.desktop.off('animationEnd', this._desktopAnimationEndHandler);
    this._detachOutlineContentMenuBarHandler();
    this.outlineContent = null;
    super._remove();
  }

  _renderViewTabs() {
    this.tabArea.render();
  }

  _renderToolBox() {
    if (this.toolBox) {
      return;
    }
    this.toolBox = this._createToolBox();
    this.toolBox.render();
  }

  _createToolBox() {
    return scout.create('DesktopToolBox', {
      parent: this,
      menus: this.desktop.menus
    });
  }

  _removeToolBox() {
    if (!this.toolBox) {
      return;
    }
    this.toolBox.destroy();
    this.toolBox = null;
  }

  _renderToolBoxVisible() {
    if (this.toolBoxVisible) {
      this._renderToolBox();
    } else {
      this._removeToolBox();
    }
    this.invalidateLayoutTree();
  }

  _renderLogoUrl() {
    if (this.logoUrl) {
      this._renderLogo();
    } else {
      this._removeLogo();
    }
    this.invalidateLayoutTree();
  }

  _renderLogo() {
    if (this.desktop.displayStyle === Desktop.DisplayStyle.COMPACT) {
      // Do not render logo in compact mode (wastes space)
      return;
    }
    if (!this.logo) {
      this.logo = this._createLogo();
      this.logo.render();
    } else {
      this.logo.setUrl(this.logoUrl);
    }
  }

  _createLogo() {
    return scout.create('DesktopLogo', {
      parent: this,
      url: this.logoUrl
    });
  }

  _removeLogo() {
    if (!this.logo) {
      return;
    }
    this.logo.destroy();
    this.logo = null;
  }

  _renderViewButtonBox() {
    if (this.viewButtonBox) {
      return;
    }
    this.viewButtonBox = this._createViewButtonBox();
    this.viewButtonBox.on('propertyChange', this._viewButtonBoxPropertyChangeHandler);
    this.viewButtonBox.render();
    this.viewButtonBox.$container.prependTo(this.$container);
    if (this.desktop.inBackground) {
      this.viewButtonBox.sendToBack();
    }
    this.updateViewButtonStyling();
  }

  _createViewButtonBox() {
    return scout.create('ViewButtonBox', {
      parent: this,
      viewButtons: this.desktop.viewButtons
    });
  }

  _removeViewButtonBox() {
    if (!this.viewButtonBox) {
      return;
    }
    this.viewButtonBox.off('propertyChange', this._viewButtonBoxPropertyChangeHandler);
    this.viewButtonBox.destroy();
    this.viewButtonBox = null;
  }

  _renderViewButtonBoxVisible() {
    if (this.viewButtonBoxVisible) {
      this._renderViewButtonBox();
    } else {
      this._removeViewButtonBox();
    }
    this.invalidateLayoutTree();
  }

  sendToBack() {
    if (this.viewButtonBox) {
      this.viewButtonBox.sendToBack();
    }
    if (this.rendered) {
      this._renderInBackground();
    }
  }

  bringToFront() {
    if (this.viewButtonBox) {
      this.viewButtonBox.bringToFront();
    }
    if (this.rendered) {
      this._renderInBackground();
    }
  }

  _renderInBackground() {
    this.$container.toggleClass('in-background', this.desktop.inBackground);
  }

  setLogoUrl(logoUrl) {
    this.setProperty('logoUrl', logoUrl);
  }

  setToolBoxVisible(visible) {
    this.setProperty('toolBoxVisible', visible);
  }

  setViewButtonBoxVisible(visible) {
    this.setProperty('viewButtonBoxVisible', visible);
  }

  setMenus(menus) {
    if (this.toolBox) {
      this.toolBox.setMenus(menus);
    }
  }

  updateViewButtonBoxVisibility() {
    // View buttons are visible in the header if the navigation is not visible
    // If there are no view buttons at all, don't show the box
    // With displayStyle is set to compact, the view buttons should never be visible in the header
    this.setViewButtonBoxVisible(this.desktop.viewButtons.length > 0 && !this.desktop.navigationVisible && this.desktop.displayStyle !== Desktop.DisplayStyle.COMPACT);
  }

  _attachOutlineContentMenuBarHandler() {
    if (!this.outlineContent) {
      return;
    }
    let menuBar = this._outlineContentMenuBar(this.outlineContent);
    if (menuBar) {
      menuBar.on('propertyChange', this._outlineContentMenuBarPropertyChangeHandler);
    }
  }

  _detachOutlineContentMenuBarHandler() {
    if (!this.outlineContent) {
      return;
    }
    let menuBar = this._outlineContentMenuBar(this.outlineContent);
    if (menuBar) {
      menuBar.off('propertyChange', this._outlineContentMenuBarPropertyChangeHandler);
    }
  }

  _outlineContentMenuBar(outlineContent) {
    if (outlineContent instanceof Form) {
      return outlineContent.rootGroupBox.menuBar;
    }
    return outlineContent.menuBar;
  }

  updateViewButtonStyling() {
    if (!this.viewButtonBoxVisible || !this.desktop.bench || !this.desktop.bench.outlineContentVisible) {
      return;
    }
    let outlineContent = this.desktop.bench.outlineContent;
    if (!outlineContent) {
      // Outline content not available yet (-> needs to be loaded first)
      return;
    }
    let hasMenuBar = false;
    if (outlineContent instanceof Form && outlineContent.detailForm) {
      let rootGroupBox = outlineContent.rootGroupBox;
      hasMenuBar = rootGroupBox.menuBar && rootGroupBox.menuBarVisible && rootGroupBox.menuBar.visible;
    } else {
      hasMenuBar = outlineContent.menuBar && outlineContent.menuBar.visible;
    }
    this.$container.toggleClass('outline-content-has-menubar', !!hasMenuBar);
  }

  _onDesktopNavigationVisibleChange(event) {
    // If navigation gets visible: Hide view buttons immediately
    // If navigation gets hidden using animation: Show view buttons when animation ends
    if (this.desktop.navigationVisible) {
      this.updateViewButtonBoxVisibility();
    }
  }

  _onDesktopAnimationEnd(event) {
    this.updateViewButtonBoxVisibility();
  }

  onBenchOutlineContentChange(content) {
    this._detachOutlineContentMenuBarHandler();
    this.outlineContent = content;
    this.updateViewButtonStyling();
    this._attachOutlineContentMenuBarHandler();
  }

  _onDesktopPropertyChange(event) {
    if (event.propertyName === 'navigationVisible') {
      this._onDesktopNavigationVisibleChange();
    }
  }

  _onOutlineContentMenuBarPropertyChange(event) {
    if (event.propertyName === 'visible') {
      this.updateViewButtonStyling();
    }
  }

  _onViewButtonBoxPropertyChange(event) {
    if (event.propertyName === 'menuButtons' || event.propertyName === 'tabButtons') {
      this.invalidateLayoutTree();
    }
  }
}
