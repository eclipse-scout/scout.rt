/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
