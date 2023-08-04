/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  aria, Desktop, DesktopHeaderEventMap, DesktopHeaderLayout, DesktopHeaderModel, DesktopLogo, DesktopTabArea, DesktopToolBox, Event, EventHandler, Form, HtmlComponent, InitModelOf, Menu, MenuBar, ObjectOrChildModel, OutlineContent,
  PropertyChangeEvent, scout, ViewButtonBox, Widget
} from '../../index';

export class DesktopHeader extends Widget implements DesktopHeaderModel {
  declare model: DesktopHeaderModel;
  declare eventMap: DesktopHeaderEventMap;
  declare self: DesktopHeader;

  desktop: Desktop;
  tabArea: DesktopTabArea;

  logoUrl: string;
  logo: DesktopLogo;

  toolBoxVisible: boolean;
  toolBox: DesktopToolBox;

  viewButtonBoxVisible: boolean;
  viewButtonBox: ViewButtonBox;

  outlineContent: OutlineContent;

  protected _desktopPropertyChangeHandler: EventHandler<PropertyChangeEvent<any, Desktop>>;
  protected _desktopAnimationEndHandler: EventHandler<Event<Desktop>>;
  protected _outlineContentMenuBarVisibleChangeHandler: EventHandler<PropertyChangeEvent<boolean, MenuBar>>;
  protected _outlineContentCssClassChangeHandler: EventHandler<PropertyChangeEvent<string, OutlineContent>>;
  protected _viewButtonBoxPropertyChangeHandler: EventHandler<PropertyChangeEvent<any, ViewButtonBox>>;

  constructor() {
    super();
    this.tabArea = null;
    this.toolBoxVisible = true;
    this.viewButtonBox = null;
    this.viewButtonBoxVisible = false;
    this.outlineContent = null;

    this._desktopPropertyChangeHandler = this._onDesktopPropertyChange.bind(this);
    this._desktopAnimationEndHandler = this._onDesktopAnimationEnd.bind(this);
    this._outlineContentMenuBarVisibleChangeHandler = this._onOutlineContentMenuBarVisibleChange.bind(this);
    this._outlineContentCssClassChangeHandler = this._onOutlineContentCssClassChange.bind(this);
    this._viewButtonBoxPropertyChangeHandler = this._onViewButtonBoxPropertyChange.bind(this);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.desktop = this.session.desktop;
    this.updateViewButtonBoxVisibility();
    this.tabArea = this._createTabArea();
  }

  protected _createTabArea(): DesktopTabArea {
    return scout.create(DesktopTabArea, $.extend({
      parent: this
    }, this.tabArea));
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('desktop-header');
    aria.role(this.$container, 'banner');
    aria.label(this.$container, this.session.text('ui.HeaderArea'));
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new DesktopHeaderLayout(this));
    this.desktop.on('propertyChange', this._desktopPropertyChangeHandler);
    this.desktop.on('animationEnd', this._desktopAnimationEndHandler);
    if (this.desktop.bench) {
      this._setOutlineContent(this.desktop.bench.outlineContent);
    }
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderViewButtonBoxVisible();
    this._renderViewTabs();
    this._renderToolBoxVisible();
    this._renderLogoUrl();
    this._renderInBackground();
  }

  protected override _remove() {
    this.desktop.off('propertyChange', this._desktopPropertyChangeHandler);
    this.desktop.off('animationEnd', this._desktopAnimationEndHandler);
    this._setOutlineContent(null);
    super._remove();
  }

  protected _renderViewTabs() {
    this.tabArea.render();
  }

  protected _renderToolBox() {
    if (this.toolBox) {
      return;
    }
    this.toolBox = this._createToolBox();
    this.toolBox.render();
  }

  protected _createToolBox(): DesktopToolBox {
    return scout.create(DesktopToolBox, {
      parent: this,
      menus: this.desktop.menus
    });
  }

  protected _removeToolBox() {
    if (!this.toolBox) {
      return;
    }
    this.toolBox.destroy();
    this.toolBox = null;
  }

  protected _renderToolBoxVisible() {
    if (this.toolBoxVisible) {
      this._renderToolBox();
    } else {
      this._removeToolBox();
    }
    this.invalidateLayoutTree();
  }

  protected _renderLogoUrl() {
    if (this.logoUrl) {
      this._renderLogo();
    } else {
      this._removeLogo();
    }
    this.invalidateLayoutTree();
  }

  protected _renderLogo() {
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

  protected _createLogo(): DesktopLogo {
    return scout.create(DesktopLogo, {
      parent: this,
      url: this.logoUrl
    });
  }

  protected _removeLogo() {
    if (!this.logo) {
      return;
    }
    this.logo.destroy();
    this.logo = null;
  }

  protected _renderViewButtonBox() {
    if (this.viewButtonBox) {
      return;
    }
    this.viewButtonBox = this._createViewButtonBox();
    this.viewButtonBox.on('propertyChange', this._viewButtonBoxPropertyChangeHandler);
    this.viewButtonBox.render();
    this.viewButtonBox.$container.prependTo(this.$container);
    this.updateViewButtonStyling();
  }

  protected _createViewButtonBox(): ViewButtonBox {
    return scout.create(ViewButtonBox, {
      parent: this,
      viewButtons: this.desktop.viewButtons,
      selectedMenuButtonAlwaysVisible: true
    });
  }

  protected _removeViewButtonBox() {
    if (!this.viewButtonBox) {
      return;
    }
    this.viewButtonBox.off('propertyChange', this._viewButtonBoxPropertyChangeHandler);
    this.viewButtonBox.destroy();
    this.viewButtonBox = null;
  }

  protected _renderViewButtonBoxVisible() {
    if (this.viewButtonBoxVisible) {
      this._renderViewButtonBox();
    } else {
      this._removeViewButtonBox();
    }
    this.$container.toggleClass('has-view-button-box', this.viewButtonBoxVisible);
    this.invalidateLayoutTree();
  }

  sendToBack() {
    if (this.rendered) {
      this._renderInBackground();
    }
  }

  bringToFront() {
    if (this.rendered) {
      this._renderInBackground();
    }
  }

  protected _renderInBackground() {
    this.$container.toggleClass('in-background', this.desktop.inBackground);
  }

  setLogoUrl(logoUrl: string) {
    this.setProperty('logoUrl', logoUrl);
  }

  setToolBoxVisible(visible: boolean) {
    this.setProperty('toolBoxVisible', visible);
  }

  setViewButtonBoxVisible(visible: boolean) {
    this.setProperty('viewButtonBoxVisible', visible);
  }

  setMenus(menus: ObjectOrChildModel<Menu>[]) {
    if (this.toolBox) {
      this.toolBox.setMenus(menus);
    }
  }

  protected _setOutlineContent(outlineContent: OutlineContent) {
    if (this.outlineContent === outlineContent) {
      return;
    }
    this._detachOutlineContentHandlers();
    this._setProperty('outlineContent', outlineContent);
    this._attachOutlineContentHandlers();
    this.updateViewButtonStyling();
  }

  updateViewButtonBoxVisibility() {
    // View buttons are visible in the header if the navigation is not visible
    // If there are no view buttons at all, don't show the box
    // With displayStyle is set to compact, the view buttons should never be visible in the header
    this.setViewButtonBoxVisible(this.desktop.viewButtons.some(button => button.visible) && !this.desktop.navigationVisible && this.desktop.displayStyle !== Desktop.DisplayStyle.COMPACT);
  }

  protected _attachOutlineContentHandlers() {
    this._attachOutlineContentMenuBarHandler();
    this._attachOutlineContentCssClassHandler();
  }

  protected _attachOutlineContentMenuBarHandler() {
    if (!this.outlineContent) {
      return;
    }
    let menuBar = this._outlineContentMenuBar(this.outlineContent);
    if (menuBar) {
      menuBar.on('propertyChange:visible', this._outlineContentMenuBarVisibleChangeHandler);
    }
  }

  protected _attachOutlineContentCssClassHandler() {
    if (!this.outlineContent) {
      return;
    }
    this.outlineContent.on('propertyChange:cssClass', this._outlineContentCssClassChangeHandler);
  }

  protected _detachOutlineContentHandlers() {
    this._detachOutlineContentMenuBarHandler();
    this._detachOutlineContentCssClassHandler();
  }

  protected _detachOutlineContentMenuBarHandler() {
    if (!this.outlineContent) {
      return;
    }
    let menuBar = this._outlineContentMenuBar(this.outlineContent);
    if (menuBar) {
      menuBar.off('propertyChange:visible', this._outlineContentMenuBarVisibleChangeHandler);
    }
  }

  protected _detachOutlineContentCssClassHandler() {
    if (!this.outlineContent) {
      return;
    }
    this.outlineContent.off('propertyChange:cssClass', this._outlineContentCssClassChangeHandler);
  }

  protected _outlineContentMenuBar(outlineContent: OutlineContent): MenuBar {
    if (outlineContent instanceof Form) {
      return outlineContent.rootGroupBox.menuBar;
    }
    return (outlineContent as { menuBar?: MenuBar }).menuBar;
  }

  updateViewButtonStyling() {
    this._updateOutlineContentHasMenuBar();
    this._updateOutlineContentHasDimmedBackground();
  }

  protected _getOutlineContentForViewButtonStyling(): OutlineContent {
    if (!this.viewButtonBoxVisible || !this.outlineContent || !this.outlineContent.visible) {
      return;
    }
    return this.outlineContent;
  }

  protected _updateOutlineContentHasMenuBar() {
    let outlineContent = this._getOutlineContentForViewButtonStyling();
    if (!outlineContent) {
      return;
    }
    let hasMenuBar = false;
    if (outlineContent instanceof Form && outlineContent.detailForm) {
      let rootGroupBox = outlineContent.rootGroupBox;
      hasMenuBar = rootGroupBox.menuBar && rootGroupBox.menuBarVisible && rootGroupBox.menuBar.visible;
    } else {
      let outlineTable = outlineContent as { menuBar?: MenuBar };
      hasMenuBar = outlineTable.menuBar && outlineTable.menuBar.visible;
    }
    this.$container.toggleClass('outline-content-has-menubar', !!hasMenuBar);
  }

  protected _updateOutlineContentHasDimmedBackground() {
    let outlineContent = this._getOutlineContentForViewButtonStyling();
    if (!outlineContent) {
      return;
    }
    let hasDimmedBackground = false;
    if (outlineContent.cssClass) {
      hasDimmedBackground = outlineContent.cssClass.indexOf('dimmed-background') > -1;
    }
    this.$container.toggleClass('outline-content-has-dimmed-background', hasDimmedBackground);
  }

  protected _onDesktopNavigationVisibleChange() {
    // If navigation gets visible: Hide view buttons immediately
    // If navigation gets hidden using animation: Show view buttons when animation ends
    if (this.desktop.navigationVisible) {
      this.updateViewButtonBoxVisibility();
    }
  }

  protected _onDesktopAnimationEnd(event: Event<Desktop>) {
    this.updateViewButtonBoxVisibility();
  }

  onBenchOutlineContentChange(content: OutlineContent, oldContent: OutlineContent) {
    this._setOutlineContent(content);
  }

  protected _onDesktopPropertyChange(event: PropertyChangeEvent<any, Desktop>) {
    if (event.propertyName === 'navigationVisible') {
      this._onDesktopNavigationVisibleChange();
    }
  }

  protected _onOutlineContentMenuBarVisibleChange(event: PropertyChangeEvent<boolean, MenuBar>) {
    this._updateOutlineContentHasMenuBar();
  }

  protected _onOutlineContentCssClassChange(event: PropertyChangeEvent<string, OutlineContent>) {
    this._updateOutlineContentHasDimmedBackground();
  }

  protected _onViewButtonBoxPropertyChange(event: PropertyChangeEvent<any, ViewButtonBox>) {
    if (event.propertyName === 'menuButtons' || event.propertyName === 'tabButtons') {
      this.invalidateLayoutTree();
    }
  }
}
