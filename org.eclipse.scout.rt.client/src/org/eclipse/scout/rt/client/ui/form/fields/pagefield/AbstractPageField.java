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
package org.eclipse.scout.rt.client.ui.form.fields.pagefield;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;

/**
 * Representation of a page as a composite of detailForm, table, searchForm for
 * usage inside a {@link IForm}
 */
public abstract class AbstractPageField<T extends IPage> extends AbstractGroupBox implements IPageField<T> {
  private T m_page;
  private SimpleOutline m_outline;

  public AbstractPageField() {
    this(true);
  }

  public AbstractPageField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected boolean getConfiguredBorderVisible() {
    return false;
  }

  @Override
  protected void initConfig() {
    m_outline = new SimpleOutline();
    super.initConfig();
  }

  @Override
  public final T getPage() {
    return m_page;
  }

  @Override
  public void setPage(T newPage) {
    setPageInternal(newPage);
  }

  private void setPageInternal(T page) {
    if (m_page == page) {
      return;
    }
    // remove old
    getDetailFormField().setInnerForm(null);
    getTableField().setTable(null, true);
    getSearchFormField().setInnerForm(null);
    if (m_page != null) {
      m_outline.disposeTree();
      m_outline = null;
      m_page = null;
    }
    // add new
    m_page = page;
    if (m_page != null) {
      m_outline = new SimpleOutline();
      m_outline.setRootNode(m_page);
      m_outline.selectNode(m_page);
      m_outline.addPropertyChangeListener(
          new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
              if (e.getPropertyName().equals(IOutline.PROP_DETAIL_FORM)) {
                getDetailFormField().setInnerForm(((IOutline) e.getSource()).getDetailForm());
              }
              else if (e.getPropertyName().equals(IOutline.PROP_DETAIL_TABLE)) {
                getTableField().setTable(((IOutline) e.getSource()).getDetailTable(), true);
              }
              else if (e.getPropertyName().equals(IOutline.PROP_SEARCH_FORM)) {
                getSearchFormField().setInnerForm(((IOutline) e.getSource()).getSearchForm());
              }
            }
          }
          );
      getDetailFormField().setInnerForm(m_outline.getDetailForm());
      getTableField().setTable(m_outline.getDetailTable(), true);
      getSearchFormField().setInnerForm(m_outline.getSearchForm());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public IWrappedFormField<IForm> getDetailFormField() {
    return getFieldByClass(DetailFormField.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public ITableField<ITable> getTableField() {
    return getFieldByClass(TableField.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public IWrappedFormField<IForm> getSearchFormField() {
    return getFieldByClass(SearchFormField.class);
  }

  @Order(10)
  public class SearchFormField extends AbstractWrappedFormField<IForm> {
    @Override
    protected int getConfiguredGridW() {
      return FULL_WIDTH;
    }

    @Override
    protected int getConfiguredGridH() {
      return 2;
    }

    @Override
    protected double getConfiguredGridWeightY() {
      return 0.001;
    }
  }

  @Order(20)
  public class TableField extends AbstractTableField<ITable> {
    @Override
    protected boolean getConfiguredLabelVisible() {
      return false;
    }

    @Override
    protected int getConfiguredGridW() {
      return FULL_WIDTH;
    }

    @Override
    protected int getConfiguredGridH() {
      return 7;
    }

    @Override
    protected double getConfiguredGridWeightY() {
      return 1;
    }

  }

  @Order(30)
  public class DetailFormField extends AbstractWrappedFormField<IForm> {
    @Override
    protected boolean getConfiguredVisible() {
      return true;
    }

    @Override
    protected int getConfiguredGridW() {
      return FULL_WIDTH;
    }

    @Override
    protected boolean execCalculateVisible() {
      return getInnerForm() != null;
    }
  }

  public static class SimpleOutline extends AbstractOutline {

    @Override
    protected boolean getConfiguredRootNodeVisible() {
      return true;
    }

  }

}
