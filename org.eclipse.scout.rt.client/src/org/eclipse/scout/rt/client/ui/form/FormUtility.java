/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public final class FormUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(FormUtility.class);

  private FormUtility() {
  }

  /**
   * Complete the configurations of the complete field tree of the form. This
   * method is normally called by the form's constructor after the form
   * initConfig. This method is normally called before {@link #initFormFields(IForm)}.
   */
  public static void postInitConfig(IForm form) throws ProcessingException {
    PostInitConfigFieldVisitor v = new PostInitConfigFieldVisitor();
    form.visitFields(v);
    v.handleResult();
  }

  /**
   * Complete the configurations of the complete field tree of the form. This
   * method is normally called by the form's constructor after the form
   * initConfig and postInitConfig. This method is normally called before {@link #initFormFields(IForm)}.
   */
  public static void rebuildFieldGrid(IForm form, boolean initMainBoxGridData) throws ProcessingException {
    RebuildFieldGridVisitor v = new RebuildFieldGridVisitor();
    form.visitFields(v);
    v.handleResult();
    //
    if (initMainBoxGridData) {
      initRootBoxGridData(form, form.getRootGroupBox());
    }
  }

  private static void initRootBoxGridData(IForm form, ICompositeField rootBox) throws ProcessingException {
    // layout data for root group box
    GridData rootData = new GridData(rootBox.getGridDataHints());
    if (rootData.w == IFormField.FULL_WIDTH) {
      rootData.w = rootBox.getGridColumnCount();
    }
    //Legacy
    if (form instanceof ISearchForm && rootData.weightY < 0) {
      String viewId = ("" + form.getDisplayViewId()).toUpperCase();
      if (viewId.indexOf("SEARCH") >= 0 || viewId.indexOf("S") >= 0) {
        rootData.weightY = 0;
      }
    }
    rootData.x = 0;
    rootData.y = 0;
    rootBox.setGridDataInternal(rootData);
  }

  /**
   * Initialize the complete field tree of the form
   */
  public static void initFormFields(IForm form) throws ProcessingException {
    InitFieldVisitor v = new InitFieldVisitor();
    form.visitFields(v);
    v.handleResult();
  }

  /**
   * Dispose the complete field tree of the form
   */
  public static void disposeFormFields(IForm form) {
    DisposeFieldVisitor v = new DisposeFieldVisitor();
    form.visitFields(v);
  }

  private static class PostInitConfigFieldVisitor implements IFormFieldVisitor {
    private ProcessingException m_firstEx;

    @Override
    public boolean visitField(IFormField field, int level, int fieldIndex) {
      try {
        field.postInitConfig();
      }
      catch (ProcessingException e) {
        if (m_firstEx == null) {
          m_firstEx = e;
        }
      }
      catch (Throwable t) {
        if (m_firstEx == null) {
          m_firstEx = new ProcessingException("Unexpected", t);
        }
      }
      return true;
    }

    public void handleResult() throws ProcessingException {
      if (m_firstEx != null) {
        throw m_firstEx;
      }
    }
  }

  private static class RebuildFieldGridVisitor implements IFormFieldVisitor {
    private ProcessingException m_firstEx;

    @Override
    public boolean visitField(IFormField field, int level, int fieldIndex) {
      try {
        if (field instanceof ICompositeField) {
          ((ICompositeField) field).rebuildFieldGrid();
        }
      }
      catch (Throwable t) {
        if (m_firstEx == null) {
          m_firstEx = new ProcessingException("Unexpected", t);
        }
      }
      return true;
    }

    public void handleResult() throws ProcessingException {
      if (m_firstEx != null) {
        throw m_firstEx;
      }
    }
  }

  private static class InitFieldVisitor implements IFormFieldVisitor {
    private ProcessingException m_firstEx;

    @Override
    public boolean visitField(IFormField field, int level, int fieldIndex) {
      try {
        field.initField();
      }
      catch (ProcessingException e) {
        if (m_firstEx == null) {
          m_firstEx = e;
        }
      }
      catch (Throwable t) {
        if (m_firstEx == null) {
          m_firstEx = new ProcessingException("Unexpected", t);
        }
      }
      return true;
    }

    public void handleResult() throws ProcessingException {
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
      catch (Throwable t) {
        LOG.warn("dispose on " + field, t);
        // nop
      }
      return true;
    }
  }

}
