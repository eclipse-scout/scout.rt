/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.annotations.IgnoreProperty;
import org.eclipse.scout.rt.platform.annotations.IgnoreProperty.Context;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonBeanTest {

  @Test
  public void testBeanWithPrimitive() {
    BeanWithPrimitives bean = new BeanWithPrimitives();
    bean.setLong(4);
    bean.setString("hello");

    MainJsonObjectFactory factory = new MainJsonObjectFactory();
    IJsonObject jsonObj = factory.createJsonObject(bean);

    JSONObject json = (JSONObject) jsonObj.toJson();
    assertEquals(4, json.optLong("long"));
    assertEquals("hello", json.getString("string"));
  }

  @Test
  public void testBeanWithDate() {
    BeanWithDate bean = new BeanWithDate();
    Calendar cal = Calendar.getInstance();
    cal.set(2015, 8, 24, 17, 38, 9);
    cal.set(Calendar.MILLISECOND, 0);
    bean.setDate(cal.getTime());

    MainJsonObjectFactory factory = new MainJsonObjectFactory();
    IJsonObject jsonObj = factory.createJsonObject(bean);

    JSONObject json = (JSONObject) jsonObj.toJson();
    assertEquals("2015-09-24 17:38:09.000", json.getString("date"));
  }

  @Test
  public void testBeanWithByteArray() {
    BeanWithByteArray bean = new BeanWithByteArray();
    String str = "hello";
    bean.setBytes(str.getBytes());

    MainJsonObjectFactory factory = new MainJsonObjectFactory();
    IJsonObject jsonObj = factory.createJsonObject(bean);

    JSONObject json = (JSONObject) jsonObj.toJson();
    assertEquals(Base64Utility.encode(str.getBytes()), json.getString("bytes"));
  }

  @Test
  public void testBeanWithCollection() {
    BeanWithCollection bean = new BeanWithCollection();
    List<Long> list = new ArrayList<Long>();
    list.add(2L);
    list.add(400L);
    list.add(5000L);
    bean.setLongs(list);

    MainJsonObjectFactory factory = new MainJsonObjectFactory();
    IJsonObject jsonObj = factory.createJsonObject(bean);

    JSONObject json = (JSONObject) jsonObj.toJson();
    JSONArray jsonArray = json.optJSONArray("longs");
    assertEquals(2, jsonArray.optLong(0));
    assertEquals(400, jsonArray.optLong(1));
    assertEquals(5000, jsonArray.optLong(2));
  }

  @Test
  public void testBeanWithMap() {
    BeanWithMap bean = new BeanWithMap();
    Map<Object, Object> map = new HashMap<Object, Object>();
    map.put("key1", new Integer(2));
    map.put("key2", new Integer(3));
    bean.setMap(map);

    MainJsonObjectFactory factory = new MainJsonObjectFactory();
    IJsonObject jsonObj = factory.createJsonObject(bean);

    JSONObject json = (JSONObject) jsonObj.toJson();
    assertEquals(2, json.getJSONObject("map").getLong("key1"));
    assertEquals(3, json.getJSONObject("map").getLong("key2"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBeanWithMapIllegal() {
    BeanWithMap bean = new BeanWithMap();
    Map<Object, Object> map = new HashMap<Object, Object>();
    map.put(new Integer(1), new Integer(2));
    map.put(new Integer(4), new Integer(3));
    bean.setMap(map);

    MainJsonObjectFactory factory = new MainJsonObjectFactory();
    IJsonObject jsonObj = factory.createJsonObject(bean);
    jsonObj.toJson();
  }

  @Test
  public void testBeanWithIgnoredProperty() {
    BeanWithIgnoredProperty bean = new BeanWithIgnoredProperty();
    bean.setProperty("property");
    bean.setIgnoredProperty("Ignored property");
    bean.setIgnoredDefault("Ignored default property");

    MainJsonObjectFactory factory = new MainJsonObjectFactory();
    IJsonObject jsonObj = factory.createJsonObject(bean);

    JSONObject json = (JSONObject) jsonObj.toJson();
    assertEquals("property", json.getString("property"));
    assertNull(json.optString("ignoredProperty", null));
    assertNull(json.optString("ignoredDefault", null));
  }

  public static class BeanWithPrimitives {
    private long m_long;
    private String m_string;

    public long getLong() {
      return m_long;
    }

    public void setLong(long l) {
      m_long = l;
    }

    public String getString() {
      return m_string;
    }

    public void setString(String string) {
      m_string = string;
    }
  }

  public static class BeanWithDate {
    private Date m_date;

    public Date getDate() {
      return m_date;
    }

    public void setDate(Date date) {
      m_date = date;
    }

  }

  public static class BeanWithCollection {
    private List<Long> m_longs;

    public List<Long> getLongs() {
      return m_longs;
    }

    public void setLongs(List<Long> longs) {
      m_longs = longs;
    }

  }

  public static class BeanWithByteArray {
    private byte[] m_bytes;

    public byte[] getBytes() {
      return m_bytes;
    }

    public void setBytes(byte[] bytes) {
      m_bytes = bytes;
    }

  }

  public static class BeanWithMap {
    private Map<Object, Object> m_map = new HashMap<Object, Object>();

    public Map getMap() {
      return m_map;
    }

    public void setMap(Map<Object, Object> map) {
      m_map = map;
    }

  }

  public static class BeanWithIgnoredProperty {
    private String m_property;
    private String m_ignoredProperty;
    private String m_ignoredDefault;

    public String getProperty() {
      return m_property;
    }

    public void setProperty(String property) {
      m_property = property;
    }

    @IgnoreProperty(Context.GUI)
    public String getIgnoredProperty() {
      return m_ignoredProperty;
    }

    public void setIgnoredProperty(String ignoredProperty) {
      m_ignoredProperty = ignoredProperty;
    }

    @IgnoreProperty
    public String getIgnoredDefault() {
      return m_ignoredDefault;
    }

    public void setIgnoredDefault(String ignoredDefault) {
      m_ignoredDefault = ignoredDefault;
    }

  }
}
