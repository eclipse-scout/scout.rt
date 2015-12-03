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
package org.eclipse.scout.rt.client.ui.desktop;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.desktop.AbstractDisplayParentViewIndex;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.index.AbstractMultiValueIndex;
import org.eclipse.scout.rt.platform.index.IndexedStore;

/**
 * {@link IndexedStore} for {@link IForm} objects.
 *
 * @since 5.1
 */
@Bean
public class FormStore extends IndexedStore<IForm> {

  private final P_TypeIndex m_typeIndex = registerIndex(new P_TypeIndex());
  private final P_DisplayParentIndex m_displayParentIndex = registerIndex(new P_DisplayParentIndex());
  private final P_DisplayParentViewIndex m_displayParentViewIndex = registerIndex(new P_DisplayParentViewIndex());
  private final P_DisplayParentDialogIndex m_displayParentDialogIndex = registerIndex(new P_DisplayParentDialogIndex());
  private final P_DisplayHintIndex m_displayHintIndex = registerIndex(new P_DisplayHintIndex());
  private final P_ClassIndex m_clazzIndex = registerIndex(new P_ClassIndex());
  private final P_ExclusiveKeyViewIndex m_viewKeyIndex = registerIndex(new P_ExclusiveKeyViewIndex());
  private final P_ApplicationModalDialogIndex m_applicationModalDialogIndex = registerIndex(new P_ApplicationModalDialogIndex());

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
   * Returns all <code>Forms</code> which are attached to the given {@link IDisplayParent}. The forms returned are
   * ordered as inserted.
   */
  public List<IForm> getByDisplayParent(final IDisplayParent displayParent) {
    return m_displayParentIndex.get(displayParent);
  }

  /**
   * Returns all <code>Views</code> which are attached to the given {@link IDisplayParent}. The forms returned are
   * ordered as inserted.
   */
  public List<IForm> getViewsByDisplayParent(final IDisplayParent displayParent) {
    return m_displayParentViewIndex.get(displayParent);
  }

  /**
   * Returns all <code>Dialogs</code> which are attached to the given {@link IDisplayParent}. The forms returned are
   * ordered as inserted.
   */
  public List<IForm> getDialogsByDisplayParent(final IDisplayParent displayParent) {
    return m_displayParentDialogIndex.get(displayParent);
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

  /**
   * Returns <code>true</code> if this store contains 'application-modal' dialogs, or <code>false</code> if not.
   */
  public boolean containsApplicationModalDialogs() {
    return m_applicationModalDialogIndex.get(Boolean.TRUE).size() > 0;
  }

  // ====  Index definitions ==== //

  private class P_TypeIndex extends AbstractMultiValueIndex<FormType, IForm> {

    @Override
    protected FormType calculateIndexFor(final IForm form) {
      return (form.getDisplayHint() == IForm.DISPLAY_HINT_VIEW ? FormType.VIEW : FormType.DIALOG);
    }
  }

  private class P_DisplayParentIndex extends AbstractMultiValueIndex<IDisplayParent, IForm> {

    @Override
    protected IDisplayParent calculateIndexFor(final IForm form) {
      return form.getDisplayParent();
    }
  }

  private class P_DisplayParentViewIndex extends AbstractDisplayParentViewIndex {

    @Override
    protected IDisplayParent calculateIndexFor(final IForm form) {
      if (form.getDisplayHint() == IForm.DISPLAY_HINT_VIEW) {
        return form.getDisplayParent();
      }
      else {
        return null;
      }
    }

    @Override
    protected int calculatePositionForElement(IForm element) {
      IForm activeForm = ClientSessionProvider.currentSession().getDesktop().getActiveForm();
      List formsOnThisDisplayParent = FormStore.this.getViewsByDisplayParent(calculateIndexFor(element));
      int position = formsOnThisDisplayParent.indexOf(activeForm);
      if (position == -1) {
        //insert at first position
        return 0;
      }
      //insert after active form.
      return position + 1;
    }
  }

  private class P_DisplayParentDialogIndex extends AbstractMultiValueIndex<IDisplayParent, IForm> {

    @Override
    protected IDisplayParent calculateIndexFor(final IForm form) {
      if (form.getDisplayHint() != IForm.DISPLAY_HINT_VIEW) {
        return form.getDisplayParent();
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

  private class P_ApplicationModalDialogIndex extends AbstractMultiValueIndex<Boolean, IForm> {

    @Override
    protected Boolean calculateIndexFor(final IForm form) {
      return form.getDisplayHint() != IForm.DISPLAY_HINT_VIEW && form.isModal() && form.getDisplayParent() == ClientSessionProvider.currentSession().getDesktop();
    }
  }
}
