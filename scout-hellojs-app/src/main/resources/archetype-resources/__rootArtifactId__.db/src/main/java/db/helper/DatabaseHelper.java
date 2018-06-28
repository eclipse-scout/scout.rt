#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.db.helper;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.jooq.DSLContext;
import org.jooq.Named;

import ${package}.persistence.PersistenceProperties.SchemaProperty;

@Bean
public class DatabaseHelper {

  public Set<String> getSchemaNames(DSLContext context) {
    return context
        .meta()
        .getSchemas()
        .stream()
        .map(Named::getName)
        .collect(Collectors.toSet());
  }

  public Set<String> getTableNames(DSLContext context) {
    return context
        .meta()
        .getTables()
        .stream()
        .filter(table -> StringUtility.equalsIgnoreCase(table.getSchema().getName(), CONFIG.getPropertyValue(SchemaProperty.class)))
        .map(Named::getName)
        .collect(Collectors.toSet());
  }
}
