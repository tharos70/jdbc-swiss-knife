package org.tharos.jdbc.swissknife.dto;

import java.util.ArrayList;
import java.util.List;

public class Table {

  private String name;
  private String sequenceName;
  private List<Column> columnList = new ArrayList<Column>();

  public String getName() {
    return name;
  }

  public String getSequenceName() {
    return sequenceName;
  }

  public void setSequenceName(String sequenceName) {
    this.sequenceName = sequenceName;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Column> getColumnList() {
    return columnList;
  }

  public void setColumnList(List<Column> columnList) {
    this.columnList = columnList;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("TABLE [" + getName() + "]\n");
    sb.append("\tCOLUMNS:\n");
    int i = 0;
    for (Column column : getColumnList()) {
      i++;
      sb.append(i + ".");
      sb.append(column.toString());
    }
    return sb.toString();
  }
}
