/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import static org.eclipse.scout.rt.platform.util.Assertions.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.form.FormUtility;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.platform.OrderedComparator;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;

public final class CompositeFieldUtility {

  private CompositeFieldUtility() {
  }

  public static void addField(IFormField f, ICompositeField compositeField, List<IFormField> fields) {
    assertNotNull(f);
    assertNotNull(compositeField);
    assertNotNull(fields);
    assertNull(f.getParentField(), "field is already contained in '{}'.", f.getParentField());
    assertTrue(f.getForm() == null || f.getForm() == compositeField.getForm(),
        "field is part of a different form,  '{}' instead of '{}'", f.getForm(), compositeField.getForm());

    fields.add(f);
    fields.sort(new OrderedComparator());
    connectFields(f, compositeField);
    if (compositeField.isInitConfigDone()) {
      f.init();
      FormUtility.rebuildFieldGrid(compositeField);
    }
  }

  static void selectIfIsTab(IGroupBox groupBox) {
    ICompositeField parentField = groupBox.getParentField();
    if (!(parentField instanceof ITabBox)) {
      return;
    }

    ITabBox t = (ITabBox) parentField;
    if (t.getSelectedTab() != groupBox) {
      t.setSelectedTab(groupBox);
    }
  }

  /**
   * Selects the tab containing the given {@link IFormField} for all parent tabboxes.<br>
   * This ensures that the given {@link IFormField} could be seen (if visible itself).
   *
   * @param formField
   *     The {@link IFormField} whose parent tabs should be selected.
   */
  public static void selectAllParentTabsOf(IFormField formField) {
    if (formField == null) {
      return;
    }
    formField.visitParents(CompositeFieldUtility::selectIfIsTab, IGroupBox.class);
  }

  /**
   * Connects the specified fields. This includes setting the parent form field and form of the specified child.
   *
   * @param child
   *     The child to connect. May not be {@code null}.
   * @param parent
   *     The parent form field. May be {@code null}.
   */
  public static void connectFields(IFormField child, ICompositeField parent) {
    if (parent == null) {
      child.setParentInternal(null);
      // do not clear form here so that a form field cannot be added to a different form later on
      return;
    }

    assertNull(child.getParentField());
    child.setParentInternal(parent);
    IForm formOfParentField = parent.getForm();
    IForm formOfChildField = child.getForm();
    if (formOfChildField == formOfParentField) {
      return; // nothing to do. already set correctly.
    }
    child.setFormInternal(formOfParentField);
  }

  public static void removeField(IFormField f, ICompositeField compositeField, List<IFormField> fields) {
    assertNotNull(fields);
    assertNotNull(f, "field must not be null");
    boolean removed = fields.remove(f);
    assertTrue(removed, "field is not part of container '{}'.", compositeField);

    connectFields(f, null);
    if (f.isInitConfigDone()) {
      f.dispose();
    }
  }

  /**
   * Changes the order of the given field and reorders its parent {@link ICompositeField} to ensure it is placed at the
   * new position.
   *
   * @param field
   *     The {@link IFormField} whose order should be changed. Must not be {@code null}.
   * @param newOrder
   *     The new order value.
   */
  public static void changeFieldOrder(IFormField field, double newOrder) {
    assertNotNull(field).setOrder(newOrder);
    reorderChildFields(field.getParentField());
  }

  /**
   * Reorders the child fields of the given {@link ICompositeField} so that the order matches the value of
   * {@link IFormField#getOrder()}. This may be useful after the order values of fields have been changed using
   * {@link IFormField#setOrder(double)}.
   *
   * @param compositeField
   *     The {@link ICompositeField} for which the child form fields should be reordered. Must not be {@code null}.
   */
  public static void reorderChildFields(ICompositeField compositeField) {
    assertNotNull(compositeField);
    List<IFormField> fields = compositeField.getFields();
    fields.sort(new OrderedComparator());
    compositeField.setFields(fields);
    compositeField.rebuildFieldGrid();
  }

  /**
   * @deprecated Will be removed with Scout 12. Use {@link #moveFieldTo(IFormField, ICompositeField, ICompositeField)}
   * instead.
   */
  @Deprecated
  public static void moveFieldTo(IFormField f, ICompositeField oldContainer, ICompositeField newContainer, Map<Class<? extends IFormField>, IFormField> movedFormFieldsByClass) {
    moveFieldTo(f, oldContainer, newContainer);
  }

  public static void moveFieldTo(IFormField f, ICompositeField oldContainer, ICompositeField newContainer) {
    assertNotNull(f, "field must not be null");
    assertNotNull(oldContainer, "old container must not be null");
    assertNotNull(newContainer, "new container must not be null");

    oldContainer.removeField(f);
    newContainer.addField(f);
  }

  public static <T extends IWidget> TreeVisitResult getWidgetByClassInternal(final IWidget widget, Holder<T> result, Class<T> classToFind) {
    if (widget.getClass() == classToFind) {
      result.setValue(classToFind.cast(widget));
    }
    else {
      T movedFieldByClass = getMovedFieldByClassIfComposite(widget, classToFind);
      if (movedFieldByClass != null) {
        result.setValue(movedFieldByClass);
      }
    }
    return result.getValue() == null ? TreeVisitResult.CONTINUE : TreeVisitResult.TERMINATE;
  }

  private static <T extends IWidget> T getMovedFieldByClassIfComposite(IWidget widget, Class<T> classToFind) {
    if (!(widget instanceof ICompositeField)) {
      return null;
    }
    return getMovedFieldByClass((ICompositeField) widget, classToFind);
  }

  private static <T extends IWidget> T getMovedFieldByClass(ICompositeField compositeField, Class<T> formFieldClass) {
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
