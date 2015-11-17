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
package org.eclipse.scout.rt.client.services.common.search;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.internal.ComposerDisplayTextBuilder;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox;
import org.eclipse.scout.rt.shared.ScoutTexts;
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
    //
    //composer
    if (field instanceof AbstractComposerField) {
      AbstractComposerField composerField = (AbstractComposerField) field;
      ITreeNode rootNode = composerField.getTree().getRootNode();
      if (rootNode != null) {
        StringBuilder buf = new StringBuilder();
        new ComposerDisplayTextBuilder().build(rootNode, buf, "");
        String s = buf.toString();
        if (StringUtility.hasText(s)) {
          search.addDisplayText(s);
        }
      }
      return;
    }
    //list box
    if (field instanceof AbstractListBox<?>) {
      AbstractListBox<?> valueField = (AbstractListBox<?>) field;
      if (!valueField.getValue().isEmpty()) {
        search.addDisplayText(label + " " + ScoutTexts.get("LogicIn") + " " + valueField.getDisplayText());
      }
      return;
    }
    //tree box
    if (field instanceof AbstractTreeBox<?>) {
      AbstractTreeBox<?> valueField = (AbstractTreeBox<?>) field;
      if (!valueField.getValue().isEmpty()) {
        search.addDisplayText(label + " " + ScoutTexts.get("LogicIn") + " " + valueField.getDisplayText());
      }
      return;
    }
    //string, html, label field
    if (field instanceof AbstractStringField || field instanceof AbstractHtmlField || field instanceof AbstractLabelField) {
      AbstractValueField<?> valueField = (AbstractValueField<?>) field;
      if (valueField.getValue() != null) {
        search.addDisplayText(label + " " + ScoutTexts.get("LogicLike") + " " + valueField.getDisplayText());
      }
      return;
    }
    //boolean field
    if (field instanceof AbstractBooleanField) {
      AbstractBooleanField valueField = (AbstractBooleanField) field;
      if (valueField.getValue() != null && valueField.getValue()) {
        search.addDisplayText(label);
      }
      return;
    }
    //radiobuttongroup field
    if (field instanceof AbstractRadioButtonGroup<?>) {
      AbstractRadioButtonGroup<?> valueField = (AbstractRadioButtonGroup<?>) field;
      if (valueField.getValue() != null) {
        IButton selectedButton = valueField.getSelectedButton();
        search.addDisplayText(label + "=" + (selectedButton != null ? ("" + selectedButton.getLabel()).replace("&", "") : ""));
      }
      return;
    }
    //value field
    if (field instanceof AbstractValueField<?>) {
      AbstractValueField<?> valueField = (AbstractValueField<?>) field;
      if (valueField.getValue() != null) {
        search.addDisplayText(label + " " + ScoutTexts.get("LogicEQ") + " " + valueField.getDisplayText());
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
