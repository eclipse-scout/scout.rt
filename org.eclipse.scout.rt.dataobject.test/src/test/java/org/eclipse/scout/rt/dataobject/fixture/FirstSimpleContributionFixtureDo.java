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
package org.eclipse.scout.rt.dataobject.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.ContributesTo;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntityContribution;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.FirstSimpleContributionFixture")
@ContributesTo(SimpleFixtureDo.class)
public final class FirstSimpleContributionFixtureDo extends DoEntity implements IDoEntityContribution {

  public DoValue<String> firstValue() {
    return doValue("firstValue");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public FirstSimpleContributionFixtureDo withFirstValue(String firstValue) {
    firstValue().set(firstValue);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getFirstValue() {
    return firstValue().get();
  }
}
