/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

import static org.eclipse.scout.rt.platform.util.StringUtility.startsWith;

import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;

public abstract class AbstractFormHybridAction<FORM extends IForm, DO_ENTITY extends IDoEntity> extends AbstractHybridAction<DO_ENTITY> {

  private final static String OPEN_FORM = "openForm";
  protected final static String OPEN_FORM_PREFIX = OPEN_FORM + DELIMITER;

  private final static String CREATE_FORM = "createForm";
  protected final static String CREATE_FORM_PREFIX = CREATE_FORM + DELIMITER;

  protected boolean isShowFormOnStart() {
    return startsWith(getHybridActionType(), OPEN_FORM);
  }

  @Override
  public void execute(DO_ENTITY data) {
    FORM form = createForm(data);

    prepareForm(form);

    form.addFormListener(e -> {
      if (FormEvent.TYPE_STORE_AFTER == e.getType()) {
        DO_ENTITY result = BEANS.get(getDoEntityClass());
        exportResult(form, result);
        fireHybridWidgetEvent("save", result);
      }
      else if (FormEvent.TYPE_RESET_COMPLETE == e.getType()) {
        fireHybridWidgetEvent("reset");
      }
      else if (FormEvent.TYPE_CLOSED == e.getType()) {
        fireHybridWidgetEvent("close");
      }
    });

    addWidget(form);

    startForm(form);
  }

  protected abstract FORM createForm(DO_ENTITY data);

  protected void prepareForm(FORM form) {
    form.setShowOnStart(isShowFormOnStart());
  }

  protected void startForm(FORM form) {
    form.start();
  }

  protected void exportResult(FORM form, DO_ENTITY result) {
    // nop
  }
}
