#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.db.setup.table;

import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ${package}.db.setup.AbstractTable;

public class PersonTable extends AbstractTable {

  public static final String TABLE = "person";

  public static final String PERSON_ID = "person_id";
  public static final String LAST_NAME = "last_name";
  public static final String FIRST_NAME = "first_name";

  @Override
  public String createSQLInternal() {
    return getContext()
        .createTable(getName())
        .column(PERSON_ID, TYPE_ID)
        .column(FIRST_NAME, TYPE_STRING_S_OPTIONAL)
        .column(LAST_NAME, TYPE_STRING_S)
        .constraints(
            DSL.constraint(getPKName()).primaryKey(PERSON_ID))
        .getSQL();
  }

  @Override
  public String getName() {
    return TABLE;
  }

  @Override
  public Logger getLogger() {
    return LoggerFactory.getLogger(PersonTable.class);
  }
}
