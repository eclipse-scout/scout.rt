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

// DataObjectInventory doesn't allow to refresh the cache for testing purposes, thus do not add @IgnoreBean here and keep it always registered, not harm there
public class IdAsStringFixtureDataObjectVisitorExtension extends AbstractDataObjectVisitorExtension<IdAsStringFixtureDo> {

  @Override
  public void visit(IdAsStringFixtureDo value, Consumer<Object> chain) {
    FixtureStringId id = FixtureStringId.of(value.getIdAsString());
    chain.accept(id);
  }

  @Override
  public IdAsStringFixtureDo replaceOrVisit(IdAsStringFixtureDo value, UnaryOperator<Object> chain) {
    FixtureStringId id = FixtureStringId.of(value.getIdAsString());
    FixtureStringId replacedId = (FixtureStringId) chain.apply(id);
    if (ObjectUtility.equals(id, replacedId)) {
      return value;
    }

    value.withIdAsString(replacedId.unwrapAsString());
    return value;
  }
}
