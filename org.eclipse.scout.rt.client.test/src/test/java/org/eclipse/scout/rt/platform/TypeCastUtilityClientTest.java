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
package org.eclipse.scout.rt.platform;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.junit.Test;

/**
 * JUnit tests for {@link TypeCastUtility} using classes from the org.eclipse.scout.rt.client.ui.form.fields package.
 * See also: {@link org.eclipse.scout.rt.platform.util.TypeCastUtilityTest} for tests that do not require this package.
 */
public class TypeCastUtilityClientTest {

  @Test
  public void testGetGenericsParameterClass() {
    Class<?> t;
    //
    t = TypeCastUtility.getGenericsParameterClass(ListBox.class, IHolder.class, 0);
    assertEquals(Set.class, t);
    //
    t = TypeCastUtility.getGenericsParameterClass(ListBox.class, IValueField.class, 0);
    assertEquals(Set.class, t);
    //
    t = TypeCastUtility.getGenericsParameterClass(ListBox.class, IListBox.class, 0);
    assertEquals(Long.class, t);
    //
    t = TypeCastUtility.getGenericsParameterClass(LongField.class, IHolder.class, 0);
    assertEquals(Long.class, t);
    //
    t = TypeCastUtility.getGenericsParameterClass(LongArrayField.class, IHolder.class, 0);
    assertEquals(Long[].class, t);
    //
    t = TypeCastUtility.getGenericsParameterClass(MapField.class, IHolder.class, 0);
    assertEquals(Map.class, t);
    //
    t = TypeCastUtility.getGenericsParameterClass(MapArrayField.class, IHolder.class, 0);
    assertEquals(Map[].class, t);
  }

  static class ListBox extends AbstractListBox<Long> {

  }

  static class LongField extends AbstractValueField<Long> {

  }

  static class LongArrayField extends AbstractValueField<Long[]> {

  }

  static class MapField extends AbstractValueField<Map<String, Integer>> {

  }

  static class MapArrayField extends AbstractValueField<Map<String[], Integer>[]> {

  }
}
