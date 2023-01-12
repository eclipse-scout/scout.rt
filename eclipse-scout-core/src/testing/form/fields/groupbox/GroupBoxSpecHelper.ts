/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
