/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Collection;
import java.util.List;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

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

  public DoValue<Collection<TestItemDo>> itemDoCollectionAttribute() {
    return doValue("itemDoCollectionAttribute");
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
  public TestCollectionsDo withItemDoCollectionAttribute(Collection<TestItemDo> itemDoCollectionAttribute) {
    itemDoCollectionAttribute().set(itemDoCollectionAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Collection<TestItemDo> getItemDoCollectionAttribute() {
    return itemDoCollectionAttribute().get();
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
}
