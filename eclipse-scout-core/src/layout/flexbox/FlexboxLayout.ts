/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, EnumObject, FlexboxLayoutData, HtmlComponent, HtmlCompPrefSizeOptions, Rectangle, webstorage} from '../../index';
import $ from 'jquery';

export class FlexboxLayout extends AbstractLayout {

  static readonly STORAGE_KEY = 'scout.flexboxLayout';

  direction: FlexboxDirection;
  cacheKey: string[];

  protected _layoutDatasToReset = new Set<FlexboxLayoutData>();

  constructor(direction: FlexboxDirection, cacheKey: string[]) {
    super();
    this.direction = direction;
    this.cacheKey = cacheKey;
  }

  static Direction = {
    COLUMN: 0,
    ROW: 1
  } as const;

  setCacheKey(cacheKey: string[]) {
    this.cacheKey = cacheKey ? [...cacheKey] : null;
  }

  protected _readCache(childCount: number): number[] {
    if (!this.cacheKey || this.cacheKey.length === 0 || childCount < 2) {
      return;
    }

    let keySequence = [...this.cacheKey, String(childCount)];
    let cacheValue = webstorage.getItemFromLocalStorage(FlexboxLayout.STORAGE_KEY);
    let cacheObj = cacheValue ? JSON.parse(cacheValue) : null;

    let i = 0;
    while (cacheObj && i < keySequence.length) {
      cacheObj = cacheObj[keySequence[i]];
      i++;
    }

    return cacheObj;
  }

  protected _writeCache(childCount: number, sizes: number[]) {
    if (!this.cacheKey || this.cacheKey.length === 0 || childCount < 2) {
      return;
    }

    let keySequence = [...this.cacheKey, String(childCount)];
    let cacheValue = webstorage.getItemFromLocalStorage(FlexboxLayout.STORAGE_KEY);
    let cacheObj = cacheValue ? JSON.parse(cacheValue) : {};

    let cachedSizes = cacheObj;
    let i = 0;
    while (i < keySequence.length - 1) {
      cachedSizes[keySequence[i]] = cachedSizes[keySequence[i]] || {};
      cachedSizes = cachedSizes[keySequence[i]];
      i++;
    }
    cachedSizes[keySequence[i]] = sizes;

    webstorage.setItemToLocalStorage(FlexboxLayout.STORAGE_KEY, JSON.stringify(cacheObj));
  }

  // layout functions

  override layout($container: JQuery) {
    let htmlContainer = HtmlComponent.get($container);
    let availableSize = htmlContainer.availableSize({exact: true})
      .subtract(htmlContainer.insets());

    let children = this._getChildren($container);
    let splitterWithDiff = children.find(c => (c.layoutData as FlexboxLayoutData).diff);

    if (splitterWithDiff) {
      this._layoutDelta(children, splitterWithDiff, availableSize);
    } else {
      this._layoutComponents(children, availableSize);
    }
  }

  protected _getChildren($container: JQuery): HtmlComponent[] {
    let children = [];
    $container.children().each(function() {
      let htmlChild = HtmlComponent.optGet($(this));
      if (htmlChild) {
        children.push(htmlChild);
      }
    });
    children = children.sort((a, b) => {
      return (a.layoutData.order || 0) - (b.layoutData.order || 0);
    });
    return children;
  }

  reset() {
    this._layoutDatasToReset.forEach(ld => ld.reset());
    this._layoutDatasToReset.clear();
  }

  protected _layoutDelta(children: HtmlComponent[], deltaComp: HtmlComponent, containerSize: Dimension) {
    this._ensureInitialValues(children, containerSize);

    let delta = (deltaComp.layoutData as FlexboxLayoutData).diff;
    let componentsBefore = children.slice(0, children.indexOf(deltaComp)).reverse();
    let componentsAfter = children.slice(children.indexOf(deltaComp) + 1);

    // calculate if the delta can be applied to the previous and following columns
    let deltaDiffPrev = distributeDelta(componentsBefore, delta, false);
    let deltaDiffNext = -distributeDelta(componentsAfter, -delta, false);

    // compute the max delta could be applied
    delta = Math.sign(delta) * (Math.min(Math.abs(delta - deltaDiffPrev), Math.abs(delta - deltaDiffNext)));

    if (delta !== 0) {
      // apply the delta to the previous and following columns
      distributeDelta(componentsBefore, delta, true);
      distributeDelta(componentsAfter, -delta, true);
    }

    this._layoutFromLayoutDataWithCache(children, containerSize);

    function distributeDelta(components, delta, applyDelta) {
      return components.reduce((delta, c) => {
        if (delta !== 0) {
          delta = c.layoutData.acceptDelta(delta, applyDelta);
        }
        return delta;
      }, delta);
    }
  }

  protected _layoutComponents(children: HtmlComponent[], containerSize: Dimension) {
    let delta = this._ensureInitialValues(children, containerSize);
    if (delta < 0) {
      this._adjust(children, delta, ld => ld.shrink);
    } else if (delta > 0) {
      this._adjust(children, delta, ld => ld.grow);
    }
    this._layoutFromLayoutDataWithCache(children, containerSize);
  }

  protected _adjust(children: HtmlComponent[], delta: number, getWeightFunction: (ld: FlexboxLayoutData) => number) {
    let flexibleLayoutDatas = children
      .map(c => c.layoutData as FlexboxLayoutData)
      .filter(ld => {
        return ld.acceptDelta(Math.sign(delta)) === 0;
      });
    // If some parts are absolute and some parts are relative, only adjust the relative parts
    if (flexibleLayoutDatas.some(ld => ld.relative) && flexibleLayoutDatas.some(ld => !ld.relative)) {
      flexibleLayoutDatas = flexibleLayoutDatas.filter(ld => ld.relative);
    }

    if (!flexibleLayoutDatas.length) {
      return;
    }

    let weightSum = flexibleLayoutDatas.reduce((weight, ld) => {
      return weight + getWeightFunction(ld);
    }, 0);
    let deltaFactor = delta / weightSum;

    // Apply delta
    delta = flexibleLayoutDatas.reduce((delta, ld) => {
      return ld.acceptDelta(deltaFactor * getWeightFunction(ld), true);
    }, delta);

    if (Math.abs(delta) > 0.2) {
      this._adjust(children, delta, getWeightFunction);
    }
  }

  protected _getPreferredSize(htmlComp: HtmlComponent): Dimension {
    return htmlComp.prefSize({useCssSize: true})
      .add(htmlComp.margins());
  }

  protected _ensureInitialValues(children: HtmlComponent[], containerSize: Dimension): number {
    // Setup initial values
    let totalPx = this._getDimensionValue(containerSize);
    let sumOfAbsolutePx = 0;
    let sumOfRelatives = 0;
    let relatives: HtmlComponent[] = [];
    children.forEach(comp => {
      let ld = comp.layoutData as FlexboxLayoutData;
      this._layoutDatasToReset.add(ld); // remember for later reset()

      if (ld.sizePx) {
        sumOfAbsolutePx += ld.sizePx;
      } else if (ld.initial < 0) {
        // use ui size
        ld.initialPx = this._getDimensionValue(this._getPreferredSize(comp));
        sumOfAbsolutePx += ld.initialPx;
      } else if (ld.relative) {
        sumOfRelatives += ld.initial;
        relatives.push(comp);
      } else {
        ld.initialPx = ld.initial;
        sumOfAbsolutePx += ld.initialPx;
      }
    });

    // Distribute remaining size to all relative parts without fixed size
    if (sumOfRelatives) {
      let totalRemainderPx = totalPx - sumOfAbsolutePx;
      let relativeFactor = totalRemainderPx / sumOfRelatives;
      relatives.forEach(comp => {
        let ld = comp.layoutData as FlexboxLayoutData;
        ld.initialPx = Math.max(30, relativeFactor * ld.initial);
      });
    }

    // Set sizePx and return "delta" value (remainder that was not distributed to any part)
    let cachedSizes = this._readCache(children.length) || [];
    return children.reduce((remainderPx, comp, i) => {
      let ld = comp.layoutData as FlexboxLayoutData;
      if (!ld.sizePx) {
        if (cachedSizes[i]) {
          ld.sizePx = ld.validate(Math.round(totalPx * cachedSizes[i]));
        } else {
          ld.sizePx = ld.initialPx;
        }
      }
      return remainderPx - ld.sizePx;
    }, totalPx);
  }

  protected _layoutFromLayoutDataWithCache(children: HtmlComponent[], containerSize: Dimension) {
    this._cacheSizes(children, containerSize);
    this._layoutFromLayoutData(children, containerSize);
  }

  protected _cacheSizes(children: HtmlComponent[], containerSize: Dimension) {
    let totalPx = this._getDimensionValue(containerSize);
    let value = children.map(c => (c.layoutData as FlexboxLayoutData).sizePx / totalPx);
    this._writeCache(children.length, value);
  }

  // functions differ from row to column mode

  protected _getDimensionValue(dimension: Dimension): number {
    return this.direction === FlexboxLayout.Direction.ROW
      ? dimension.width
      : dimension.height;
  }

  protected _layoutFromLayoutData(children: HtmlComponent[], containerSize: Dimension) {
    return this.direction === FlexboxLayout.Direction.ROW
      ? this._layoutFromLayoutDataRow(children, containerSize)
      : this._layoutFromLayoutDataColumn(children, containerSize);
  }

  protected _layoutFromLayoutDataRow(children: HtmlComponent[], containerSize: Dimension) {
    children.reduce((x, comp) => {
      let margins = comp.margins();
      let insets = comp.insets();
      let w = (comp.layoutData as FlexboxLayoutData).sizePx;
      let bounds = new Rectangle(x - insets.left - margins.left, 0, w + insets.left + insets.right, containerSize.height);
      comp.setBounds(bounds);
      return x + w;
    }, 0);
  }

  protected _layoutFromLayoutDataColumn(children: HtmlComponent[], containerSize: Dimension) {
    children.reduce((y, comp) => {
      let margins = comp.margins();
      let insets = comp.insets();
      let h = (comp.layoutData as FlexboxLayoutData).sizePx;
      let bounds = new Rectangle(0, y - insets.top - margins.top, containerSize.width, h + insets.top + insets.bottom);
      comp.setBounds(bounds);
      return y + h;
    }, 0);
  }

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    return this.direction === FlexboxLayout.Direction.ROW
      ? this._preferredLayoutSizeRow($container, options)
      : this._preferredLayoutSizeColumn($container, options);
  }

  protected _preferredLayoutSizeRow($container: JQuery, options: HtmlCompPrefSizeOptions): Dimension {
    return this._getChildren($container).reduce((size, c) => {
      let prefSize = this._getPreferredSize(c);
      size.height = Math.max(prefSize.height, size.height);
      size.width += prefSize.width;
      return size;
    }, new Dimension(0, 0));
  }

  protected _preferredLayoutSizeColumn($container: JQuery, options: HtmlCompPrefSizeOptions): Dimension {
    return this._getChildren($container).reduce((size, c) => {
      let prefSize = this._getPreferredSize(c);
      size.width = Math.max(prefSize.width, size.width);
      size.height += prefSize.height;
      return size;
    }, new Dimension(0, 0));
  }
}

export type FlexboxDirection = EnumObject<typeof FlexboxLayout.Direction>;
