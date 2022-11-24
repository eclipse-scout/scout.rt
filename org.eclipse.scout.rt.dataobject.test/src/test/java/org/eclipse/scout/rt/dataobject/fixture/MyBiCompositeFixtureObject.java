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
package org.eclipse.scout.rt.dataobject.fixture;

public class MyBiCompositeFixtureObject extends BiCompositeFixtureObject {

  protected MyBiCompositeFixtureObject(FixtureStringId id1, FixtureStringId id2) {
    super(id1, id2);
  }

  public static MyBiCompositeFixtureObject of(FixtureStringId id1, FixtureStringId id2) {
    return new MyBiCompositeFixtureObject(id1, id2);
  }
}
