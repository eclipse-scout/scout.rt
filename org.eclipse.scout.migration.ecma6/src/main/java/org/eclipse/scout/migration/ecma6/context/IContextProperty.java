package org.eclipse.scout.migration.ecma6.context;

import org.eclipse.scout.rt.platform.ApplicationScoped;

@ApplicationScoped
public interface IContextProperty<VALUE> {

  void setup(Context context);

  VALUE getValue();
}
