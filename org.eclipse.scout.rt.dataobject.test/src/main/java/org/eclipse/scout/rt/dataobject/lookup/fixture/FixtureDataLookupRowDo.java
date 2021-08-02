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
package org.eclipse.scout.rt.dataobject.lookup.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.lookup.AbstractLookupRowDo;

@TypeName("start.FixtureDataLookupRow")
public class FixtureDataLookupRowDo extends AbstractLookupRowDo<FixtureDataLookupRowDo, Long> {

  @Override
  public DoValue<Long> id() {
    return createIdAttribute(this);
  }

  public DoValue<String> additionalData() {
    return doValue("additionalData");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRowDo withId(Long id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRowDo withAdditionalData(String additionalData) {
    additionalData().set(additionalData);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getAdditionalData() {
    return additionalData().get();
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRowDo withText(String text) {
    text().set(text);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRowDo withEnabled(Boolean enabled) {
    enabled().set(enabled);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRowDo withActive(Boolean active) {
    active().set(active);
    return this;
  }
}
