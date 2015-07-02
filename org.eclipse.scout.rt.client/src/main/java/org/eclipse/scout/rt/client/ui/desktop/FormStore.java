/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.index.AbstractMultiValueIndex;
import org.eclipse.scout.commons.index.IndexedStore;
import org.eclipse.scout.rt.client.ui.desktop.outline.IFormParent;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;

/**
 * {@link IndexedStore} for {@link IForm} objects.
 *
 * @since 5.1
 */
@Bean
public class FormStore extends IndexedStore<IForm> {

  private final P_TypeIndex m_typeIndex = registerIndex(new P_TypeIndex());
  private final P_ViewFormParentIndex m_formParentViewIndex = registerIndex(new P_ViewFormParentIndex());
  private final P_DialogFormParentIndex m_formParentDialogIndex = registerIndex(new P_DialogFormParentIndex());
  private final P_DisplayHintIndex m_displayHintIndex = registerIndex(new P_DisplayHintIndex());
  private final P_ClassIndex m_clazzIndex = registerIndex(new P_ClassIndex());
  private final P_ExclusiveKeyViewIndex m_viewKeyIndex = registerIndex(new P_ExclusiveKeyViewIndex());

  private static enum FormType {
    VIEW, DIALOG;
  }

  /**
   * Returns all <code>Views</code> in the order as inserted.
   */
  public List<IForm> getViews() {
    return m_typeIndex.get(FormType.VIEW);
  }

  /**
   * Returns all <code>Dialogs</code> in the order as inserted.
   */
  public List<IForm> getDialogs() {
    return m_typeIndex.get(FormType.DIALOG);
  }

  /**
   * Returns all <code>Views</code> which are attached to the given {@link IFormParent}. The forms returned are ordered
   * as inserted.
   */
  public List<IForm> getViewsByFormParent(final IFormParent formParent) {
    return m_formParentViewIndex.get(formParent);
  }

  /**
   * Returns all <code>Dialogs</code> which are attached to the given {@link IFormParent}. The forms returned are
   * ordered as inserted.
   */
  public List<IForm> getDialogsByFormParent(final IFormParent formParent) {
    return m_formParentDialogIndex.get(formParent);
  }

  /**
   * Returns all <code>Forms</code> of the given <code>displayHint</code>, e.g. to query all Views or Dialogs. The forms
   * returned are ordered as inserted.
   */
  public List<IForm> getByDisplayHint(final int displayHint) {
    return m_displayHintIndex.get(displayHint);
  }

  /**
   * Returns all <code>Forms</code> of the given {@link Class type}. The forms returned are ordered as inserted. The
   * forms are ordered as inserted.
   */
  public List<IForm> getByClass(final Class<? extends IForm> clazz) {
    return m_clazzIndex.get(clazz);
  }

  /**
   * Returns all <code>Views</code> which compute to the given 'exclusive key'. The forms returned are ordered as
   * inserted.
   */
  public List<IForm> getViewsByKey(final Object key) {
    return m_viewKeyIndex.get(key);
  }

  // ====  Index definitions ==== //

  private class P_TypeIndex extends AbstractMultiValueIndex<FormType, IForm> {

    @Override
    protected FormType calculateIndexFor(final IForm form) {
      return (form.getDisplayHint() == IForm.DISPLAY_HINT_VIEW ? FormType.VIEW : FormType.DIALOG);
    }
  }

  private class P_ViewFormParentIndex extends AbstractMultiValueIndex<IFormParent, IForm> {

    @Override
    protected IFormParent calculateIndexFor(final IForm form) {
      if (form.getDisplayHint() == IForm.DISPLAY_HINT_VIEW) {
        return form.getFormParent();
      }
      else {
        return null;
      }
    }
  }

  private class P_DialogFormParentIndex extends AbstractMultiValueIndex<IFormParent, IForm> {

    @Override
    protected IFormParent calculateIndexFor(final IForm form) {
      if (form.getDisplayHint() != IForm.DISPLAY_HINT_VIEW) {
        return form.getFormParent();
      }
      else {
        return null;
      }
    }
  }

  private class P_DisplayHintIndex extends AbstractMultiValueIndex<Integer, IForm> {

    @Override
    protected Integer calculateIndexFor(final IForm form) {
      return form.getDisplayHint();
    }
  }

  private class P_ClassIndex extends AbstractMultiValueIndex<Class<? extends IForm>, IForm> {

    @Override
    protected Class<? extends IForm> calculateIndexFor(final IForm form) {
      return form.getClass();
    }
  }

  private class P_ExclusiveKeyViewIndex extends AbstractMultiValueIndex<Object, IForm> {

    @Override
    protected Object calculateIndexFor(final IForm form) {
      try {
        if (form.getDisplayHint() == IForm.DISPLAY_HINT_VIEW) {
          return form.computeExclusiveKey();
        }
        else {
          return null;
        }
      }
      catch (final ProcessingException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
        return null;
      }
    }
  }
}
