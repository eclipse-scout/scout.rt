package org.eclipse.scout.rt.ui.rap.mobile.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;

/**
 * A {@link IActionNode} which wraps a {@link IButton}. <br/> {@link PropertyChangeEvent}s fired by the button are delegated
 * to the action
 * 
 * @since 3.8.0
 */
public class ButtonWrappingAction extends AbstractMenu {
  private IButton m_wrappedButton;
  private P_ButtonPropertyChangeListener m_buttonPropertyChangeListener;

  public ButtonWrappingAction(IButton wrappedButton) {
    super(false);

    m_wrappedButton = wrappedButton;

    m_buttonPropertyChangeListener = new P_ButtonPropertyChangeListener();
    m_wrappedButton.addPropertyChangeListener(m_buttonPropertyChangeListener);

    callInitializer();
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    setVisible(getWrappedButton().isVisible());
    setVisibleGranted(getWrappedButton().isVisibleGranted());
    setEnabled(getWrappedButton().isEnabled());
    setEnabledGranted(getWrappedButton().isEnabledGranted());
    setIconId(getWrappedButton().getIconId());
    setText(getWrappedButton().getLabel());
    setTooltipText(getWrappedButton().getTooltipText());
    setToggleAction(getWrappedButton().getDisplayStyle() == IButton.DISPLAY_STYLE_TOGGLE);
    setSelected(getWrappedButton().isSelected());
  }

  @Override
  protected void execAction() throws ProcessingException {
    getWrappedButton().doClick();
  }

  @Override
  protected void execToggleAction(boolean selected) throws ProcessingException {
    getWrappedButton().setSelected(selected);
  }

  private void handleButtonPropertyChange(String name, Object newValue) {
    if (name.equals(IFormField.PROP_ENABLED)) {
      setEnabled(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IFormField.PROP_LABEL)) {
      setText((String) newValue);
    }
    else if (name.equals(IFormField.PROP_TOOLTIP_TEXT)) {
      setTooltipText((String) newValue);
    }
    else if (name.equals(IFormField.PROP_VISIBLE)) {
      setVisible(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IButton.PROP_ICON_ID)) {
      setIconId((String) newValue);
    }
    else if (name.equals(IButton.PROP_SELECTED)) {
      setSelected(((Boolean) newValue).booleanValue());
    }
  }

  public IButton getWrappedButton() {
    return m_wrappedButton;
  }

  private class P_ButtonPropertyChangeListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      handleButtonPropertyChange(evt.getPropertyName(), evt.getNewValue());
    }

  }

}
