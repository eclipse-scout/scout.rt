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

import org.eclipse.scout.rt.dataobject.DataObjectHelper;
import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.dataobject.migration.AbstractDoValueMigrationHandler;
import org.eclipse.scout.rt.dataobject.migration.DataObjectMigrationContext;
import org.eclipse.scout.rt.dataobject.migration.DoValueMigrationId;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_2;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;

/**
 * Acts on {@link CustomerFixtureDo#gender()} and renames "f" to "female" and "m" to "male".
 */
// Cannot directly operate on CustomerGenderFixtureEnum because for an unknown enum value,
// an instance of CustomerGenderFixtureEnum cannot be created, thus operating on the containing DO (CustomerFixtureDo) instead.
@IgnoreBean
public class CustomerGenderFixtureDoValueMigrationHandler_2 extends AbstractDoValueMigrationHandler<CustomerFixtureDo> {

  public static final DoValueMigrationId ID = DoValueMigrationId.of("1c94c354-6345-4261-93d0-7977806f7711");

  @Override
  public DoValueMigrationId id() {
    return ID;
  }

  @Override
  public Class<? extends ITypeVersion> typeVersionClass() {
    return CharlieFixture_2.class;
  }

  @Override
  public CustomerFixtureDo migrate(DataObjectMigrationContext ctx, CustomerFixtureDo value) {
    // lenient data object mapper returned a string for an unknown enum because the enum itself doesn't exist anymore
    String gender = value.getString(value.gender().getAttributeName());
    if ("m".equals(gender)) {
      return BEANS.get(DataObjectHelper.class).cloneLenient(value).withGender(CustomerGenderFixtureEnum.MALE);
    }
    else if ("f".equals(gender)) {
      return BEANS.get(DataObjectHelper.class).cloneLenient(value).withGender(CustomerGenderFixtureEnum.FEMALE);
    }
    return value; // unchanged
  }
}
