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
package org.eclipse.scout.rt.client.extension.ui.form;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractCompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.classid.ClassIdentifier;
import org.eclipse.scout.rt.shared.extension.IInternalExtensionRegistry;
import org.eclipse.scout.rt.shared.extension.IMoveModelObjectToRootMarker;
import org.eclipse.scout.rt.shared.extension.MoveDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveFormFieldsHandler {

  private static final Logger LOG = LoggerFactory.getLogger(MoveFormFieldsHandler.class);

  private final IForm m_form;
  private final IInternalExtensionRegistry m_extensionRegistry;
  private final Set<MoveDescriptor<IFormField>> m_moveDescriptors;

  public MoveFormFieldsHandler(IForm form) {
    m_form = form;
    m_extensionRegistry = BEANS.get(IInternalExtensionRegistry.class);
    m_moveDescriptors = new HashSet<MoveDescriptor<IFormField>>();
  }

  public void moveFields() {
    P_FormFieldVisitor visitor = new P_FormFieldVisitor();
    m_form.visitFields(visitor);

    if (m_moveDescriptors.isEmpty()) {
      return;
    }

    StringBuilder sb = new StringBuilder();
    for (MoveDescriptor<IFormField> moveItem : m_moveDescriptors) {
      IFormField field = moveItem.getModel();
      ICompositeField oldContainer = field.getParentField();
      ICompositeField newContainer = findContainer(oldContainer, moveItem.getNewContainerIdentifer(), null);
      if (newContainer != null) {
        Double newOrder = moveItem.getNewOrder();
        if (newOrder != null) {
          field.setOrder(newOrder);
        }
        try {
          field.setFieldChanging(true);
          oldContainer.moveFieldTo(field, newContainer);
          // field grid is not rebuilt for performance reasons and because this handler is intended to be used during initConfig of a form.
        }
        finally {
          field.setFieldChanging(false);
        }
      }
      else {
        if (sb.length() == 0) {
          sb.append("Invalid field move commands:");
        }
        sb.append("  \nField '").append(field).append("' cannot be moved into container '").append(newContainer).append("'");
      }
    }

    if (sb.length() > 0) {
      throw new IllegalArgumentException(sb.toString());
    }
  }

  protected ICompositeField findContainer(ICompositeField container, ClassIdentifier newModelContainerClassIdentifier, ICompositeField ignoredChildContainer) {
    // 1. no new container defined. Hence do not move field into different container.
    if (newModelContainerClassIdentifier == null) {
      return container;
    }

    Class<?> newModelContainerClass = newModelContainerClassIdentifier.getLastSegment();

    // 2. field is moved to root, i.e. into the main box
    if (newModelContainerClass == IMoveModelObjectToRootMarker.class) {
      return container.getForm().getRootGroupBox();
    }

    // 3. current container matches
    if (newModelContainerClass.isInstance(container) && matchesClassIdentifier(container, newModelContainerClassIdentifier)) {
      return container;
    }

    // 4. check current container's child composite fields
    for (IFormField c : container.getFields()) {
      if (newModelContainerClass.isInstance(c) && c instanceof ICompositeField && matchesClassIdentifier((ICompositeField) c, newModelContainerClassIdentifier)) {
        return (ICompositeField) c;
      }
    }

    // 5. visit child containers
    for (IFormField c : container.getFields()) {
      if (c == ignoredChildContainer || !(c instanceof ICompositeField)) {
        continue;
      }
      ICompositeField recursiveContainer = findContainer((ICompositeField) c, newModelContainerClassIdentifier, container);
      if (recursiveContainer != null) {
        return recursiveContainer;
      }
    }

    // 6. current container is a template field. Do not exit template scope
    if (container instanceof AbstractCompositeField && ((AbstractCompositeField) container).isTemplateField()) {
      LOG.warn("Current field is a template. Stop visiting its parent field.");
      return null;
    }

    // 7. continue search on parent container (without revisiting the current container)
    ICompositeField parent = container.getParentField();
    if (parent != null && parent != ignoredChildContainer) {
      return findContainer(parent, newModelContainerClassIdentifier, container);
    }

    return null;
  }

  protected boolean matchesClassIdentifier(ICompositeField container, ClassIdentifier identifier) {
    if (identifier.size() <= 1) {
      // identifier has only one single segment, i.e. no deep linking. No additional checks required
      return true;
    }
    P_FormFieldParentIterator fieldParentIterator = new P_FormFieldParentIterator(container);
    P_ClassIdentifierReverseIterator identifierIterator = new P_ClassIdentifierReverseIterator(identifier);
    while (identifierIterator.hasNext() && fieldParentIterator.hasNext()) {
      Class<?> nextSegment = identifierIterator.next();
      boolean parentIsInstanceOfSegment = false;
      while (!parentIsInstanceOfSegment && fieldParentIterator.hasNext()) {
        Object parent = fieldParentIterator.next();
        if (nextSegment.isInstance(parent)) {
          parentIsInstanceOfSegment = true;
        }
      }
      if (!parentIsInstanceOfSegment) {
        // field parent iterator has no more next elements and the identifier's current segment does not match. -> no match
        return false;
      }
    }
    return !identifierIterator.hasNext();
  }

  private class P_FormFieldVisitor implements IFormFieldVisitor {
    private final P_FormFieldParentIterator parentIterator = new P_FormFieldParentIterator();

    @Override
    public boolean visitField(IFormField field, int level, int fieldIndex) {
      // setup parent field iterator
      parentIterator.setCurrentField(field);
      // lookup move items
      MoveDescriptor<IFormField> moveDesc = m_extensionRegistry.createModelMoveDescriptorFor(field, parentIterator);
      if (moveDesc != null) {
        m_moveDescriptors.add(moveDesc);
      }
      return true;
    }
  }

  private static class P_FormFieldParentIterator implements Iterator<Object> {

    private IFormField m_currentField;

    public P_FormFieldParentIterator() {
      this(null);
    }

    public P_FormFieldParentIterator(IFormField field) {
      m_currentField = field;
    }

    void setCurrentField(IFormField field) {
      if (field == null) {
        throw new IllegalArgumentException("field must not be null.");
      }
      m_currentField = field;
    }

    @Override
    public boolean hasNext() {
      return m_currentField != null && (m_currentField.getParentField() != null || m_currentField.getForm() != null);
    }

    @Override
    public Object next() {
      if (m_currentField == null) {
        throw new IllegalStateException();
      }

      IFormField field = m_currentField;
      m_currentField = field.getParentField();
      if (m_currentField != null) {
        return m_currentField;
      }

      IForm form = field.getForm();
      if (form != null) {
        return form;
      }
      throw new IllegalStateException();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private static class P_ClassIdentifierReverseIterator implements Iterator<Class<?>> {

    private final ClassIdentifier m_identifier;
    private int m_index;

    public P_ClassIdentifierReverseIterator(ClassIdentifier identifier) {
      m_identifier = identifier;
      // ignore last segment
      m_index = m_identifier.size() - 2;
    }

    @Override
    public boolean hasNext() {
      return m_index >= 0;
    }

    @Override
    public Class<?> next() {
      if (m_index < 0) {
        throw new IllegalStateException();
      }
      Class<?> next = m_identifier.getSegment(m_index);
      m_index--;
      return next;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
