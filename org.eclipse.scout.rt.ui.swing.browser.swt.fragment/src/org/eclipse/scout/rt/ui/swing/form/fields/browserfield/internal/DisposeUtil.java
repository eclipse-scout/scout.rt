package org.eclipse.scout.rt.ui.swing.form.fields.browserfield.internal;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class DisposeUtil {

  public static void closeAndDisposeSafe(Shell shell) {
    try {
      if (shell != null && !shell.isDisposed()) {
        shell.close();
        shell.dispose();
      }
    }
    catch (Exception e) {
      // nop
    }
  }

  public static void disposeSafe(Control control) {
    try {
      if (control != null && !control.isDisposed()) {
        control.dispose();
      }
    }
    catch (Exception e) {
      // nop
    }
  }

  public static void disposeSafe(Device device) {
    try {
      if (device != null && !device.isDisposed()) {
        device.dispose();
      }
    }
    catch (Exception e) {
      // nop
    }
  }

}
