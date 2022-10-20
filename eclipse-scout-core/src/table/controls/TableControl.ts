/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Action, HtmlComponent, KeyStrokeContext, NullLayout, scout, styles, Table, TableControlActionKeyStroke, TableControlCloseKeyStroke} from '../../index';

export default class TableControl extends Action {

  constructor() {
    super();
    this.tableFooter = null;
    this.contentRendered = false;
    this.height = null;
    this.animateDuration = null;
    this.resizerVisible = true;
    this.toggleAction = true;
    this.showTooltipWhenSelected = false;
  }

  static CONTAINER_SIZE = 345; // Defined in sizes.less
  static CONTAINER_ANIMATE_DURATION = 350;

  _init(model) {
    this.parent = model.parent;
    this.table = this.getTable();
    super._init(model);
    // retain the size with 'dense' in case we're in dense mode
    this.denseClass = this.session.desktop.dense ? ' dense' : '';
    TableControl.CONTAINER_SIZE = styles.getSize('table-control-container' + this.denseClass, 'height', 'height', TableControl.CONTAINER_SIZE);
    this.height = TableControl.CONTAINER_SIZE;
    this.animateDuration = TableControl.CONTAINER_ANIMATE_DURATION;
    this._setSelected(this.selected);
  }

  /**
   * @override
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.tableControlKeyStrokeContext = this._createKeyStrokeContextForTableControl();
  }

  _createKeyStrokeContextForTableControl() {
    let keyStrokeContext = new KeyStrokeContext();
    keyStrokeContext.$scopeTarget = () => this.tableFooter.$controlContent;
    keyStrokeContext.$bindTarget = () => this.tableFooter.$controlContent;
    keyStrokeContext.registerKeyStroke(new TableControlCloseKeyStroke(this));
    return keyStrokeContext;
  }

  _createLayout() {
    return new NullLayout();
  }

  _render() {
    let classes = 'table-control ';
    if (this.cssClass) {
      classes += this.cssClass + '-table-control';
    }
    this.$container = this.$parent.appendDiv(classes)
      .on('mousedown', this._onMouseDown.bind(this))
      .data('control', this);
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(this._createLayout());
  }

  remove() {
    this.removeContent();
    super.remove();
  }

  _renderContent($parent) {
    // to be implemented by subclass
  }

  _removeContent() {
    // to be implemented by subclass
  }

  removeContent() {
    if (this.contentRendered) {
      this._removeContent();
      if (this.cssClass) {
        this.tableFooter.$controlContainer.removeClass(this.cssClass + '-table-control-container');
        this.tableFooter.$controlContent.removeClass(this.cssClass + '-table-control-content');
      }
      this.session.keyStrokeManager.uninstallKeyStrokeContext(this.tableControlKeyStrokeContext);
      this.contentRendered = false;
    }
  }

  /**
   * Renders the content if not already rendered.<br>
   * Opens the container if the container is not already open.<br>
   * Does nothing if the content is not available yet to -> don't open container if content is not rendered yet to prevent blank container or laggy opening.<br>
   * Does nothing if the control is not selected.
   */
  renderContent() {
    if (!this.contentRendered && !this.isContentAvailable()) {
      return;
    }

    if (!this.selected) {
      return;
    }

    if (!this.tableFooter.open) {
      this.tableFooter.openControlContainer(this);
    }

    if (!this.contentRendered) {
      if (this.cssClass) {
        this.tableFooter.$controlContainer.addClass(this.cssClass + '-table-control-container');
        this.tableFooter.$controlContent.addClass(this.cssClass + '-table-control-content');
      }
      this.tableFooter.$controlContent.data('control', this);
      this._renderContent(this.tableFooter.$controlContent);
      this.session.keyStrokeManager.installKeyStrokeContext(this.tableControlKeyStrokeContext);
      if (this.htmlComp) {
        this.htmlComp.invalidateLayoutTree(false);
      }
      this.contentRendered = true;
    }
  }

  /**
   * @override
   */
  get$Scrollable() {
    return this.$contentContainer;
  }

  _renderSelected(selected, options) {
    selected = scout.nvl(selected, this.selected);
    options = $.extend({}, {closeWhenUnselected: true}, options);

    this.$container.select(selected);

    if (selected) {
      this.tableFooter.onControlSelected(this);
      this.renderContent();
    } else {

      // Don't modify the state initially, only on property change events
      if (this.rendered) {

        if (options.closeWhenUnselected && this === this.tableFooter.selectedControl) {
          // Don't remove immediately, wait for the animation to finish (handled by onControlContainerClosed)
          this.tableFooter.onControlSelected(null);
          this.tableFooter.closeControlContainer(this, options);
        } else {
          this.removeContent();
        }

      }
    }
    this._updateTooltip();
  }

  /**
   * Returns true if the table control may be displayed (opened).
   */
  isContentAvailable() {
    return true;
  }

  toggle() {
    if (!this.enabledComputed) {
      return;
    }
    if (this.tableFooter.selectedControl === this) {
      this.setSelected(false);
    } else {
      this.setSelected(true);
    }
  }

  setSelected(selected, options) {
    if (selected && !this.visible) {
      return;
    }
    if (selected === this.selected) {
      return;
    }

    if (this.tableFooter && this.tableFooter.selectedControl && this.tableFooter.selectedControl !== this) {
      this.tableFooter.selectedControl.setSelected(false, {closeWhenUnselected: false});
    }

    // Instead of calling parent.setSelected(), we manually execute the required code. Otherwise
    // we would not be able to pass 'closeWhenUnselected' to _renderSelected().
    this._setSelected(selected);
    options = $.extend({}, {closeWhenUnselected: true}, options);
    if (this.rendered) {
      this._renderSelected(selected, options);
    } else if (options.closeWhenUnselected && this.tableFooter && this === this.tableFooter.selectedControl && !selected) {
      this.tableFooter.onControlSelected(null);
    }
  }

  _setSelected(selected) {
    // Does not nothing more than the default but allows for extension by a subclass
    this._setProperty('selected', selected);
  }

  _configureTooltip() {
    let options = super._configureTooltip();
    options.cssClass = 'table-control-tooltip';
    return options;
  }

  _onMouseDown() {
    this.toggle();
  }

  onControlContainerOpened() {
    // nop
  }

  onControlContainerClosed() {
    this.removeContent();
  }

  /**
   * @override Action.js
   */
  _createActionKeyStroke() {
    return new TableControlActionKeyStroke(this);
  }

  getTable() {
    let parent = this.parent;
    while (parent) {
      if (parent instanceof Table) {
        return parent;
      }
      parent = parent.parent;
    }

    return null;
  }
}
