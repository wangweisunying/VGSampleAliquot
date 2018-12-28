/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package samplecolelctiondatarenew;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.DataBaseCon;
import model.ExcelOperation;
import model.LXDataBaseCon;
import model.V7DataBaseCon;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 *
 * @author Wei Wang
 */
public class SampleColelctionDataRenew {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException, IOException {
//        dataMigrate();
        List<Integer> julienList = getJulien("C:\\Users\\Wei Wang\\Desktop\\Test_TimeLine_Review\\test.xlsx");
        assignJulien(julienList);
        
        
    }
    
    
    private static void assignJulien(List<Integer> list) throws SQLException{
        DataBaseCon db = new V7DataBaseCon();
        String preSql = "SELECT well_plate_id , row , col FROM vg_test_tracking.vg_well_info order by str_to_date(substring_index(substring_index(well_plate_id,'_',2),'_',-1) , '%Y%M') desc , substring_index(well_plate_id,'_',-1) desc  , row desc ,col desc limit 1;";
        String wellPlateId = "";
        ResultSet rs = db.read(preSql);
        String lastRow = "";
        int lastCol = 0;
        while(rs.next()){
            wellPlateId = rs.getString(1);
            lastRow = rs.getString(2);
            lastCol = rs.getInt(3);
        }
        System.out.println(wellPlateId);
       
        
        
        int count = wellPlateId.equals("") ? -1 : ((lastRow.charAt(0) - 65) * 12 + lastCol - 1);
        wellPlateId = wellPlateId.equals("") ? "IGE_2018December_1" : wellPlateId;
        String[] arr = wellPlateId.split("_");
        int plateNum = Integer.parseInt(arr[2]);
        for(int julienBacode : list){
            ++count;
            int num = plateNum + count / 96;
            int curCount =  count % 96;
            String row = "" +  (char)(65 + curCount / 12);
            int col = curCount % 12 + 1;
            String sql = "insert into vg_test_tracking.vg_well_info values("+ julienBacode +" , concat('"+arr[0]+"_', date_format(now(),'%Y%M'),'_"+ num +"') , '" +
            row +"' , "+ col +");";
            db.write(sql);
            System.out.println(sql);
        }
        db.close();
    }
    
    private static List<Integer> getJulien(String path) throws IOException{
        List<Integer> list = new ArrayList();
        Workbook wb = ExcelOperation.getReadConnection(path, ExcelOperation.ExcelType.XLSX);
        Sheet sheet = wb.getSheetAt(0);
        int row = 0;
        while(sheet.getRow(row)!= null){
            list.add((int) sheet.getRow(row++).getCell(0).getNumericCellValue());
        }
        return list;
        
    }
    
    
    
    private static void dataMigrate() throws SQLException{
        DataBaseCon dbLX = new LXDataBaseCon();
        DataBaseCon dbV7 = new V7DataBaseCon();
        dbV7.write("truncate vg_test_tracking.sample_collection_data;");
        
        
        String sql = "SELECT D.sample_barcode , sample_collection_time , sample_type ,  D.rack_type ,D.Rack_dimension ,D.Rack_id , `row` ,col \n" +
"FROM vibrant_america_information.sample_data AS A \n" +
"JOIN vibrant_america_information.prod_sample_storage AS D ON (D.sample_id=A.sample_id AND (D.rack_type='Serum' OR D.rack_type ='Duplicate')) where julien_barcode > 1801010000;";
        ResultSet rs = dbLX.read(sql);
        while(rs.next()){
            String writeSql = "insert into vg_test_tracking.sample_collection_data values("+ rs.getInt(1) + " ,'"+ rs.getString(2) +"', '"+ rs.getString(3) 
                    +"','"+ rs.getString(4) +"','"+ rs.getString(5) + "',"+ rs.getInt(6) +",'"+ rs.getString(7) +"',"+rs.getString(8)+");";
            dbV7.write(writeSql);
//            System.out.println(writeSql);
        }
        dbLX.close();
        dbV7.close();
    }
    
    
    
    
}
