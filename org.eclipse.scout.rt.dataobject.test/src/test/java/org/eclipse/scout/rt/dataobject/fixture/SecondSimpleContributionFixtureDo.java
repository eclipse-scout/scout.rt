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

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.ContributesTo;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntityContribution;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.SecondSimpleContributionFixture")
@ContributesTo(SimpleFixtureDo.class)
public final class SecondSimpleContributionFixtureDo extends DoEntity implements IDoEntityContribution {

  public DoValue<String> secondValue() {
    return doValue("secondValue");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public SecondSimpleContributionFixtureDo withSecondValue(String secondValue) {
    secondValue().set(secondValue);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getSecondValue() {
    return secondValue().get();
  }
}
