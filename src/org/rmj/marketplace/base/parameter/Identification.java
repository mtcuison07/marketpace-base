package org.rmj.marketplace.base.parameter;

import com.sun.rowset.CachedRowSetImpl;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import javax.sql.RowSetMetaData;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;
import org.json.simple.JSONObject;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.ui.showFXDialog;
import org.rmj.appdriver.constants.EditMode;
import org.rmj.appdriver.constants.RecordStatus;
import org.rmj.marketplace.base.LMasDetTrans;

/**
 * @author Jonathan Sabiniano
 * @since July 07, 2022
 */
public class Identification {
    private final String MASTER_TABLE = "Identification";
    
    private final GRider p_oApp;
    private final boolean p_bWithParent;
    
    private String p_sBranchCd;
    private int p_nEditMode;

    private String p_sMessage;
    private boolean p_bWithUI = true;
    
    private LMasDetTrans p_oListener;
    
    private CachedRowSet p_oRecord;
    
    public Identification(GRider foApp, String fsBranchCd, boolean fbWithParent){
        p_oApp = foApp;
        p_sBranchCd = fsBranchCd;
        p_bWithParent = fbWithParent;        
                
        if (p_sBranchCd.isEmpty()) p_sBranchCd = p_oApp.getBranchCode();
                
        p_nEditMode = EditMode.UNKNOWN;
    }
    
    public void setListener(LMasDetTrans foValue){
        p_oListener = foValue;
    }
    
    public void setWithUI(boolean fbValue){
        p_bWithUI = fbValue;
    }
    
    public int getEditMode(){
        return p_nEditMode;
    }
    
    public String getMessage(){
        return p_sMessage;
    }
    
    public boolean NewRecord() throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        initRecord();

        p_nEditMode = EditMode.ADDNEW;
        return true;
    }
    
    public boolean SaveRecord() throws SQLException{
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
        
        p_oRecord.updateObject("sModified", p_oApp.getUserID());
        p_oRecord.updateObject("dModified", p_oApp.getServerDate());
        p_oRecord.updateRow();
        
        String lsSQL;
        
        if (p_nEditMode == EditMode.ADDNEW){           
            lsSQL = MiscUtil.getNextCode(MASTER_TABLE, "sIDCodexx", false, p_oApp.getConnection(), "");
            p_oRecord.updateObject("sIDCodexx", lsSQL);
            p_oRecord.updateRow();
            
            lsSQL = MiscUtil.rowset2SQL(p_oRecord, MASTER_TABLE, "");
        } else {            
            lsSQL = MiscUtil.rowset2SQL(p_oRecord, 
                                        MASTER_TABLE, 
                                        "", 
                                        "sIDCodexx = " + SQLUtil.toSQL(p_oRecord.getString("sIDCodexx")));
        }
        
        if (!lsSQL.isEmpty()){
            if (!p_bWithParent) p_oApp.beginTrans();
            
            if (p_oApp.executeQuery(lsSQL, MASTER_TABLE, p_sBranchCd, "") <= 0){
                if (!p_bWithParent) p_oApp.rollbackTrans();
                p_sMessage = p_oApp.getErrMsg() + ";" + p_oApp.getMessage();
                return false;
            }
            
            if (!p_bWithParent) p_oApp.commitTrans();
            
            return true;
        } else
            p_sMessage = "No record to update.";
        
        p_nEditMode = EditMode.UNKNOWN;
        return false;
    }
    
    public boolean ActivateRecord() throws SQLException{
        if (p_nEditMode != EditMode.READY){
            p_sMessage = "Invalid edit mode.";
            return false;
        }
        
        p_oRecord.first();
        
        if ("1".equals(p_oRecord.getString("cRecdStat"))){
            p_sMessage = "Record is active.";
            return false;
        }
        
        String lsSQL = "UPDATE " + MASTER_TABLE + " SET" +
                            "  cRecdStat = '1'" +
                            ", sModified = " + SQLUtil.toSQL(p_oApp.getUserID()) +
                            ", dModified = " + SQLUtil.toSQL(p_oApp.getServerDate()) +
                        " WHERE sIDCodexx = " + SQLUtil.toSQL(p_oRecord.getString("sIDCodexx"));

        if (!p_bWithParent) p_oApp.beginTrans();
        if (p_oApp.executeQuery(lsSQL, MASTER_TABLE, p_sBranchCd, "") <= 0){
            if (!p_bWithParent) p_oApp.rollbackTrans();
            p_sMessage = p_oApp.getMessage() + ";" + p_oApp.getErrMsg();
            return false;
        }
        if (!p_bWithParent) p_oApp.commitTrans();
        
        if (p_oListener != null) p_oListener.MasterRetreive(6, "1");
        
        p_nEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    public boolean DeactivateRecord() throws SQLException{
        if (p_nEditMode != EditMode.READY){
            p_sMessage = "Invalid edit mode.";
            return false;
        }
        
        p_oRecord.first();
        
        if ("0".equals(p_oRecord.getString("cRecdStat"))){
            p_sMessage = "Record is inactive.";
            return false;
        }
        
        String lsSQL = "UPDATE " + MASTER_TABLE + " SET" +
                            "  cRecdStat = '0'" +
                            ", sModified = " + SQLUtil.toSQL(p_oApp.getUserID()) +
                            ", dModified = " + SQLUtil.toSQL(p_oApp.getServerDate()) +
                        " WHERE sIDCodexx = " + SQLUtil.toSQL(p_oRecord.getString("sIDCodexx"));

        if (!p_bWithParent) p_oApp.beginTrans();
        if (p_oApp.executeQuery(lsSQL, MASTER_TABLE, p_sBranchCd, "") <= 0){
            if (!p_bWithParent) p_oApp.rollbackTrans();
            p_sMessage = p_oApp.getMessage() + ";" + p_oApp.getErrMsg();
            return false;
        }
        if (!p_bWithParent) p_oApp.commitTrans();
        
        if (p_oListener != null) p_oListener.MasterRetreive(6, "0");
        
        p_nEditMode = EditMode.UNKNOWN;
        return true;
    }
    
    public boolean SearchRecord(String fsValue, boolean fbByCode) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        String lsSQL = getSQ_Record();
        
        if (p_bWithUI){
            JSONObject loJSON = showFXDialog.jsonSearch(
                                p_oApp, 
                                lsSQL, 
                                fsValue, 
                                "ID Code»ID Name", 
                                "sIDCodexx»sIDNamexx", 
                                "sIDCodexx»sIDNamexx", 
                                fbByCode ? 0 : 1);
            
            if (loJSON != null) 
                return OpenRecord((String) loJSON.get("sIDCodexx"));
            else {
                p_sMessage = "No record selected.";
                return false;
            }
        }
        
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "sIDCodexx = " + SQLUtil.toSQL(fsValue));   
        else {
            lsSQL = MiscUtil.addCondition(lsSQL, "sIDNamexx LIKE " + SQLUtil.toSQL(fsValue + "%")); 
            lsSQL += " LIMIT 1";
        }
        
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
        
        if (!loRS.next()){
            MiscUtil.close(loRS);
            p_sMessage = "No transaction found for the givern criteria.";
            return false;
        }
        
        lsSQL = loRS.getString("sIDCodexx");
        MiscUtil.close(loRS);
        
        return OpenRecord(lsSQL);
    }
    
    public boolean OpenRecord(String fsValue) throws SQLException{
        p_nEditMode = EditMode.UNKNOWN;
        
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
        lsSQL = MiscUtil.addCondition(getSQ_Record(), "sIDCodexx= " + SQLUtil.toSQL(fsValue));
        loRS = p_oApp.executeQuery(lsSQL);
        p_oRecord = factory.createCachedRowSet();
        p_oRecord.populate(loRS);
        MiscUtil.close(loRS);
        
        p_nEditMode = EditMode.READY;
        return true;
    }
    
    public boolean UpdateRecord() throws SQLException{
        if (p_nEditMode != EditMode.READY){
            p_sMessage = "Invalid edit mode.";
            return false;
        }
        
        p_nEditMode = EditMode.UPDATE;
        return true;
    }
    
    public Object getMaster(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oRecord.first();
        return p_oRecord.getObject(fnIndex);
    }
    
    public Object getMaster(String fsIndex) throws SQLException{
        return getMaster(getColumnIndex(fsIndex));
    }
    
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        p_oRecord.first();
        switch (fnIndex){
        
            case 1://sIDCodexx
            case 2://sIDNamexx
            case 3://sIDLabelx
            case 6://sModified
                p_oRecord.updateString(fnIndex, ((String) foValue).trim());
                p_oRecord.updateRow();

                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oRecord.getString(fnIndex));
                break;
            case 4: //cWithBack
            case 5: //cRecdStat
                if (foValue instanceof Integer)
                    p_oRecord.updateInt(fnIndex, (int) foValue);
                else 
                    p_oRecord.updateInt(fnIndex, 0);
                
                p_oRecord.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oRecord.getString(fnIndex));
                break;
        }
    }
    
    public void setMaster(String fsIndex, Object foValue) throws SQLException{        
        setMaster(getColumnIndex(fsIndex), foValue);
    }

    public void displayMasFields() throws SQLException{
        if (p_nEditMode != EditMode.ADDNEW && p_nEditMode != EditMode.UPDATE) return;
        
        int lnRow = p_oRecord.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + p_oRecord.getMetaData().getColumnLabel(lnCtr));
            if (p_oRecord.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                p_oRecord.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + p_oRecord.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
    }
    
    private int getColumnIndex(String fsValue) throws SQLException{
        int lnIndex = 0;
        int lnRow = p_oRecord.getMetaData().getColumnCount();
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            if (fsValue.equals(p_oRecord.getMetaData().getColumnLabel(lnCtr))){
                lnIndex = lnCtr;
                break;
            }
        }
        
        return lnIndex;
    }
    
    private boolean isEntryOK() throws SQLException{
        p_oRecord.first();
        
        if (p_oRecord.getString("sIDCodexx").isEmpty()){
            p_sMessage = "ID code must not be empty.";
            return false;
        }
        
        if (p_oRecord.getString("sIDNamexx").isEmpty()){
            p_sMessage = "ID name must not be empty.";
            return false;
        }
        
        
        return true;
    }
    
    private void initRecord() throws SQLException{
        RowSetMetaData meta = new RowSetMetaDataImpl();

        meta.setColumnCount(8);

        meta.setColumnName(1, "sIDCodexx");
        meta.setColumnLabel(1, "sIDCodexx");
        meta.setColumnType(1, Types.VARCHAR);
        meta.setColumnDisplaySize(1, 5);
        
        meta.setColumnName(2, "sIDNamexx");
        meta.setColumnLabel(2, "sIDNamexx");
        meta.setColumnType(2, Types.VARCHAR);
        meta.setColumnDisplaySize(2, 20);
        
        meta.setColumnName(3, "sIDLabelx");
        meta.setColumnLabel(3, "sIDLabelx");
        meta.setColumnType(3, Types.VARCHAR);
        meta.setColumnDisplaySize(3, 50);
        
        meta.setColumnName(4, "cWithExpr");
        meta.setColumnLabel(4, "cWithExpr");
        meta.setColumnType(4, Types.VARCHAR);
        meta.setColumnDisplaySize(4, 1);
        
        meta.setColumnName(5, "cWithBack");
        meta.setColumnLabel(5, "cWithBack");
        meta.setColumnType(5, Types.VARCHAR);
        meta.setColumnDisplaySize(5, 1);
        
        meta.setColumnName(6, "cRecdStat");
        meta.setColumnLabel(6, "cRecdStat");
        meta.setColumnType(6, Types.VARCHAR);
        meta.setColumnDisplaySize(6, 1);
        
        meta.setColumnName(7, "sModified");
        meta.setColumnLabel(7, "sModified");
        meta.setColumnType(7, Types.VARCHAR);
        meta.setColumnDisplaySize(7, 12);
        
        meta.setColumnName(8, "dModified");
        meta.setColumnLabel(8, "dModified");
        meta.setColumnType(8, Types.DATE);
        
        p_oRecord = new CachedRowSetImpl();
        p_oRecord.setMetaData(meta);
        
        p_oRecord.last();
        p_oRecord.moveToInsertRow();
        
        MiscUtil.initRowSet(p_oRecord);    
        
        p_oRecord.updateString("sIDCodexx", MiscUtil.getNextCode(MASTER_TABLE, "sIDCodexx", false, p_oApp.getConnection(), ""));

        p_oRecord.updateString("cWithBack", "0");
        p_oRecord.updateString("cRecdStat", RecordStatus.ACTIVE);
        
        p_oRecord.insertRow();
        p_oRecord.moveToCurrentRow();
    }
    
    private String getSQ_Record(){
        return "SELECT" +
                    "  sIDCodexx" +
                    ", sIDNamexx" +	
                    ", sIDLabelx" +
                    ", cWithExpr" +
                    ", cWithBack" +
                    ", cRecdStat" +
                    ", sModified" +
                    ", dModified" +	
                " FROM " + MASTER_TABLE;
        
    }
}
