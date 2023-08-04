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
  AbstractLayout, Action, ActionKeyStroke, HtmlComponent, InitModelOf, KeyStrokeContext, NullLayout, scout, styles, Table, TableControlActionKeyStroke, TableControlCloseKeyStroke, TableControlModel, TableFooter, TooltipSupport
} from '../../index';

export class TableControl extends Action implements TableControlModel {
  declare model: TableControlModel;

  tableFooter: TableFooter;
  table: Table;
  contentRendered: boolean;
  height: number;
  animateDuration: number;
  resizerVisible: boolean;
  denseClass: string;
  tableControlKeyStrokeContext: KeyStrokeContext;
  $contentContainer: JQuery;

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

  protected override _init(model: InitModelOf<this>) {
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

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.tableControlKeyStrokeContext = this._createKeyStrokeContextForTableControl();
  }

  protected _createKeyStrokeContextForTableControl(): KeyStrokeContext {
    let keyStrokeContext = new KeyStrokeContext();
    keyStrokeContext.$scopeTarget = () => this.tableFooter.$controlContent;
    keyStrokeContext.$bindTarget = () => this.tableFooter.$controlContent;
    keyStrokeContext.registerKeyStroke(new TableControlCloseKeyStroke(this));
    return keyStrokeContext;
  }

  protected override _createLayout(): AbstractLayout {
    return new NullLayout();
  }

  protected override _render() {
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

  override remove() {
    this.removeContent();
    super.remove();
  }

  protected _renderContent($parent: JQuery) {
    // to be implemented by subclass
  }

  protected _removeContent() {
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
   * Does nothing if the content is not available yet to -> don't open container if content is not rendered yet to prevent blank container or lags during open.<br>
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

  override get$Scrollable(): JQuery {
    return this.$contentContainer;
  }

  protected override _renderSelected(selected?: boolean, options?: {
    closeWhenUnselected?: boolean;
    animate?: boolean;
  }) {
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
  isContentAvailable(): boolean {
    return true;
  }

  override toggle() {
    if (!this.enabledComputed) {
      return;
    }
    if (this.tableFooter.selectedControl === this) {
      this.setSelected(false);
    } else {
      this.setSelected(true);
    }
  }

  override setSelected(selected: boolean, options?: {
    closeWhenUnselected?: boolean;
    animate?: boolean;
  }) {
    if (selected && !this.visible) {
      return;
    }
    if (selected === this.selected) {
      return;
    }

    if (this.tableFooter && this.tableFooter.selectedControl && this.tableFooter.selectedControl !== this) {
      this.tableFooter.selectedControl.setSelected(false, {closeWhenUnselected: false});
    }

    // Instead of calling parent.setSelected(), we manually execute the required code. Otherwise,
    // we would not be able to pass 'closeWhenUnselected' to _renderSelected().
    this._setSelected(selected);
    options = $.extend({}, {closeWhenUnselected: true}, options);
    if (this.rendered) {
      this._renderSelected(selected, options);
    } else if (options.closeWhenUnselected && this.tableFooter && this === this.tableFooter.selectedControl && !selected) {
      this.tableFooter.onControlSelected(null);
    }
  }

  protected _setSelected(selected: boolean) {
    // Does nothing more than the default but allows for extension by a subclass
    this._setProperty('selected', selected);
  }

  protected override _configureTooltip(): InitModelOf<TooltipSupport> {
    let options = super._configureTooltip();
    options.cssClass = 'table-control-tooltip';
    return options;
  }

  protected _onMouseDown(event: JQuery.MouseDownEvent) {
    this.toggle();
  }

  onControlContainerOpened() {
    // nop
  }

  onControlContainerClosed() {
    this.removeContent();
  }

  protected override _createActionKeyStroke(): ActionKeyStroke {
    return new TableControlActionKeyStroke(this);
  }

  getTable(): Table {
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
