package org.eclipse.scout.rt.shared.ui.menu;

import java.util.EventListener;

public interface ActionListener extends EventListener {
  void actionChanged(ActionEvent e);
}
