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

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.AttributeDataObjectSignature")
public class AttributeDataObjectSignatureDo extends DoEntity {

  public DoValue<String> name() {
    return doValue("name");
  }

  public DoValue<String> valueType() {
    return doValue("valueType");
  }

  public DoValue<Boolean> list() {
    return doValue("list");
  }

  public DoValue<String> formatPattern() {
    return doValue("formatPattern");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public AttributeDataObjectSignatureDo withName(String name) {
    name().set(name);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getName() {
    return name().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public AttributeDataObjectSignatureDo withValueType(String valueType) {
    valueType().set(valueType);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getValueType() {
    return valueType().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public AttributeDataObjectSignatureDo withList(Boolean list) {
    list().set(list);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getList() {
    return list().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isList() {
    return nvl(getList());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public AttributeDataObjectSignatureDo withFormatPattern(String formatPattern) {
    formatPattern().set(formatPattern);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getFormatPattern() {
    return formatPattern().get();
  }
}
