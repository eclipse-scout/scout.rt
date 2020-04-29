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
import {Dimension, graphics, GroupLayout, GroupToggleCollapseKeyStroke, HtmlComponent, Icon, Insets, KeyStrokeContext, LoadingSupport, scout, tooltips, Widget} from '../index';
import $ from 'jquery';

export default class Group extends Widget {

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
    this._addWidgetProperties(['header']);
    this._addWidgetProperties(['body']);
  }

  static CollapseStyle = {
    LEFT: 'left',
    RIGHT: 'right',
    BOTTOM: 'bottom'
  };

  _init(model) {
    super._init(model);
    this.resolveTextKeys(['title', 'titleSuffix']);
    this.resolveIconIds(['iconId']);
    this._setBody(this.body);
    this._setHeader(this.header);
  }

  /**
   * @override
   */
  _createKeyStrokeContext() {
    return new KeyStrokeContext();
  }

  /**
   * @override
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    // Key stroke should only work when header is focused
    this.keyStrokeContext.$bindTarget = function() {
      return this.$header;
    }.bind(this);
    this.keyStrokeContext.registerKeyStroke([
      new GroupToggleCollapseKeyStroke(this)
    ]);
  }

  _render() {
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
  }

  _renderProperties() {
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

  _remove() {
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

  _renderEnabled() {
    super._renderEnabled();
    this.$header.setTabbable(this.enabledComputed);
  }

  setIconId(iconId) {
    this.setProperty('iconId', iconId);
  }

  /**
   * Adds an image or font-based icon to the group header by adding either an IMG or SPAN element.
   */
  _renderIconId() {
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
    this.icon = scout.create('Icon', {
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

  _updateIconStyle() {
    let hasTitle = !!this.title;
    this.get$Icon().toggleClass('with-title', hasTitle);
    this.get$Icon().addClass('group-icon');
    this._renderCollapseStyle();
  }

  get$Icon() {
    if (this.icon) {
      return this.icon.$container;
    }
    return $();
  }

  _removeIconId() {
    if (this.icon) {
      this.icon.destroy();
    }
  }

  setHeader(header) {
    this.setProperty('header', header);
  }

  _setHeader(header) {
    this._setProperty('header', header);
  }

  setHeaderFocusable(headerFocusable) {
    this.setProperty('headerFocusable', headerFocusable);
  }

  _renderHeaderFocusable() {
    this.$header.toggleClass('unfocusable', !this.headerFocusable);
  }

  setTitle(title) {
    this.setProperty('title', title);
  }

  _renderTitle() {
    if (this.$title) {
      if (this.titleHtmlEnabled) {
        this.$title.htmlOrNbsp(this.title);
      } else {
        this.$title.textOrNbsp(this.title);
      }
      this._updateIconStyle();
    }
  }

  setTitleSuffix(titleSuffix) {
    this.setProperty('titleSuffix', titleSuffix);
  }

  _renderTitleSuffix() {
    if (this.$titleSuffix) {
      this.$titleSuffix.text(this.titleSuffix || '');
    }
  }

  setHeaderVisible(headerVisible) {
    this.setProperty('headerVisible', headerVisible);
  }

  _renderHeaderVisible() {
    this.$header.setVisible(this.headerVisible);
    this._renderCollapsible();
    this.invalidateLayoutTree();
  }

  setBody(body) {
    this.setProperty('body', body);
  }

  _setBody(body) {
    if (!body) {
      // Create empty body if no body was provided
      body = scout.create('Widget', {
        parent: this,
        _render: function() {
          this.$container = this.$parent.appendDiv('group');
          this.htmlComp = HtmlComponent.install(this.$container, this.session);
        }
      });
    }
    this._setProperty('body', body);
  }

  _createLoadingSupport() {
    return new LoadingSupport({
      widget: this,
      $container: function() {
        return this.$header;
      }.bind(this)
    });
  }

  _renderHeader() {
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
    this.$header.on('mousedown', this._onHeaderMouseDown.bind(this));
    this.invalidateLayoutTree();
  }

  _renderBody() {
    this.body.render();
    this.body.$container.insertAfter(this.$header);
    this.body.$container.addClass('group-body');
    this.body.invalidateLayoutTree();
  }

  /**
   * @override
   */
  getFocusableElement() {
    if (!this.rendered) {
      return false;
    }
    return this.$header;
  }

  toggleCollapse() {
    this.setCollapsed(!this.collapsed && this.collapsible);
  }

  setCollapsed(collapsed) {
    this.setProperty('collapsed', collapsed);
  }

  _renderCollapsed() {
    this.$container.toggleClass('collapsed', this.collapsed);
    this.$collapseIcon.toggleClass('collapsed', this.collapsed);
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

  setCollapsible(collapsible) {
    this.setProperty('collapsible', collapsible);
  }

  _renderCollapsible() {
    this.$container.toggleClass('collapsible', this.collapsible);
    this.$header.toggleClass('disabled', !this.collapsible);
    // footer is visible if collapseStyle is 'bottom' and either header is visible or has a (collapsible) body
    this.$footer.setVisible(this.collapseStyle === Group.CollapseStyle.BOTTOM && (this.headerVisible || this.collapsible));
    this.$collapseIcon.setVisible(this.collapsible);
    this.invalidateLayoutTree();
  }

  setCollapseStyle(collapseStyle) {
    this.setProperty('collapseStyle', collapseStyle);
  }

  _renderCollapseStyle() {
    this.$header.toggleClass('collapse-right', this.collapseStyle === Group.CollapseStyle.RIGHT);
    this.$container.toggleClass('collapse-bottom', this.collapseStyle === Group.CollapseStyle.BOTTOM);

    if (this.collapseStyle === Group.CollapseStyle.RIGHT && !this.header) {
      this.$collapseIcon.appendTo(this.$header);
    } else if (this.collapseStyle === Group.CollapseStyle.LEFT && !this.header) {
      this.$collapseIcon.prependTo(this.$header);
    } else if (this.collapseStyle === Group.CollapseStyle.BOTTOM) {
      let sibling = this.body.$container ? this.body.$container : this.$header;
      this.$footer.insertAfter(sibling);
      this.$collapseIcon.insertAfter(this.$collapseBorderLeft);
    }

    this._renderCollapsible();
    this.invalidateLayoutTree();
  }

  _onHeaderMouseDown(event) {
    if (this.collapsible && (!this.header || this.collapseStyle !== Group.CollapseStyle.BOTTOM)) {
      this.setCollapsed(!this.collapsed && this.collapsible);
    }
  }

  _onFooterMouseDown(event) {
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
        // Another animation has been started in the mean time -> ignore done event
        return;
      }
      if (this.collapsed) {
        this.body.remove();
      }
      this.invalidateLayoutTree();
    });
  }

  /**
   * @param {object} [options]
   * @returns {Promise}
   */
  animateToggleCollapse(options) {
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
      // Expanding
      // Expand to preferred size of the body
      targetHeight = this.body.htmlComp.prefSize({
        widthHint: currentSize.width
      }).height;

      // Make sure body is layouted correctly before starting the animation (with the target size)
      // Use setSize to explicitly call its layout (this might even be necessary during the animation, see GroupLayout.invalidate)
      this.body.htmlComp.setSize(new Dimension(this.body.$container.outerWidth(), targetHeight));

      if (this.bodyAnimating) {
        // The group may be expanded while being collapsed or vice verca.
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
        progress: function() {
          this.trigger('bodyHeightChange');
          this.revalidateLayoutTree();
        }.bind(this),
        complete: function() {
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
        }.bind(this)
      })
      .promise();
  }
}
