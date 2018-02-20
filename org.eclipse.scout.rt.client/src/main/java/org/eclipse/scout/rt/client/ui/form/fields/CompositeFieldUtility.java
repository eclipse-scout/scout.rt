/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;
import static org.eclipse.scout.rt.platform.util.Assertions.assertNull;
import static org.eclipse.scout.rt.platform.util.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.scout.rt.client.ui.form.FormUtility;
import org.eclipse.scout.rt.platform.OrderedComparator;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;

public final class CompositeFieldUtility {

  private CompositeFieldUtility() {
  }

  public static void addField(IFormField f, ICompositeField compositeField, List<IFormField> fields) {
    assertNotNull(f);
    assertNotNull(compositeField);
    assertNotNull(fields);
    assertNull(f.getParentField(), "field is already contained in '{}'.", f.getParentField());

    Assertions.assertTrue(f.getForm() == null || f.getForm() == compositeField.getForm(),
        "field is part of a different form,  '{}' instead of '{}'", f.getForm(), compositeField.getForm());

    fields.add(f);
    fields.sort(new OrderedComparator());
    f.setParentFieldInternal(compositeField);
    f.setFormInternal(compositeField.getForm());
    if (compositeField.isInitConfigDone()) {
      f.init();
      FormUtility.rebuildFieldGrid(compositeField);
    }
  }

  public static void removeField(IFormField f, ICompositeField compositeField, List<IFormField> fields) {
    assertNotNull(fields);
    assertNotNull(f, "field must not be null");

    boolean removed = fields.remove(f);
    assertTrue(removed, "field is not part of container '{}'.", compositeField);

    f.setParentFieldInternal(null);
    if (f.isInitConfigDone()) {
      f.dispose();
    }
  }

  public static void moveFieldTo(IFormField f, ICompositeField oldContainer, ICompositeField newContainer, Map<Class<? extends IFormField>, IFormField> movedFormFieldsByClass) {
    assertNotNull(f, "field must not be null");
    assertNotNull(oldContainer, "old container must not be null");
    assertNotNull(newContainer, "new container must not be null");

    oldContainer.removeField(f);
    newContainer.addField(f);
    movedFormFieldsByClass.put(f.getClass(), f);
  }

  public static <T extends IFormField> T getFieldByClass(ICompositeField compositeField, final Class<T> formFieldClass) {
    // check local moved fields
    IFormField movedField = getMovedFieldByClass(compositeField, formFieldClass);
    if (movedField != null) {
      return formFieldClass.cast(movedField);
    }
    // visit child fields
    final Holder<T> found = new Holder<>(formFieldClass);
    Function<IFormField, TreeVisitResult> v = field -> {
      if (field.getClass() == formFieldClass) {
        found.setValue(formFieldClass.cast(field));
      }
      else if (field instanceof ICompositeField) {
        T movedFieldByClass = getMovedFieldByClass((ICompositeField) field, formFieldClass);
        if (movedFieldByClass != null) {
          found.setValue(movedFieldByClass);
        }
      }
      return found.getValue() == null ? TreeVisitResult.CONTINUE : TreeVisitResult.TERMINATE;
    };
    compositeField.visit(v, IFormField.class);
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
    final Holder<T> found = new Holder<>(type);
    Function<IFormField, TreeVisitResult> v = field -> {
      if (type.isAssignableFrom(field.getClass()) && field.getFieldId().equals(id)) {
        found.setValue(type.cast(field));
      }
      else if (field instanceof ICompositeField) {
        T movedFieldById = getMovedFieldById((ICompositeField) field, id, type);
        if (movedFieldById != null) {
          found.setValue(movedFieldById);
        }
      }
      return found.getValue() == null ? TreeVisitResult.CONTINUE : TreeVisitResult.TERMINATE;
    };
    compositeField.visit(v, IFormField.class);
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
