package az.inci.department_jobs;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RowData
{
    private int parentRowId;
    private boolean isParent;
    private boolean isChild;
    private String className;
    private String tag;
    private List<CellData> cellDataList;

    public void addCellData(CellData cellData)
    {
        cellDataList.add(cellData);
    }

    public static RowData childCopyOf(RowData rowData, int parentRowId)
    {
        RowData copy = new RowData();
        copy.parentRowId = parentRowId;
        copy.isParent = false;
        copy.isChild = true;
        copy.className = rowData.getTag();
        copy.cellDataList = new ArrayList<>(rowData.getCellDataList());

        return copy;
    }
}
