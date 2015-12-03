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
package org.eclipse.scout.rt.client.ui.form.fields;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.platform.OrderedComparator;
import org.eclipse.scout.rt.platform.holders.Holder;

public final class CompositeFieldUtility {

  private CompositeFieldUtility() {
  }

  public static void addField(IFormField f, ICompositeField compositeField, List<IFormField> fields) {
    checkFieldStateForMove(f);
    checkFieldStateForMove(compositeField);
    if (f.getParentField() != null) {
      throw new IllegalArgumentException("field is already contained in '" + f.getParentField() + "'");
    }
    if (f.getForm() != null && f.getForm() != compositeField.getForm()) {
      throw new IllegalArgumentException("field is part of a different form,  '" + f.getForm() + "' instead of '" + compositeField.getForm() + "'");
    }
    fields.add(f);
    Collections.sort(fields, new OrderedComparator());
    f.setParentFieldInternal(compositeField);
    f.setFormInternal(compositeField.getForm());
    if (compositeField instanceof AbstractCompositeField) {
      f.addPropertyChangeListener(((AbstractCompositeField) compositeField).new P_FieldPropertyChangeListener());
    }
  }

  public static void removeField(IFormField f, ICompositeField compositeField, List<IFormField> fields) {
    checkFieldStateForMove(f);
    checkFieldStateForMove(compositeField);
    if (!fields.remove(f)) {
      throw new IllegalArgumentException("field is not part of container '" + compositeField + "'");
    }
    f.setParentFieldInternal(null);
  }

  public static void moveFieldTo(IFormField f, ICompositeField oldContainer, ICompositeField newContainer, Map<Class<? extends IFormField>, IFormField> movedFormFieldsByClass) {
    if (f == null) {
      throw new IllegalArgumentException("field must not be null");
    }
    if (oldContainer == null) {
      throw new IllegalArgumentException("old container must not be null");
    }
    if (newContainer == null) {
      throw new IllegalArgumentException("new container must not be null");
    }
    oldContainer.removeField(f);
    newContainer.addField(f);
    movedFormFieldsByClass.put(f.getClass(), f);
  }

  private static void checkFieldStateForMove(IFormField f) {
    if (f == null) {
      throw new IllegalArgumentException("field must not be null");
    }
    IForm form = f.getForm();
    if (form == null) {
      return;
    }
    if (form.getHandler() == null) {
      return;
    }
    if (form.isShowing()) {
      throw new IllegalStateException("field '" + f + "' is already showing on desktop. Structural changes are not allowed anymore.");
    }
  }

  public static <T extends IFormField> T getFieldByClass(ICompositeField compositeField, final Class<T> formFieldClass) {
    // check local moved fields
    IFormField movedField = getMovedFieldByClass(compositeField, formFieldClass);
    if (movedField != null) {
      return formFieldClass.cast(movedField);
    }
    // visit child fields
    final Holder<T> found = new Holder<T>(formFieldClass);
    IFormFieldVisitor v = new IFormFieldVisitor() {
      @Override
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (field.getClass() == formFieldClass) {
          found.setValue(formFieldClass.cast(field));
        }
        else if (field instanceof ICompositeField) {
          T movedFieldByClass = getMovedFieldByClass((ICompositeField) field, formFieldClass);
          if (movedFieldByClass != null) {
            found.setValue(movedFieldByClass);
          }
        }
        return found.getValue() == null;
      }
    };
    compositeField.visitFields(v, 0);
    return found.getValue();
  }

  private static <T extends IFormField> T getMovedFieldByClass(ICompositeField compositeField, Class<T> formFieldClass) {
    Map<Class<? extends IFormField>, IFormField> movedFields = compositeField.getMovedFields();
    IFormField f = movedFields.get(formFieldClass);
    return formFieldClass.cast(f);
  }

  public static IFormField getFieldById(ICompositeField compositeField, final String id) {
    return getFieldById(compositeField, id, IFormField.class);
  }

  public static <T extends IFormField> T getFieldById(ICompositeField compositeField, final String id, final Class<T> type) {
    // check local moved fields
    T movedField = getMovedFieldById(compositeField, id, type);
    if (movedField != null) {
      return movedField;
    }
    // visit child fields
    final Holder<T> found = new Holder<T>(type);
    IFormFieldVisitor v = new IFormFieldVisitor() {
      @Override
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (type.isAssignableFrom(field.getClass()) && field.getFieldId().equals(id)) {
          found.setValue(type.cast(field));
        }
        else if (field instanceof ICompositeField) {
          T movedFieldById = getMovedFieldById((ICompositeField) field, id, type);
          if (movedFieldById != null) {
            found.setValue(movedFieldById);
          }
        }
        return found.getValue() == null;
      }
    };
    compositeField.visitFields(v, 0);
    return found.getValue();
  }

  private static <T extends IFormField> T getMovedFieldById(ICompositeField compositeField, String id, final Class<T> type) {
    Collection<IFormField> movedFields = compositeField.getMovedFields().values();
    for (IFormField f : movedFields) {
      if (type.isAssignableFrom(f.getClass()) && f.getFieldId().equals(id)) {
        return type.cast(f);
      }
    }
    return null;
  }
}
