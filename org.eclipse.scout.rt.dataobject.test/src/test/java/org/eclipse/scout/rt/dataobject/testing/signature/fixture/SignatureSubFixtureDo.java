/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.testing.signature.fixture;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.dataobject.fixture.DataObjectFixtureTypeVersions.DataObjectFixture_1_0_0_034;

@TypeName("dataObjectFixture.SignatureSubFixture")
@TypeVersion(DataObjectFixture_1_0_0_034.class)
public class SignatureSubFixtureDo extends DoEntity {

  public DoValue<String> text() {
    return doValue("text");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public SignatureSubFixtureDo withText(String text) {
    text().set(text);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getText() {
    return text().get();
  }
}
