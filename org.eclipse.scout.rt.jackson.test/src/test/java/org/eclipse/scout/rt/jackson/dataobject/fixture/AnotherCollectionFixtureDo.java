/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Collection;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.AnotherCollectionFixture")
public class AnotherCollectionFixtureDo extends DoEntity {

  public DoCollection<String> anotherDoCollection() {
    return doCollection("anotherDoCollection");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public AnotherCollectionFixtureDo withAnotherDoCollection(Collection<? extends String> anotherDoCollection) {
    anotherDoCollection().updateAll(anotherDoCollection);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public AnotherCollectionFixtureDo withAnotherDoCollection(String... anotherDoCollection) {
    anotherDoCollection().updateAll(anotherDoCollection);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Collection<String> getAnotherDoCollection() {
    return anotherDoCollection().get();
  }
}
