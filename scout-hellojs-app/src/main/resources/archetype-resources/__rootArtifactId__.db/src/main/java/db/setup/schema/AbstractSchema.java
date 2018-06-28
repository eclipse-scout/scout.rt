#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.db.setup.schema;

import ${package}.db.setup.AbstractDatabaseObject;

public abstract class AbstractSchema extends AbstractDatabaseObject implements IDatabaseSchema {

  @Override
  public void create() {
    getLogger().info("SQL-DEV create schema: {}", getName());
    super.create();
  }

  @Override
  public void drop() {
    getContext()
        .dropSchemaIfExists(getName())
        //        .cascade()
        .execute();
  }
}
