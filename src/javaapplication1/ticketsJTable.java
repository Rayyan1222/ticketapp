package javaapplication1;
//import all necessary fields//
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;
//Create class ticketsjTable// 
public class ticketsJTable {
	//Method to build DefaultTableModel from ResultSet
	public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
		//MetaData about ResultSet//
		ResultSetMetaData metaData = rs.getMetaData();

		// names of columns
		Vector<String> columnNames = new Vector<String>();
		int columnCount = metaData.getColumnCount();
		for (int column = 1; column <= columnCount; column++) {
			columnNames.add(metaData.getColumnName(column));
		}

		// data of the table
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		while (rs.next()) {
			Vector<Object> vector = new Vector<Object>();
			for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
				vector.add(rs.getObject(columnIndex));
			}
			data.add(vector);
		}
		// return data/col.names for JTable
		return new DefaultTableModel(data, columnNames); 

	}

}
