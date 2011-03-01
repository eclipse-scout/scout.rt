package org.eclipse.scout.rt.ui.swt;

import org.eclipse.swt.widgets.Composite;

public class DefaultValidateRoot implements IValidateRoot {
  private final Composite m_root;

  public DefaultValidateRoot(Composite root) {
    m_root = root;
  }

  public void validate() {
    if (m_root != null && !m_root.isDisposed()) {
      m_root.layout(true, true);
    }
  }

  public Composite getComposite() {
    return m_root;
  }

}
