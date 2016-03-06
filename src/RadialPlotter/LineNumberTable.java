package RadialPlotter;

import javax.swing.*;
import javax.swing.table.*;
 
public class LineNumberTable extends JTable {
 
    public LineNumberTable(JTable table) {
        super();
        mainTable = table;
        setAutoCreateColumnsFromModel( false );
        setModel( mainTable.getModel() );
        setSelectionModel( mainTable.getSelectionModel() );
        setAutoscrolls( false );

        addColumn( new TableColumn() );

        getColumnModel().getColumn(0).setCellRenderer(
                mainTable.getTableHeader().getDefaultRenderer() );
        getColumnModel().getColumn(0).setPreferredWidth(50);
        setPreferredScrollableViewportSize(getPreferredSize());
    }

    public JTable getMainTable() {
        return mainTable;
    }
        
    @Override
    public boolean isCellEditable(int row, int column)
    {
            return false;
    }
 
    @Override
    public Object getValueAt(int row, int column)
    {
            return new Integer(row + 1);
    }
 
    @Override
    public int getRowHeight(int row)
    {
            return mainTable.getRowHeight();
    }

    protected JTable mainTable;
    
}