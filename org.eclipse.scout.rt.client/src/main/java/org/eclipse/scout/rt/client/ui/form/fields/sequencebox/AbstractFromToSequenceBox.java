/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.sequencebox;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

/**
 * {@link AbstractSequenceBox} implementation for two fields: 'from' and 'to'. The fields are grouped within this
 * sequence box and do not need to have own labels. The error status message uses generic field names for from and to.
 *
 * @see SequenceValidationHelper to validate a pair of from/to fields without grouping them within a sequence box.
 */
@ClassId("4a0d8a5e-45d5-4ba3-a2d8-f456bb92132c")
public abstract class AbstractFromToSequenceBox extends AbstractSequenceBox {

  protected AbstractFromToSequenceBox() {
    this(true);
  }

  protected AbstractFromToSequenceBox(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected <T extends Comparable<T>> void execCheckFromTo(IValueField<T>[] valueFields, int changedIndex) {
    if (valueFields == null
        || valueFields.length != 2
        || valueFields[0] == null
        || valueFields[1] == null
        || valueFields[0].getValue() == null
        || valueFields[1].getValue() == null) {
      clearErrorStatus();
      return;
    }
    T o1 = valueFields[0].getValue();
    T o2 = valueFields[1].getValue();
    if (ObjectUtility.compareTo(o1, o2) > 0) {
      InvalidSequenceStatus errorStatus = new InvalidSequenceStatus(TEXTS.get("XMustBeGreaterThanOrEqualY", TEXTS.get("to"), TEXTS.get("from")));
      addErrorStatus(errorStatus);
      return;
    }
    clearErrorStatus();
  }

  @Override
  protected void execAddSearchTerms(SearchFilter search) {
    if (getFields().size() < 2) {
      return;
    }
    IValueField<?> field1 = (IValueField<?>) getFields().get(0);
    Object value1 = field1.getValue();
    IValueField<?> field2 = (IValueField<?>) getFields().get(1);
    Object value2 = field2.getValue();
    if (value1 != null && value2 != null) {
      search.addDisplayText(
          getLabel() + " "
              + TEXTS.get("from") + " " + field1.getDisplayText() + " "
              + TEXTS.get("to") + " " + field2.getDisplayText());
    }
    else if (value1 != null) {
      search.addDisplayText(
          getLabel() + " "
              + TEXTS.get("from") + " " + field1.getDisplayText());
    }
    else if (value2 != null) {
      search.addDisplayText(
          getLabel() + " "
              + TEXTS.get("to") + " " + field2.getDisplayText());
    }
  }

  @Override
  protected boolean execIsLabelSuffixCandidate(IFormField formField) {
    if (getFieldIndex(formField) > 0) {
      return false; // never show the label of the second field in the label of the sequence box
    }
    return super.execIsLabelSuffixCandidate(formField);
  }
}
