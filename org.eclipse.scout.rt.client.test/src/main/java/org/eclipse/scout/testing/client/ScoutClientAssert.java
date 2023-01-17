/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.testing.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.form.IForm;
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
    // TODO [7.0] abr: check row visibility
    Set<Object> expectedKeys = new HashSet<>(Arrays.asList(keys));
    Set<Object> unexpectedKeys = new HashSet<>();
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
    if ((field.isVisible() != visible) || (field.isEnabledIncludingParents() != enabled) || (field.isMandatory() != mandatory)) {
      //noinspection StringBufferReplaceableByString
      StringBuilder builder = new StringBuilder();
      builder.append(field.getFieldId());
      builder.append(" (");
      builder.append(formatFormFieldHierarchy(field));
      builder.append(")");
      builder.append(" is expected ");
      builder.append(visible ? "visible" : "invisible");
      builder.append(", ");
      builder.append(enabled ? "enabled" : "disabled");
      builder.append(", ");
      builder.append(mandatory ? "mandatory" : "non-mandatory");
      builder.append(" but was ");
      builder.append(field.isVisible() ? "visible" : "invisible");
      builder.append(", ");
      builder.append(field.isEnabledIncludingParents() ? "enabled" : "disabled");
      builder.append(", ");
      builder.append(field.isMandatory() ? "mandatory" : "non-mandatory");
      Assert.fail(builder.toString());
    }
  }

  public static void assertVisibleStrictly(IForm form, IFormField... fields) {
    assertView(ViewKind.VISIBLE, true, form, fields);
  }

  public static void assertInvisibleStrictly(IForm form, IFormField... fields) {
    assertView(ViewKind.INVISIBLE, true, form, fields);
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
    assertView(ViewKind.NON_MANDATORY, true, form, fields);
  }

  public static void assertVisible(IForm form, IFormField... fields) {
    assertView(ViewKind.VISIBLE, false, form, fields);
  }

  public static void assertInvisible(IForm form, IFormField... fields) {
    assertView(ViewKind.INVISIBLE, false, form, fields);
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
    assertView(ViewKind.NON_MANDATORY, false, form, fields);
  }

  private enum ViewKind {
    VISIBLE("Visible", "visible", "invisible") {
      @Override
      public boolean testField(IFormField field) {
        return field.isVisible();
      }
    },
    INVISIBLE("Invisible", "invisible", "visible") {
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
    NON_MANDATORY("Non-mandatory", "non-mandatory", "mandatory") {
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
    final Set<IFormField> expectedFields = new HashSet<>(Arrays.asList(fields));
    final List<IFormField> unexpectedFields = new ArrayList<>();
    form.visit(field -> {
      if (viewKind.testField(field)) {
        boolean expected = expectedFields.remove(field);
        if (strict && !expected) {
          unexpectedFields.add(field);
        }
      }
    }, IFormField.class);
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

  private static String formatFormFieldHierarchy(IFormField field) {
    StringBuilder builder = new StringBuilder();
    builder.append(field.getFieldId());
    IFormField parent = field.getParentField();
    while (parent != null) {
      builder.insert(0, ".");
      builder.insert(0, parent.getFieldId());
      parent = parent.getParentField();
    }
    IForm form = field.getForm();
    if (form != null) {
      builder.insert(0, ".");
      builder.insert(0, form.getFormId());
    }
    return builder.toString();
  }

  private static String formatFieldNames(Collection<IFormField> fields) {
    StringBuilder builder = new StringBuilder();
    boolean first = true;
    for (IFormField field : fields) {
      if (!first) {
        builder.append(", ");
      }
      else {
        first = false;
      }
      builder.append(field.getFieldId());
    }
    return builder.toString();
  }
}
