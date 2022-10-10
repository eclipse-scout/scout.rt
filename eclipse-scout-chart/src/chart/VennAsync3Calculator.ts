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

// place venn 3 by simulation
// find angle and distance (to a) and radius where "error" is minimal
export default class VennAsync3Calculator {

  constructor(helper, venn1, venn2, venn3, u, v, w, uv, uw, vw, uvw, d12, d13, d23) {
    // if circles are empty, they are drawn as small circle, so: adjust u v w to find better errors
    if (u === 0 && uv === 0 && uw === 0 && uvw === 0) {
      u = 1;
    }
    if (v === 0 && uv === 0 && vw === 0 && uvw === 0) {
      v = 1;
    }
    if (w === 0 && uw === 0 && vw === 0 && uvw === 0) {
      w = 1;
    }

    this.helper = helper;
    this.venn1 = venn1;
    this.venn2 = venn2;
    this.venn3 = venn3;
    this.u = u;
    this.v = v;
    this.w = w;
    this.uv = uv;
    this.uw = uw;
    this.vw = vw;
    this.uvw = uvw;

    // step and ranges for loops
    this.maxD = this.venn1.r + 2 * this.venn2.r + 2 * this.venn1.r + this.helper.distR;
    this.dStep = this.maxD / 30;
    this.rStep = venn3.r / 4;
    this.alphaStep = Math.PI / 30;

    // best vars (initialize with 0 so the optimizer knows that they are numbers)
    this.alphaBest = 0;
    this.dBest = 0;
    this.rBest = 0;
    this.errorBest = 0;

    this.callback = null;
    this.cancelled = false;
  }

  start(callback) {
    this.callback = callback;
    setTimeout(this._next.bind(this, 0));
  }

  cancel() {
    this.cancelled = true;
  }

  _end() {
    // set  x and y and r of  venn3
    this.venn3.x = this.venn1.x + this.dBest * Math.cos(this.alphaBest);
    this.venn3.y = this.venn1.y - this.dBest * Math.sin(this.alphaBest);
    this.venn3.r = this.rBest;

    this.callback();
  }

  _next(alpha) {
    if (!this.cancelled) {
      // iterate
      this._iteration(alpha);
    }
    if (this.cancelled) {
      return; // stop loop if interrupted
    }

    alpha += this.alphaStep;
    if (alpha < Math.PI) {
      // schedule next loop iteration
      setTimeout(this._next.bind(this, alpha));
    } else {
      // end loop
      this._end();
    }
  }

  _iteration(alpha) {
    // optimize speed: no var lookup (should help the optimizer in general, and IE in particular)
    let maxD = this.maxD,
      dStep = this.dStep,
      minR = this.helper.minR,
      rStep = this.rStep,
      total = this.helper.total,
      x1 = this.venn1.x,
      y1 = this.venn1.y,
      r1 = this.venn1.r,
      x2 = this.venn2.x,
      y2 = this.venn2.y,
      r2 = this.venn2.r,
      r3 = this.venn3.r,
      u = this.u,
      v = this.v,
      w = this.w,
      uv = this.uv,
      uw = this.uw,
      vw = this.vw,
      uvw = this.uvw,
      alphaBest = this.alphaBest,
      dBest = this.dBest,
      rBest = this.rBest,
      errorBest = this.errorBest;

    for (let d = 0; d < maxD; d += dStep) {
      // calc x, y
      let x = x1 + d * Math.cos(alpha);
      let y = y1 - d * Math.sin(alpha);

      for (let r = Math.max(minR, r3 * 0.75); r <= r3 * 1.25; r += rStep) {

        // find areas with monte carlo, do not laugh! i tried even this:
        // http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.507.1195&rep=rep1&type=pdf

        let minX = Math.min(x1 - r1, x2 - r2, x - r);
        let maxX = Math.max(x1 + r1, x2 + r2, x + r);
        let minY = Math.min(y1 - r1, y2 - r2, y - r);
        let maxY = Math.max(y1 + r1, y2 + r2, y + r);
        let stepX = (maxX - minX) / 100;
        let stepY = (maxY - minY) / 100;

        // areas of venn
        let a1 = 0,
          a2 = 0,
          a3 = 0,
          a12 = 0,
          a13 = 0,
          a23 = 0,
          a123 = 0;

        for (let testX = minX; testX < maxX; testX += stepX) {
          for (let testY = minY; testY < maxY; testY += stepY) {
            // optimize speed for ie: no function call
            let t1 = ((testX - x1) * (testX - x1) + (testY - y1) * (testY - y1)) < (r1 * r1);
            let t2 = ((testX - x2) * (testX - x2) + (testY - y2) * (testY - y2)) < (r2 * r2);
            let t3 = ((testX - x) * (testX - x) + (testY - y) * (testY - y)) < (r * r);

            // check if inside
            if (t1 && t2 && t3) {
              a123++;
            } else if (t1 && t2 && !t3) {
              a12++;
            } else if (t1 && !t2 && !t3) {
              a1++;
            } else if (!t1 && t2 && !t3) {
              a2++;
            } else if (t1 && !t2 && t3) {
              a13++;
            } else if (!t1 && t2 && t3) {
              a23++;
            } else if (!t1 && !t2 && t3) {
              a3++;
            }
          }
        }

        let aTotal = a1 + a2 + a3 + a12 + a13 + a23 + a123;

        // calc error
        let error = d / maxD;
        error += this._error(uvw, total, a123, aTotal);
        error += this._error(uv, total, a12, aTotal);
        error += this._error(uw, total, a13, aTotal);
        error += this._error(vw, total, a23, aTotal);
        error += this._error(u, total, a1, aTotal);
        error += this._error(v, total, a2, aTotal);
        error += this._error(w, total, a3, aTotal);

        // better than before?
        if (alpha === 0 || error < errorBest) {
          alphaBest = alpha;
          dBest = d;
          rBest = r;
          errorBest = error;
        }
      }
    }

    this.alphaBest = alphaBest;
    this.dBest = dBest;
    this.rBest = rBest;
    this.errorBest = errorBest;
  }

  _error(u, total, a, aTotal) {
    // be brutal if basic error
    if ((u === 0 && a !== 0) || (u !== 0 && a === 0)) {
      return 1000;
    }
    return Math.abs(u / total - a / aTotal) * 100;
  }
}
