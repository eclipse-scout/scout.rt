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
package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.desktop.outline.IOutlineTableFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineTableFieldChains.OutlineTableFieldTableTitleChangedChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * This field can be used to place the default outline table (which changes on every outline tree change) inside a
 * custom form.<br>
 * The default outline table form makes use of this field.
 */
@ClassId("f0770b37-8113-4476-a486-73ceabfdef90")
public abstract class AbstractOutlineTableField extends AbstractTableField<ITable> implements IOutlineTableField {
  private DesktopListener m_desktopListener;
  private PropertyChangeListener m_tablePropertyListener;

  public AbstractOutlineTableField() {
    this(true);
  }

  public AbstractOutlineTableField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected boolean getConfiguredLabelVisible() {
    return false;
  }

  /**
   * called whenever the table title changed
   */
  @ConfigOperation
  @Order(1000)
  protected void execTableTitleChanged() {
    if (getTable() != null) {
      setLabel(getTable().getTitle());
    }
    else {
      setLabel(null);
    }
  }

  @Override
  protected void execInitField() {
    m_tablePropertyListener = e -> {
      if (e.getPropertyName().equals(ITable.PROP_TITLE)) {
        interceptTableTitleChanged();
      }
    };
  }

  @Override
  protected void execDisposeField() {
    super.execDisposeField();
    ClientSessionProvider.currentSession().getDesktop().removeDesktopListener(m_desktopListener);
    m_desktopListener = null;
  }

  public void installTable(ITable table) {
    if (getTable() == table) {
      return;
    }
    //
    if (getTable() != null) {
      getTable().removePropertyChangeListener(m_tablePropertyListener);
    }
    setTable(table, true);
    if (getTable() != null) {
      getTable().addPropertyChangeListener(m_tablePropertyListener);
    }
    interceptTableTitleChanged();
  }

  protected final void interceptTableTitleChanged() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    OutlineTableFieldTableTitleChangedChain chain = new OutlineTableFieldTableTitleChangedChain(extensions);
    chain.execTableTitleChanged();
  }

  protected static class LocalOutlineTableFieldExtension<OWNER extends AbstractOutlineTableField> extends LocalTableFieldExtension<ITable, OWNER> implements IOutlineTableFieldExtension<OWNER> {

    public LocalOutlineTableFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execTableTitleChanged(OutlineTableFieldTableTitleChangedChain chain) {
      getOwner().execTableTitleChanged();
    }
  }

  @Override
  protected IOutlineTableFieldExtension<? extends AbstractOutlineTableField> createLocalExtension() {
    return new LocalOutlineTableFieldExtension<>(this);
  }
}
