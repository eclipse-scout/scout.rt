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

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.eclipse.scout.rt.dataobject.AbstractDataObjectVisitorExtension;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

// DataObjectInventory doesn't allow to refresh the cache for testing purposes, thus do not add @IgnoreBean here and keep it always registered, no harm there
public class BiCompositeFixtureObjectDataObjectVisitorExtension extends AbstractDataObjectVisitorExtension<BiCompositeFixtureObject> {

  @Override
  public void visit(BiCompositeFixtureObject value, Consumer<Object> chain) {
    chain.accept(value.getId1());
    chain.accept(value.getId2());
  }

  @Override
  public BiCompositeFixtureObject replaceOrVisit(BiCompositeFixtureObject value, UnaryOperator<Object> chain) {
    FixtureStringId replaceId1 = (FixtureStringId) chain.apply(value.getId1());
    FixtureStringId replaceId2 = (FixtureStringId) chain.apply(value.getId2());
    if (ObjectUtility.equals(value.getId1(), replaceId1)
        && ObjectUtility.equals(value.getId2(), replaceId2)) { // okay because FixtureStringId is immutable
      return value; // no change
    }
    return BiCompositeFixtureObject.of(replaceId1, replaceId2);
  }
}
