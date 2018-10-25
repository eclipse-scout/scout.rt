/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.client.ui.form.fields.mode;

import org.eclipse.scout.rt.client.ui.action.IAction;

public interface IMode<T> extends IAction {

  String PROP_REF = "ref";

  T getRef();

  void setRef(T value);
}
