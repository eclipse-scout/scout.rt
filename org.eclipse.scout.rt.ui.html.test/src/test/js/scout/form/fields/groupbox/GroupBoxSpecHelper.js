/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/

scout.GroupBoxSpecHelper = {
};

scout.GroupBoxSpecHelper.assertGridData = function (x, y, w, h, gd) {
  expect(gd.x).toEqual(x); // GridData[x]
  expect(gd.y).toEqual(y); // GridData[y]
  expect(gd.w).toEqual(w); // GridData[w]
  expect(gd.h).toEqual(h); // GridData[h]
};
