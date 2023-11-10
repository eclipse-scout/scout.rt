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
  AbstractLayout, Dimension, EventHandler, HtmlComponent, HtmlCompPrefSizeOptions, Insets, LayoutConstants, LogicalGridData, LogicalGridLayoutConfig, LogicalGridLayoutConfigModel, LogicalGridLayoutInfo, ObjectOrModel, Rectangle, Widget
} from '../../index';
import $ from 'jquery';

/**
 * The logical grid layout arranges the elements in a container automatically based on the {@link LogicalGridData}.
 * The {@link LogicalGridData} is typically initialized from a {@link GridData}, and a {@link GridData} is calculated by a {@link LogicalGrid} derived from {@link LogicalGridWidget.gridDataHints}.
 *
 * This may sound a bit confusing at first. The important part is:
 * based on the order of the widgets (e.g. the order of {@link GroupBoxModel.fields}) and the {@link LogicalGridWidget.gridDataHints} (e.g. {@link FormField.gridDataHints}),
 * the widgets will be arranged automatically by the logical grid layout. So you only need to take care of the order and of configuring the hints.
 *
 * The width, height and margin of a grid cell is pre-defined, but you can adjust it using {@link LogicalGridLayoutConfig}, e.g. by configuring {@link GroupBoxModel.bodyLayoutConfig}.
 */
export class LogicalGridLayout extends AbstractLayout {
  validityBasedOnContainerSize: Dimension;
  valid: boolean;
  widget: Widget;
  info: LogicalGridLayoutInfo;
  layoutConfig: LogicalGridLayoutConfig;
  htmlPropertyChangeHandler: EventHandler;
  hgap: number;
  vgap: number;
  columnWidth: number;
  rowHeight: number;
  minWidth: number;

  constructor(widget: Widget, layoutConfig: ObjectOrModel<LogicalGridLayoutConfig>) {
    super();
    this.cssClass = 'logical-grid-layout';
    this.validityBasedOnContainerSize = new Dimension();
    this.valid = false;
    this.widget = widget;
    this.info = null;

    this.layoutConfig = LogicalGridLayoutConfig.ensure(layoutConfig || {} as LogicalGridLayoutConfigModel);
    this.layoutConfig.applyToLayout(this);
  }

  validateLayout($container: JQuery, options: HtmlCompPrefSizeOptions) {
    let visibleComps = [],
      visibleCons = [];

    // If there is a logical grid, validate it (= recalculate if it is dirty) and use the grid config to get the grid relevant widgets (Scout JS).
    // If there is no logical grid the grid relevant widgets are found using DOM by selecting the children with a html component (Scout classic).
    if (this.widget.logicalGrid) {
      this.widget.validateLogicalGrid();
      // It is important that the logical grid and the layout use the same widgets. Otherwise, there may be widgets without a gridData which is required by the layout.
      // This can happen if the widgets are inserted and removed by an animation before the layout has been done. If the widget is removed using an animation it is not in the list of getGridWidgets() anymore but may still be in the DOM.
      this.widget.logicalGrid.gridConfig.getGridWidgets().forEach(function(widget) {
        if (!widget.rendered) {
          // getGridWidgets may return non rendered widgets, but grid should be calculated nevertheless
          return;
        }
        if (!widget.htmlComp) {
          $.log.isWarnEnabled() && $.log.warn('(LogicalGridLayout#validateLayout) no htmlComp found, widget cannot be layouted. Widget: ' + widget);
          return;
        }
        validateGridData.call(this, widget.htmlComp);
      }, this);
    } else {
      $container.children().each((idx, elem) => {
        let $comp = $(elem);
        let htmlComp = HtmlComponent.optGet($comp);
        if (!htmlComp) {
          // Only consider elements with a html component
          return;
        }
        validateGridData.call(this, htmlComp);
      });
    }

    function validateGridData(htmlComp) {
      if (this._validateGridData(htmlComp)) {
        visibleComps.push(htmlComp.$comp);
        visibleCons.push(htmlComp.layoutData);
      }
    }

    this.info = new LogicalGridLayoutInfo({
      $components: visibleComps,
      cons: visibleCons,
      hgap: this.hgap,
      vgap: this.vgap,
      rowHeight: this.rowHeight,
      columnWidth: this.columnWidth,
      widthHint: options.widthHint,
      widthOnly: options.widthOnly
    });
    $.log.isTraceEnabled() && $.log.trace('(LogicalGridLayout#validateLayout) $container=' + HtmlComponent.get($container).debug());
  }

  protected _validateGridData(htmlComp: HtmlComponent): boolean {
    let $comp = htmlComp.$comp;
    let widget = $comp.data('widget');
    // Prefer the visibility state of the widget, if there is one.
    // This allows for transitions, because the $component may still be in the process of being made invisible
    let visible = widget ? widget.visible : $comp.isVisible();
    if (visible) {
      (<LogicalGridData>htmlComp.layoutData).validate();
      return true;
    }
  }

  override layout($container: JQuery) {
    this._layout($container);
  }

  protected _layout($container: JQuery) {
    let htmlContainer = HtmlComponent.get($container),
      containerSize = htmlContainer.availableSize(),
      containerInsets = htmlContainer.insets();
    this.validateLayout($container, {
      widthHint: containerSize.width - containerInsets.horizontal(),
      heightHint: containerSize.height - containerInsets.vertical()
    });
    if (this.minWidth > 0 && containerSize.width < this.minWidth) {
      containerSize.width = this.minWidth;
    }
    $.log.isTraceEnabled() && $.log.trace('(LogicalGridLayout#layout) container ' + htmlContainer.debug() + ' size=' + containerSize + ' insets=' + containerInsets);
    let cellBounds = this._layoutCellBounds(containerSize, containerInsets);

    // Set bounds of components
    let r1, r2, r, d, $comp, i, htmlComp, data, delta, margins;
    for (i = 0; i < this.info.$components.length; i++) {
      $comp = this.info.$components[i];
      htmlComp = HtmlComponent.get($comp);
      data = this.info.gridDatas[i];
      r1 = cellBounds[data.gridy][data.gridx];
      r2 = cellBounds[data.gridy + data.gridh - 1][data.gridx + data.gridw - 1];
      r = r1.union(r2);
      margins = htmlComp.margins();
      r.width -= margins.horizontal();
      r.height -= margins.vertical();
      if (data.fillHorizontal && data.fillVertical) {
        // ok
      } else {
        d = this.info.compSize[i];
        if (!data.fillHorizontal && d.width < r.width) {
          delta = r.width - d.width;
          r.width = d.width;
          if (data.horizontalAlignment === 0) {
            // Do ceil the result as other layout managers of Java also handle floating calculation results that way.
            // This is important if being used in conjunction with another layout manager.
            // E.g. the editable checkbox in inline table cell is a JCheckBox and rendered by LogicalGridLayout,
            // whereas the default boolean representation in a table cell is simply an image on a label positioned by
            // default layout manager. If switching in between of edit and non-edit mode, the widget would bounce otherwise.
            r.x += Math.ceil(delta / 2.0);
          } else if (data.horizontalAlignment > 0) {
            r.x += delta;
          }
        }
        if (!data.fillVertical && d.height < r.height) {
          delta = r.height - d.height;
          if (data.heightHint === 0) {
            r.height = d.height;
          } else {
            r.height = data.heightHint;
          }
          if (data.verticalAlignment === 0) {
            // Do ceil the result as other layout managers of Java also handle floating calculation results that way.
            // This is important if being used in conjunction with another layout manager.
            // E.g. the editable checkbox in inline table cell is a JCheckBox and rendered by LogicalGridLayout,
            // whereas the default boolean representation in a table cell is simply an image on a label positioned by
            // default layout manager. If switching in between of edit and non-edit mode, the widget would bounce otherwise.
            r.y += Math.ceil(delta / 2.0);
          } else if (data.verticalAlignment > 0) {
            r.y += delta;
          }
        }
      }
      $.log.isTraceEnabled() && $.log.trace('(LogicalGridLayout#layout) comp=' + htmlComp.debug() + ' bounds=' + r);
      htmlComp.setBounds(r);
    }
  }

  protected _layoutCellBounds(containerSize: Dimension, containerInsets: Insets): Rectangle[][] {
    return this.info.layoutCellBounds(containerSize, containerInsets);
  }

  override preferredLayoutSize($container: JQuery, options: HtmlCompPrefSizeOptions): Dimension {
    // widthHint and heightHint are already adjusted by HtmlComponent, no need to remove insets here
    this.validateLayout($container, options);

    let sizeFlag = LayoutConstants.PREF;
    let dim = new Dimension();
    // w
    let i, w, h, useCount = 0;
    for (i = 0; i < this.info.cols; i++) {
      w = this.info.width[i][sizeFlag];
      if (useCount > 0) {
        dim.width += this.hgap;
      }
      dim.width += w;
      useCount++;
    }
    // h
    useCount = 0;
    for (i = 0; i < this.info.rows; i++) {
      h = this.info.height[i][sizeFlag];
      if (useCount > 0) {
        dim.height += this.vgap;
      }
      dim.height += h;
      useCount++;
    }
    // insets
    let insets = HtmlComponent.get($container).insets();
    dim.width += insets.horizontal();
    dim.height += insets.vertical();
    return dim;
  }
}
