/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.testing.signature;

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.EnumApiSignature")
public class EnumApiSignatureDo extends DoEntity {

  public DoValue<String> enumName() {
    return doValue("enumName");
  }

  public DoList<String> values() {
    return doList("values");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public EnumApiSignatureDo withEnumName(String enumName) {
    enumName().set(enumName);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getEnumName() {
    return enumName().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public EnumApiSignatureDo withValues(Collection<? extends String> values) {
    values().updateAll(values);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public EnumApiSignatureDo withValues(String... values) {
    values().updateAll(values);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<String> getValues() {
    return values().get();
  }
}
