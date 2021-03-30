/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.migration.fixture.house;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_2;

/**
 * No changes.
 *
 * @since charlieFixture-2 but introduced and persisted without a type version first.
 */
@TypeName("charlieFixture.PostalAddressFixture")
@TypeVersion(CharlieFixture_2.class)
public class PostalAddressFixtureDo extends DoEntity {

  public DoValue<String> street() {
    return doValue("street");
  }

  public DoValue<String> zipCode() {
    return doValue("zipCode");
  }

  public DoValue<String> city() {
    return doValue("city");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public PostalAddressFixtureDo withStreet(String street) {
    street().set(street);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getStreet() {
    return street().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public PostalAddressFixtureDo withZipCode(String zipCode) {
    zipCode().set(zipCode);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getZipCode() {
    return zipCode().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public PostalAddressFixtureDo withCity(String city) {
    city().set(city);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getCity() {
    return city().get();
  }
}
