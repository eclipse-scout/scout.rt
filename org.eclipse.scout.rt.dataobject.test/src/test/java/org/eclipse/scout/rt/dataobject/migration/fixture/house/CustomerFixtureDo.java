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
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_3;

/**
 * Changes:
 * <ul>
 * <li>alfaFixture-3: using uppercase first name</li>
 * </ul>
 *
 * @since alfaFixture-1
 */
@TypeName("alfaFixture.CustomerFixture")
@TypeVersion(AlfaFixture_3.class)
public class CustomerFixtureDo extends DoEntity {

  public DoValue<String> firstName() {
    return doValue("firstName");
  }

  public DoValue<String> lastName() {
    return doValue("lastName");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public CustomerFixtureDo withFirstName(String firstName) {
    firstName().set(firstName);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getFirstName() {
    return firstName().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CustomerFixtureDo withLastName(String lastName) {
    lastName().set(lastName);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getLastName() {
    return lastName().get();
  }
}
