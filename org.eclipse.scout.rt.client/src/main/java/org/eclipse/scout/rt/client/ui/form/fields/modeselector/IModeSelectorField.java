/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.client.ui.form.fields.modeselector;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.mode.IMode;

public interface IModeSelectorField<T> extends IValueField<T> {

  String PROP_MODES = "modes";

  List<IMode<T>> getModes();

  IMode<T> getModeFor(T value);
}
