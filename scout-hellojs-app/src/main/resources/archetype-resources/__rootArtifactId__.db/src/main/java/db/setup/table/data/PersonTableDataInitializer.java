#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.db.setup.table.data;

import java.util.UUID;

import org.jooq.DSLContext;

import ${package}.persistence.tables.records.PersonRecord;

public class PersonTableDataInitializer implements IDataInitializer {

  @Override
  public void initialize(DSLContext context) {
  }

  @Override
  public void addSamples(DSLContext context) {
    context.executeInsert(new PersonRecord(UUID.randomUUID().toString(), "Alice", "Miller"));
    context.executeInsert(new PersonRecord(UUID.randomUUID().toString(), "Bob", "Smith"));
  }
}
