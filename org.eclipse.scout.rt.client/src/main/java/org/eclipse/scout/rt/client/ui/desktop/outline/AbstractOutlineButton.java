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

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

/**
 * An outline button is associated with an {@link IOutline} instance, a click on the button activates the outline on the
 * desktop.
 */
@ClassId("b235fb65-6b50-4870-895a-f8a26ee41c96")
public abstract class AbstractOutlineButton extends AbstractButton {
  private IOutline m_outline;

  public AbstractOutlineButton() {
    this(true);
  }

  public AbstractOutlineButton(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * Configuration: an outline button is a toggle button.
   *
   * @return {@code IButton.DISPLAY_STYLE_TOGGLE}
   */
  @Override
  protected int getConfiguredDisplayStyle() {
    return DISPLAY_STYLE_TOGGLE;
  }

  /**
   * Configuration: an outline button is not a process button.
   *
   * @return {@code false}
   */
  @Override
  protected boolean getConfiguredProcessButton() {
    return false;
  }

  /**
   * Configures the outline associated with this outline button.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return a type token defining an outline
   * @see IOutline
   */
  @ConfigProperty(ConfigProperty.OUTLINE)
  protected Class<? extends IOutline> getConfiguredOutline() {
    return null;
  }

  /**
   * Initializes this outline button.
   * <p>
   * This implementation does the following:
   * <ul>
   * <li>find an instance of {@code IOutline} on the desktop consistent with the configured outline of this button, this
   * becomes the associated outline instance for this button
   * <li>the label for this button is taken from the outline
   * <li>a property change listener is registered with the outline such that this button can react on dynamic changes of
   * its associated outline (label, icon, visible, enabled etc.)
   * </ul>
   *
   * @throws ProcessingException
   *           if initialization fails
   */
  @Override
  protected void execInitField() {
    final IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
    for (IOutline o : desktop.getAvailableOutlines()) {
      if (o.getClass() == getConfiguredOutline()) {
        m_outline = o;
        break;
      }
    }
    if (m_outline != null) {
      setVisible(m_outline.isVisible());
      setEnabled(m_outline.isEnabled(), true, false);
      setLabel(m_outline.getTitle());
      setTooltipText(m_outline.getTitle());
      setSelected(desktop.getOutline() == m_outline);
      // add selection listener
      desktop.addDesktopListener(
          e -> {
            switch (e.getType()) {
              case DesktopEvent.TYPE_OUTLINE_CHANGED: {
                setSelected(e.getOutline() == m_outline);
                break;
              }
            }
          });
      // add change listener
      m_outline.addPropertyChangeListener(
          e -> {
            String n = e.getPropertyName();
            Object v = e.getNewValue();
            if (n.equals(IOutline.PROP_VISIBLE)) {
              setVisible((Boolean) v);
            }
            else if (n.equals(IOutline.PROP_ENABLED)) {
              setEnabled((Boolean) v, true, false);
            }
            else if (n.equals(IOutline.PROP_TITLE)) {
              setLabel((String) v);
            }
            else if (n.equals(IOutline.PROP_DEFAULT_ICON_ID)) {
              setIconId((String) v);
            }
          });
    }
  }

  /**
   * Activates the outline associated with this outline button (i.e. sets the outline as the active outline on the
   * desktop) if {@code selected} is {@code true}, does nothing otherwise.
   *
   * @param selected
   *          the state of the toggle button
   */
  @Override
  protected final void execSelectionChanged(boolean selection) {
    if (selection) {
      IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
      if (desktop != null) {
        // activate outline
        desktop.activateOutline(m_outline);
      }
    }
  }

}
