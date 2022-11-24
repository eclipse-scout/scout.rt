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
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

@Order(4000) // lower order than BiCompositeFixtureObjectDataObjectVisitorExtension required, otherwise that visitor extension would take care of TriCompositeFixtureObject
// DataObjectInventory doesn't allow to refresh the cache for testing purposes, thus do not add @IgnoreBean here and keep it always registered, no harm there
public class TriCompositeFixtureObjectDataObjectVisitorExtension extends AbstractDataObjectVisitorExtension<TriCompositeFixtureObject> {

  @Override
  public void visit(TriCompositeFixtureObject value, Consumer<Object> chain) {
    chain.accept(value.getId1());
    chain.accept(value.getId2());
    chain.accept(value.getId3());
  }

  @Override
  public TriCompositeFixtureObject replaceOrVisit(TriCompositeFixtureObject value, UnaryOperator<Object> chain) {
    FixtureStringId replaceId1 = (FixtureStringId) chain.apply(value.getId1());
    FixtureStringId replaceId2 = (FixtureStringId) chain.apply(value.getId2());
    FixtureStringId replaceId3 = (FixtureStringId) chain.apply(value.getId3());
    if (ObjectUtility.equals(value.getId1(), replaceId1)
        && ObjectUtility.equals(value.getId2(), replaceId2)
        && ObjectUtility.equals(value.getId3(), replaceId3)) { // okay because FixtureStringId is immutable
      return value; // no change
    }
    return TriCompositeFixtureObject.of(replaceId1, replaceId2, replaceId3);
  }
}
