package org.eclipse.scout.rt.platform.exception;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

/**
 * Represents errors that occur during application execution which can have associated context information.
 *
 * @since 6.1
 */
public interface IThrowableWithContextInfo {

  /**
   * Returns the context info associated with this throwable.
   */
  List<String> getContextInfos();

  /**
   * Associates this throwable with some contextual information, which help to diagnose the root cause of the error, and
   * to provide some information about the current calling context. If the same 'name-value' pair is already associated,
   * or the <code>name</code> or <code>key</code> is <code>null</code> or empty, this method call does nothing.
   * <p>
   * Optionally, <em>formatting anchors</em> in the form of {} pairs can be used in the value, which will be replaced by
   * the respective argument.
   * <p>
   * Internally, {@link MessageFormatter} is used to provide substitution functionality. Hence, The format is the very
   * same as if using {@link Logger SLF4j Logger}.
   *
   * @param name
   *          the name of the context info.
   * @param value
   *          the value with support for <em>formatting anchors</em> in the form of {} pairs.
   * @param valueArgs
   *          optional arguments to substitute <em>formatting anchors</em> in the value.
   */
  IThrowableWithContextInfo withContextInfo(String name, Object value, Object... valueArgs);

  /**
   * Returns whether this throwable was already consumed.
   */
  boolean isConsumed();

  /**
   * Marks this throwable as <em>consumed</em>.
   */
  void consume();

  /**
   * Returns the bare message without context messages. This method should be used to show the error to the user.
   */
  String getDisplayMessage();

  String getMessage();

}
