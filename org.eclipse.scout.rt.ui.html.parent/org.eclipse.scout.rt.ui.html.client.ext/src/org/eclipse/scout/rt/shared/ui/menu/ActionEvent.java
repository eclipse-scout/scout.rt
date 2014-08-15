package org.eclipse.scout.rt.shared.ui.menu;

import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.action.IAction;

public class ActionEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_PERFORMED = 1;

  private final int m_type;

  public ActionEvent(IAction source, int type) {
    super(source);
    m_type = type;
  }

  public IAction getAction() {
    return (IAction) super.getSource();
  }

  public int getType() {
    return m_type;
  }
}
