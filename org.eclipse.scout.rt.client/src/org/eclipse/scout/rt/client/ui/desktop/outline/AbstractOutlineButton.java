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
package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;

public abstract class AbstractOutlineButton extends AbstractButton {
  private IOutline m_outline;

  public AbstractOutlineButton() {
  }

  @ConfigPropertyValue("DISPLAY_STYLE_TOGGLE")
  @Override
  protected int getConfiguredDisplayStyle() {
    return DISPLAY_STYLE_TOGGLE;
  }

  @Override
  protected boolean getConfiguredProcessButton() {
    return false;
  }

  @ConfigProperty(ConfigProperty.OUTLINE)
  @ConfigPropertyValue("null")
  protected Class<? extends IOutline> getConfiguredOutline() {
    return null;
  }

  @Override
  protected void execInitField() throws ProcessingException {
    final IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
    for (IOutline o : desktop.getAvailableOutlines()) {
      if (o.getClass() == getConfiguredOutline()) {
        m_outline = o;
        break;
      }
    }
    if (m_outline != null) {
      setVisible(m_outline.isVisible());
      setEnabled(m_outline.isEnabled());
      setLabel(m_outline.getTitle());
      setTooltipText(m_outline.getTitle());
      setIconId(m_outline.getIconId());
      setSelected(desktop.getOutline() == m_outline);
      // add selection listener
      desktop.addDesktopListener(
          new DesktopListener() {
            @Override
            public void desktopChanged(DesktopEvent e) {
              switch (e.getType()) {
                case DesktopEvent.TYPE_OUTLINE_CHANGED: {
                  setSelected(e.getOutline() == m_outline);
                  break;
                }
              }
            }
          }
          );
      // add change listener
      m_outline.addPropertyChangeListener(
          new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
              String n = e.getPropertyName();
              Object v = e.getNewValue();
              if (n.equals(IOutline.PROP_VISIBLE)) {
                setVisible((Boolean) v);
              }
              else if (n.equals(IOutline.PROP_ENABLED)) {
                setEnabled((Boolean) v);
              }
              else if (n.equals(IOutline.PROP_TITLE)) {
                setLabel((String) v);
              }
              else if (n.equals(IOutline.PROP_ICON_ID)) {
                setIconId((String) v);
              }
            }
          }
          );
    }
  }

  @Override
  protected final void execToggleAction(boolean selected) {
    if (selected) {
      IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
      if (desktop != null) {
        // activate outline
        desktop.setOutline(m_outline);
      }
    }
  }

}
