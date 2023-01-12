/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.common.search;

import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IRadioButton;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

@Order(5100)
public class DefaultSearchFilterService implements ISearchFilterService {

  @Override
  public SearchFilter createNewSearchFilter() {
    return new SearchFilter();
  }

  @Override
  public void applySearchDelegate(IFormField field, SearchFilter search, boolean includeChildren) {
    String label = field.getLabel();
    if (field.getParentField() instanceof ISequenceBox && field.getParentField() instanceof AbstractFormField) {
      AbstractFormField range = (AbstractFormField) field.getParentField();
      if (range.getInitialLabel() != null) {
        label = range.getInitialLabel() + (StringUtility.isNullOrEmpty(label) ? "" : " " + label);
      }
    }
    label = StringUtility.nullIfEmpty(label);

    //list box
    if (field instanceof AbstractListBox<?>) {
      AbstractListBox<?> valueField = (AbstractListBox<?>) field;
      if (!valueField.getValue().isEmpty()) {
        search.addDisplayText(StringUtility.box("", label, " " + TEXTS.get("LogicIn") + " ") + valueField.getDisplayText());
      }
      return;
    }
    //tree box
    if (field instanceof AbstractTreeBox<?>) {
      AbstractTreeBox<?> valueField = (AbstractTreeBox<?>) field;
      if (!valueField.getValue().isEmpty()) {
        search.addDisplayText(StringUtility.box("", label, " " + TEXTS.get("LogicIn") + " ") + valueField.getDisplayText());
      }
      return;
    }
    //string, html, label field
    if (field instanceof AbstractStringField || field instanceof AbstractHtmlField || field instanceof AbstractLabelField) {
      AbstractValueField<?> valueField = (AbstractValueField<?>) field;
      if (valueField.getValue() != null) {
        search.addDisplayText(StringUtility.box("", label, " " + TEXTS.get("LogicLike") + " ") + valueField.getDisplayText());
      }
      return;
    }
    //boolean field
    if (field instanceof AbstractBooleanField) {
      AbstractBooleanField valueField = (AbstractBooleanField) field;
      if (valueField.getValue() != null && valueField.getValue() && label != null) {
        search.addDisplayText(label);
      }
      return;
    }
    //radiobuttongroup field
    if (field instanceof AbstractRadioButtonGroup<?>) {
      AbstractRadioButtonGroup<?> valueField = (AbstractRadioButtonGroup<?>) field;
      if (valueField.getValue() != null) {
        IRadioButton<?> selectedButton = valueField.getSelectedButton();
        String valueLabel = (selectedButton != null ? selectedButton.getLabel() : null);
        if (valueLabel == null) {
          if (label != null) {
            search.addDisplayText(label);
          }
        }
        else {
          search.addDisplayText(StringUtility.box("", label, " = ") + valueLabel);
        }
      }
      return;
    }
    //value field
    if (field instanceof AbstractValueField<?>) {
      AbstractValueField<?> valueField = (AbstractValueField<?>) field;
      if (valueField.getValue() != null) {
        search.addDisplayText(StringUtility.box("", label, " " + TEXTS.get("LogicEQ") + " ") + valueField.getDisplayText());
      }
      return;
    }

    if (includeChildren) {
      applySearchDelegateForChildren(field, search);
    }
  }

  protected void applySearchDelegateForChildren(IFormField field, SearchFilter search) {
    if (field instanceof ICompositeField) {
      for (IFormField f : ((ICompositeField) field).getFields()) {
        f.applySearch(search);
      }
    }
  }
}
