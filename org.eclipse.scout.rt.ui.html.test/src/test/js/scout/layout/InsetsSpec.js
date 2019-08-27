/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

describe("Insets", function() {

  it("equals", function() {
    var i1 = new scout.Insets(0, 0, 10, 5);
    var i2 = new scout.Insets(0, 0, 20, -1);
    var i3 = new scout.Insets(0, 0, 20, -1);
    var i4 = new scout.Insets(0.5, 0.1, 10.2, 5.9);
    var i5 = new scout.Insets(14, 15, 10, 5);
    var i6 = new scout.Insets(14, 15, 20, -1);

    expect(i1.equals(i2)).toBe(false);
    expect(i2.equals(i3)).toBe(true);
    expect(i1.equals(i4)).toBe(false);
    expect(i1.equals(i4.floor())).toBe(true);
    expect(i1.equals(i4.ceil())).toBe(false);
    expect(i1.equals(i5)).toBe(false);
    expect(i2.equals(i6)).toBe(false);
    expect(i5.equals(i6)).toBe(false);
  });
});
