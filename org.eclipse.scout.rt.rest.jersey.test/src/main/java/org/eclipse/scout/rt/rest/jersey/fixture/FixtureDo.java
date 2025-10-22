/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.fixture;

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.ScoutTypeVersions.Scout_26_1_001;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.dataobject.fixture.FixtureDateId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;

@TypeName("scout.Fixture")
@TypeVersion(Scout_26_1_001.class)
public class FixtureDo extends DoEntity {

  public DoValue<FixtureStringId> id() {
    return doValue("id");
  }

  public DoValue<FixtureUuId> uuid() {
    return doValue("uuid");
  }

  public DoList<FixtureDateId> dates() {
    return doList("dates");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDo withId(FixtureStringId id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureStringId getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDo withUuid(FixtureUuId uuid) {
    uuid().set(uuid);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureUuId getUuid() {
    return uuid().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDo withDates(Collection<? extends FixtureDateId> dates) {
    dates().updateAll(dates);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDo withDates(FixtureDateId... dates) {
    dates().updateAll(dates);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<FixtureDateId> getDates() {
    return dates().get();
  }
}
