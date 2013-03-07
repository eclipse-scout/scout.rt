/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.data.form.properties;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link AbstractPropertyData}
 */
public class FormDataPropertyTest {

  @Test
  public void testGetPropertyById() {
    PropertyTestFormData data = new PropertyTestFormData();
    Assert.assertSame(data.getNameProperty(), data.getPropertyById("Name"));
    Assert.assertSame(data.getNameProperty(), data.getPropertyById("name"));
    Assert.assertNull(data.getPropertyById("nameProperty"));

    Assert.assertSame(data.getCityProp(), data.getPropertyById("CityProp"));
    Assert.assertSame(data.getCityProp(), data.getPropertyById("cityProp"));
    Assert.assertNull(data.getPropertyById("city"));
  }

  public static class PropertyTestFormData extends AbstractFormData {

    private static final long serialVersionUID = 1L;

    public NameProperty getNameProperty() {
      return getPropertyByClass(NameProperty.class);
    }

    public String getName() {
      return getNameProperty().getValue();
    }

    public void setName(String name) {
      getNameProperty().setValue(name);
    }

    public CityProp getCityProp() {
      return getPropertyByClass(CityProp.class);
    }

    public String getCity() {
      return getCityProp().getValue();
    }

    public void setCity(String city) {
      getCityProp().setValue(city);
    }

    public class NameProperty extends AbstractPropertyData<String> {
      private static final long serialVersionUID = 1L;
    }

    public class CityProp extends AbstractPropertyData<String> {
      private static final long serialVersionUID = 1L;
    }
  }
}
