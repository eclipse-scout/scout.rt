/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.testing.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox.DefaultListBoxTable;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.junit.Assert;

/**
 * Convenience assertion class for Scout clients.
 */
public final class ScoutClientAssert {

  private ScoutClientAssert() {
  }

  public static void assertContainsKeysStrictly(AbstractListBox<?> listbox, Object... keys) {
    assertContainsKeys(true, listbox, keys);
  }

  public static void assertContainsKeys(AbstractListBox<?> listbox, Object... keys) {
    assertContainsKeys(false, listbox, keys);
  }

  private static void assertContainsKeys(boolean strict, AbstractListBox<?> listbox, Object... keys) {
    // TODO [6.2] abr: check row visibility
    HashSet<Object> expectedKeys = new HashSet<Object>(Arrays.asList(keys));
    HashSet<Object> unexpectedKeys = new HashSet<Object>();
    List listBoxKeys = ((DefaultListBoxTable) listbox.getTable()).getKeyColumn().getValues();
    for (Object key : listBoxKeys) {
      boolean expected = expectedKeys.remove(key);
      if (strict && !expected) {
        unexpectedKeys.add(key);
      }
    }
    if (!expectedKeys.isEmpty() || !unexpectedKeys.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      builder.append("Listbox entries:");
      if (!expectedKeys.isEmpty()) {
        builder.append("\n\tmissing entries: ");
        builder.append(StringUtility.collectionToString(expectedKeys));
      }
      if (!unexpectedKeys.isEmpty()) {
        builder.append("\n\tunexpected entries: ");
        builder.append(StringUtility.collectionToString(unexpectedKeys));
      }
      Assert.fail(builder.toString());
    }
  }

  public static void assertValid(IFormField field) {
    Assert.assertNotNull(field);
    Assert.assertNull(field.getFieldId() + " is expected valid", field.getErrorStatus());
  }

  public static void assertInvalid(IFormField field) {
    Assert.assertNotNull(field);
    Assert.assertNotNull(field.getFieldId() + " is expected invalid", field.getErrorStatus());
  }

  public static void assertVisible(IFormField field) {
    Assert.assertNotNull(field);
    Assert.assertTrue(field.getFieldId() + " is expected visible", field.isVisible());
  }

  public static void assertInvisible(IFormField field) {
    Assert.assertNotNull(field);
    Assert.assertFalse(field.getFieldId() + " is expected invisible", field.isVisible());
  }

  public static void assertEnabled(IFormField field) {
    Assert.assertNotNull(field);
    Assert.assertTrue(field.getFieldId() + " is expected enabled", field.isEnabled());
  }

  public static void assertDisabled(IFormField field) {
    Assert.assertNotNull(field);
    Assert.assertFalse(field.getFieldId() + " is expected disabled", field.isEnabled());
  }

  public static void assertMandatory(IFormField field) {
    Assert.assertNotNull(field);
    Assert.assertTrue(field.getFieldId() + " is expected mandatory", field.isMandatory());
  }

  public static void assertNonMandatory(IFormField field) {
    Assert.assertNotNull(field);
    Assert.assertFalse(field.getFieldId() + " is expected non-mandatory", field.isMandatory());
  }

  public static void assertView(boolean visible, boolean enabled, boolean mandatory, IFormField field) {
    Assert.assertNotNull(field);
    if ((field.isVisible() != visible) || (field.isEnabled() != enabled) || (field.isMandatory() != mandatory)) {
      StringBuilder builder = new StringBuilder();
      builder.append(field.getFieldId());
      builder.append(" is expected ");
      builder.append(visible ? "visible" : "invisible");
      builder.append(", ");
      builder.append(enabled ? "enabled" : "disabled");
      builder.append(", ");
      builder.append(mandatory ? "mandatory" : "non-mandatory");
      builder.append(" but was ");
      builder.append(field.isVisible() ? "visible" : "invisible");
      builder.append(", ");
      builder.append(field.isEnabled() ? "enabled" : "disabled");
      builder.append(", ");
      builder.append(field.isMandatory() ? "mandatory" : "non-mandatory");
      Assert.fail(builder.toString());
    }
  }

  public static void assertVisibleStrictly(IForm form, IFormField... fields) {
    assertView(ViewKind.VISIBILE, true, form, fields);
  }

  public static void assertInvisibleStrictly(IForm form, IFormField... fields) {
    assertView(ViewKind.INVISIBILE, true, form, fields);
  }

  public static void assertEnabledStrictly(IForm form, IFormField... fields) {
    assertView(ViewKind.ENABLED, true, form, fields);
  }

  public static void assertDisabledStrictly(IForm form, IFormField... fields) {
    assertView(ViewKind.DISABLED, true, form, fields);
  }

  public static void assertMandatoryStrictly(IForm form, IFormField... fields) {
    assertView(ViewKind.MANDATORY, true, form, fields);
  }

  public static void assertNonMandatoryStrictly(IForm form, IFormField... fields) {
    assertView(ViewKind.NONMANDATORY, true, form, fields);
  }

  public static void assertVisible(IForm form, IFormField... fields) {
    assertView(ViewKind.VISIBILE, false, form, fields);
  }

  public static void assertInvisible(IForm form, IFormField... fields) {
    assertView(ViewKind.INVISIBILE, false, form, fields);
  }

  public static void assertEnabled(IForm form, IFormField... fields) {
    assertView(ViewKind.ENABLED, false, form, fields);
  }

  public static void assertDisabled(IForm form, IFormField... fields) {
    assertView(ViewKind.DISABLED, false, form, fields);
  }

  public static void assertMandatory(IForm form, IFormField... fields) {
    assertView(ViewKind.MANDATORY, false, form, fields);
  }

  public static void assertNonMandatory(IForm form, IFormField... fields) {
    assertView(ViewKind.NONMANDATORY, false, form, fields);
  }

  private enum ViewKind {
    VISIBILE("Visible", "visible", "invisible") {
      @Override
      public boolean testField(IFormField field) {
        return field.isVisible();
      }
    },
    INVISIBILE("Invisible", "invisible", "visible") {
      @Override
      public boolean testField(IFormField field) {
        return !field.isVisible();
      }
    },
    ENABLED("Enabled", "enabled", "disabled") {
      @Override
      public boolean testField(IFormField field) {
        return field.isEnabled();
      }
    },
    DISABLED("Disabled", "disabled", "enabled") {
      @Override
      public boolean testField(IFormField field) {
        return !field.isEnabled();
      }
    },
    MANDATORY("Mandatory", "mandatory", "non-mandatory") {
      @Override
      public boolean testField(IFormField field) {
        return field.isMandatory();
      }
    },
    NONMANDATORY("Non-mandatory", "non-mandatory", "mandatory") {
      @Override
      public boolean testField(IFormField field) {
        return !field.isMandatory();
      }
    };

    private final String m_name;
    private final String m_positive;
    private final String m_negative;

    ViewKind(String name, String positive, String negative) {
      m_name = name;
      m_positive = positive;
      m_negative = negative;
    }

    public abstract boolean testField(IFormField field);

    public String getName() {
      return m_name;
    }

    public String getPositive() {
      return m_positive;
    }

    public String getNegative() {
      return m_negative;
    }
  }

  private static void assertView(final ViewKind viewKind, final boolean strict, IForm form, IFormField... fields) {
    Assert.assertNotNull(form);
    Assert.assertNotNull(fields);
    final HashSet<IFormField> expectedFields = new HashSet<IFormField>(Arrays.asList(fields));
    final ArrayList<IFormField> unexpectedFields = new ArrayList<IFormField>();
    form.visitFields(new IFormFieldVisitor() {
      @Override
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (viewKind.testField(field)) {
          boolean expected = expectedFields.remove(field);
          if (strict && !expected) {
            unexpectedFields.add(field);
          }
        }
        return true;
      }
    });
    if (!expectedFields.isEmpty() || !unexpectedFields.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      builder.append(viewKind.getName());
      builder.append(" fields:");
      if (!expectedFields.isEmpty()) {
        builder.append("\n\texpected ").append(viewKind.getPositive()).append(" but ").append(viewKind.getNegative()).append(": ");
        builder.append(formatFieldNames(expectedFields));
      }
      if (!unexpectedFields.isEmpty()) {
        builder.append("\n\texpected ").append(viewKind.getNegative()).append(" but ").append(viewKind.getPositive()).append(": ");
        builder.append(formatFieldNames(unexpectedFields));
      }
      Assert.fail(builder.toString());
    }
  }

  private static String formatFieldNames(Collection<IFormField> fields) {
    StringBuilder builder = new StringBuilder();
    boolean first = true;
    for (Iterator<IFormField> it = fields.iterator(); it.hasNext();) {
      if (!first) {
        builder.append(", ");
      }
      else {
        first = false;
      }
      builder.append(it.next().getFieldId());
    }
    return builder.toString();
  }
}
