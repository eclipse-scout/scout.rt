#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.persistence;

import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.scout.rt.platform.service.IService;
import org.jooq.DSLContext;

public interface IJooqService extends IService {

  /**
   * @param task
   *          The task to execute. Must not be {@code null}.
   * @return The result of the task specified.
   */
  <T> T apply(Function<DSLContext, T> task);

  /**
   * @param task
   *          The task to execute. Must not be {@code null}.
   */
  void accept(Consumer<DSLContext> task);

}
