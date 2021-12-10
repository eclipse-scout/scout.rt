/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {CollapseHandle, Desktop, DesktopNavigationLayout, HtmlComponent, scout, SingleLayout, styles, Tree, Widget} from '../../index';

export default class DesktopNavigation extends Widget {

  constructor() {
    super();
    this.$body = null;
    this.layoutData = {};
    this.toolBoxVisible = false;
    this.viewButtonBox = null;
    this._outlinePropertyChangeHandler = this._onOutlinePropertyChange.bind(this);
    this._desktopPropertyChangeHandler = this._onDesktopPropertyChange.bind(this);
    this._viewButtonBoxPropertyChangeHandler = this._onViewButtonBoxPropertyChange.bind(this);
  }

  static DEFAULT_STYLE_WIDTH = null; // Configured in sizes.css
  static BREADCRUMB_STYLE_WIDTH = null; // Configured in sizes.css
  static MIN_WIDTH = null; // Configured in sizes.css

  _init(model) {
    super._init(model);
    DesktopNavigation.MIN_WIDTH = styles.getSize('desktop-navigation', 'min-width', 'minWidth', 49);
    DesktopNavigation.DEFAULT_STYLE_WIDTH = styles.getSize('desktop-navigation', 'width', 'width', 290);
    DesktopNavigation.BREADCRUMB_STYLE_WIDTH = styles.getSize('desktop-navigation-breadcrumb', 'width', 'width', 240);
    this.desktop = this.parent;
    this.updateHandleVisibility();
    this._setOutline(model.outline);
    this.viewButtonBox = scout.create('ViewButtonBox', {
      parent: this,
      viewButtons: this.desktop.viewButtons
    });
    this.viewButtonBox.on('propertyChange', this._viewButtonBoxPropertyChangeHandler);
  }

  _render() {
    this.$container = this.$parent.appendDiv('desktop-navigation');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new DesktopNavigationLayout(this));
    this.htmlComp.layoutData = this.layoutData;

    this.$body = this.$container.appendDiv('navigation-body')
      .on('mousedown', this._onNavigationBodyMouseDown.bind(this));
    this.htmlCompBody = HtmlComponent.install(this.$body, this.session);
    this.htmlCompBody.setLayout(new SingleLayout(null, {exact: true}));

    this.desktop.on('propertyChange', this._desktopPropertyChangeHandler);
  }

  _remove() {
    this.desktop.off('propertyChange', this._desktopPropertyChangeHandler);
    super._remove();
  }

  _renderProperties() {
    super._renderProperties();
    this._renderViewButtonBox();
    this._renderViewButtonBoxVisible();
    this._renderToolBoxVisible();
    this._renderOutline();
    this._renderHandleVisible();
    this._renderInBackground();
  }

  _renderInBackground() {
    this.$container.toggleClass('in-background', this.desktop.inBackground && this.desktop.displayStyle !== Desktop.DisplayStyle.COMPACT);
  }

  _renderViewButtonBox() {
    this.viewButtonBox.render();
    this.viewButtonBox.$container.insertBefore(this.$body);
  }

  _removeOutline() {
    if (!this.outline) {
      return;
    }
    this.outline.remove();
  }

  _renderOutline() {
    if (!this.outline) {
      return;
    }
    this.outline.render(this.$body);
    this.outline.invalidateLayoutTree();
    // Layout immediate to prevent flickering when breadcrumb mode is enabled
    // but not initially while desktop gets rendered because it will be done at the end anyway
    if (this.rendered) {
      this.outline.validateFocus();
    }
  }

  setOutline(outline) {
    this.setProperty('outline', outline);
  }

  _setOutline(newOutline) {
    let oldOutline = this.outline;
    if (this.outline) {
      this.outline.off('propertyChange', this._outlinePropertyChangeHandler);
    }
    if (this.rendered) {
      this._removeOutline();
    }
    this.outline = newOutline;
    if (this.outline) {
      this.outline.setParent(this);
      this.outline.setBreadcrumbTogglingThreshold(DesktopNavigation.BREADCRUMB_STYLE_WIDTH);
      // if both have breadcrumb-toggling enabled: make sure new outline uses same display style as old
      if (this.outline.toggleBreadcrumbStyleEnabled && oldOutline && oldOutline.toggleBreadcrumbStyleEnabled &&
        oldOutline.displayStyle) {
        this.outline.setDisplayStyle(oldOutline.displayStyle);
      }
      this.outline.inBackground = this.desktop.inBackground;
      this.outline.on('propertyChange', this._outlinePropertyChangeHandler);
      this._updateHandle();
    }
  }

  _renderViewButtonBoxVisible() {
    this.$container.toggleClass('view-button-box-invisible', !this.viewButtonBox.visible);
  }

  sendToBack() {
    if (this.outline) {
      this.outline.sendToBack();
    }
    if (this.rendered) {
      this._renderInBackground();
    }
  }

  bringToFront() {
    if (this.outline) {
      this.outline.bringToFront();
    }
    if (this.rendered) {
      this._renderInBackground();
    }
  }

  setToolBoxVisible(toolBoxVisible) {
    this.setProperty('toolBoxVisible', toolBoxVisible);
  }

  setHandleVisible(visible) {
    this.setProperty('handleVisible', visible);
  }

  _updateHandle() {
    if (this.handle) {
      this.handle.setRightVisible(this.outline && this.outline.toggleBreadcrumbStyleEnabled &&
        this.desktop.outlineDisplayStyle() === Tree.DisplayStyle.BREADCRUMB);
    }
  }

  updateHandleVisibility() {
    // Don't show handle if desktop says handle must not be visible
    this.setHandleVisible(this.desktop.navigationHandleVisible);
  }

  _renderToolBoxVisible() {
    if (this.toolBoxVisible) {
      this._renderToolBox();
    } else {
      this._removeToolBox();
    }
  }

  _renderToolBox() {
    if (this.toolBox) {
      return;
    }
    this.toolBox = scout.create('DesktopToolBox', {
      parent: this,
      menus: this.desktop.menus
    });
    this.toolBox.render();
  }

  _removeToolBox() {
    if (!this.toolBox) {
      return;
    }
    this.toolBox.destroy();
    this.toolBox = null;
  }

  _renderHandleVisible() {
    if (this.handleVisible) {
      this._renderHandle();
    } else {
      this._removeHandle();
    }
  }

  _createHandle() {
    return scout.create('DesktopNavigationHandle', {
      parent: this,
      rightVisible: false,
      horizontalAlignment: CollapseHandle.HorizontalAlignment.RIGHT
    });
  }

  _renderHandle() {
    if (this.handle) {
      return;
    }
    this.handle = this._createHandle();
    this.handle.render();
    this.handle.addCssClass('navigation-open');
    this.handle.on('action', this._onHandleAction.bind(this));
    this._updateHandle();
  }

  _removeHandle() {
    if (!this.handle) {
      return;
    }
    this.handle.destroy();
    this.handle = null;
  }

  _onNavigationBodyMouseDown(event) {
    this.desktop.bringOutlineToFront();
  }

  _onViewButtonBoxPropertyChange(event) {
    if (event.propertyName === 'visible') {
      if (this.rendered) {
        this._renderViewButtonBoxVisible();
      }
    }
  }

  _onOutlinePropertyChange(event) {
    if (event.propertyName === 'displayStyle') {
      this._updateHandle();
    }
  }

  _onDesktopPropertyChange(event) {
    if (event.propertyName === 'navigationHandleVisible') {
      this.updateHandleVisibility();
    }
  }

  _onHandleAction(event) {
    if (event.left) {
      this.desktop.shrinkNavigation();
    } else {
      this.desktop.enlargeNavigation();
    }
  }
}
