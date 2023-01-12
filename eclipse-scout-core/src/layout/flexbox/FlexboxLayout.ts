/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, EnumObject, FlexboxLayoutData, HtmlComponent, HtmlCompPrefSizeOptions, Rectangle, webstorage} from '../../index';
import $ from 'jquery';

export type FlexboxDirection = EnumObject<typeof FlexboxLayout.Direction>;

export class FlexboxLayout extends AbstractLayout {
  childrenLayoutDatas: FlexboxLayoutData[];
  cacheKey: string[];
  protected _getDimensionValue: (dimension: Dimension) => number;
  protected _layoutFromLayoutData: (children: HtmlComponent[], containerSize: Dimension) => void;

  constructor(direction: FlexboxDirection, cacheKey: string[]) {
    super();
    this.childrenLayoutDatas = [];
    this.cacheKey = null;
    this.setCacheKey(cacheKey);
    if (direction === FlexboxLayout.Direction.ROW) {
      this.preferredLayoutSize = this.preferredLayoutSizeRow;
      this._getDimensionValue = this._getWidth;
      this._layoutFromLayoutData = this._layoutFromLayoutDataRow;
    } else {
      this.preferredLayoutSize = this.preferredLayoutSizeColumn;
      this._getDimensionValue = this._getHeight;
      this._layoutFromLayoutData = this._layoutFromLayoutDataColumn;
    }
  }

  static Direction = {
    COLUMN: 0,
    ROW: 1
  } as const;

  setCacheKey(cacheKey: string[]) {
    this.cacheKey = cacheKey;
    if (this.cacheKey && this.cacheKey.length > 0) {
      this.cacheKey.unshift('scout.flexboxLayout');
    }
  }

  protected _readCache(childCount: number): number[] {
    if (!this.cacheKey || this.cacheKey.length === 0 || childCount < 2) {
      return;
    }
    let keySequence = this.cacheKey.slice(),
      cacheValue = webstorage.getItemFromLocalStorage(keySequence[0]),
      i = 1,
      cacheObj;
    keySequence.push('' + childCount);
    if (cacheValue) {
      cacheObj = JSON.parse(cacheValue);
    }
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
    let keySequence = this.cacheKey.slice(),
      cacheValue = webstorage.getItemFromLocalStorage(keySequence[0]),
      i = 1,
      cacheObj,
      cachedSizes;
    keySequence.push('' + childCount);
    if (cacheValue) {
      cacheObj = JSON.parse(cacheValue);
    } else {
      cacheObj = {};
    }
    cachedSizes = cacheObj;
    while (i < keySequence.length - 1) {
      if (!cachedSizes[keySequence[i]]) {
        cachedSizes[keySequence[i]] = {};
      }
      cachedSizes = cachedSizes[keySequence[i]];
      i++;
    }
    cachedSizes[keySequence[i]] = sizes;
    webstorage.setItemToLocalStorage(keySequence[0], JSON.stringify(cacheObj));
  }

  protected _computeCacheKey(childCount: number): string {
    // no need to cache bounds of a single child
    if (!this.cacheKey || childCount < 2) {
      return;
    }
    return this.cacheKey + '-' + childCount;
  }

  // layout functions
  override layout($container: JQuery) {
    let children = this._getChildren($container),
      htmlContainer = HtmlComponent.get($container),
      containerSize = htmlContainer.availableSize({
        exact: true
      }),
      splitterWithDelta;

    containerSize = containerSize.subtract(htmlContainer.insets());

    splitterWithDelta = children.filter(c => (<FlexboxLayoutData>c.layoutData).diff)[0];

    if (splitterWithDelta) {
      this._layoutDelta(children, splitterWithDelta, containerSize);
    } else {
      this._layoutComponents(children, containerSize);
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
    this.childrenLayoutDatas.forEach(ld => {
      ld.sizePx = 0;
      ld.initialPx = 0;
      ld.diff = null;
    });
    this.childrenLayoutDatas = [];
  }

  protected _layoutDelta(children: HtmlComponent[], deltaComp: HtmlComponent, containerSize: Dimension) {
    this.ensureInitialValues(children, containerSize);
    // noinspection JSVoidFunctionReturnValueUsed
    let delta = (<FlexboxLayoutData>deltaComp.layoutData).diff,
      componentsBefore = children.slice(0, children.indexOf(deltaComp)).reverse(),
      componentsAfter = children.slice(children.indexOf(deltaComp) + 1),
      deltaDiffPrev,
      deltaDiffNext;

    // calculate if the delta can be applied to the previous and following columns
    deltaDiffPrev = _distributeDelta(componentsBefore, delta, false);
    deltaDiffNext = -_distributeDelta(componentsAfter, -delta, false);
    // compute the max delta could be applied

    delta = Math.sign(delta) * (Math.min(Math.abs(delta - deltaDiffPrev), Math.abs(delta - deltaDiffNext)));

    if (delta !== 0) {
      // apply the delta to the previous and following columns
      _distributeDelta(componentsBefore, delta, true);
      _distributeDelta(componentsAfter, -delta, true);
    }

    this._layoutFromLayoutDataWithCache(children, containerSize);

    /* private functions */
    function _distributeDelta(components, delta, applyDelta) {
      return components.reduce((diff, c) => {
        if (diff !== 0) {
          diff = c.layoutData.acceptDelta(diff, applyDelta);
        }
        return diff;
      }, delta);
    }
  }

  protected _layoutComponents(children: HtmlComponent[], containerSize: Dimension) {
    let delta = this.ensureInitialValues(children, containerSize);
    if (delta < 0) {
      this._adjust(children, delta, ld => ld.shrink);
    } else if (delta > 0) {
      this._adjust(children, delta, ld => ld.grow);
    }
    this._layoutFromLayoutDataWithCache(children, containerSize);
  }

  protected _adjust(children: HtmlComponent[], delta: number, getWeightFunction: (ld: FlexboxLayoutData) => number) {
    let weightSum,
      deltaFactor,
      layoutDatas = children.map(c => c.layoutData as FlexboxLayoutData).filter(ld => {
        // resizable
        return ld.acceptDelta(Math.sign(delta)) === 0;
      });

    if (layoutDatas.length < 1) {
      return;
    }

    weightSum = layoutDatas.reduce((sum, ld) => {
      return sum + getWeightFunction(ld);
    }, 0);

    // delta factor
    deltaFactor = delta / weightSum;
    delta = layoutDatas.reduce((delta, ld) => {
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

  ensureInitialValues(children: HtmlComponent[], containerSize: Dimension): number {
    let totalPx = this._getDimensionValue(containerSize),
      sumOfAbsolutePx = 0,
      sumOfRelatives = 0,
      colLayoutDatas = children.map(c => {
        return c.layoutData as FlexboxLayoutData;
      }),
      cachedSizes = this._readCache(children.length) || [];

    // setup initial values
    children.forEach((comp, i) => {
      let ld = comp.layoutData as FlexboxLayoutData;

      if (ld.sizePx) {
        sumOfAbsolutePx += ld.sizePx;
      } else if (ld.initial < 0) {
        // use ui height
        ld.initialPx = this._getDimensionValue(this._getPreferredSize(comp));
        sumOfAbsolutePx += ld.initialPx;

      } else if (ld.relative) {
        sumOfRelatives += ld.initial;
      } else {
        ld.initialPx = ld.initial;
        sumOfAbsolutePx += ld.initialPx;
      }
    });

    let relativeFactor = (totalPx - sumOfAbsolutePx) / sumOfRelatives;
    colLayoutDatas.filter(ld => {
      return ld.relative && ld.initial > -1 && !ld.sizePx;
    }).reduce((restWidth, ld) => {
      ld.initialPx = Math.max(30, relativeFactor * ld.initial);
      return restWidth - ld.initialPx;
    }, (totalPx - sumOfAbsolutePx));

    // set px values
    return colLayoutDatas
      .reduce((restWidth, ld, i) => {
        if (!ld.sizePx) {
          if (cachedSizes[i]) {
            ld.sizePx = ld.validate(Math.round(totalPx * cachedSizes[i]));
          } else {
            ld.sizePx = ld.initialPx;
          }
        }
        this.childrenLayoutDatas.push(ld);
        return restWidth - ld.sizePx;
      }, totalPx);
  }

  protected _layoutFromLayoutDataWithCache(children: HtmlComponent[], containerSize: Dimension) {
    this._cacheSizes(children, containerSize);
    this._layoutFromLayoutData(children, containerSize);
  }

  protected _cacheSizes(children: HtmlComponent[], containerSize: Dimension) {
    let totalPx = this._getDimensionValue(containerSize);
    let value = children.map(c => (<FlexboxLayoutData>c.layoutData).sizePx / totalPx);
    this._writeCache(children.length, value);
  }

  // functions differ from row to column mode

  preferredLayoutSizeColumn($container: JQuery, options: HtmlCompPrefSizeOptions): Dimension {
    return this._getChildren($container).reduce((size, c) => {
      let prefSize = this._getPreferredSize(c);
      size.width = Math.max(prefSize.width, size.width);
      size.height += prefSize.height;
      return size;
    }, new Dimension(0, 0));
  }

  preferredLayoutSizeRow($container: JQuery, options: HtmlCompPrefSizeOptions): Dimension {
    return this._getChildren($container).reduce((size, c) => {
      let prefSize = this._getPreferredSize(c);
      size.height = Math.max(prefSize.height, size.height);
      size.width += prefSize.width;
      return size;
    }, new Dimension(0, 0));
  }

  protected _getWidth(dimension: Dimension): number {
    return dimension.width;
  }

  protected _getHeight(dimension: Dimension): number {
    return dimension.height;
  }

  protected _layoutFromLayoutDataRow(children: HtmlComponent[], containerSize: Dimension) {
    children.reduce((x, comp) => {
      let margins = comp.margins();
      let insets = comp.insets();
      let w = (<FlexboxLayoutData>comp.layoutData).sizePx;
      let bounds = new Rectangle(x - insets.left - margins.left, 0, w + insets.left + insets.right, containerSize.height);
      comp.setBounds(bounds);
      return x + w;
    }, 0);
  }

  protected _layoutFromLayoutDataColumn(children: HtmlComponent[], containerSize: Dimension) {
    children.reduce((y, comp) => {
      let margins = comp.margins();
      let insets = comp.insets();
      let h = (<FlexboxLayoutData>comp.layoutData).sizePx;
      let bounds = new Rectangle(0, y - insets.top - margins.top, containerSize.width, h + insets.top + insets.bottom);
      comp.setBounds(bounds);
      return y + h;
    }, 0);
  }
}
