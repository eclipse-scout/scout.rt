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
package org.eclipse.scout.rt.client.ui.form.fields.pagefield;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.pagefield.IPageFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.pagefield.PageFieldChains.PageFieldPageChangedChain;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.controls.SearchFormTableControl;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;

/**
 * Representation of a page as a composite of detailForm, table, searchForm for usage inside a {@link IForm}
 */
@ClassId("e2f4e714-637f-4a9b-b3be-d672900e1374")
public abstract class AbstractPageField<PAGE extends IPage> extends AbstractGroupBox implements IPageField<PAGE> {
  private PAGE m_page;
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
  protected boolean getConfiguredStatusVisible() {
    return false;
  }

  @Override
  protected void initConfig() {
    m_outline = new SimpleOutline();
    super.initConfig();
  }

  @Override
  public final PAGE getPage() {
    return m_page;
  }

  @Override
  public void setPage(PAGE newPage) {
    setPageInternal(newPage);
  }

  private void setPageInternal(PAGE page) {
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
    PAGE oldPage = m_page;
    m_page = page;

    if (m_page != null) {
      m_outline = new SimpleOutline();
      m_outline.setRootNode(m_page);
      m_outline.selectNode(m_page);
      m_outline.addPropertyChangeListener(new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
          try {
            if (e.getPropertyName().equals(IOutline.PROP_DETAIL_FORM)) {
              getDetailFormField().setInnerForm(((IOutline) e.getSource()).getDetailForm());
            }
            else if (e.getPropertyName().equals(IOutline.PROP_DETAIL_TABLE)) {
              getTableField().setTable(detachSearchFormTableControl(((IOutline) e.getSource()).getDetailTable()), true);
            }
            else if (e.getPropertyName().equals(IOutline.PROP_SEARCH_FORM)) {
              getSearchFormField().setInnerForm(((IOutline) e.getSource()).getSearchForm());
            }
          }
          catch (RuntimeException ex) {
            BEANS.get(ExceptionHandler.class).handle(ex);
          }
        }
      });

      // Detail Form
      getDetailFormField().setInnerForm(m_outline.getDetailForm());
      getTableField().setTable(detachSearchFormTableControl(m_outline.getDetailTable()), true);

      // Search Form
      getSearchFormField().setInnerForm(m_outline.getSearchForm());
    }

    interceptPageChanged(oldPage, m_page);
  }

  /**
   * If the given table has a table control of type {@link SearchFormTableControl}, this table control is removed.
   * Otherwise, the form would be rendered twice (1x table control, 1x SearchFormField).
   */
  protected ITable detachSearchFormTableControl(ITable table) {
    SearchFormTableControl searchControl = table.getTableControl(SearchFormTableControl.class);
    if (searchControl != null) {
      table.removeTableControl(searchControl);
    }
    return table;
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

  /**
   * Method invoked once the Page is changed.
   *
   * @param oldPage
   *          the old {@link IPage}; might be <code>null</code>.
   * @param newInnerForm
   *          the new {@link IPage}; might be <code>null</code>.
   */
  @ConfigOperation
  protected void execPageChanged(PAGE oldPage, PAGE newPage) {
  }

  @Order(10)
  @ClassId("5eb7b330-3549-4bdf-a4e9-cc4486e44f36")
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
      return 0;
    }

    @Override
    protected boolean execCalculateVisible() {
      return getInnerForm() != null;
    }

    @Override
    protected void initConfig() {
      super.initConfig();
      addPropertyChangeListener(PROP_INNER_FORM, new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          calculateVisibleInternal();
        }
      });
    }
  }

  @Order(20)
  @ClassId("5bff61f0-9f9a-492c-ba33-cdc1407eeade")
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

    @Override
    protected boolean getConfiguredStatusVisible() {
      return false;
    }

    @Override
    protected boolean execCalculateVisible() {
      return getTable() != null;
    }

    @Override
    protected void initConfig() {
      super.initConfig();
      addPropertyChangeListener(PROP_TABLE, new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          calculateVisibleInternal();
        }
      });
    }
  }

  @Order(30)
  @ClassId("8808ad60-c941-4086-b470-72f23dd8125e")
  public class DetailFormField extends AbstractWrappedFormField<IForm> {

    @Override
    protected int getConfiguredGridW() {
      return FULL_WIDTH;
    }

    @Override
    protected boolean execCalculateVisible() {
      return getInnerForm() != null;
    }

    @Override
    protected void initConfig() {
      super.initConfig();
      addPropertyChangeListener(PROP_INNER_FORM, new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          calculateVisibleInternal();
        }
      });
    }
  }

  @Order(10)
  @ClassId("6be65846-72ec-49ec-aad2-42caa75709d3")
  public static class SimpleOutline extends AbstractOutline {

    @Override
    protected boolean getConfiguredRootNodeVisible() {
      return true;
    }
  }

  protected final void interceptPageChanged(PAGE oldPage, PAGE newPage) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    PageFieldPageChangedChain<PAGE> chain = new PageFieldPageChangedChain<>(extensions);
    chain.execPageChanged(oldPage, newPage);
  }

  protected static class LocalPageFieldExtension<T extends IPage, OWNER extends AbstractPageField<T>> extends LocalGroupBoxExtension<OWNER> implements IPageFieldExtension<T, OWNER> {

    public LocalPageFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execPageChanged(PageFieldPageChangedChain<T> chain, T oldPage, T newPage) {
      getOwner().execPageChanged(oldPage, newPage);
    }
  }

  @Override
  protected IPageFieldExtension<PAGE, ? extends AbstractPageField<PAGE>> createLocalExtension() {
    return new LocalPageFieldExtension<PAGE, AbstractPageField<PAGE>>(this);
  }
}
