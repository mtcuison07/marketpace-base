package org.rmj.marketplace.base;

import com.sun.rowset.CachedRowSetImpl;
import java.sql.ResultSet;
import java.sql.Types;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.sql.RowSetMetaData;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.StringUtil;
import org.rmj.appdriver.agentfx.ui.showFXDialog;
import org.rmj.appdriver.constants.EditMode;

/**
 * @author Michael Cuison
 * @since
 */
public class ProductListing {
    private final String MASTER_TABLE = "MP_Inv_Master";
    private final String IMAGE_TABLE = "MP_Inv_Images";
    
    private final GRider p_oApp;
    private final boolean p_bWithParent;
    
    private String p_sBranchCd;
    
    private int p_nEditMode;
    private int p_nTranStat;

    private String p_sMessage;
    private boolean p_bWithUI = true;

    private CachedRowSet p_oMaster;
    private CachedRowSet p_oDetail;
    private CachedRowSet p_oImages;
    private LTransaction p_oListener;
   
    public ProductListing(GRider foApp, String fsBranchCd, boolean fbWithParent){        
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
    
    public boolean NewTransaction() throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        createMaster();
        createImages();

        p_nEditMode = EditMode.ADDNEW;
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
        
        if (p_nEditMode == EditMode.ADDNEW){            
            //set transaction number on records
            String lsTransNox = MiscUtil.getNextCode(MASTER_TABLE, "sListngID", true, p_oApp.getConnection(), p_sBranchCd);
            p_oMaster.updateObject("sListngID", lsTransNox);
            p_oMaster.updateObject("sCreatedx", p_oApp.getUserID());
            p_oMaster.updateObject("dCreatedx", p_oApp.getServerDate());
            p_oMaster.updateRow();
            
            lsSQL = MiscUtil.rowset2SQL(p_oMaster, MASTER_TABLE, "xBarCodex;xDescript;xBrandNme;xModelNme;xColorNme;xCategrNm");
            
            if (!lsSQL.isEmpty()){
                if (!p_bWithParent) p_oApp.beginTrans();
                
                if (p_oApp.executeQuery(lsSQL, MASTER_TABLE, p_sBranchCd, lsTransNox.substring(0, 4)) <= 0){
                    if (!p_bWithParent) p_oApp.rollbackTrans();
                    p_sMessage = p_oApp.getMessage() + ";" + p_oApp.getErrMsg();
                    return false;
                }
                
                if (!p_bWithParent) p_oApp.commitTrans();
                
                p_nEditMode = EditMode.UNKNOWN;
                return true;
            } else{
                p_sMessage = "No record to save.";
                return false;
            }
        } else {           
            //set transaction number on records
            String lsTransNox = (String) getMaster("sListngID");

            lsSQL = MiscUtil.rowset2SQL(p_oMaster, 
                                        MASTER_TABLE, 
                                        "xBarCodex;xDescript;xBrandNme;xModelNme;xColorNme;xCategrNm", 
                                        "sListngID = " + SQLUtil.toSQL(lsTransNox));
            
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
    }
    
    public boolean SearchTransaction(String fsValue, boolean fbByCode) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";    
        
        String lsSQL = MiscUtil.addCondition(getSQ_Master(), "sBranchCd = " + SQLUtil.toSQL(p_sBranchCd));
        
        if (p_bWithUI){
            JSONObject loJSON = showFXDialog.jsonSearch(
                                p_oApp, 
                                lsSQL, 
                                fsValue, 
                                "Listing No.»Brief Description»Stock ID", 
                                "sListngID»sBriefDsc»sStockIDx", 
                                "sListngID»sBriefDsc»sStockIDx", 
                                fbByCode ? 0 : 1);
            
            if (loJSON != null) 
                return OpenTransaction((String) loJSON.get("sListngID"));
            else {
                p_sMessage = "No record selected.";
                return false;
            }
        }
        
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "sListngID = " + SQLUtil.toSQL(fsValue));   
        else {
            if (!fsValue.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, "sBriefDsc LIKE " + SQLUtil.toSQL(fsValue + "%")); 
                lsSQL += " LIMIT 1";
            }
        }
        
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
        
        if (!loRS.next()){
            MiscUtil.close(loRS);
            p_sMessage = "No transaction found for the givern criteria.";
            return false;
        }
        
        lsSQL = loRS.getString("sListngID");
        MiscUtil.close(loRS);
        
        return OpenTransaction(lsSQL);
    }
    
    public boolean OpenTransaction(String fsTransNox) throws SQLException{
        p_nEditMode = EditMode.UNKNOWN;
        
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        lsSQL = getSQ_Detail() + " HAVING sListngID = " + SQLUtil.toSQL(fsTransNox);
        
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
        
        if (Integer.parseInt((String) getMaster("cTranStat")) > 0){
            p_sMessage = "Unable to update processed transactions.";
            return false;
        }
        
        p_nEditMode = EditMode.UPDATE;
        return true;
    }
    
    public boolean CloseTransaction() throws SQLException{        
        if (p_nEditMode != EditMode.READY){
            p_sMessage = "Invalid update mode detected.";
            return false;
        }
        
        p_sMessage = "";
        
        if (p_bWithParent) {
            p_sMessage = "Confirming listings from other object is not allowed.";
            return false;
        }
        
        if (((String) getMaster("cTranStat")).equals("1")){            
            p_sMessage = "Listing was already approved.";
            return false;
        }
        
        String lsTransNox = (String) getMaster("sTransNox");
        String lsSQL = "UPDATE " + MASTER_TABLE + " SET" +
                            "  cTranStat = '1'" +
                            ", sApproved = " + SQLUtil.toSQL(p_oApp.getUserID()) +
                            ", dApproved = " + SQLUtil.toSQL(p_oApp.getServerDate()) +
                            ", dActivate = " + SQLUtil.toSQL(p_oApp.getServerDate()) +
                        " WHERE sTransNox = " + SQLUtil.toSQL(lsTransNox);
        
        if (p_oApp.executeQuery(lsSQL, MASTER_TABLE, p_sBranchCd, lsTransNox.substring(0, 4)) <= 0){
            p_sMessage = p_oApp.getErrMsg() + "; " + p_oApp.getMessage();
            return false;
        }
        
        p_nEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    public boolean PostTransaction() throws SQLException{
        if (p_nEditMode != EditMode.READY){
            p_sMessage = "Invalid update mode detected.";
            return false;
        }
        
        p_sMessage = "This feature is not supported.";
        
        return false;
    }
    
    public boolean CancelTransaction() throws SQLException{
        if (p_nEditMode != EditMode.READY){
            p_sMessage = "Invalid update mode detected.";
            return false;
        }
        
        p_sMessage = "";
        
        if (p_bWithParent) {
            p_sMessage = "Cancelling transactions from other object is not allowed.";
            return false;
        }
        
        if (((String) getMaster("cTranStat")).equals("2")){
            p_sMessage = "Unable to cancel posted transactions.";
            return false;
        }
        
        if (((String) getMaster("cTranStat")).equals("3")){
            p_sMessage = "Transaction was already cancelled.";
            return false;
        }
        
        String lsTransNox = (String) getMaster("sTransNox");
        
        
        
        String lsSQL = "UPDATE " + MASTER_TABLE + " SET" +
                            "  cTranStat = '3'" +
                            ", sModified = " + SQLUtil.toSQL(p_oApp.getUserID()) +
                            ", dModified = " + SQLUtil.toSQL(p_oApp.getServerDate()) +
                        " WHERE sTransNox = " + SQLUtil.toSQL(lsTransNox);
        
        if (p_oApp.executeQuery(lsSQL, MASTER_TABLE, p_sBranchCd, lsTransNox.substring(0, 4)) <= 0){
            p_sMessage = p_oApp.getErrMsg() + "; " + p_oApp.getMessage();
            return false;
        }
        
        p_nEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    public boolean LoadList(String fsValue, boolean fbByCode) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";    
        
        String lsSQL = "";
        
        if (fbByCode)
            lsSQL = getSQ_Detail() + " HAVING xBarrCode LIKE " + SQLUtil.toSQL(fsValue + "%");
        else
            lsSQL = getSQ_Detail() + " HAVING xDescript LIKE " + SQLUtil.toSQL(fsValue + "%");
        
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
    
        if (MiscUtil.RecordCount(loRS) == 0){
            MiscUtil.close(loRS);
            p_sMessage = "No record found for the given criteria.";
            return false;
        }
        
        RowSetFactory factory = RowSetProvider.newFactory();
        p_oDetail = factory.createCachedRowSet();
        p_oDetail.populate(loRS);
        MiscUtil.close(loRS);
        
        return true;
    }
    
//    public Object getImageInfo(int fnRow, int fnIndex) throws SQLException{
//        if (fnIndex == 0) return null;
//        if (getImageCount() == 0 || fnRow > getImageCount()) return null;
//        
//        p_oImages.absolute(fnRow);
//        return p_oImages.getObject(fnIndex);
//    }
//    
//    public Object getImageInfo(int fnRow, String fsIndex) throws SQLException{
//        return getImageInfo(fnRow, getColumnIndex(p_oImages, fsIndex));
//    }
    
//    public void setImageInfo(int fnRow, int fnIndex, Object foValue) throws SQLException{
//        if (getImageCount()== 0 || fnRow == 0) return;
//        
//        p_oImages.absolute(fnRow);
//        
//        switch (fnIndex){
//            case 2: //nEntryNox
//            case 4: //nPriority
//                if (foValue instanceof Integer)
//                    p_oImages.updateObject(fnIndex, (int) foValue);
//                else
//                    p_oImages.updateObject(fnIndex, 0);
//                
//                p_oImages.updateRow();   
//                break;
//            default:
//                p_oImages.updateObject(fnIndex, foValue);
//                break;
//        }
//    }
//    
//    public void setImageInfo(int fnRow, String fsIndex, Object foValue) throws SQLException{
//        setImageInfo(fnRow, getColumnIndex(p_oImages, fsIndex), foValue);
//    }
    
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
        
        switch (fnIndex){
            case 10: //dListStrt
            case 11: //dListEndx
                if (foValue instanceof Date){
                    p_oMaster.updateDate(fnIndex, SQLUtil.toDate((Date) foValue));
                } else
                    p_oMaster.updateDate(fnIndex, SQLUtil.toDate(p_oApp.getServerDate()));
                
                p_oMaster.updateRow();
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
            case 3: //sBriefDsc
            case 4: //sDescript
            case 27: //sImagesxx
                p_oMaster.updateString(fnIndex, (String) foValue);
                p_oMaster.updateRow();

                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
            case 5: //nTotalQty
            case 6: //nQtyOnHnd
            case 7: //nResvOrdr
            case 8: //nSoldQtyx
                if (foValue instanceof Integer)
                    p_oMaster.updateInt(fnIndex, (int) foValue);
                else 
                    p_oMaster.updateInt(fnIndex, 0);
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
            case 9: //nUnitPrce
                if (foValue instanceof Double)
                    p_oMaster.updateDouble(fnIndex, (double) foValue);
                else 
                    p_oMaster.updateDouble(fnIndex, 0.000);
                
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
        
        p_oDetail.absolute(fnRow);
        return p_oDetail.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, String fsIndex) throws SQLException{
        return getDetail(fnRow, getColumnIndex(p_oDetail, fsIndex));
    }
    
    public int getItemCount() throws SQLException{
        p_oDetail.last();
        return p_oDetail.getRow();
    }
    
    public void displayImageFields() throws SQLException{
        if (p_nEditMode != EditMode.ADDNEW && p_nEditMode != EditMode.UPDATE) return;
        
        int lnRow = p_oImages.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("IMAGE TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + p_oImages.getMetaData().getColumnLabel(lnCtr));
            System.out.println("Column type: " + (lnCtr) + " --> " + p_oImages.getMetaData().getColumnType(lnCtr));
            if (p_oImages.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                p_oImages.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + p_oImages.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("----------------------------------------");
        System.out.println("END: IMAGE TABLE INFO");
        System.out.println("----------------------------------------");
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
    
    private void createMaster() throws SQLException{
        RowSetMetaData meta = new RowSetMetaDataImpl();

        meta.setColumnCount(27);

        meta.setColumnName(1, "sListngID");
        meta.setColumnLabel(1, "sListngID");
        meta.setColumnType(1, Types.VARCHAR);
        meta.setColumnDisplaySize(1, 12);

        meta.setColumnName(2, "sStockIDx");
        meta.setColumnLabel(2, "sStockIDx");
        meta.setColumnType(2, Types.VARCHAR);
        meta.setColumnDisplaySize(2, 12);
        
        meta.setColumnName(3, "sBriefDsc");
        meta.setColumnLabel(3, "sBriefDsc");
        meta.setColumnType(3, Types.VARCHAR);
        meta.setColumnDisplaySize(3, 64);
        
        meta.setColumnName(4, "sDescript");
        meta.setColumnLabel(4, "sDescript");
        meta.setColumnType(4, Types.VARCHAR);
        meta.setColumnDisplaySize(4, 2048);
        
        meta.setColumnName(5, "nTotalQty");
        meta.setColumnLabel(5, "nTotalQty");
        meta.setColumnType(5, Types.INTEGER);
        
        meta.setColumnName(6, "nQtyOnHnd");
        meta.setColumnLabel(6, "nQtyOnHnd");
        meta.setColumnType(6, Types.INTEGER);
        
        meta.setColumnName(7, "nResvOrdr");
        meta.setColumnLabel(7, "nResvOrdr");
        meta.setColumnType(7, Types.INTEGER);
        
        meta.setColumnName(8, "nSoldQtyx");
        meta.setColumnLabel(8, "nSoldQtyx");
        meta.setColumnType(8, Types.INTEGER);
        
        meta.setColumnName(9, "nUnitPrce");
        meta.setColumnLabel(9, "nUnitPrce");
        meta.setColumnType(9, Types.DOUBLE);
        
        meta.setColumnName(10, "dListStrt");
        meta.setColumnLabel(10, "dListStrt");
        meta.setColumnType(10, Types.TIMESTAMP);
        
        meta.setColumnName(11, "dListEndx");
        meta.setColumnLabel(11, "dListEndx ");
        meta.setColumnType(11, Types.TIMESTAMP);
            
        meta.setColumnName(12, "sCategrID");
        meta.setColumnLabel(12, "sCategrID");
        meta.setColumnType(12, Types.VARCHAR);
        meta.setColumnDisplaySize(12, 4);
        
        meta.setColumnName(13, "sCreatedx");
        meta.setColumnLabel(13, "sCreatedx");
        meta.setColumnType(13, Types.CHAR);
        meta.setColumnDisplaySize(13, 12);
        
        meta.setColumnName(14, "dCreatedx");
        meta.setColumnLabel(14, "dCreatedx");
        meta.setColumnType(14, Types.TIMESTAMP);
        
        meta.setColumnName(15, "sApproved");
        meta.setColumnLabel(15, "sApproved");
        meta.setColumnType(15, Types.CHAR);
        meta.setColumnDisplaySize(15, 12);
        
        meta.setColumnName(16, "dApproved");
        meta.setColumnLabel(16, "dApproved");
        meta.setColumnType(16, Types.TIMESTAMP);
        
        meta.setColumnName(17, "sInactive");
        meta.setColumnLabel(17, "sInactive");
        meta.setColumnType(17, Types.CHAR);
        meta.setColumnDisplaySize(17, 12);
        
        meta.setColumnName(18, "dInactive");
        meta.setColumnLabel(18, "dInactive");
        meta.setColumnType(18, Types.TIMESTAMP);
        
        meta.setColumnName(19, "dActivate");
        meta.setColumnLabel(19, "dActivate");
        meta.setColumnType(19, Types.TIMESTAMP);
        
        meta.setColumnName(20, "cTranStat");
        meta.setColumnLabel(20, "cTranStat");
        meta.setColumnType(20, Types.CHAR);
        meta.setColumnDisplaySize(20, 1);        
        
        meta.setColumnName(21, "xBarCodex");
        meta.setColumnLabel(21, "xBarCodex");
        meta.setColumnType(21, Types.VARCHAR);
        meta.setColumnDisplaySize(21, 25);
        
        meta.setColumnName(22, "xDescript");
        meta.setColumnLabel(22, "xDescript");
        meta.setColumnType(22, Types.VARCHAR);
        meta.setColumnDisplaySize(22, 128);
        
        meta.setColumnName(23, "xBrandNme");
        meta.setColumnLabel(23, "xBrandNme");
        meta.setColumnType(23, Types.VARCHAR);
        
        meta.setColumnName(24, "xModelNme");
        meta.setColumnLabel(24, "xModelNme");
        meta.setColumnType(24, Types.VARCHAR);
        
        meta.setColumnName(25, "xColorNme");
        meta.setColumnLabel(25, "xColorNme");
        meta.setColumnType(25, Types.VARCHAR);
        
        meta.setColumnName(26, "xCategrNm");
        meta.setColumnLabel(26, "xCategrNm");
        meta.setColumnType(26, Types.VARCHAR);
        
        meta.setColumnName(27, "sImagesxx");
        meta.setColumnLabel(27, "sImagesxx");
        meta.setColumnType(27, Types.VARCHAR);
         meta.setColumnDisplaySize(27, 2048);  
        
        p_oMaster = new CachedRowSetImpl();
        p_oMaster.setMetaData(meta);
        
        p_oMaster.last();
        p_oMaster.moveToInsertRow();
        
        MiscUtil.initRowSet(p_oMaster);       
        
        p_oMaster.updateObject("sListngID", MiscUtil.getNextCode(MASTER_TABLE, "sListngID", true, p_oApp.getConnection(), p_sBranchCd));
        p_oMaster.updateObject("sDescript", "0");
        p_oMaster.updateObject("sImagesxx", "0");
        p_oMaster.updateObject("cTranStat", "0");
        
        p_oMaster.insertRow();
        p_oMaster.moveToCurrentRow();
    }
    
    private void createImages() throws SQLException{
        RowSetMetaData meta = new RowSetMetaDataImpl();

        meta.setColumnCount(5);

        meta.setColumnName(1, "sListngID");
        meta.setColumnLabel(1, "sListngID");
        meta.setColumnType(1, Types.VARCHAR);
        meta.setColumnDisplaySize(1, 12);

        meta.setColumnName(2, "nEntryNox");
        meta.setColumnLabel(2, "nEntryNox");
        meta.setColumnType(2, Types.INTEGER);
        
        meta.setColumnName(3, "sImageURL");
        meta.setColumnLabel(3, "sImageURL");
        meta.setColumnType(3, Types.VARCHAR);
        meta.setColumnDisplaySize(3, 128);
        
        meta.setColumnName(4, "nPriority");
        meta.setColumnLabel(4, "nPriority");
        meta.setColumnType(4, Types.INTEGER);
        
        meta.setColumnName(5, "cRecdStat");
        meta.setColumnLabel(5, "cRecdStat");
        meta.setColumnType(5, Types.CHAR);
        meta.setColumnDisplaySize(5, 1);
        
        p_oImages = new CachedRowSetImpl();
        p_oImages.setMetaData(meta);
        
//        addImage();
    }
    
//    public boolean addImage() throws SQLException{
//        int lnRow = getImageCount();
//        
//        p_oImages.last();
//        p_oImages.moveToInsertRow();
//        
//        MiscUtil.initRowSet(p_oImages);      
//        
//        p_oImages.updateObject("nEntryNox", lnRow + 1);
//        p_oImages.updateObject("nPriority", lnRow + 1);
//        p_oImages.updateObject("cRecdStat", "1");
//        
//        p_oImages.insertRow();
//        p_oImages.moveToCurrentRow();
//        
//        return true;
//    }
    
    private boolean isEntryOK() throws SQLException{           
        //validate master               
        if ("".equals((String) getMaster("sStockIDx"))){
            p_sMessage = "No product was selected to list.";
            return false;
        }
        
        if ("".equals((String) getMaster("sBriefDsc"))){
            p_sMessage = "Product brief description is not set.";
            return false;
        }
        
        return true;
    }
    
    public boolean setDescriptPriority(int fnRow, boolean fbMoveUpxx) throws SQLException, ParseException{
        String lsDescript = (String) getMaster("sDescript");
        
        JSONArray loArray;
        
        if (lsDescript.isEmpty()) 
            return false;
        else {
            JSONParser loParser = new JSONParser();
            loArray = (JSONArray) loParser.parse(lsDescript);
            
            if (fnRow > loArray.size()-1 || fnRow < 0) return false;
            
            if (fbMoveUpxx && fnRow == 0) return false;
            if (!fbMoveUpxx && fnRow == loArray.size()-1) return false;
            
            JSONObject loTemp = (JSONObject) loArray.get(fnRow);
            loArray.remove(fnRow);
            
            if (fbMoveUpxx)
                loArray.add(fnRow - 1, loTemp);
            else
                loArray.add(fnRow + 1, loTemp);
        }
            
        setMaster("sDescript", loArray.toJSONString());
        
        return true;
    }
    
    public boolean addDescription(String fsValue, boolean fbEmphasis) throws SQLException, ParseException{
        String lsDescript = (String) getMaster("sDescript");
        
        JSONArray loArray ;
        
        JSONObject loJSON = new JSONObject();
        loJSON.put("sDescript", fsValue);
        loJSON.put("bEmphasis", fbEmphasis);
        
        if (lsDescript.isEmpty()){    
            loArray = new JSONArray();
            loArray.add(loJSON);
        } else {
            JSONParser loParser = new JSONParser();
            loArray = (JSONArray) loParser.parse(lsDescript);
            loArray.add(loJSON);
        }
            
        setMaster("sDescript", loArray.toJSONString());
        return true;
    }
    
    public boolean delDescription(int fnRow) throws SQLException, ParseException{
        String lsDescript = (String) getMaster("sDescript");
        
        JSONArray loArray;
        
        if (lsDescript.isEmpty()) 
            return false;
        else {
            JSONParser loParser = new JSONParser();
            loArray = (JSONArray) loParser.parse(lsDescript);
            
            if (fnRow > loArray.size()) return false;
            
            loArray.remove(fnRow);
        }
            
        setMaster("sDescript", loArray.toJSONString());
        return true;
    }
    
    public boolean setImagePriority(int fnRow, boolean fbMoveUpxx) throws SQLException, ParseException{
        String lsDescript = (String) getMaster("sImagesxx");
        
        JSONArray loArray;
        
        if (lsDescript.isEmpty()) 
            return false;
        else {
            JSONParser loParser = new JSONParser();
            loArray = (JSONArray) loParser.parse(lsDescript);
            
            if (fnRow > loArray.size()-1 || fnRow < 0) return false;
            
            if (fbMoveUpxx && fnRow == 0) return false;
            if (!fbMoveUpxx && fnRow == loArray.size()-1) return false;
            
            JSONObject loTemp = (JSONObject) loArray.get(fnRow);
            loArray.remove(fnRow);
            
            if (fbMoveUpxx)
                loArray.add(fnRow - 1, loTemp);
            else
                loArray.add(fnRow + 1, loTemp);
        }
            
        setMaster("sImagesxx", loArray.toJSONString());
        
        return true;
    }
    
    public boolean addImage(String fsValue) throws SQLException, ParseException{
        String lsDescript = (String) getMaster("sImagesxx");
        
        JSONArray loArray ;
        
        JSONObject loJSON = new JSONObject();
        loJSON.put("sImageURL", fsValue);
        
        if (lsDescript.isEmpty()){    
            loArray = new JSONArray();
            loArray.add(loJSON);
        } else {
            JSONParser loParser = new JSONParser();
            loArray = (JSONArray) loParser.parse(lsDescript);
            loArray.add(loJSON);
        }
            
        setMaster("sImagesxx", loArray.toJSONString());
        return true;
    }
    
    public boolean delImage(int fnRow) throws SQLException, ParseException{
        String lsDescript = (String) getMaster("sImagesxx");
        
        JSONArray loArray;
        
        if (lsDescript.isEmpty()) 
            return false;
        else {
            JSONParser loParser = new JSONParser();
            loArray = (JSONArray) loParser.parse(lsDescript);
            
            if (fnRow > loArray.size()) return false;
            
            loArray.remove(fnRow);
        }
            
        setMaster("sImagesxx", loArray.toJSONString());
        return true;
    }
    
//    public boolean removeImage(int fnRow) throws SQLException{
//        if (p_nEditMode != EditMode.ADDNEW && p_nEditMode != EditMode.UPDATE) {
//            p_sMessage = "Invalid edit mode detected.";
//            return false;
//        }
//        
//        if (fnRow > getImageCount()) return false;
//        
//        p_oImages.absolute(fnRow);
//        p_oImages.deleteRow();
//        
//        return true;
//    }
    
//    public void setImagePriority(int fnRow, int fnAdd) throws SQLException, ParseException{
//        p_oImages.absolute(fnRow);
//        int lnOld = (int) p_oImages.getObject("nPriority");
//        int lnNew = lnOld - fnAdd;
//        
//        int lnCtr = 0;
//        
//        p_oImages.beforeFirst();
//        while (p_oImages.next()){
//            lnCtr++;
//            if (fnRow >= lnCtr){
//                if (fnRow == lnCtr){
//                    p_oImages.updateObject("nPriority", lnNew);
//                    p_oImages.updateRow();
//                } else {
//                    if (lnNew <= (int) p_oImages.getObject("nPriority")){
//                        p_oImages.updateObject("nPriority", (int) p_oImages.getObject("nPriority") + fnAdd);
//                        p_oImages.updateRow();
//                    }
//                }
//            } else break;
//        }
//        
//        //sort the items based on priority
//        JSONParser loParser = new JSONParser();
//        
//        p_oImages.beforeFirst();
//        String lsValue = MiscUtil.RS2JSON(p_oImages).toJSONString();
//        JSONArray laValue = (JSONArray) loParser.parse(lsValue);
//        
//        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
//        
//        for (int i = 0; i < laValue.size(); i++) {
//            jsonValues.add((JSONObject) laValue.get(i));
//        }
//        
//        Collections.sort(jsonValues, new Comparator<JSONObject>() {
//            private static final String KEY_NAME = "nPriority";
//            
//            String string1;
//            String string2;
//            
//            @Override
//            public int compare(JSONObject lhs, JSONObject rhs) {
//                return Long.parseLong(String.valueOf(lhs.get(KEY_NAME))) > Long.parseLong(String.valueOf(rhs.get(KEY_NAME))) ? 1 : 
//                    (Long.parseLong(String.valueOf(lhs.get(KEY_NAME))) < Long.parseLong(String.valueOf(rhs.get(KEY_NAME))) ? -1 : 0);
//            }
//        });
//        
//        String lsSQL = MiscUtil.addCondition(getSQ_Images(), "0=1");
//        ResultSet loRS = p_oApp.executeQuery(lsSQL);
//        p_oImages.populate(loRS);
//        MiscUtil.close(loRS);
//        
//        JSONObject loJSON;
//        
//        for (int i = 0; i <= jsonValues.size()-1; i++) {            
//            loJSON = (JSONObject) jsonValues.get(i);
//            addImage();
//            p_oImages.absolute(i + 1);
//            p_oImages.updateObject("nEntryNox", Integer.parseInt(String.valueOf(loJSON.get("nEntryNox"))));
//            p_oImages.updateObject("sImageURL", (String) loJSON.get("sImageURL"));
//            p_oImages.updateObject("nPriority", Integer.parseInt(String.valueOf(loJSON.get("nPriority"))));
//            p_oImages.updateObject("cRecdStat", (String) loJSON.get("cRecdStat"));
//            p_oImages.updateRow();
//        }
//    }
    
    public String getSQ_Master(){
        String lsSQL = "";
                
        lsSQL = "SELECT" + 
                    "  a.sListngID" +
                    ", a.sStockIDx" +
                    ", a.sBriefDsc" +
                    ", a.sDescript" +
                    ", a.nTotalQty" +
                    ", a.nQtyOnHnd" +
                    ", a.nResvOrdr" +
                    ", a.nSoldQtyx" +
                    ", a.nUnitPrce" +
                    ", a.dListStrt" +
                    ", a.dListEndx" +
                    ", a.sCategrID" +
                    ", a.sCreatedx" +
                    ", a.dCreatedx" +
                    ", a.sApproved" +
                    ", a.dApproved" +
                    ", a.sInactive" +
                    ", a.dInactive" +
                    ", a.dActivate" +
                    ", a.cTranStat" +
                    ", '' xBarCodex" +
                    ", '' xDescript" +
                    ", '' xBrandNme" +
                    ", '' xModelNme" +
                    ", '' xColorNme" +
                    ", c.sDescript xCategrNm" +
                    ", a.sImagesxx" +
                " FROM " + MASTER_TABLE + " a" +
                    " LEFT JOIN Inv_Category b ON a.sCategrID = b.sCategrID";
        
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
            
            lsCondition = "a.cTranStat IN (" + lsSQL.substring(2) + ")";
        } else 
            lsCondition = "a.cTranStat = " + SQLUtil.toSQL(lsStat);
                
        lsSQL = "SELECT" + 
                    "  a.sListngID" +
                    ", a.sStockIDx" +
                    ", a.sBriefDsc" +
                    ", a.sDescript" +
                    ", a.nTotalQty" +
                    ", a.nQtyOnHnd" +
                    ", a.nResvOrdr" +
                    ", a.nSoldQtyx" +
                    ", a.nUnitPrce" +
                    ", a.dListStrt" +
                    ", a.dListEndx" +
                    ", a.sCategrID" +
                    ", a.sCreatedx" +
                    ", a.dCreatedx" +
                    ", a.sApproved" +
                    ", a.dApproved" +
                    ", a.sInactive" +
                    ", a.dInactive" +
                    ", a.dActivate" +
                    ", a.cTranStat" +
                    ", c.sBarrcode xBarCodex" +
                    ", c.sDescript xDescript" +
                    ", IFNULL(d.sBrandNme, '') xBrandNme" +
                    ", IFNULL(e.sModelNme, '') xModelNme" +
                    ", IFNULL(f.sColorNme, '') xColorNme" +
                    ", b.sDescript xCategrNm" +
                    ", a.sImagesxx" +
                " FROM " + MASTER_TABLE + " a" +
                    " LEFT JOIN Inv_Category b ON a.sCategrID = b.sCategrID" +
                    ", CP_Inventory c" +
                        " LEFT JOIN CP_Brand d ON c.sBrandIDx = d.sBrandIDx" +
                        " LEFT JOIN CP_Model e ON c.sModelIDx = e.sModelIDx" +
                        " LEFT JOIN Color f ON c.sColorIDx = f.sColorIDx" +
                " WHERE a.sStockIDx = c.sStockIDx" +
                    " AND a.sCategrID IN ('0002', '0004')" +
                    " AND " + lsCondition;
        
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
    
    public boolean searchItem(String fsValue, boolean fbByCode, boolean fbSearch) throws SQLException{
        if (p_nEditMode != EditMode.ADDNEW) {
            p_sMessage = "This feature was for New Tranactions Only.";
            return false;
        }
                
        String lsCondition = "";
        
        if (fbByCode)
            lsCondition = "a.sStockIDx = " + SQLUtil.toSQL(fsValue);
        else {
            if (!fsValue.trim().isEmpty()){
                if (fbSearch)
                    lsCondition = "a.sBarrCode LIKE " + SQLUtil.toSQL(fsValue + "%");
                else
                    lsCondition = "a.sDescript LIKE " + SQLUtil.toSQL(fsValue + "%");
            }
        }
        
        String lsSQL = "SELECT" +
                            "  a.sBarrCode" +
                            ", a.sDescript" +
                            ", e.sBrandNme" +
                            ", c.sModelNme" +
                            ", d.sColorNme" +
                            ", a.sStockIDx" +
                            ", a.cHsSerial" +
                            ", a.nSelPrice" +
                            ", '0002' xCategrID" +
                            ", 'Mobile Phone' xCategrNm" +
                        " FROM CP_Inventory a" +
                            " LEFT JOIN Color d" +
                               " ON a.sColorIDx = d.sColorIDx" +
                            " LEFT JOIN Size g" +
                               " ON a.sSizeIDxx = g.sSizeIDxx" +
                            ", CP_Inventory_Master b" +
                            ", CP_Model c" +
                            ", CP_Brand e" +
                        " WHERE b.sStockIDx = a.sStockIDx" +
                            " AND a.sBrandIDx = e.sBrandIDx" +
                            " AND a.sModelIDx = c.sModelIDx" +
                            " AND b.sBranchCd =  " + SQLUtil.toSQL(p_oApp.getBranchCode()) +
                            " AND b.cRecdStat = '1'";
        
        if (!lsCondition.isEmpty())
            lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
        
        ResultSet loRS;
        
        p_oMaster.first();
        MiscUtil.initRowSet(p_oMaster);       
        p_oMaster.updateObject("sListngID", MiscUtil.getNextCode(MASTER_TABLE, "sListngID", true, p_oApp.getConnection(), p_sBranchCd));
        p_oMaster.updateObject("cTranStat", "0");
        
        if (!p_bWithUI){
            lsSQL += " LIMIT 1";
            
            loRS = p_oApp.executeQuery(lsSQL);
            
            if (loRS.next()){
                p_oMaster.updateObject("sStockIDx", loRS.getString("sStockIDx"));
                p_oMaster.updateObject("xBarCodex", loRS.getString("sBarrCode"));
                p_oMaster.updateObject("xDescript", loRS.getString("sDescript"));
                p_oMaster.updateObject("sBriefDsc", loRS.getString("sDescript"));
                p_oMaster.updateObject("xBrandNme", loRS.getString("sBrandNme"));
                p_oMaster.updateObject("xModelNme", loRS.getString("sModelNme"));
                p_oMaster.updateObject("xColorNme", loRS.getString("sColorNme"));
                p_oMaster.updateObject("sCategrID", loRS.getString("xCategrID"));
                p_oMaster.updateObject("xCategrNm", loRS.getString("xCategrNm"));
                p_oMaster.updateObject("nUnitPrce", loRS.getDouble("nSelPrice"));
                p_oMaster.updateRow();
                
                if (p_oListener != null){
                    p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "sBriefDsc"), getMaster("sBriefDsc"));
                    p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "sDescript"), getMaster("sDescript"));
                    p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "nTotalQty"), getMaster("nTotalQty"));
                    p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "nQtyOnHnd"), getMaster("nQtyOnHnd"));
                    p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "nResvOrdr"), getMaster("nResvOrdr"));
                    p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "nSoldQtyx"), getMaster("nSoldQtyx"));
                    p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "nUnitPrce"), getMaster("nUnitPrce"));
                    p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "xBarCodex"), getMaster("xBarCodex"));
                    p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "xDescript"), getMaster("xDescript"));
                    p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "xBrandNme"), getMaster("xBrandNme"));
                    p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "xModelNme"), getMaster("xModelNme"));
                    p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "xColorNme"), getMaster("xColorNme"));
                    p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "xCategrNm"), getMaster("xCategrNm"));
                }
                
                return true;
            } else return false;
        }
        
        System.out.println(lsSQL);
        loRS = p_oApp.executeQuery(lsSQL);
        
        JSONObject loJSON;
        
        loJSON = showFXDialog.jsonBrowse(
                    p_oApp, 
                    loRS, 
                    "Barcode»Description»Brand»Model»Color»SRP", 
                    "sBarrCode»sDescript»sBrandNme»sModelNme»sColorNme»nSelPrice");

        if (loJSON != null){
            p_oMaster.updateObject("sStockIDx", (String) loJSON.get("sStockIDx"));
            p_oMaster.updateObject("xBarCodex", (String) loJSON.get("sBarrCode"));
            p_oMaster.updateObject("xDescript", (String) loJSON.get("sDescript"));
            p_oMaster.updateObject("sBriefDsc", (String) loJSON.get("sDescript"));
            p_oMaster.updateObject("xBrandNme", (String) loJSON.get("sBrandNme"));
            p_oMaster.updateObject("xModelNme", (String) loJSON.get("sModelNme"));
            p_oMaster.updateObject("xColorNme", (String) loJSON.get("sColorNme"));
            p_oMaster.updateObject("sCategrID", (String) loJSON.get("xCategrID"));
            p_oMaster.updateObject("xCategrNm", (String) loJSON.get("xCategrNm"));
            p_oMaster.updateObject("nUnitPrce", Double.valueOf((String) loJSON.get("nSelPrice")));
            p_oMaster.updateRow();

            if (p_oListener != null){
                p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "sBriefDsc"), getMaster("sBriefDsc"));
                p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "sDescript"), getMaster("sDescript"));
                p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "nTotalQty"), getMaster("nTotalQty"));
                p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "nQtyOnHnd"), getMaster("nQtyOnHnd"));
                p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "nResvOrdr"), getMaster("nResvOrdr"));
                p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "nSoldQtyx"), getMaster("nSoldQtyx"));
                p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "nUnitPrce"), getMaster("nUnitPrce"));
                p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "xBarCodex"), getMaster("xBarCodex"));
                p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "xDescript"), getMaster("xDescript"));
                p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "xBrandNme"), getMaster("xBrandNme"));
                p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "xModelNme"), getMaster("xModelNme"));
                p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "xColorNme"), getMaster("xColorNme"));
                p_oListener.MasterRetreive(getColumnIndex(p_oMaster, "xCategrNm"), getMaster("xCategrNm"));
            }

            return true;
        }
        return false;
    }
}