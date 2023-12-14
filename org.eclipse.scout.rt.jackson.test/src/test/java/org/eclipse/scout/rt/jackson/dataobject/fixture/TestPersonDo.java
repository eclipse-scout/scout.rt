/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestPerson")
public class TestPersonDo extends DoEntity {

  public DoValue<Date> birthday() {
    return doValue("birthday");
  }

  public DoValue<AbstractTestAddressDo> defaultAddress() {
    return doValue("defaultAddress");
  }

  public DoList<AbstractTestAddressDo> addresses() {
    return doList("addresses");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestPersonDo withBirthday(Date birthday) {
    birthday().set(birthday);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Date getBirthday() {
    return birthday().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestPersonDo withDefaultAddress(AbstractTestAddressDo defaultAddress) {
    defaultAddress().set(defaultAddress);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public AbstractTestAddressDo getDefaultAddress() {
    return defaultAddress().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestPersonDo withAddresses(Collection<? extends AbstractTestAddressDo> addresses) {
    addresses().updateAll(addresses);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestPersonDo withAddresses(AbstractTestAddressDo... addresses) {
    addresses().updateAll(addresses);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<AbstractTestAddressDo> getAddresses() {
    return addresses().get();
  }
}
