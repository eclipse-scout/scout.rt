package org.eclipse.scout.rt.shared.ui;

import java.io.Serializable;

@FunctionalInterface
public interface IUiSystem extends Serializable {
  String getIdentifier();
}
