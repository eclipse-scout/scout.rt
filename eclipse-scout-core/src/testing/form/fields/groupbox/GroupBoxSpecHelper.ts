/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import {GridData} from '../../../../index';

export class GroupBoxSpecHelper {
  static assertGridData(x: number, y: number, w: number, h: number, gd: GridData) {
    expect(gd.x).toEqual(x); // GridData[x]
    expect(gd.y).toEqual(y); // GridData[y]
    expect(gd.w).toEqual(w); // GridData[w]
    expect(gd.h).toEqual(h); // GridData[h]
  }
}
