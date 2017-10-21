package bachelorThesisPlayground.readers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReader {
	Iterator<Row> rowIterator;
	XSSFWorkbook myWorkBook;
	FileInputStream fis;
	public void init(String file) throws IOException{
		File myFile = new File(file);
        fis = new FileInputStream(myFile);

        // Finds the workbook instance for XLSX file
        myWorkBook = new XSSFWorkbook (fis);
       
        // Return first sheet from the XLSX workbook
        XSSFSheet mySheet = myWorkBook.getSheetAt(0);
       
        // Get iterator to all the rows in current sheet
        rowIterator = mySheet.iterator();           
    }
	
	public void close(){
		try {
			myWorkBook.close();
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public boolean hasNext(){
		return rowIterator.hasNext();
	}
	
	public List<String> next(){
		Row row = rowIterator.next();

        List<String> result = new ArrayList<String>();
        
        int lastColumn = row.getLastCellNum();

        for (int cn = 0; cn < lastColumn; cn++) {
           Cell cell = row.getCell(cn, Row.RETURN_BLANK_AS_NULL);
           if (cell == null) {
        	   result.add(null);
           } else {
        	   switch (cell.getCellType()) {
			   		case Cell.CELL_TYPE_STRING:
			   			result.add(cell.getStringCellValue());
			   			break;
			   		case Cell.CELL_TYPE_NUMERIC:
			   			result.add("" + cell.getNumericCellValue());
			   			break;
			   		case Cell.CELL_TYPE_BOOLEAN:
			   			result.add("" + cell.getBooleanCellValue());
			   			break;
			   		default : result.add(null);	     
		       }
           }
        }
        
	    return result;
	}
}
