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

import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureNamespace;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureNamespace;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_3;
import org.eclipse.scout.rt.platform.Replace;

/**
 * {@link CustomerFixtureDo} from {@link AlfaFixtureNamespace} is replaced in {@link CharlieFixtureNamespace}.
 * <p>
 * If a data object having a {@link TypeVersion} annotation is replaced, an own {@link TypeVersion} annotation should be
 * applied due to different data object structure. Migration handlers from origin namespace will not trigger anymore on
 * this data object, thus replacing data objects having a type version is not recommended (use contributions instead).
 * <p>
 * Type name renames unchanged.
 * <p>
 * Migration handler to switch namespace is not present (assuming that data object were only persisted when subclassed
 * bean was already used).
 * <p>
 * Changes in charlie namespace:
 * <ul>
 * <li>charlieFixture-3: using lowercase first name</li>
 * </ul>
 *
 * @since charlieFixture-2
 */
@TypeVersion(CharlieFixture_3.class)
@Replace
public class CharlieCustomerFixtureDo extends CustomerFixtureDo {

  public DoValue<String> emailAddress() {
    return doValue("emailAddress");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public CharlieCustomerFixtureDo withEmailAddress(String emailAddress) {
    emailAddress().set(emailAddress);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getEmailAddress() {
    return emailAddress().get();
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public CharlieCustomerFixtureDo withFirstName(String firstName) {
    firstName().set(firstName);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public CharlieCustomerFixtureDo withLastName(String lastName) {
    lastName().set(lastName);
    return this;
  }
}
