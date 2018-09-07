package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("TestEntityWithLists")
public class TestEntityWithListsDo extends DoEntity {

  public DoValue<List<TestItemDo>> itemsListAttribute() {
    return doValue("itemsListAttribute");
  }

  public DoValue<List<String>> stringListAttribute() {
    return doValue("stringListAttribute");
  }

  public DoList<TestItemDo> itemsDoListAttribute() {
    return doList("itemsDoListAttribute");
  }

  public DoList<String> stringDoListAttribute() {
    return doList("stringDoListAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithListsDo withItemsListAttribute(List<TestItemDo> itemsListAttribute) {
    itemsListAttribute().set(itemsListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<TestItemDo> getItemsListAttribute() {
    return itemsListAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithListsDo withStringListAttribute(List<String> stringListAttribute) {
    stringListAttribute().set(stringListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<String> getStringListAttribute() {
    return stringListAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithListsDo withItemsDoListAttribute(Collection<? extends TestItemDo> itemsDoListAttribute) {
    itemsDoListAttribute().clear();
    itemsDoListAttribute().get().addAll(itemsDoListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithListsDo withItemsDoListAttribute(TestItemDo... itemsDoListAttribute) {
    return withItemsDoListAttribute(Arrays.asList(itemsDoListAttribute));
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<TestItemDo> getItemsDoListAttribute() {
    return itemsDoListAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithListsDo withStringDoListAttribute(Collection<? extends String> stringDoListAttribute) {
    stringDoListAttribute().clear();
    stringDoListAttribute().get().addAll(stringDoListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithListsDo withStringDoListAttribute(String... stringDoListAttribute) {
    return withStringDoListAttribute(Arrays.asList(stringDoListAttribute));
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<String> getStringDoListAttribute() {
    return stringDoListAttribute().get();
  }
}
