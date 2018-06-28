#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.db;

import java.util.function.Consumer;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.jooq.DSLContext;

import ${package}.persistence.PersistenceProperties.DialectProperty;
import ${package}.persistence.PersistenceProperties.JdbcMappingNameProperty;
import ${package}.persistence.PersistenceProperties.PasswordProperty;
import ${package}.persistence.PersistenceProperties.UsernameProperty;

public class Environment {

  public void runWithConfig(Consumer<DSLContext> task) {
    Assertions.assertNotNull(task);
    BEANS.get(JooqEnvironmentService.class).runInJooq(task,
        CONFIG.getPropertyValue(JdbcMappingNameProperty.class),
        CONFIG.getPropertyValue(DialectProperty.class),
        CONFIG.getPropertyValue(UsernameProperty.class),
        CONFIG.getPropertyValue(PasswordProperty.class));
  }
}
