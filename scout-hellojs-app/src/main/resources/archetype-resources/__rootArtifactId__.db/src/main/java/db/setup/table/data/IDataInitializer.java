#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.db.setup.table.data;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.jooq.DSLContext;

@ApplicationScoped
public interface IDataInitializer {

  void initialize(DSLContext context);

  void addSamples(DSLContext context);
}
