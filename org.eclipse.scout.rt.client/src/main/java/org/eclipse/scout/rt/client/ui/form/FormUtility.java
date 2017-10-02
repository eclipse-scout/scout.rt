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
package org.eclipse.scout.rt.client.ui.form;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FormUtility {
  private static final Logger LOG = LoggerFactory.getLogger(FormUtility.class);

  private FormUtility() {
  }

  public static String normalizeDisplayViewId(String viewId) {
    if (viewId == null) {
      return IForm.VIEW_ID_CENTER;
    }
    switch (viewId) {
      case IForm.VIEW_ID_NW:
      case IForm.VIEW_ID_W:
      case IForm.VIEW_ID_SW:

      case IForm.VIEW_ID_N:
      case IForm.VIEW_ID_CENTER:
      case IForm.VIEW_ID_S:

      case IForm.VIEW_ID_NE:
      case IForm.VIEW_ID_E:
      case IForm.VIEW_ID_SE:
        return viewId;
      default:
        return IForm.VIEW_ID_CENTER;
    }
  }

  /**
   * Complete the configurations of the complete field tree of the form. This method is normally called by the form's
   * constructor after the form initConfig. This method is normally called before {@link #initFormFields(IForm)}.
   */
  public static void postInitConfig(IForm form) {
    PostInitConfigFieldVisitor v = new PostInitConfigFieldVisitor();
    form.visitFields(v);
    v.handleResult();
  }

  /**
   * Complete the configurations of the complete field tree of the form. This method is normally called by the form's
   * constructor after the form initConfig and postInitConfig. This method is normally called before
   * {@link #initFormFields(IForm)}.
   */
  public static void rebuildFieldGrid(IForm form, boolean initMainBoxGridData) {
    RebuildFieldGridVisitor v = new RebuildFieldGridVisitor();
    form.visitFields(v);
    v.handleResult();
    //
    if (initMainBoxGridData) {
      initRootBoxGridData(form, form.getRootGroupBox());
    }
  }

  private static void initRootBoxGridData(IForm form, ICompositeField rootBox) {
    // layout data for root group box
    GridData rootData = new GridData(rootBox.getGridDataHints());
    if (rootData.w == IFormField.FULL_WIDTH) {
      rootData.w = rootBox.getFieldGrid().getGridColumnCount();
    }
    rootBox.setGridDataInternal(rootData);
  }

  /**
   * Initialize the complete field tree of the form
   */
  public static void initFormFields(IForm form) {
    InitFieldVisitor v = new InitFieldVisitor();
    form.visitFields(v);
    v.handleResult();
  }

  public static void initFormFields(ICompositeField field) {
    InitFieldVisitor v = new InitFieldVisitor();
    field.visitFields(v);
    v.handleResult();
  }

  /**
   * Dispose the complete field tree of the form
   */
  public static void disposeFormFields(IForm form) {
    DisposeFieldVisitor v = new DisposeFieldVisitor();
    form.visitFields(v);
  }

  public static void disposeFormFields(ICompositeField field) {
    DisposeFieldVisitor v = new DisposeFieldVisitor();
    field.visitFields(v);
  }

  /**
   * With this method it's possible to set the mark strategy of all tab boxes of the given form.
   *
   * @param form
   *          the form
   * @param strategy
   *          one of {@link ITabBox#MARK_STRATEGY_EMPTY}, {@link ITabBox#MARK_STRATEGY_SAVE_NEEDED}
   * @since 3.8.2
   */
  public static void setTabBoxMarkStrategy(IForm form, int strategy) {
    form.visitFields(new TabBoxMarkStrategyVisitor(strategy));
  }

  private static class PostInitConfigFieldVisitor implements IFormFieldVisitor {
    private RuntimeException m_firstEx;

    @Override
    public boolean visitField(IFormField field, int level, int fieldIndex) {
      try {
        field.postInitConfig();
      }
      catch (RuntimeException e) {
        if (m_firstEx == null) {
          m_firstEx = e;
        }
      }
      return true;
    }

    public void handleResult() {
      if (m_firstEx != null) {
        throw m_firstEx;
      }
    }
  }

  private static class RebuildFieldGridVisitor implements IFormFieldVisitor {
    private RuntimeException m_firstEx;

    @Override
    public boolean visitField(IFormField field, int level, int fieldIndex) {
      try {
        if (field instanceof ICompositeField) {
          ((ICompositeField) field).rebuildFieldGrid();
        }
      }
      catch (RuntimeException e) {
        if (m_firstEx == null) {
          m_firstEx = e;
        }
      }
      return true;
    }

    public void handleResult() {
      if (m_firstEx != null) {
        throw m_firstEx;
      }
    }
  }

  private static class InitFieldVisitor implements IFormFieldVisitor {
    private RuntimeException m_firstEx;

    @Override
    public boolean visitField(IFormField field, int level, int fieldIndex) {
      try {
        field.initField();
      }
      catch (RuntimeException e) {
        if (m_firstEx == null) {
          m_firstEx = e;
        }
      }
      return true;
    }

    public void handleResult() {
      if (m_firstEx != null) {
        throw m_firstEx;
      }
    }
  }

  private static class DisposeFieldVisitor implements IFormFieldVisitor {
    @Override
    public boolean visitField(IFormField field, int level, int fieldIndex) {
      try {
        field.disposeField();
      }
      catch (Exception t) {
        LOG.warn("Could not dispose field '{}'", field, t);
        // nop
      }
      return true;
    }
  }

  private static final class TabBoxMarkStrategyVisitor implements IFormFieldVisitor {
    private final int m_strategy;

    private TabBoxMarkStrategyVisitor(int strategy) {
      m_strategy = strategy;
    }

    @Override
    public boolean visitField(IFormField field, int level, int fieldIndex) {
      if (field instanceof ITabBox) {
        ((ITabBox) field).setMarkStrategy(m_strategy);
      }
      return true;
    }
  }
}
