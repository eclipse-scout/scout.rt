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
import {Point} from '../../src/index';

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
describe('Point', function() {

  it('equals', function() {
    var p1 = new Point(10, 5);
    var p2 = new Point(20, -1);
    var p3 = new Point(20, -1);
    var p4 = new Point(10.2, 5.9);

    expect(p1.equals(p2)).toBe(false);
    expect(p2.equals(p3)).toBe(true);
    expect(p1.equals(p4)).toBe(false);
    expect(p1.equals(p4.floor())).toBe(true);
    expect(p1.equals(p4.ceil())).toBe(false);
  });
});
