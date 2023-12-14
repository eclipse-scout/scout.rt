/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.migration.fixture.house;

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_2;

/**
 * Changes:
 * <ul>
 * <li>charlieFixture-2: added relation attribute
 * </ul>
 */
@TypeName("charlieFixture.PersonFixture")
@TypeVersion(CharlieFixture_2.class)
public class PersonFixtureDo extends DoEntity {

  public DoValue<String> name() {
    return doValue("name");
  }

  public DoValue<String> relation() {
    return doValue("relation");
  }

  public DoList<PersonFixtureDo> children() {
    return doList("children");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public PersonFixtureDo withName(String name) {
    name().set(name);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getName() {
    return name().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public PersonFixtureDo withRelation(String relation) {
    relation().set(relation);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getRelation() {
    return relation().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public PersonFixtureDo withChildren(Collection<? extends PersonFixtureDo> children) {
    children().updateAll(children);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public PersonFixtureDo withChildren(PersonFixtureDo... children) {
    children().updateAll(children);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<PersonFixtureDo> getChildren() {
    return children().get();
  }
}
