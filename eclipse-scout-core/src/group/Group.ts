/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  aria, Dimension, EnumObject, graphics, GroupEventMap, GroupLayout, GroupModel, GroupToggleCollapseKeyStroke, HtmlComponent, Icon, InitModelOf, Insets, KeyStrokeContext, LoadingSupport, ObjectOrChildModel, scout, tooltips, Widget
} from '../index';
import $ from 'jquery';
import MouseDownEvent = JQuery.MouseDownEvent;

export type GroupCollapseStyle = EnumObject<typeof Group.CollapseStyle>;

export class Group<TBody extends Widget = Widget> extends Widget implements GroupModel<TBody> {
  declare model: GroupModel<TBody>;
  declare eventMap: GroupEventMap;
  declare self: Group;

  bodyAnimating: boolean;
  collapsed: boolean;
  collapsible: boolean;
  title: string;
  titleHtmlEnabled: boolean;
  titleSuffix: string;
  header: Widget;
  headerFocusable: boolean;
  headerVisible: boolean;
  body: TBody;
  collapseStyle: GroupCollapseStyle;
  htmlHeader: HtmlComponent;
  htmlFooter: HtmlComponent;
  iconId: string;
  icon: Icon;
  $header: JQuery;
  $footer: JQuery;
  $collapseIcon: JQuery;
  $collapseBorderLeft: JQuery;
  $collapseBorderRight: JQuery;
  $title: JQuery;
  $titleSuffix: JQuery;

  constructor() {
    super();
    this.bodyAnimating = false;
    this.collapsed = false;
    this.collapsible = true;
    this.title = null;
    this.titleHtmlEnabled = false;
    this.titleSuffix = null;
    this.header = null;
    this.headerFocusable = false;
    this.headerVisible = true;
    this.body = null;

    this.$container = null;
    this.$header = null;
    this.$footer = null;
    this.$collapseIcon = null;
    this.$collapseBorderLeft = null;
    this.$collapseBorderRight = null;
    this.collapseStyle = Group.CollapseStyle.LEFT;
    this.htmlComp = null;
    this.htmlHeader = null;
    this.htmlFooter = null;
    this.iconId = null;
    this.icon = null;
    this._addWidgetProperties(['header', 'body']);
  }

  static CollapseStyle = {
    LEFT: 'left',
    RIGHT: 'right',
    BOTTOM: 'bottom'
  } as const;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.resolveTextKeys(['title', 'titleSuffix']);
    this.resolveIconIds(['iconId']);
    this._setBody(this.body);
    this._setHeader(this.header);
  }

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    // Keystroke should only work when header is focused
    this.keyStrokeContext.$bindTarget = () => this.$header;
    this.keyStrokeContext.registerKeyStrokes([
      new GroupToggleCollapseKeyStroke(this)
    ]);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('group');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new GroupLayout(this));

    this._renderHeader();

    this.$collapseIcon = this.$header.appendDiv('group-collapse-icon');
    this.$footer = this.$container.appendDiv('group-footer');
    this.$collapseBorderLeft = this.$footer.appendDiv('group-collapse-border');
    this.$collapseBorderRight = this.$footer.appendDiv('group-collapse-border');
    this.htmlFooter = HtmlComponent.install(this.$footer, this.session);
    this.$footer.on('mousedown', this._onFooterMouseDown.bind(this));
    this._addAriaFieldDescription();
  }

  protected _addAriaFieldDescription() {
    aria.addHiddenDescriptionAndLinkToElement(this.$header, this.id + '-func-desc', this.session.text('ui.AriaGroupDescription'));
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderIconId();
    this._renderTitle();
    this._renderTitleSuffix();
    this._renderHeaderVisible();
    this._renderHeaderFocusable();
    this._renderCollapsed();
    this._renderCollapseStyle();
    this._renderCollapsible();
  }

  protected override _remove() {
    this.$header = null;
    this.$title = null;
    this.$titleSuffix = null;
    this.$footer = null;
    this.$collapseIcon = null;
    this.$collapseBorderLeft = null;
    this.$collapseBorderRight = null;
    this._removeIconId();
    super._remove();
  }

  protected override _renderEnabled() {
    super._renderEnabled();
    this.$header.setTabbable(this.enabledComputed);
  }

  /** @see GroupModel.iconId */
  setIconId(iconId: string) {
    this.setProperty('iconId', iconId);
  }

  /**
   * Adds an image or font-based icon to the group header by adding either an IMG or SPAN element.
   */
  protected _renderIconId() {
    let iconId = this.iconId || '';
    // If the icon is an image (and not a font icon), the Icon class will invalidate the layout when the image has loaded
    if (!iconId) {
      this._removeIconId();
      this._updateIconStyle();
      return;
    }
    if (this.icon) {
      this.icon.setIconDesc(iconId);
      this._updateIconStyle();
      return;
    }
    this.icon = scout.create(Icon, {
      parent: this,
      iconDesc: iconId,
      prepend: true
    });
    this.icon.one('destroy', () => {
      this.icon = null;
    });
    this.icon.render(this.$header);
    this._updateIconStyle();
  }

  protected _updateIconStyle() {
    let hasTitle = !!this.title;
    this.get$Icon().toggleClass('with-title', hasTitle);
    this.get$Icon().addClass('group-icon');
    this._renderCollapseStyle();
  }

  get$Icon(): JQuery {
    if (this.icon) {
      return this.icon.$container;
    }
    return $();
  }

  protected _removeIconId() {
    if (this.icon) {
      this.icon.destroy();
    }
  }

  /** @see GroupModel.header */
  setHeader(header: ObjectOrChildModel<Widget>) {
    this.setProperty('header', header);
  }

  protected _setHeader(header: Widget) {
    this._setProperty('header', header);
  }

  /** @see GroupModel.headerFocusable */
  setHeaderFocusable(headerFocusable: boolean) {
    this.setProperty('headerFocusable', headerFocusable);
  }

  protected _renderHeaderFocusable() {
    this.$header.toggleClass('unfocusable', !this.headerFocusable);
  }

  setTitle(title: string) {
    this.setProperty('title', title);
  }

  protected _renderTitle() {
    if (this.$title) {
      if (this.titleHtmlEnabled) {
        this.$title.htmlOrNbsp(this.title);
      } else {
        this.$title.textOrNbsp(this.title);
      }
      this._updateIconStyle();
    }
  }

  setTitleSuffix(titleSuffix: string) {
    this.setProperty('titleSuffix', titleSuffix);
  }

  protected _renderTitleSuffix() {
    if (this.$titleSuffix) {
      this.$titleSuffix.text(this.titleSuffix || '');
    }
  }

  setHeaderVisible(headerVisible: boolean) {
    this.setProperty('headerVisible', headerVisible);
  }

  protected _renderHeaderVisible() {
    this.$header.setVisible(this.headerVisible);
    this._renderCollapsible();
    this.invalidateLayoutTree();
  }

  /** @see GroupModel.body */
  setBody(body: ObjectOrChildModel<TBody>) {
    this.setProperty('body', body);
  }

  protected _setBody(body: TBody) {
    if (!body) {
      // Create empty body if none was provided
      body = scout.create(EmptyBody, {
        parent: this
      }) as unknown as TBody;
    }
    this._setProperty('body', body);
  }

  protected override _createLoadingSupport(): LoadingSupport {
    return new LoadingSupport({
      widget: this,
      $container: () => this.$header
    });
  }

  protected _renderHeader() {
    if (this.$header) {
      this.$header.remove();
      this._removeIconId();
    }
    if (this.header) {
      this.header.render();
      this.$header = this.header.$container
        .addClass('group-header')
        .addClass('custom-header-widget')
        .prependTo(this.$container);
      this.htmlHeader = this.header.htmlComp;
    } else {
      this.$header = this.$container
        .prependDiv('group-header')
        .unfocusable()
        .addClass('prevent-initial-focus');
      this.$title = this.$header.appendDiv('group-title');
      this.$titleSuffix = this.$header.appendDiv('group-title-suffix');
      tooltips.installForEllipsis(this.$title, {
        parent: this
      });
      this.htmlHeader = HtmlComponent.install(this.$header, this.session);
      if (!this.rendering) {
        this._renderIconId();
        this._renderTitle();
        this._renderTitleSuffix();
      }
    }
    aria.role(this.$header, 'button');
    aria.linkElementWithLabel(this.$header, this.$title);
    this.$header.on('mousedown', this._onHeaderMouseDown.bind(this));
    this.invalidateLayoutTree();
  }

  protected _renderBody() {
    this.body.render();
    this.body.$container.insertAfter(this.$header);
    this.body.$container.addClass('group-body');
    aria.linkElementWithLabel(this.body.$container, this.$title);
    aria.linkElementWithControls(this.$header, this.body.$container);
    this.body.invalidateLayoutTree();
  }

  override getFocusableElement(): HTMLElement | JQuery {
    if (!this.rendered) {
      return null;
    }
    return this.$header;
  }

  toggleCollapse() {
    this.setCollapsed(!this.collapsed && this.collapsible);
  }

  setCollapsed(collapsed: boolean) {
    this.setProperty('collapsed', collapsed);
  }

  protected _renderCollapsed() {
    this.$container.toggleClass('collapsed', this.collapsed);
    this.$collapseIcon.toggleClass('collapsed', this.collapsed);
    aria.expanded(this.$header, !this.collapsed);
    if (!this.collapsed && !this.bodyAnimating) {
      this._renderBody();
    }
    if (this.rendered) {
      this.resizeBody();
    } else if (this.collapsed) {
      // Body will be removed after the animation, if there is no animation, remove it now
      this.body.remove();
    }
    this.invalidateLayoutTree();
  }

  setCollapsible(collapsible: boolean) {
    this.setProperty('collapsible', collapsible);
  }

  protected _renderCollapsible() {
    this.$container.toggleClass('collapsible', this.collapsible);
    this.$header.toggleClass('disabled', !this.collapsible);
    aria.disabled(this.$header, !this.collapsible || null);
    // footer is visible if collapseStyle is 'bottom' and either header is visible or has a (collapsible) body
    this.$footer.setVisible(this.collapseStyle === Group.CollapseStyle.BOTTOM && (this.headerVisible || this.collapsible));
    this.$collapseIcon.setVisible(this.collapsible);
    this.invalidateLayoutTree();
  }

  setCollapseStyle(collapseStyle: GroupCollapseStyle) {
    this.setProperty('collapseStyle', collapseStyle);
  }

  protected _renderCollapseStyle() {
    this.$header.toggleClass('collapse-right', this.collapseStyle === Group.CollapseStyle.RIGHT);
    this.$container.toggleClass('collapse-bottom', this.collapseStyle === Group.CollapseStyle.BOTTOM);

    if (this.collapseStyle === Group.CollapseStyle.RIGHT) {
      this.$collapseIcon.appendTo(this.$header);
    } else if (this.collapseStyle === Group.CollapseStyle.LEFT) {
      this.$collapseIcon.prependTo(this.$header);
    } else if (this.collapseStyle === Group.CollapseStyle.BOTTOM) {
      let sibling = this.body.$container ? this.body.$container : this.$header;
      this.$footer.insertAfter(sibling);
      this.$collapseIcon.insertAfter(this.$collapseBorderLeft);
    }

    this._renderCollapsible();
    this.invalidateLayoutTree();
  }

  protected _onHeaderMouseDown(event: MouseDownEvent) {
    if (this.collapsible && (!this.header || this.collapseStyle !== Group.CollapseStyle.BOTTOM)) {
      this.setCollapsed(!this.collapsed && this.collapsible);
    }
  }

  protected _onFooterMouseDown(event: MouseDownEvent) {
    if (this.collapsible) {
      this.setCollapsed(!this.collapsed && this.collapsible);
    }
  }

  /**
   * Resizes the body to its preferred size by animating the height.
   */
  resizeBody() {
    this.animateToggleCollapse().done(() => {
      if (this.bodyAnimating) {
        // Another animation has been started in the meantime -> ignore done event
        return;
      }
      if (this.collapsed) {
        this.body.remove();
      }
      this.invalidateLayoutTree();
    });
  }

  animateToggleCollapse(): JQuery.Promise<JQuery> {
    let currentSize = graphics.cssSize(this.body.$container);
    let currentMargins = graphics.margins(this.body.$container);
    let currentPaddings = graphics.paddings(this.body.$container);
    let targetHeight, targetMargins, targetPaddings;

    if (this.collapsed) {
      // Collapsing
      // Set target values to 0 when collapsing
      targetHeight = 0;
      targetMargins = new Insets();
      targetPaddings = new Insets();
    } else {
      // Expand to preferred size of the body
      targetHeight = this.body.htmlComp.prefSize({
        widthHint: currentSize.width
      }).height;

      // Make sure body is layouted correctly before starting the animation (with the target size)
      // Use setSize to explicitly call its layout (this might even be necessary during the animation, see GroupLayout.invalidate)
      this.body.htmlComp.setSize(new Dimension(this.body.$container.outerWidth(), targetHeight));

      if (this.bodyAnimating) {
        // The group may be expanded while being collapsed or vice versa.
        // In that case, use the current values of the inline style as starting values

        // Clear current insets to read target insets from CSS anew
        this.body.$container
          .cssMarginY('')
          .cssPaddingY('');
        targetMargins = graphics.margins(this.body.$container);
        targetPaddings = graphics.paddings(this.body.$container);
      } else {
        // If toggling is not already in progress, start expanding from 0
        currentSize.height = 0;
        currentMargins = new Insets();
        currentPaddings = new Insets();
        targetMargins = graphics.margins(this.body.$container);
        targetPaddings = graphics.paddings(this.body.$container);
      }
    }

    this.bodyAnimating = true;
    if (this.collapsed) {
      this.$container.addClass('collapsing');
    }
    return this.body.$container
      .stop(true)
      .cssHeight(currentSize.height)
      .cssMarginTop(currentMargins.top)
      .cssMarginBottom(currentMargins.bottom)
      .cssPaddingTop(currentPaddings.top)
      .cssPaddingBottom(currentPaddings.bottom)
      .animate({
        height: targetHeight,
        marginTop: targetMargins.top,
        marginBottom: targetMargins.bottom,
        paddingTop: targetPaddings.top,
        paddingBottom: targetPaddings.bottom
      }, {
        duration: 350,
        progress: () => {
          this.trigger('bodyHeightChange');
          this.revalidateLayoutTree();
        },
        complete: () => {
          this.bodyAnimating = false;
          if (this.body.rendered) {
            // Remove inline styles when finished
            this.body.$container.cssMarginY('');
            this.body.$container.cssPaddingY('');
          }
          if (this.rendered) {
            this.$container.removeClass('collapsing');
          }
          this.trigger('bodyHeightChangeDone');
        }
      })
      .promise();
  }
}

export class EmptyBody extends Widget {
  protected override _render() {
    this.$container = this.$parent.appendDiv('group');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
  }
}
