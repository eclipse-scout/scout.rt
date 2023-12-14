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
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("DataObjectWithCompositeFixture")
public class DataObjectWithCompositeFixtureDo extends DoEntity {

  public DoValue<FixtureStringId> id() {
    return doValue("id");
  }

  public DoValue<BiCompositeFixtureObject> composite() {
    return doValue("composite");
  }

  public DoList<BiCompositeFixtureObject> compositeList() {
    return doList("compositeList");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public DataObjectWithCompositeFixtureDo withId(FixtureStringId id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureStringId getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DataObjectWithCompositeFixtureDo withComposite(BiCompositeFixtureObject composite) {
    composite().set(composite);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BiCompositeFixtureObject getComposite() {
    return composite().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DataObjectWithCompositeFixtureDo withCompositeList(Collection<? extends BiCompositeFixtureObject> compositeList) {
    compositeList().updateAll(compositeList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public DataObjectWithCompositeFixtureDo withCompositeList(BiCompositeFixtureObject... compositeList) {
    compositeList().updateAll(compositeList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<BiCompositeFixtureObject> getCompositeList() {
    return compositeList().get();
  }
}
