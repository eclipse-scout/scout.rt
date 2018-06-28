#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.db.setup;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.jooq.DSLContext;
import org.slf4j.Logger;

@ApplicationScoped
public interface IDatabaseObject {

	void setContext(DSLContext context);
	DSLContext getContext();

	String getName();
	String getCreateSQL();

	void create();
	void drop();

	Logger getLogger();
}
