#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.db.helper;

import java.util.Set;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.jooq.DSLContext;
import org.jooq.Named;

import ${package}.persistence.PersistenceProperties.SchemaProperty;

import static java.util.stream.Collectors.toSet;

@Bean
public class DatabaseHelper {

  public Set<String> getSchemaNames(DSLContext context) {
    return context
      .meta()
      .getSchemas()
      .stream()
      .map(Named::getName)
      .collect(toSet());
  }

  public Set<String> getTableNames(DSLContext context) {
    String schema = CONFIG.getPropertyValue(SchemaProperty.class);
    return context
      .meta()
      .filterSchemas(s -> s.getName().equals(schema))
      .getTables()
      .stream()
      .map(Named::getName)
      .collect(toSet());
  }
}
