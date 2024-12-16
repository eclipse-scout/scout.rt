/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.wizard.IWizardStep;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;

/**
 * Class to retrieve a unique ID for a Scout object. Typically, the {@link ITypeWithClassId#classId() classId} of the
 * object is used (if available).
 */
@Bean
public class InspectorObjectIdProvider {

  public String getIdForColumn(IColumn<?> column) {
    if (column == null) {
      return null;
    }

    // The UUID of the column is only valid within its table. Therefore, only use the own ClassId.
    // Accordingly, column.classId() cannot be used here as this would include the UUID of the containing table.
    // Also do not use column.getColumnId() as this is less refactoring safe.
    return ConfigurationUtility.getAnnotatedClassIdWithFallback(column.getClass());
  }

  public String getIdForPage(IPage<?> page) {
    if (page == null) {
      return null;
    }
    return page.classId();
  }

  public String getIdForWizardStep(IWizardStep<? extends IForm> step) {
    if (step == null) {
      return null;
    }
    return step.classId();
  }

  public String getIdForWidget(IWidget widget) {
    if (widget == null) {
      return null;
    }
    return widget.classId();
  }

  public String getId(Object o) {
    if (o instanceof IWidget) {
      return getIdForWidget((IWidget) o);
    }
    if (o instanceof IColumn) {
      return getIdForColumn((IColumn<?>) o);
    }
    if (o instanceof IPage) {
      return getIdForPage((IPage<?>) o);
    }
    if (o instanceof IWizardStep) {
      return getIdForWizardStep((IWizardStep<? extends IForm>) o);
    }
    if (o instanceof ITypeWithClassId) {
      // matches e.g. ICode, ICodeType, IWizard, IDataModelAttribute, IDataModelEntity
      return ((ITypeWithClassId) o).classId();
    }
    return null;
  }
}
