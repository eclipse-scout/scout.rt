#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.persistence;

import org.eclipse.scout.rt.platform.service.IService;
import org.jooq.DSLContext;

public interface IJooqService extends IService {

  DSLContext getContext();

}
