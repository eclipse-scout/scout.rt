package org.eclipse.scout.rt.client.ui;

import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;

/**
 * @since 7.1
 */
public interface IWidget extends IPropertyObserver, IStyleable {

  String PROP_INIT_CONFIG_DONE = "initConfigDone";
  String PROP_POST_INIT_CONFIG_DONE = "postInitConfigDone";
  String PROP_INIT_DONE = "initDone";
  String PROP_DISPOSE_DONE = "disposeDone";

  /**
   * @return true if {@link #initConfig()} has been called, false if not
   */
  boolean isInitConfigDone();

  void postInitConfig();

  /**
   * @return true if {@link #postInitConfig()} has been called, false if not
   */
  boolean isPostInitConfigDone();

  /**
   * Init is supposed to be called after the widget has been created. This is usually done by the framework itself. E.g.
   * if the widgets are used on a form, the form will initialize all the widgets on form startup.
   * <p>
   * After the execution of this method, {@link #isInitDone()} will return true. A repeated execution of {@link #init()}
   * will do nothing unless initDone would be set to false again which is done by {@link #reinit()}.
   */
  void init();

  /**
   * @return true if {@link #init()} has been called, false if not
   */
  boolean isInitDone();

  /**
   * Re initializes the widget by setting initDone to false and calling {@link #init()} again.
   */
  void reinit();

  /**
   * Dispose needs to be called if the widget should not be used anymore in order to release any bound resources. This
   * is usually done by the framework itself. E.g. if the widgets are used on a form, the form will dispose all the
   * widgets when the form closes.
   * <p>
   * After the execution of this method, {@link #isDisposeDone()} will return true. A repeated execution of
   * {@link #dispose()} will do nothing.
   */
  void dispose();

  /**
   * @return true if {@link #dispose()} has been called, false if not
   */
  boolean isDisposeDone();
}
