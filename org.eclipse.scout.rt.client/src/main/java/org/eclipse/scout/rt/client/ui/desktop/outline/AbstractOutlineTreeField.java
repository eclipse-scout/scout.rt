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

import org.eclipse.scout.rt.client.extension.ui.desktop.outline.IOutlineTreeFieldExtension;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.AbstractTreeField;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("b04fef69-6bd2-409f-b37e-ea9e941d362d")
public abstract class AbstractOutlineTreeField extends AbstractTreeField implements IOutlineTreeField {
  private DesktopListener m_desktopListener;
  private PropertyChangeListener m_treePropertyListener;

  public AbstractOutlineTreeField() {
    this(true);
  }

  public AbstractOutlineTreeField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected boolean getConfiguredLabelVisible() {
    return false;
  }

  @Override
  protected void execInitField() {
    m_desktopListener = e -> {
      switch (e.getType()) {
        case DesktopEvent.TYPE_OUTLINE_CHANGED: {
          installOutline(e.getOutline());
          break;
        }
      }
    };
    m_treePropertyListener = e -> {
      if (e.getPropertyName().equals(ITree.PROP_TITLE)) {
        setLabel((String) e.getNewValue());
      }
    };
    //
    IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
    desktop.addDesktopListener(m_desktopListener);
    installOutline(desktop.getOutline());
  }

  @Override
  protected void execDisposeField() {
    ClientSessionProvider.currentSession().getDesktop().removeDesktopListener(m_desktopListener);
    m_desktopListener = null;
  }

  private void installOutline(IOutline outline) {
    if (getTree() == outline) {
      return;
    }
    //
    if (getTree() != null) {
      getTree().removePropertyChangeListener(m_treePropertyListener);
      setLabel(null);
    }
    setTree(outline, true);
    if (getTree() != null) {
      getTree().addPropertyChangeListener(m_treePropertyListener);
      setLabel(getTree().getTitle());
    }
  }

  protected static class LocalOutlineTreeFieldExtension<OWNER extends AbstractOutlineTreeField> extends LocalTreeFieldExtension<OWNER> implements IOutlineTreeFieldExtension<OWNER> {

    public LocalOutlineTreeFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IOutlineTreeFieldExtension<? extends AbstractOutlineTreeField> createLocalExtension() {
    return new LocalOutlineTreeFieldExtension<>(this);
  }
}
