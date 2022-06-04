/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.rmj.marketplace.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.constants.EditMode;

/**
 *
 * @author User
 */
public class FAQuestions {
    private final String MASTER_TABLE = "MP_Questions";
    private final GRider p_oApp;
    private final boolean p_bWithParent;
    
    private String p_sBranchCd;
    
    private int p_nEditMode;
    private int p_nTranStat;

    private String p_sMessage;
    private boolean p_bWithUI = true;

    private CachedRowSet p_oMaster;
    private LTransaction p_oListener;
   
    public FAQuestions(GRider foApp, String fsBranchCd, boolean fbWithParent){        
        p_oApp = foApp;
        p_sBranchCd = fsBranchCd;
        p_bWithParent = fbWithParent;        
                
        if (p_sBranchCd.isEmpty()) p_sBranchCd = p_oApp.getBranchCode();
        
        p_nTranStat = 0;
        p_nEditMode = EditMode.UNKNOWN;
    }
    
    public void setTranStat(int fnValue){
        p_nTranStat = fnValue;
    }
   
    public void setListener(LTransaction foValue){
        p_oListener = foValue;
    }
    
    public void setWithUI(boolean fbValue){
        p_bWithUI = fbValue;
    }
    
    public int getEditMode(){
        return p_nEditMode;
    }
    
    public boolean LoadList(String fsTransNox, boolean fbByCode) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";    
        
        String lsSQL = "";
        
        if (fbByCode)
            lsSQL = getSQ_Master()+ " HAVING sCreatedx LIKE " + SQLUtil.toSQL(fsTransNox + "%");
        else
            lsSQL = getSQ_Master() + " HAVING sCompnyNm LIKE " + SQLUtil.toSQL(fsTransNox + "%");
//        
        ResultSet loRS = p_oApp.executeQuery(getSQ_Master());
        if (MiscUtil.RecordCount(loRS) == 0){
            MiscUtil.close(loRS);
            p_sMessage = "No record found for the given criteria.";
            return false;
        }
        
        RowSetFactory factory = RowSetProvider.newFactory();
        p_oMaster = factory.createCachedRowSet();
        p_oMaster.populate(loRS);
        MiscUtil.close(loRS);
        
        return true;
    }
    
    public boolean SaveTransaction() throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        if (p_nEditMode != EditMode.ADDNEW &&
            p_nEditMode != EditMode.UPDATE){
            p_sMessage = "Invalid edit mode detected.";
            return false;
        }
        
        if (!isEntryOK()) return false;
        
        int lnCtr;
        int lnRow;
        String lsSQL;
        
            
        //set transaction number on records
        String lsTransNox = (String) getMaster("sListngID");

        String lsEntryNox = getMaster("nEntryNox").toString();
        p_oMaster.updateObject("sRepliedx", p_oApp.getUserID());
        p_oMaster.updateObject("dRepliedx", p_oApp.getServerDate().toString());
        p_oMaster.updateRow();

        lsSQL = MiscUtil.rowset2SQL(p_oMaster, 
                                    MASTER_TABLE, 
                                    "xBarCodex;xDescript;xBrandNme;xModelNme;xColorNme;xCategrNm;sImagesxx;sCompnyNm", 
                                    "sListngID = " + SQLUtil.toSQL(lsTransNox) + " AND nEntryNox = " + SQLUtil.toSQL(lsEntryNox));

        if (!lsSQL.isEmpty()){
            if (!p_bWithParent) p_oApp.beginTrans();
            if (!lsSQL.isEmpty()){
                if (p_oApp.executeQuery(lsSQL, MASTER_TABLE, p_sBranchCd, lsTransNox.substring(0, 4)) <= 0){
                    if (!p_bWithParent) p_oApp.rollbackTrans();
                    p_sMessage = p_oApp.getMessage() + ";" + p_oApp.getErrMsg();
                    return false;
                }
            }
            if (!p_bWithParent) p_oApp.commitTrans();

            p_nEditMode = EditMode.UNKNOWN;
            return true;
        } else{
            p_sMessage = "No record to save.";
            return false;
        }
        
    }
    
    public boolean ReadReview() throws SQLException{
          if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        String lsTransNox = (String) getMaster("sListngID");
        String lsEntryNox = getMaster("nEntryNox").toString();
        if (lsTransNox.trim().isEmpty()){
            p_sMessage = "No record selected.";
            return false;
        }
       if ("1".equals((String) getMaster("cReadxxxx"))){
            return false;
        }
        System.out.println("EntryNox = " + lsEntryNox);
        int lnCtr;
        int lnRow;
        String lsSQL;
        //set transaction number on records
           
            p_oMaster.updateObject("cReadxxxx", 1);
            p_oMaster.updateObject("sReadxxxx", p_oApp.getUserID());
            p_oMaster.updateObject("dReadxxxx", p_oApp.getServerDate().toString());
            p_oMaster.updateRow();
          
            lsSQL = MiscUtil.rowset2SQL(p_oMaster, 
                                        MASTER_TABLE, 
                                        "xBarCodex;xDescript;xBrandNme;xModelNme;xColorNme;xCategrNm;sImagesxx;sCompnyNm", 
                                        "sListngID = " + SQLUtil.toSQL(lsTransNox) + " AND nEntryNox = " + SQLUtil.toSQL(lsEntryNox));
            if (!lsSQL.isEmpty()){
                if (!p_bWithParent) p_oApp.beginTrans();
                if (!lsSQL.isEmpty()){
                    if (p_oApp.executeQuery(lsSQL, MASTER_TABLE, p_sBranchCd, lsTransNox.substring(0, 4)) <= 0){
                        if (!p_bWithParent) p_oApp.rollbackTrans();
                        p_sMessage = p_oApp.getMessage() + ";" + p_oApp.getErrMsg();
                        return false;
                    }
                }
                if (!p_bWithParent) p_oApp.commitTrans();

                p_nEditMode = EditMode.UPDATE;
                return true;
            } else{
                p_sMessage = "No record to save.";
                return false;
            }
        
    }
    
    public Object getMaster(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oMaster.first();
        return p_oMaster.getObject(fnIndex);
    }
    
    public Object getMaster(String fsIndex) throws SQLException{
        return getMaster(getColumnIndex(p_oMaster, fsIndex));
    }
    
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        if (p_nEditMode != EditMode.ADDNEW && p_nEditMode != EditMode.UPDATE) {
            System.out.println("Invalid Edit Mode Detected.");
            return;
        }
        
        p_oMaster.first();
        
        System.out.println("fnIndex = " + fnIndex);
        System.out.println("foValue = " + foValue);
        switch (fnIndex){
            case 8:
            case 10: 
            case 12: 
            case 15: 
                if (foValue instanceof Date){
                    p_oMaster.updateDate(fnIndex, SQLUtil.toDate((Date) foValue));
                } else
                    p_oMaster.updateDate(fnIndex, SQLUtil.toDate(p_oApp.getServerDate()));
                
                p_oMaster.updateRow();
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
            case 1: 
            case 4:
            case 5: 
            case 7:
            case 9:
            case 11:
            case 13:
                p_oMaster.updateString(fnIndex, (String) foValue);
                p_oMaster.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
            case 2: 
            case 3: 
            case 6:
            case 14:
                if (foValue instanceof Integer)
                    p_oMaster.updateInt(fnIndex, (int) foValue);
                else 
                    p_oMaster.updateInt(fnIndex, 0);
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
        }
    }
     public void setMaster(String fsIndex, Object foValue) throws SQLException{
        setMaster(getColumnIndex(p_oMaster, fsIndex), foValue);
    }
   
    public String getMessage(){
        return p_sMessage;
    }
    
    public Object getDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oMaster.absolute(fnRow);
        return p_oMaster.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, String fsIndex) throws SQLException{
        return getDetail(fnRow, getColumnIndex(p_oMaster, fsIndex));
    }
    
    public int getItemCount() throws SQLException{
        p_oMaster.last();
        return p_oMaster.getRow();
    }
    
    public void displayMasFields() throws SQLException{
        if (p_nEditMode != EditMode.ADDNEW && p_nEditMode != EditMode.UPDATE) return;
        
        int lnRow = p_oMaster.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + p_oMaster.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + p_oMaster.getMetaData().getColumnType(lnCtr));
            if (p_oMaster.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                p_oMaster.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + p_oMaster.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("----------------------------------------");
        System.out.println("END: MASTER TABLE INFO");
        System.out.println("----------------------------------------");
    }
    
    private boolean isEntryOK() throws SQLException{           
        //validate master               
        if ("".equals((String) getMaster("sListngID"))){
            p_sMessage = "No product was selected to list.";
            return false;
        }
        
        if ("".equals((String) getMaster("sReplyxxx"))){
            p_sMessage = "Product review reply is not set.";
            return false;
        }
        
        return true;
    }
    public String getSQ_Master(){
        String lsSQL = "";
        String lsCondition = "";
        String lsStat = String.valueOf(p_nTranStat);
        
        if (lsStat.length() > 1){
            for (int lnCtr = 0; lnCtr <= lsStat.length()-1; lnCtr++){
                lsSQL += ", " + SQLUtil.toSQL(Character.toString(lsStat.charAt(lnCtr)));
            }
            
            lsCondition = "a.cRecdStat IN (" + lsSQL.substring(2) + ")";
        } else 
            lsCondition = "a.cRecdStat = " + SQLUtil.toSQL(lsStat);
        
        lsSQL = "SELECT " +
                    " a.sListngID " +
                    ", a.nEntryNox " +
                    ",  a.sQuestion " +
                    ",  a.sReplyxxx " +
                    ",  a.nPriority " +
                    ",  a.sCreatedx " +
                    ",  a.dCreatedx " +
                    ",  a.sRepliedx " +
                    ",  a.dRepliedx " +
                    ",  a.cReadxxxx " +
                    ",  IFNULL(a.dReadxxxx, '') dReadxxxx" +
                    ",  a.sReadxxxx " +
                    ",  a.cRecdStat " +
                    ",  a.dTimeStmp " +
                    ",  '' xBarCodex " +
                    ",  '' xDescript " +
                    ",  '' xBrandNme " +
                    ",  '' xModelNme " +
                    ",  '' xColorNme " +
                    ",  '' xCategrNm " +
                    ",  '' sImagesxx " +
                    ",  CONCAT(b.sFrstName, ' ', b.sMiddName,' ', b.sLastName) AS sCompnyNm " +
                " FROM " + MASTER_TABLE + " a " +
                    "  LEFT JOIN Client_Master b " +
                    "    ON a.sCreatedx = b.sClientID " +
                " WHERE " + lsCondition;
        System.out.println(lsSQL);
        return lsSQL;
    }
    public String getSQ_Detail(){
        String lsSQL = "";
        String lsCondition = "";
        String lsStat = String.valueOf(p_nTranStat);
        
        if (lsStat.length() > 1){
            for (int lnCtr = 0; lnCtr <= lsStat.length()-1; lnCtr++){
                lsSQL += ", " + SQLUtil.toSQL(Character.toString(lsStat.charAt(lnCtr)));
            }
            
            lsCondition = "a.cRecdStat IN (" + lsSQL.substring(2) + ")";
        } else 
            lsCondition = "a.cRecdStat = " + SQLUtil.toSQL(lsStat);
        
        lsSQL = "SELECT " +
                    " a.sListngID " +
                    ", a.nEntryNox " +
                    ",  a.nRatingxx " +
                    ",  a.sRemarksx " +
                    ",  a.sReplyxxx " +
                    ",  a.nPriority " +
                    ",  a.sCreatedx " +
                    ",  a.dCreatedx " +
                    ",  a.sRepliedx " +
                    ",  a.dRepliedx " +
                    ",  a.cReadxxxx " +
                    ",  IFNULL(a.dReadxxxx, '') dReadxxxx" +
                    ",  a.sReadxxxx " +
                    ",  a.cRecdStat " +
                    ",  a.dTimeStmp " +
                    ",  d.sBarrcode xBarCodex " +
                    ",  d.sDescript xDescript " +
                    ",  IFNULL(e.sBrandNme, '') xBrandNme " +
                    ",  IFNULL(f.sModelNme, '') xModelNme " +
                    ",  IFNULL(g.sColorNme, '') xColorNme " +
                    ",  c.sDescript xCategrNm " +
                    ",  b.sImagesxx " +
                    ",  CONCAT(h.sFrstName, ' ', h.sMiddName,' ', h.sLastName) AS sCompnyNm " +
                " FROM " + MASTER_TABLE + " a " +
                    "  LEFT JOIN Client_Master h " +
                    "    ON a.sCreatedx = h.sClientID " +
                    "  LEFT JOIN MP_Inv_Master b " +
                    "    ON a.sListngID = b.sListngID " +
                    "  LEFT JOIN Inv_Category c " +
                    "    ON b.sCategrID = c.sCategrID " +
                    ",  CP_Inventory d " +
                    "  LEFT JOIN CP_Brand e " +
                    "    ON d.sBrandIDx = e.sBrandIDx " +
                    "  LEFT JOIN CP_Model f " +
                    "    ON d.sModelIDx = f.sModelIDx " +
                    "  LEFT JOIN Color g " +
                    "    ON d.sColorIDx = g.sColorIDx " +
                " WHERE b.sStockIDx = d.sStockIDx " +
                    " AND b.sCategrID IN('0002', '0004') " +
                    " AND " + lsCondition;
        System.out.println(lsSQL);
        return lsSQL;
    }
    
    private int getColumnIndex(CachedRowSet loRS, String fsValue) throws SQLException{
        int lnIndex = 0;
        int lnRow = loRS.getMetaData().getColumnCount();
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            if (fsValue.equals(loRS.getMetaData().getColumnLabel(lnCtr))){
                lnIndex = lnCtr;
                break;
            }
        }
        
        return lnIndex;
    }
    public boolean OpenTransaction(String fsTransNox, String fsEntryNox) throws SQLException{
        p_nEditMode = EditMode.UNKNOWN;
        
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        lsSQL = getSQ_Detail() + " AND a.sListngID = " + SQLUtil.toSQL(fsTransNox) + " AND a.nEntryNox = " + SQLUtil.toSQL(fsEntryNox);
        
        //open master
        loRS = p_oApp.executeQuery(lsSQL);
        p_oMaster = factory.createCachedRowSet();
        p_oMaster.populate(loRS);
        MiscUtil.close(loRS);
        
        p_oMaster.last();
        if (p_oMaster.getRow() <= 0) {
            p_sMessage = "No transaction was loaded.";
            return false;
        }
        p_nEditMode = EditMode.READY;
        
        return true;
    }
    
    public boolean UpdateTransaction() throws SQLException{
        if (p_nEditMode != EditMode.READY){
            p_sMessage = "Invalid edit mode.";
            return false;
        }
        
        if (p_bWithParent) {
            p_sMessage = "Updating of record from other object is not allowed.";
            return false;
        }
//        
        p_nEditMode = EditMode.UPDATE;
        return true;
    }
    
}
