/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ConfigPropertyTest {

  @Test
  public void testListProperty() {
    Assert.assertEquals(Arrays.asList(1234, -44, 40, 4589, 5, 4, 574, 5, 4, 98, 2, 23), new TestIntegerListProperty().getValue("my-namespace"));
    Assert.assertEquals(Arrays.asList(22, 33), new TestIntegerListProperty().getValue("not-existing"));
    Assert.assertEquals(Arrays.asList(11111, 22222), new TestIntegerListProperty().getValue());
  }

  @Test
  public void testListener() {
    final List<ConfigPropertyChangeEvent> b = new ArrayList<>();
    TestProperty testProperty = new TestProperty();
    IConfigChangedListener listener = event -> b.add(event);
    testProperty.addListener(listener);
    Assert.assertEquals("default", testProperty.getValue());
    testProperty.invalidate();
    Assert.assertEquals("default", testProperty.getValue("ns"));
    testProperty.setValue("other", "ns");
    testProperty.setValue("other2");
    Assert.assertEquals("other2", testProperty.getValue());
    Assert.assertEquals("other", testProperty.getValue("ns"));

    Assert.assertEquals(5, b.size());
    Assert.assertEquals("ConfigPropertyChangeEvent [property=testString, newValue=default, type=3]", b.get(0).toString());
    Assert.assertEquals("ConfigPropertyChangeEvent [property=testString, type=2]", b.get(1).toString());
    Assert.assertEquals("ConfigPropertyChangeEvent [property=testString, namespace=ns, newValue=default, type=3]", b.get(2).toString());
    Assert.assertEquals("ConfigPropertyChangeEvent [property=testString, namespace=ns, oldValue=default, newValue=other, type=1]", b.get(3).toString());
    Assert.assertEquals("ConfigPropertyChangeEvent [property=testString, newValue=other2, type=1]", b.get(4).toString());
    testProperty.removeListener(listener);
    testProperty.invalidate();
    Assert.assertEquals(5, b.size()); // ensure listener is no longer fired.
  }

  private static final class TestProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "testString";
    }

    @Override
    public String description() {
      return null;
    }

    @Override
    public String getDefaultValue() {
      return "default";
    }
  }

  /**
   * Property must be public because there are test-properties with that key!
   */
  public static final class TestIntegerListProperty extends AbstractConfigProperty<List<Integer>, List<String>> {

    @Override
    protected List<String> readFromSource(String namespace) {
      return ConfigUtility.getPropertyList(getKey(), null, namespace);
    }

    @Override
    public String description() {
      return null;
    }

    @Override
    protected List<Integer> parse(List<String> value) {
      List<Integer> result = new ArrayList<>(value.size());
      for (String s : value) {
        result.add(Integer.valueOf(s));
      }
      return result;
    }

    @Override
    public List<Integer> getDefaultValue() {
      return Arrays.asList(22, 33);
    }

    @Override
    public String getKey() {
      return "myListKey";
    }
  }
}
