/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

import static org.eclipse.scout.rt.platform.util.StringUtility.startsWith;

import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;

/**
 * An action to remotely create, start and show a form.
 * <p>
 * A subclass has to create the form using {@link #createForm(IDoEntity)} and add a {@link HybridActionType} annotation
 * with a value that either starts with {@link #CREATE_FORM_PREFIX} or {@link #OPEN_FORM_PREFIX}. If
 * {@link #OPEN_FORM_PREFIX} is used, the form will not only be started but also shown (added to the desktop).
 * <p>
 * When the form is saved, reset or closed, an event is sent to the remote client to inform about the operation. The
 * save event will also contain the data returned by {@link #exportResult(IForm)}. Implement this method or
 * {@link #exportResult(IForm, IDoEntity)} to provide the data that should be returned in this case.
 */
public abstract class AbstractFormHybridAction<FORM extends IForm, DO_ENTITY extends IDoEntity> extends AbstractHybridAction<DO_ENTITY> {

  private static final String OPEN_FORM = "openForm";
  protected static final String OPEN_FORM_PREFIX = OPEN_FORM + DELIMITER;

  private static final String CREATE_FORM = "createForm";
  protected static final String CREATE_FORM_PREFIX = CREATE_FORM + DELIMITER;

  protected boolean isShowFormOnStart() {
    return startsWith(getHybridActionType(), OPEN_FORM);
  }

  @Override
  public void execute(DO_ENTITY data) {
    FORM form = createForm(data);
    prepareForm(form);
    addFormListeners(form);
    startForm(form);
    addWidget(form);
  }

  protected abstract FORM createForm(DO_ENTITY data);

  protected void prepareForm(FORM form) {
    form.setShowOnStart(isShowFormOnStart());
  }

  protected void addFormListeners(FORM form) {
    form.addFormListener(e -> {
      if (FormEvent.TYPE_STORE_AFTER == e.getType()) {
        DO_ENTITY result = exportResultInternal(form);
        fireHybridWidgetEvent("save", result);
      }
      else if (FormEvent.TYPE_RESET_COMPLETE == e.getType()) {
        DO_ENTITY result = exportResultInternal(form);
        fireHybridWidgetEvent("reset", result);
      }
      else if (FormEvent.TYPE_CLOSED == e.getType()) {
        fireHybridWidgetEvent("close");
      }
    });
  }

  protected void startForm(FORM form) {
    form.start();
  }

  protected DO_ENTITY createEmptyResult() {
    if (getDoEntityClass() == IDoEntity.class) {
      //noinspection unchecked
      return (DO_ENTITY) BEANS.get(DoEntity.class);
    }
    return BEANS.get(getDoEntityClass());
  }

  protected DO_ENTITY exportResultInternal(FORM form) {
    DO_ENTITY result = exportResult(form);
    if (result == null) {
      result = createEmptyResult();
    }
    return result;
  }

  protected DO_ENTITY exportResult(FORM form) {
    DO_ENTITY result = createEmptyResult();
    exportResult(form, result);
    return result;
  }

  protected void exportResult(FORM form, DO_ENTITY result) {
    // nop
  }
}
