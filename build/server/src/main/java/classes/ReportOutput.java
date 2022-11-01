package classes;

import d3e.core.SchemaConstants;
import java.util.List;
import store.D3EPersistanceList;
import store.DBObject;

public class ReportOutput extends DBObject {
  public static final int _OPTIONS = 0;
  public static final int _COLUMNS = 1;
  public static final int _SUBCOLUMNS = 2;
  public static final int _ATTRIBUTES = 3;
  public static final int _ROWS = 4;
  private long id;
  private List<ReportOutOption> options = new D3EPersistanceList<>(this, _OPTIONS);
  private List<ReportOutColumn> columns = new D3EPersistanceList<>(this, _COLUMNS);
  private List<ReportOutColumn> subColumns = new D3EPersistanceList<>(this, _SUBCOLUMNS);
  private List<ReportOutAttribute> attributes = new D3EPersistanceList<>(this, _ATTRIBUTES);
  private List<ReportOutRow> rows = new D3EPersistanceList<>(this, _ROWS);

  public ReportOutput() {}

  public ReportOutput(
      List<ReportOutAttribute> attributes,
      List<ReportOutColumn> columns,
      List<ReportOutOption> options,
      List<ReportOutRow> rows,
      List<ReportOutColumn> subColumns) {
    this.attributes.addAll(attributes);
    this.columns.addAll(columns);
    this.options.addAll(options);
    this.rows.addAll(rows);
    this.subColumns.addAll(subColumns);
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public List<ReportOutOption> getOptions() {
    return options;
  }

  public void setOptions(List<ReportOutOption> options) {
    ((D3EPersistanceList<ReportOutOption>) this.options).setAll(options);
  }

  public void addToOptions(ReportOutOption val, long index) {
    if (index == -1) {
      this.options.add(val);
    } else {
      this.options.add(((int) index), val);
    }
  }

  public void removeFromOptions(ReportOutOption val) {
    this.options.remove(val);
  }

  public List<ReportOutColumn> getColumns() {
    return columns;
  }

  public void setColumns(List<ReportOutColumn> columns) {
    ((D3EPersistanceList<ReportOutColumn>) this.columns).setAll(columns);
  }

  public void addToColumns(ReportOutColumn val, long index) {
    if (index == -1) {
      this.columns.add(val);
    } else {
      this.columns.add(((int) index), val);
    }
  }

  public void removeFromColumns(ReportOutColumn val) {
    this.columns.remove(val);
  }

  public List<ReportOutColumn> getSubColumns() {
    return subColumns;
  }

  public void setSubColumns(List<ReportOutColumn> subColumns) {
    ((D3EPersistanceList<ReportOutColumn>) this.subColumns).setAll(subColumns);
  }

  public void addToSubColumns(ReportOutColumn val, long index) {
    if (index == -1) {
      this.subColumns.add(val);
    } else {
      this.subColumns.add(((int) index), val);
    }
  }

  public void removeFromSubColumns(ReportOutColumn val) {
    this.subColumns.remove(val);
  }

  public List<ReportOutAttribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<ReportOutAttribute> attributes) {
    ((D3EPersistanceList<ReportOutAttribute>) this.attributes).setAll(attributes);
  }

  public void addToAttributes(ReportOutAttribute val, long index) {
    if (index == -1) {
      this.attributes.add(val);
    } else {
      this.attributes.add(((int) index), val);
    }
  }

  public void removeFromAttributes(ReportOutAttribute val) {
    this.attributes.remove(val);
  }

  public List<ReportOutRow> getRows() {
    return rows;
  }

  public void setRows(List<ReportOutRow> rows) {
    ((D3EPersistanceList<ReportOutRow>) this.rows).setAll(rows);
  }

  public void addToRows(ReportOutRow val, long index) {
    if (index == -1) {
      this.rows.add(val);
    } else {
      this.rows.add(((int) index), val);
    }
  }

  public void removeFromRows(ReportOutRow val) {
    this.rows.remove(val);
  }

  @Override
  public int _typeIdx() {
    return SchemaConstants.ReportOutput;
  }

  @Override
  public String _type() {
    return "ReportOutput";
  }

  @Override
  public int _fieldsCount() {
    return 5;
  }

  public void _convertToObjectRef() {
    this.options.forEach((a) -> a._convertToObjectRef());
    this.columns.forEach((a) -> a._convertToObjectRef());
    this.subColumns.forEach((a) -> a._convertToObjectRef());
    this.attributes.forEach((a) -> a._convertToObjectRef());
    this.rows.forEach((a) -> a._convertToObjectRef());
  }
}
