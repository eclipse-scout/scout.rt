/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

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
  public TestPersonDo withAddresses(Collection<AbstractTestAddressDo> addresses) {
    addresses().clear();
    addresses().get().addAll(addresses);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestPersonDo withAddresses(AbstractTestAddressDo... addresses) {
    return withAddresses(Arrays.asList(addresses));
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<AbstractTestAddressDo> getAddresses() {
    return addresses().get();
  }
}
