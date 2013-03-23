/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.testing.client;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;

/**
 * Deprecated: use {@link org.eclipse.scout.rt.testing.client.ScoutClientAssert} instead
 * will be removed with the L-Release.
 */
@Deprecated
public final class ScoutClientAssert {

  private ScoutClientAssert() {
  }

  public static void assertContainsKeysStrictly(AbstractListBox<?> listbox, Object... keys) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertContainsKeysStrictly(listbox, keys);
  }

  public static void assertContainsKeys(AbstractListBox<?> listbox, Object... keys) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertContainsKeys(listbox, keys);
  }

  public static void assertValid(IFormField field) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertValid(field);
  }

  public static void assertInvalid(IFormField field) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertInvalid(field);
  }

  public static void assertVisible(IFormField field) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertVisible(field);
  }

  public static void assertInvisible(IFormField field) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertInvisible(field);
  }

  public static void assertEnabled(IFormField field) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertEnabled(field);
  }

  public static void assertDisabled(IFormField field) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertDisabled(field);
  }

  public static void assertMandatory(IFormField field) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertMandatory(field);
  }

  public static void assertNonMandatory(IFormField field) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertNonMandatory(field);
  }

  public static void assertView(boolean visible, boolean enabled, boolean mandatory, IFormField field) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertView(visible, enabled, mandatory, field);
  }

  public static void assertVisibleStrictly(IForm form, IFormField... fields) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertVisibleStrictly(form, fields);
  }

  public static void assertInvisibleStrictly(IForm form, IFormField... fields) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertInvisibleStrictly(form, fields);
  }

  public static void assertEnabledStrictly(IForm form, IFormField... fields) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertEnabledStrictly(form, fields);
  }

  public static void assertDisabledStrictly(IForm form, IFormField... fields) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertDisabledStrictly(form, fields);
  }

  public static void assertMandatoryStrictly(IForm form, IFormField... fields) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertMandatoryStrictly(form, fields);
  }

  public static void assertNonMandatoryStrictly(IForm form, IFormField... fields) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertNonMandatoryStrictly(form, fields);
  }

  public static void assertVisible(IForm form, IFormField... fields) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertVisible(form, fields);
  }

  public static void assertInvisible(IForm form, IFormField... fields) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertInvisible(form, fields);
  }

  public static void assertEnabled(IForm form, IFormField... fields) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertEnabled(form, fields);
  }

  public static void assertDisabled(IForm form, IFormField... fields) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertDisabled(form, fields);
  }

  public static void assertMandatory(IForm form, IFormField... fields) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertMandatory(form, fields);
  }

  public static void assertNonMandatory(IForm form, IFormField... fields) {
    org.eclipse.scout.rt.testing.client.ScoutClientAssert.assertNonMandatory(form, fields);
  }
}
