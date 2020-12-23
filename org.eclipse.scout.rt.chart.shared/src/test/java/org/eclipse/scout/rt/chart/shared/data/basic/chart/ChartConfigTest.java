/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.chart.shared.data.basic.chart;

import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ChartConfigTest {

  private static final String TYPE1 = "doughnut";
  private static final String TYPE2 = "pie";

  private static final String LEVEL1 = "level1";
  private static final String LEVEL2_PLAIN = "level2";
  private static final String LEVEL2 = LEVEL1 + "." + LEVEL2_PLAIN;
  private static final String LEVEL3_PLAIN = "level3";
  private static final String LEVEL3 = LEVEL2 + "." + LEVEL3_PLAIN;

  private static final String INT_PROPERTY_PLAIN = "int";
  private static final String INT_PROPERTY = LEVEL3 + "." + INT_PROPERTY_PLAIN;
  private static final int INT_PROPERTY_VALUE1 = 42;
  private static final int INT_PROPERTY_VALUE2 = 43;
  private static final String STRING_PROPERTY_PLAIN = "string";
  private static final String STRING_PROPERTY = LEVEL3 + "." + STRING_PROPERTY_PLAIN;
  private static final String STRING_PROPERTY_VALUE = "lorem ipsum dolor";
  private static final String BOOLEAN_PROPERTY_PLAIN = "boolean";
  private static final String BOOLEAN_PROPERTY = LEVEL2 + "." + BOOLEAN_PROPERTY_PLAIN;
  private static final boolean BOOLEAN_PROPERTY_VALUE = false;

  private static final String LIST_PLAIN = "list";
  private static final String LIST = LEVEL1 + "." + LIST_PLAIN;
  private static final String LIST_PROPERTY_PLAIN = "property";
  private static final String LIST0 = LIST + "[0]";
  private static final String LIST0_PROPERTY = LIST0 + "." + LIST_PROPERTY_PLAIN;
  private static final int LIST0_PROPERTY_VALUE = 1;
  private static final String LIST3 = LIST + "[3]";
  private static final String LIST3_PROPERTY = LIST3 + "." + LIST_PROPERTY_PLAIN;
  private static final int LIST3_PROPERTY_VALUE = 8;
  private static final String LIST7 = LIST + "[7]";
  private static final String LIST7_PROPERTY = LIST7 + "." + LIST_PROPERTY_PLAIN;
  private static final int LIST7_PROPERTY_VALUE1 = 128;
  private static final int LIST7_PROPERTY_VALUE2 = 129;

  @Test
  public void testWithAndRemove() {
    IChartConfig config = BEANS.get(IChartConfig.class)
        .withType(TYPE1)
        .withAnimated(true)
        .withProperty(INT_PROPERTY, INT_PROPERTY_VALUE1)
        .withProperty(STRING_PROPERTY, STRING_PROPERTY_VALUE)
        .withProperty(BOOLEAN_PROPERTY, BOOLEAN_PROPERTY_VALUE);

    Assert.assertEquals(config.getType(), TYPE1);
    Assert.assertTrue(config.isAnimated());
    Assert.assertEquals(config.getProperty(INT_PROPERTY), INT_PROPERTY_VALUE1);
    Assert.assertEquals(config.getProperty(STRING_PROPERTY), STRING_PROPERTY_VALUE);
    Assert.assertEquals(config.getProperty(BOOLEAN_PROPERTY), BOOLEAN_PROPERTY_VALUE);

    config
        .withType(TYPE2)
        .withProperty(INT_PROPERTY, INT_PROPERTY_VALUE2);

    Assert.assertEquals(config.getType(), TYPE2);
    Assert.assertEquals(config.getProperty(INT_PROPERTY), INT_PROPERTY_VALUE2);

    config.removeProperty(INT_PROPERTY);

    Assert.assertNull(config.getProperty(INT_PROPERTY));
    Assert.assertEquals(config.getProperty(STRING_PROPERTY), STRING_PROPERTY_VALUE);
    Assert.assertEquals(config.getProperty(BOOLEAN_PROPERTY), BOOLEAN_PROPERTY_VALUE);

    config.removeProperty(LEVEL3);

    Assert.assertNull(config.getProperty(INT_PROPERTY));
    Assert.assertNull(config.getProperty(STRING_PROPERTY));
    Assert.assertEquals(config.getProperty(BOOLEAN_PROPERTY), BOOLEAN_PROPERTY_VALUE);
  }

  @Test
  public void testWithAndRemoveList() {
    IChartConfig config = BEANS.get(IChartConfig.class)
        .withProperty(LIST7_PROPERTY, LIST7_PROPERTY_VALUE1);

    Assert.assertNotNull(config.getProperty(LIST));
    Assert.assertNotNull(config.getProperty(LIST0));
    Assert.assertNotNull(config.getProperty(LIST3));
    Assert.assertNull(config.getProperty(LIST3_PROPERTY));
    Assert.assertNotNull(config.getProperty(LIST7));
    Assert.assertEquals(config.getProperty(LIST7_PROPERTY), LIST7_PROPERTY_VALUE1);

    config.withProperty(LIST3_PROPERTY, LIST3_PROPERTY_VALUE);

    Assert.assertEquals(config.getProperty(LIST3_PROPERTY), LIST3_PROPERTY_VALUE);

    config.removeProperty(LIST7_PROPERTY);

    Assert.assertEquals(config.getProperty(LIST3_PROPERTY), LIST3_PROPERTY_VALUE);
    Assert.assertNotNull(config.getProperty(LIST7));
    Assert.assertNull(config.getProperty(LIST7_PROPERTY));

    config.removeProperty(LIST3);

    Assert.assertNotNull(config.getProperty(LIST3));
    Assert.assertNull(config.getProperty(LIST3_PROPERTY));
    Assert.assertNotNull(config.getProperty(LIST7));

    config.removeProperty(LIST);

    Assert.assertNull(config.getProperty(LIST));
    Assert.assertNull(config.getProperty(LIST0));
    Assert.assertNull(config.getProperty(LIST3));
    Assert.assertNull(config.getProperty(LIST7));
  }

  @Test
  public void testAddProperties() {
    IChartConfig source = BEANS.get(IChartConfig.class)
        .withType(TYPE1)
        .withProperty(INT_PROPERTY, INT_PROPERTY_VALUE1)
        .withProperty(STRING_PROPERTY, STRING_PROPERTY_VALUE)
        .withProperty(LIST3_PROPERTY, LIST3_PROPERTY_VALUE)
        .withProperty(LIST7_PROPERTY, LIST7_PROPERTY_VALUE1);

    IChartConfig target = BEANS.get(IChartConfig.class)
        .withType(TYPE2)
        .withProperty(INT_PROPERTY, INT_PROPERTY_VALUE2)
        .withProperty(BOOLEAN_PROPERTY, BOOLEAN_PROPERTY_VALUE)
        .withProperty(LIST0_PROPERTY, LIST0_PROPERTY_VALUE)
        .withProperty(LIST7_PROPERTY, LIST7_PROPERTY_VALUE2);

    target.addProperties(source, false);

    Assert.assertEquals(target.getType(), TYPE2);
    Assert.assertEquals(target.getProperty(INT_PROPERTY), INT_PROPERTY_VALUE2);
    Assert.assertEquals(target.getProperty(STRING_PROPERTY), STRING_PROPERTY_VALUE);
    Assert.assertEquals(target.getProperty(BOOLEAN_PROPERTY), BOOLEAN_PROPERTY_VALUE);
    Assert.assertEquals(target.getProperty(LIST0_PROPERTY), LIST0_PROPERTY_VALUE);
    Assert.assertEquals(target.getProperty(LIST3_PROPERTY), LIST3_PROPERTY_VALUE);
    Assert.assertEquals(target.getProperty(LIST7_PROPERTY), LIST7_PROPERTY_VALUE2);

    target.addProperties(source, true);

    Assert.assertEquals(target.getType(), TYPE1);
    Assert.assertEquals(target.getProperty(INT_PROPERTY), INT_PROPERTY_VALUE1);
    Assert.assertEquals(target.getProperty(LIST7_PROPERTY), LIST7_PROPERTY_VALUE1);
  }

  @Test
  public void testGetProperties() {
    IChartConfig config = BEANS.get(IChartConfig.class)
        .withType(TYPE1)
        .withProperty(INT_PROPERTY, INT_PROPERTY_VALUE1)
        .withProperty(STRING_PROPERTY, STRING_PROPERTY_VALUE)
        .withProperty(BOOLEAN_PROPERTY, BOOLEAN_PROPERTY_VALUE)
        .withProperty(LIST0_PROPERTY, LIST0_PROPERTY_VALUE)
        .withProperty(LIST3_PROPERTY, LIST3_PROPERTY_VALUE)
        .withProperty(LIST7_PROPERTY, LIST7_PROPERTY_VALUE1);

    Map<String, Object> properties = config.getProperties();
    Assert.assertNotNull(properties);
    Assert.assertEquals(properties.size(), 2);

    Object level1Obj = properties.get(LEVEL1);
    Assert.assertTrue(level1Obj instanceof Map);
    @SuppressWarnings("unchecked")
    Map<String, Object> level1 = (Map<String, Object>) level1Obj;
    Assert.assertEquals(level1.size(), 2);

    Object listObj = level1.get(LIST_PLAIN);
    Assert.assertTrue(listObj instanceof List);
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> list = (List<Map<String, Object>>) listObj;
    Assert.assertEquals(list.size(), 8);

    Map<String, Object> list0 = list.get(0);
    Assert.assertNotNull(list0);
    Assert.assertEquals(list0.size(), 1);

    Object list0Obj = list0.get(LIST_PROPERTY_PLAIN);
    Assert.assertNotNull(list0Obj);
    Assert.assertEquals(list0Obj, LIST0_PROPERTY_VALUE);

    Map<String, Object> list3 = list.get(3);
    Assert.assertNotNull(list3);
    Assert.assertEquals(list3.size(), 1);

    Object list3Obj = list3.get(LIST_PROPERTY_PLAIN);
    Assert.assertNotNull(list3Obj);
    Assert.assertEquals(list3Obj, LIST3_PROPERTY_VALUE);

    Map<String, Object> list6 = list.get(6);
    Assert.assertNotNull(list6);
    Assert.assertTrue(list6.isEmpty());

    Map<String, Object> list7 = list.get(7);
    Assert.assertNotNull(list7);
    Assert.assertEquals(list7.size(), 1);

    Object list7Obj = list7.get(LIST_PROPERTY_PLAIN);
    Assert.assertNotNull(list7Obj);
    Assert.assertEquals(list7Obj, LIST7_PROPERTY_VALUE1);

    Object level2Obj = level1.get(LEVEL2_PLAIN);
    Assert.assertTrue(level2Obj instanceof Map);
    @SuppressWarnings("unchecked")
    Map<String, Object> level2 = (Map<String, Object>) level2Obj;
    Assert.assertEquals(level2.size(), 2);

    Object booleanObj = level2.get(BOOLEAN_PROPERTY_PLAIN);
    Assert.assertNotNull(booleanObj);
    Assert.assertEquals(booleanObj, BOOLEAN_PROPERTY_VALUE);

    Object level3Obj = level2.get(LEVEL3_PLAIN);
    Assert.assertTrue(level3Obj instanceof Map);
    @SuppressWarnings("unchecked")
    Map<String, Object> level3 = (Map<String, Object>) level3Obj;
    Assert.assertEquals(level3.size(), 2);

    Object stringObj = level3.get(STRING_PROPERTY_PLAIN);
    Assert.assertNotNull(stringObj);
    Assert.assertEquals(stringObj, STRING_PROPERTY_VALUE);

    Object intObj = level3.get(INT_PROPERTY_PLAIN);
    Assert.assertNotNull(intObj);
    Assert.assertEquals(intObj, INT_PROPERTY_VALUE1);

    Map<String, Object> propertiesFlat = config.getPropertiesFlat();
    Assert.assertNotNull(propertiesFlat);
    Assert.assertEquals(propertiesFlat.size(), 7);
    Assert.assertEquals(propertiesFlat.get(INT_PROPERTY), INT_PROPERTY_VALUE1);
    Assert.assertEquals(propertiesFlat.get(STRING_PROPERTY), STRING_PROPERTY_VALUE);
    Assert.assertEquals(propertiesFlat.get(BOOLEAN_PROPERTY), BOOLEAN_PROPERTY_VALUE);
    Assert.assertEquals(propertiesFlat.get(LIST0_PROPERTY), LIST0_PROPERTY_VALUE);
    Assert.assertEquals(propertiesFlat.get(LIST3_PROPERTY), LIST3_PROPERTY_VALUE);
    Assert.assertEquals(propertiesFlat.get(LIST7_PROPERTY), LIST7_PROPERTY_VALUE1);
  }
}
