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
import {AbstractLayout, Dimension, HtmlComponent, HtmlEnvironment, LayoutConstants, LogicalGridLayoutConfig, LogicalGridLayoutInfo} from '../../index';
import $ from 'jquery';

/**
 * JavaScript port of org.eclipse.scout.rt.ui.swing.LogicalGridLayout.
 *
 * @param options available options: hgap, vgap, rowHeight, columnWidth, minWidth
 *
 */
export default class LogicalGridLayout extends AbstractLayout {

  constructor(widget, layoutConfig) {
    super();
    this.cssClass = 'logical-grid-layout';
    this.validityBasedOnContainerSize = new Dimension();
    this.valid = false;
    this.widget = widget;
    this.info = null;

    this._initDefaults();
    this.layoutConfig = LogicalGridLayoutConfig.ensure(layoutConfig || {});
    this.layoutConfig.applyToLayout(this);

    this.htmlPropertyChangeHandler = this._onHtmlEnvironmentPropertyChange.bind(this);
    HtmlEnvironment.get().on('propertyChange', this.htmlPropertyChangeHandler);
    this.widget.one('remove', () => {
      HtmlEnvironment.get().off('propertyChange', this.htmlPropertyChangeHandler);
    });

  }

  _initDefaults() {
    let env = HtmlEnvironment.get();
    this.hgap = env.formColumnGap;
    this.vgap = env.formRowGap;
    this.columnWidth = env.formColumnWidth;
    this.rowHeight = env.formRowHeight;
    this.minWidth = 0;
  }

  _onHtmlEnvironmentPropertyChange() {
    this._initDefaults();
    this.layoutConfig.applyToLayout(this);
    this.widget.invalidateLayoutTree();
    this.widget.invalidateLogicalGrid();
  }

  validateLayout($container, options) {
    let visibleComps = [],
      visibleCons = [];

    // If there is a logical grid, validate it (= recalculate if it is dirty) and use the grid config to get the grid relevant widgets (Scout JS).
    // If there is no logical grid the grid relevant widgets are found using DOM by selecting the children with a html component (Scout classic).
    if (this.widget.logicalGrid) {
      this.widget.validateLogicalGrid();
      // It is important that the logical grid and the layout use the same widgets. Otherwise there may be widgets without a gridData which is required by the layout.
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
      heightHint: options.heightHint,
      widthOnly: options.widthOnly
    });
    $.log.isTraceEnabled() && $.log.trace('(LogicalGridLayout#validateLayout) $container=' + HtmlComponent.get($container).debug());
  }

  _validateGridData(htmlComp) {
    let $comp = htmlComp.$comp;
    let widget = $comp.data('widget');
    // Prefer the visibility state of the widget, if there is one.
    // This allows for transitions, because the $component may still be in the process of being made invisible
    let visible = widget ? widget.isVisible() : $comp.isVisible();
    if (visible) {
      htmlComp.layoutData.validate();
      return true;
    }
  }

  layout($container) {
    this._layout($container);
  }

  _layout($container) {
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

  _layoutCellBounds(containerSize, containerInsets) {
    return this.info.layoutCellBounds(containerSize, containerInsets);
  }

  preferredLayoutSize($container, options) {
    // widthHint and heightHint are already adjusted by HtmlComponent, no need to remove insets here
    this.validateLayout($container, options);

    let sizeflag = LayoutConstants.PREF;
    let dim = new Dimension();
    // w
    let i, w, h, useCount = 0;
    // noinspection DuplicatedCode
    for (i = 0; i < this.info.cols; i++) {
      w = this.info.width[i][sizeflag];
      if (useCount > 0) {
        dim.width += this.hgap;
      }
      dim.width += w;
      useCount++;
    }
    // h
    useCount = 0;
    // noinspection DuplicatedCode
    for (i = 0; i < this.info.rows; i++) {
      h = this.info.height[i][sizeflag];
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
