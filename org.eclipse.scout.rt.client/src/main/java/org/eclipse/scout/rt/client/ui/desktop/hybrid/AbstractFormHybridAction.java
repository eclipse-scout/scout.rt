/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
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
    addWidget(form);
    startForm(form);
  }

  protected abstract FORM createForm(DO_ENTITY data);

  protected void prepareForm(FORM form) {
    form.setShowOnStart(isShowFormOnStart());
  }

  protected void addFormListeners(FORM form) {
    form.addFormListener(e -> {
      if (FormEvent.TYPE_STORE_AFTER == e.getType()) {
        DO_ENTITY result = createEmptyResult();
        exportResult(form, result);
        fireHybridWidgetEvent("save", result);
      }
      else if (FormEvent.TYPE_RESET_COMPLETE == e.getType()) {
        DO_ENTITY result = createEmptyResult();
        exportResult(form, result);
        fireHybridWidgetEvent("reset");
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

  protected void exportResult(FORM form, DO_ENTITY result) {
    // nop
  }
}
