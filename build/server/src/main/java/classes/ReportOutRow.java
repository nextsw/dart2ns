package classes;

import d3e.core.SchemaConstants;
import java.util.List;
import store.D3EPersistanceList;
import store.DBObject;

public class ReportOutRow extends DBObject {
  public static final int _KEY = 0;
  public static final int _PARENTKEY = 1;
  public static final int _CELLS = 2;
  public static final int _GROUPINGKEY = 3;
  private long id;
  private String key;
  private String parentKey;
  private List<ReportOutCell> cells = new D3EPersistanceList<>(this, _CELLS);
  private String groupingKey;

  public ReportOutRow() {}

  public ReportOutRow(List<ReportOutCell> cells, String groupingKey, String key, String parentKey) {
    this.cells.addAll(cells);
    this.groupingKey = groupingKey;
    this.key = key;
    this.parentKey = parentKey;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    fieldChanged(_KEY, this.key, key);
    this.key = key;
  }

  public String getParentKey() {
    return parentKey;
  }

  public void setParentKey(String parentKey) {
    fieldChanged(_PARENTKEY, this.parentKey, parentKey);
    this.parentKey = parentKey;
  }

  public List<ReportOutCell> getCells() {
    return cells;
  }

  public void setCells(List<ReportOutCell> cells) {
    ((D3EPersistanceList<ReportOutCell>) this.cells).setAll(cells);
  }

  public void addToCells(ReportOutCell val, long index) {
    if (index == -1) {
      this.cells.add(val);
    } else {
      this.cells.add(((int) index), val);
    }
  }

  public void removeFromCells(ReportOutCell val) {
    this.cells.remove(val);
  }

  public String getGroupingKey() {
    return groupingKey;
  }

  public void setGroupingKey(String groupingKey) {
    fieldChanged(_GROUPINGKEY, this.groupingKey, groupingKey);
    this.groupingKey = groupingKey;
  }

  @Override
  public int _typeIdx() {
    return SchemaConstants.ReportOutRow;
  }

  @Override
  public String _type() {
    return "ReportOutRow";
  }

  @Override
  public int _fieldsCount() {
    return 4;
  }

  public void _convertToObjectRef() {
    this.cells.forEach((a) -> a._convertToObjectRef());
  }
}
