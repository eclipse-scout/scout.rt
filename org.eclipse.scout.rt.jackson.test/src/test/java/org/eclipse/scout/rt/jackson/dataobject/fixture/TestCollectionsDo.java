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
import java.util.List;
import java.util.Set;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

/**
 * Test {@link DoEntity} for various types of collections.
 */
@TypeName("TestCollections")
public class TestCollectionsDo extends DoEntity {

  // single item attributes

  public DoValue<TestItemDo> itemDoAttribute() {
    return doValue("itemDoAttribute");
  }

  public DoValue<TestItemPojo> itemPojoAttribute() {
    return doValue("itemPojoAttribute");
  }

  // list of items attributes

  public DoValue<List<TestItemDo>> itemListAttribute() {
    return doValue("itemListAttribute");
  }

  public DoValue<List<TestItemPojo>> itemPojoListAttribute() {
    return doValue("itemPojoListAttribute");
  }

  // collection of items attributes

  public DoValue<Collection<TestItemDo>> itemCollectionAttribute() {
    return doValue("itemCollectionAttribute");
  }

  public DoValue<Collection<TestItemPojo>> itemPojoCollectionAttribute() {
    return doValue("itemPojoCollectionAttribute");
  }

  // DoList of items attributes

  public DoList<TestItemDo> itemDoListAttribute() {
    return doList("itemDoListAttribute");
  }

  public DoList<TestItemPojo> itemPojoDoListAttribute() {
    return doList("itemPojoDoListAttribute");
  }

  public DoList<TestItemPojo2> itemPojo2DoListAttribute() {
    return doList("itemPojo2DoListAttribute");
  }

  // DoSet of items attributes

  public DoSet<TestItemDo> itemDoSetAttribute() {
    return doSet("itemDoSetAttribute");
  }

  public DoSet<TestItemPojo> itemPojoDoSetAttribute() {
    return doSet("itemPojoDoSetAttribute");
  }

  public DoSet<TestItemPojo2> itemPojo2DoSetAttribute() {
    return doSet("itemPojo2DoSetAttribute");
  }

  // DoCollection of items attributes

  public DoCollection<TestItemDo> itemDoCollectionAttribute() {
    return doCollection("itemDoCollectionAttribute");
  }

  public DoCollection<TestItemPojo> itemPojoDoCollectionAttribute() {
    return doCollection("itemPojoDoCollectionAttribute");
  }

  public DoCollection<TestItemPojo2> itemPojo2DoCollectionAttribute() {
    return doCollection("itemPojo2DoCollectionAttribute");
  }

  // DoList and DoCollection of interface/abstract data object

  public DoList<AbstractTestAddressDo> itemDoListAbstractAttribute() {
    return doList("itemDoListAbstractAttribute");
  }

  public DoList<ITestAddressDo> itemDoListInterfaceAttribute() {
    return doList("itemDoListInterfaceAttribute");
  }

  public DoValue<List<AbstractTestAddressDo>> itemListAbstractAttribute() {
    return doValue("itemListAbstractAttribute");
  }

  public DoValue<List<ITestAddressDo>> itemListInterfaceAttribute() {
    return doValue("itemListInterfaceAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemDoAttribute(TestItemDo itemDoAttribute) {
    itemDoAttribute().set(itemDoAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestItemDo getItemDoAttribute() {
    return itemDoAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemPojoAttribute(TestItemPojo itemPojoAttribute) {
    itemPojoAttribute().set(itemPojoAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestItemPojo getItemPojoAttribute() {
    return itemPojoAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemListAttribute(List<TestItemDo> itemListAttribute) {
    itemListAttribute().set(itemListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<TestItemDo> getItemListAttribute() {
    return itemListAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemPojoListAttribute(List<TestItemPojo> itemPojoListAttribute) {
    itemPojoListAttribute().set(itemPojoListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<TestItemPojo> getItemPojoListAttribute() {
    return itemPojoListAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemCollectionAttribute(Collection<TestItemDo> itemCollectionAttribute) {
    itemCollectionAttribute().set(itemCollectionAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Collection<TestItemDo> getItemCollectionAttribute() {
    return itemCollectionAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemPojoCollectionAttribute(Collection<TestItemPojo> itemPojoCollectionAttribute) {
    itemPojoCollectionAttribute().set(itemPojoCollectionAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Collection<TestItemPojo> getItemPojoCollectionAttribute() {
    return itemPojoCollectionAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemDoListAttribute(Collection<? extends TestItemDo> itemDoListAttribute) {
    itemDoListAttribute().updateAll(itemDoListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemDoListAttribute(TestItemDo... itemDoListAttribute) {
    itemDoListAttribute().updateAll(itemDoListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<TestItemDo> getItemDoListAttribute() {
    return itemDoListAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemPojoDoListAttribute(Collection<? extends TestItemPojo> itemPojoDoListAttribute) {
    itemPojoDoListAttribute().updateAll(itemPojoDoListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemPojoDoListAttribute(TestItemPojo... itemPojoDoListAttribute) {
    itemPojoDoListAttribute().updateAll(itemPojoDoListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<TestItemPojo> getItemPojoDoListAttribute() {
    return itemPojoDoListAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemPojo2DoListAttribute(Collection<? extends TestItemPojo2> itemPojo2DoListAttribute) {
    itemPojo2DoListAttribute().updateAll(itemPojo2DoListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemPojo2DoListAttribute(TestItemPojo2... itemPojo2DoListAttribute) {
    itemPojo2DoListAttribute().updateAll(itemPojo2DoListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<TestItemPojo2> getItemPojo2DoListAttribute() {
    return itemPojo2DoListAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemDoSetAttribute(Collection<? extends TestItemDo> itemDoSetAttribute) {
    itemDoSetAttribute().updateAll(itemDoSetAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemDoSetAttribute(TestItemDo... itemDoSetAttribute) {
    itemDoSetAttribute().updateAll(itemDoSetAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<TestItemDo> getItemDoSetAttribute() {
    return itemDoSetAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemPojoDoSetAttribute(Collection<? extends TestItemPojo> itemPojoDoSetAttribute) {
    itemPojoDoSetAttribute().updateAll(itemPojoDoSetAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemPojoDoSetAttribute(TestItemPojo... itemPojoDoSetAttribute) {
    itemPojoDoSetAttribute().updateAll(itemPojoDoSetAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<TestItemPojo> getItemPojoDoSetAttribute() {
    return itemPojoDoSetAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemPojo2DoSetAttribute(Collection<? extends TestItemPojo2> itemPojo2DoSetAttribute) {
    itemPojo2DoSetAttribute().updateAll(itemPojo2DoSetAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemPojo2DoSetAttribute(TestItemPojo2... itemPojo2DoSetAttribute) {
    itemPojo2DoSetAttribute().updateAll(itemPojo2DoSetAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<TestItemPojo2> getItemPojo2DoSetAttribute() {
    return itemPojo2DoSetAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemDoCollectionAttribute(Collection<? extends TestItemDo> itemDoCollectionAttribute) {
    itemDoCollectionAttribute().updateAll(itemDoCollectionAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemDoCollectionAttribute(TestItemDo... itemDoCollectionAttribute) {
    itemDoCollectionAttribute().updateAll(itemDoCollectionAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Collection<TestItemDo> getItemDoCollectionAttribute() {
    return itemDoCollectionAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemPojoDoCollectionAttribute(Collection<? extends TestItemPojo> itemPojoDoCollectionAttribute) {
    itemPojoDoCollectionAttribute().updateAll(itemPojoDoCollectionAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemPojoDoCollectionAttribute(TestItemPojo... itemPojoDoCollectionAttribute) {
    itemPojoDoCollectionAttribute().updateAll(itemPojoDoCollectionAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Collection<TestItemPojo> getItemPojoDoCollectionAttribute() {
    return itemPojoDoCollectionAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemPojo2DoCollectionAttribute(Collection<? extends TestItemPojo2> itemPojo2DoCollectionAttribute) {
    itemPojo2DoCollectionAttribute().updateAll(itemPojo2DoCollectionAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemPojo2DoCollectionAttribute(TestItemPojo2... itemPojo2DoCollectionAttribute) {
    itemPojo2DoCollectionAttribute().updateAll(itemPojo2DoCollectionAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Collection<TestItemPojo2> getItemPojo2DoCollectionAttribute() {
    return itemPojo2DoCollectionAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemDoListAbstractAttribute(Collection<? extends AbstractTestAddressDo> itemDoListAbstractAttribute) {
    itemDoListAbstractAttribute().updateAll(itemDoListAbstractAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemDoListAbstractAttribute(AbstractTestAddressDo... itemDoListAbstractAttribute) {
    itemDoListAbstractAttribute().updateAll(itemDoListAbstractAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<AbstractTestAddressDo> getItemDoListAbstractAttribute() {
    return itemDoListAbstractAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemDoListInterfaceAttribute(Collection<? extends ITestAddressDo> itemDoListInterfaceAttribute) {
    itemDoListInterfaceAttribute().updateAll(itemDoListInterfaceAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemDoListInterfaceAttribute(ITestAddressDo... itemDoListInterfaceAttribute) {
    itemDoListInterfaceAttribute().updateAll(itemDoListInterfaceAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<ITestAddressDo> getItemDoListInterfaceAttribute() {
    return itemDoListInterfaceAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemListAbstractAttribute(List<AbstractTestAddressDo> itemListAbstractAttribute) {
    itemListAbstractAttribute().set(itemListAbstractAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<AbstractTestAddressDo> getItemListAbstractAttribute() {
    return itemListAbstractAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCollectionsDo withItemListInterfaceAttribute(List<ITestAddressDo> itemListInterfaceAttribute) {
    itemListInterfaceAttribute().set(itemListInterfaceAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<ITestAddressDo> getItemListInterfaceAttribute() {
    return itemListInterfaceAttribute().get();
  }
}
