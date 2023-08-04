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
  aria, CollapseHandle, CollapseHandleActionEvent, Desktop, DesktopNavigationEventMap, DesktopNavigationHandle, DesktopNavigationLayout, DesktopNavigationModel, DesktopToolBox, Event, EventHandler, HtmlComponent, InitModelOf, LayoutData,
  Outline, PropertyChangeEvent, scout, SingleLayout, strings, styles, Tree, ViewButtonBox, Widget
} from '../../index';

export class DesktopNavigation extends Widget implements DesktopNavigationModel {
  declare model: DesktopNavigationModel;
  declare eventMap: DesktopNavigationEventMap;
  declare self: DesktopNavigation;
  declare parent: Desktop;
  declare htmlComp: HtmlComponent & { layoutData: DesktopNavigationLayoutData };

  desktop: Desktop;
  outline: Outline;

  handleVisible: boolean;
  handle: DesktopNavigationHandle;

  toolBox: DesktopToolBox;
  toolBoxVisible: boolean;

  viewButtonBox: ViewButtonBox;
  layoutData: DesktopNavigationLayoutData;
  $body: JQuery;
  htmlCompBody: HtmlComponent;
  $screenReaderStatus: JQuery;

  protected _outlinePropertyChangeHandler: EventHandler<PropertyChangeEvent<any, Outline>>;
  protected _outlineSelectedNodesChangeHandler: EventHandler<Event<Outline>>;
  protected _desktopPropertyChangeHandler: EventHandler<PropertyChangeEvent<any, Desktop>>;
  protected _viewButtonBoxPropertyChangeHandler: EventHandler<PropertyChangeEvent<any, ViewButtonBox>>;

  constructor() {
    super();
    this.$body = null;
    this.layoutData = {};
    this.toolBoxVisible = false;
    this.viewButtonBox = null;
    this.handleVisible = true;
    this.$screenReaderStatus = null;
    this._outlinePropertyChangeHandler = this._onOutlinePropertyChange.bind(this);
    this._outlineSelectedNodesChangeHandler = this._onOutlineSelectedNodesChange.bind(this);
    this._desktopPropertyChangeHandler = this._onDesktopPropertyChange.bind(this);
    this._viewButtonBoxPropertyChangeHandler = this._onViewButtonBoxPropertyChange.bind(this);
  }

  static DEFAULT_STYLE_WIDTH = null; // Configured in sizes.css
  static BREADCRUMB_STYLE_WIDTH = null; // Configured in sizes.css
  static MIN_WIDTH = null; // Configured in sizes.css

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    DesktopNavigation.MIN_WIDTH = styles.getSize('desktop-navigation', 'min-width', 'minWidth', 49);
    DesktopNavigation.DEFAULT_STYLE_WIDTH = styles.getSize('desktop-navigation', 'width', 'width', 290);
    DesktopNavigation.BREADCRUMB_STYLE_WIDTH = styles.getSize('desktop-navigation-breadcrumb', 'width', 'width', 240);
    this.desktop = this.parent;
    this.updateHandleVisibility();
    this._setOutline(model.outline);
    this.viewButtonBox = scout.create(ViewButtonBox, {
      parent: this,
      viewButtons: this.desktop.viewButtons
    });
    this.viewButtonBox.on('propertyChange', this._viewButtonBoxPropertyChangeHandler);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('desktop-navigation');
    aria.role(this.$container, 'navigation');
    aria.label(this.$container, this.session.text('ui.NavigationArea'));
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new DesktopNavigationLayout(this));
    this.htmlComp.layoutData = this.layoutData;

    this.$body = this.$container.appendDiv('navigation-body')
      .on('mousedown', this._onNavigationBodyMouseDown.bind(this));
    this.htmlCompBody = HtmlComponent.install(this.$body, this.session);
    this.htmlCompBody.setLayout(new SingleLayout(null, {exact: true}));

    this._addScreenReaderStatus();
    this.desktop.on('propertyChange', this._desktopPropertyChangeHandler);
  }

  protected _addScreenReaderStatus() {
    this.$screenReaderStatus = this.$container.appendDiv();
    aria.role(this.$screenReaderStatus, 'status');
    aria.screenReaderOnly(this.$screenReaderStatus);
  }

  _renderScreenReaderStatus() {
    if (!this.$screenReaderStatus || !this.outline) {
      return;
    }
    this.$screenReaderStatus.empty();
    let textRep = this.session.text('ui.NavigationX', this.outline.title);
    let page = this.outline.selectedNode();
    // there may be cases (i.e. after startup or outline switch) where there is no selected node, nonetheless we set the status to outline title so at least current outline is read
    if (page) {
      let outlinePath = [];
      outlinePath.unshift(page);
      // recursively go up in the tree until there is no more parent
      while (page) {
        page = page.parentNode;
        if (page) {
          outlinePath.unshift(page);
        }
      }

      // for each node in the path announce its text and level in the tree
      outlinePath.forEach(page => {
        textRep += ' ' + strings.join(' ', this.session.text('ui.LevelX', (page.level + 1)), strings.plainText(page.text));
      });
      // if the last page (i.e. the current page) is expanded, notify the user how many child nodes there are
      let lastPage = outlinePath[outlinePath.length - 1];
      if (lastPage.expanded) {
        textRep += ' ' + strings.join(' ', this.session.text('ui.Expanded'), this.session.text('ui.SubItemCountX', lastPage.childNodes?.length));
      }
    }
    this.$screenReaderStatus.appendSpan().addClass('sr-outline-path').text(textRep);
  }

  protected override _remove() {
    this.desktop.off('propertyChange', this._desktopPropertyChangeHandler);
    super._remove();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderViewButtonBox();
    this._renderViewButtonBoxVisible();
    this._renderToolBoxVisible();
    this._renderOutline();
    this._renderHandleVisible();
    this._renderInBackground();
  }

  protected _renderInBackground() {
    this.$container.toggleClass('in-background', this.desktop.inBackground && this.desktop.displayStyle !== Desktop.DisplayStyle.COMPACT);
  }

  protected _renderViewButtonBox() {
    this.viewButtonBox.render();
    this.viewButtonBox.$container.insertBefore(this.$body);
  }

  protected _removeOutline() {
    if (!this.outline) {
      return;
    }
    this.outline.remove();
  }

  protected _renderOutline() {
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
    this._renderScreenReaderStatus();
  }

  setOutline(outline: Outline) {
    this.setProperty('outline', outline);
  }

  protected _setOutline(newOutline: Outline) {
    let oldOutline = this.outline;
    if (this.outline) {
      this.outline.off('propertyChange', this._outlinePropertyChangeHandler);
      this.outline.off('nodesSelected', this._outlineSelectedNodesChangeHandler);
    }
    if (this.rendered) {
      this._removeOutline();
    }
    this.outline = newOutline;
    if (this.outline) {
      this.outline.setParent(this);
      this.outline.setBreadcrumbTogglingThreshold(DesktopNavigation.BREADCRUMB_STYLE_WIDTH);
      // if both have breadcrumb-toggling enabled: make sure new outline uses same display style as old
      if (this.outline.toggleBreadcrumbStyleEnabled && oldOutline && oldOutline.toggleBreadcrumbStyleEnabled && oldOutline.displayStyle) {
        this.outline.setDisplayStyle(oldOutline.displayStyle);
      }
      this.outline.inBackground = this.desktop.inBackground;
      this.outline.on('propertyChange', this._outlinePropertyChangeHandler);
      this.outline.on('nodesSelected', this._outlineSelectedNodesChangeHandler);
      this._updateHandle();
    }
  }

  protected _renderViewButtonBoxVisible() {
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

  setToolBoxVisible(toolBoxVisible: boolean) {
    this.setProperty('toolBoxVisible', toolBoxVisible);
  }

  setHandleVisible(visible: boolean) {
    this.setProperty('handleVisible', visible);
  }

  protected _updateHandle() {
    if (this.handle) {
      this.handle.setRightVisible(this.outline && this.outline.toggleBreadcrumbStyleEnabled && this.desktop.outlineDisplayStyle() === Tree.DisplayStyle.BREADCRUMB);
    }
  }

  updateHandleVisibility() {
    // Don't show handle if desktop says handle must not be visible
    this.setHandleVisible(this.desktop.navigationHandleVisible);
  }

  protected _renderToolBoxVisible() {
    if (this.toolBoxVisible) {
      this._renderToolBox();
    } else {
      this._removeToolBox();
    }
  }

  protected _renderToolBox() {
    if (this.toolBox) {
      return;
    }
    this.toolBox = scout.create(DesktopToolBox, {
      parent: this,
      menus: this.desktop.menus
    });
    this.toolBox.render();
  }

  protected _removeToolBox() {
    if (!this.toolBox) {
      return;
    }
    this.toolBox.destroy();
    this.toolBox = null;
  }

  protected _renderHandleVisible() {
    if (this.handleVisible) {
      this._renderHandle();
    } else {
      this._removeHandle();
    }
  }

  protected _createHandle(): DesktopNavigationHandle {
    return scout.create(DesktopNavigationHandle, {
      parent: this,
      rightVisible: false,
      horizontalAlignment: CollapseHandle.HorizontalAlignment.RIGHT
    });
  }

  protected _renderHandle() {
    if (this.handle) {
      return;
    }
    this.handle = this._createHandle();
    this.handle.render();
    this.handle.addCssClass('navigation-open');
    this.handle.on('action', this._onHandleAction.bind(this));
    this._updateHandle();
  }

  protected _removeHandle() {
    if (!this.handle) {
      return;
    }
    this.handle.destroy();
    this.handle = null;
  }

  protected _onNavigationBodyMouseDown(event: JQuery.MouseDownEvent) {
    this.desktop.bringOutlineToFront();
  }

  protected _onViewButtonBoxPropertyChange(event: PropertyChangeEvent<any, ViewButtonBox>) {
    if (event.propertyName === 'visible') {
      if (this.rendered) {
        this._renderViewButtonBoxVisible();
      }
    }
  }

  protected _onOutlineSelectedNodesChange() {
    this._renderScreenReaderStatus();
  }

  protected _onOutlinePropertyChange(event: PropertyChangeEvent<any, Outline>) {
    if (event.propertyName === 'displayStyle') {
      this._updateHandle();
    }
  }

  protected _onDesktopPropertyChange(event: PropertyChangeEvent<any, Desktop>) {
    if (event.propertyName === 'navigationHandleVisible') {
      this.updateHandleVisibility();
    }
  }

  protected _onHandleAction(event: CollapseHandleActionEvent) {
    if (event.left) {
      this.desktop.shrinkNavigation();
    } else {
      this.desktop.enlargeNavigation();
    }
  }
}

export type DesktopNavigationLayoutData = LayoutData & { fullWidth?: boolean };
