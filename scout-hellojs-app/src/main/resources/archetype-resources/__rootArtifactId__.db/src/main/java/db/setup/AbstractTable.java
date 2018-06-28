#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.db.setup;

import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.jooq.impl.DSL;

import ${package}.db.setup.table.IGenerateTable;
import ${package}.persistence.PersistenceProperties.SchemaProperty;

public abstract class AbstractTable extends AbstractDatabaseObject implements IGenerateTable {

  public static final String PRIMARY_KEY_POSTFIX = "_pk";

  @Override
  public String getSchemaName() {
    return CONFIG.getPropertyValue(SchemaProperty.class);
  }

  @Override
  public void create() {
    getLogger().info("SQL-DEV create table: {}", getName());
    super.create();
  }

  @Override
  public void drop() {
    getLogger().info("SQL-DEV drop table: {}", getName());

    boolean exists = getContext().fetchExists(DSL.table(DSL.name(getName())));

    if (exists) {
      getContext().dropTable(getName()).execute();
    }
  }

  protected String getPKName() {
    return getName() + PRIMARY_KEY_POSTFIX;
  }

  @Override
  public String getCreateSQL() {
    return postProcessForSchema(createSQLInternal());
  }

  private String postProcessForSchema(String sql) {
    if (!StringUtility.hasText(sql)) {
      return null;
    }

    String schema = getSchemaName();
    if (!StringUtility.hasText(schema)) {
      return sql;
    }

    String sqlLC = sql.toLowerCase();
    if (!sqlLC.startsWith("create table")) {
      return sql;
    }

    if (!sqlLC.startsWith("create table \"" + schema + "\".")) {
      return String.format("create table \"%s\".%s", schema, sql.substring(13));
    }

    return sql;
  }
}
