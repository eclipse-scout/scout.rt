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
import {VennCircle} from '../index';

export class VennCircleHelper {
  distR: number;
  maxR: number;
  minR: number;
  total: number;

  constructor(distR: number, maxR: number, minR: number, total: number) {
    this.distR = distR;
    this.maxR = maxR;
    this.minR = minR;
    this.total = total;
  }

  findBalance2(venn1: VennCircle, venn2: VennCircle) {
    // find center
    let midX = (venn1.r * venn1.x + venn2.r * venn2.x) / (venn1.r + venn2.r);
    let midY = (venn1.r * venn1.y + venn2.r * venn2.y) / (venn1.r + venn2.r);

    // move to center
    venn1.x = venn1.x - midX;
    venn2.x = venn2.x - midX;

    venn1.y = venn1.y - midY;
    venn2.y = venn2.y - midY;
  }

  findBalance3(venn1: VennCircle, venn2: VennCircle, venn3: VennCircle) {
    // find center
    let midX = (venn1.r * venn1.x + venn2.r * venn2.x + venn3.r * venn3.x) / (venn1.r + venn2.r + venn3.r);
    let midY = (venn1.r * venn1.y + venn2.r * venn2.y + venn3.r * venn3.y) / (venn1.r + venn2.r + venn3.r);

    // move to center
    venn1.x = venn1.x - midX;
    venn2.x = venn2.x - midX;
    venn3.x = venn3.x - midX;

    venn1.y = venn1.y - midY;
    venn2.y = venn2.y - midY;
    venn3.y = venn3.y - midY;
  }

  calcR(count: number, factorMax: number): number {
    if (count === -1) {
      return this.maxR * factorMax;
    }
    return Math.max(this.minR, Math.sqrt(count / this.total) * this.maxR * factorMax);
  }

  calcD(venn1: VennCircle, venn2: VennCircle, u: number, v: number, uv: number, changeR: boolean): number {
    let ret: number;
    if (uv === 0) {
      // separated circles
      ret = venn1.r + venn2.r + this.distR * 2;

    } else if (u === 0 && v === 0) {
      // same circle
      ret = 0;

    } else if (u === 0) {
      // a part of b
      ret = Math.max(0, venn2.r - venn1.r - this.distR);

      // check if inner circle is to large
      if (ret < 1.5 * this.distR && changeR) {
        ret = 1.5 * this.distR;
        venn1.r = venn2.r - ret;
      }

    } else if (v === 0) {
      // b part of a
      ret = Math.max(0, venn1.r - venn2.r - this.distR);

      // check if inner circle is to large
      if (ret < 1.5 * this.distR && changeR) {
        ret = 1.5 * this.distR;
        venn2.r = venn1.r - ret;
      }

    } else {
      //  this is pure magic ;) please do not touch!
      let ri = Math.min(venn1.r, venn2.r);
      let ra = Math.max(venn1.r, venn2.r);
      let q = Math.min(u, v) / (Math.min(u, v) + uv);

      // unfortunately, there is no closed solution for cricles, so handle as squares
      ret = 2 * q * ri - ri + ra;

      // check and fix
      ret = Math.max(3 * this.distR, ret);
      ret = Math.min(ri + ra - 3 * this.distR, ret);
      ret = Math.max(ra - ri + 3 * this.distR, ret);
    }

    return ret;
  }
}
