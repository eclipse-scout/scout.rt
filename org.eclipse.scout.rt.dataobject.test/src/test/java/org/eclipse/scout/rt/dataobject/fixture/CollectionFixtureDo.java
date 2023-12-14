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

import java.util.Collection;
import java.util.Set;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.CollectionFixture")
public class CollectionFixtureDo extends DoEntity {

  public DoSet<SimpleFixtureDo> simpleDoSet() {
    return doSet("simpleDoSet");
  }

  public DoCollection<SimpleFixtureDo> simpleDoCollection() {
    return doCollection("simpleDoCollection");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withSimpleDoSet(Collection<? extends SimpleFixtureDo> simpleDoSet) {
    simpleDoSet().updateAll(simpleDoSet);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withSimpleDoSet(SimpleFixtureDo... simpleDoSet) {
    simpleDoSet().updateAll(simpleDoSet);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<SimpleFixtureDo> getSimpleDoSet() {
    return simpleDoSet().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withSimpleDoCollection(Collection<? extends SimpleFixtureDo> simpleDoCollection) {
    simpleDoCollection().updateAll(simpleDoCollection);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CollectionFixtureDo withSimpleDoCollection(SimpleFixtureDo... simpleDoCollection) {
    simpleDoCollection().updateAll(simpleDoCollection);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Collection<SimpleFixtureDo> getSimpleDoCollection() {
    return simpleDoCollection().get();
  }
}
