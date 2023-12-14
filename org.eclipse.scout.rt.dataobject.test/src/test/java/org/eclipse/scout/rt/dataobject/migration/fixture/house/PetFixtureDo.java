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

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_3;

/**
 * Introduced in namespace bravo with type version bravo-1, attribute name. Changes:
 * <ul>
 * <li>bravo-2: transform name to first letter uppercase</li>
 * <li>alfa-3: changed to alfa namespace (type name and type version) and added new attribute familyFriendly default
 * true</li>
 * </ul>
 */
@TypeName("alfaFixture.PetFixture")
@TypeVersion(AlfaFixture_3.class)
public class PetFixtureDo extends DoEntity {

  public DoValue<String> name() {
    return doValue("name");
  }

  public DoValue<Boolean> familyFriendly() {
    return doValue("familyFriendly");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public PetFixtureDo withName(String name) {
    name().set(name);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getName() {
    return name().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public PetFixtureDo withFamilyFriendly(Boolean familyFriendly) {
    familyFriendly().set(familyFriendly);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getFamilyFriendly() {
    return familyFriendly().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isFamilyFriendly() {
    return nvl(getFamilyFriendly());
  }
}
