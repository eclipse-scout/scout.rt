package org.eclipse.scout.rt.ui.swt;

import org.eclipse.swt.widgets.Composite;

public interface IValidateRoot {

  String VALIDATE_ROOT_DATA = "LayoutValidateManager.validateRoot";

  void validate();

  Composite getComposite();

}
