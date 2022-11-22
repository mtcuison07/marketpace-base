/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.rmj.marketplace.base;

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

/**
 *
 * @author User
 */
public class Pickup {
    private final String MASTER_TABLE = "ECommerce_Pickup_Master";
    private final String ORDER_TABLE = "ECommerce_Order_Master";
    private final String ODETAIL_TABLE = "ECommerce_Order_Detail";
    
    private final GRider p_oApp;
    private final boolean p_bWithParent;    
    
    private String p_sBranchCd;
    
    private int p_nEditMode;
    private int p_nTranStat;

    private String p_sMessage;
    private boolean p_bWithUI = true;

    private CachedRowSet p_oMaster;
    private CachedRowSet p_oDetail;
    private CachedRowSet p_oWaybill;
    private LTransaction p_oListener;
    private LResult p_oResult;
   
    public Pickup(GRider foApp, String fsBranchCd, boolean fbWithParent){        
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
    
    public void setListener(LResult foValue){
        p_oResult = foValue;
    }
    
    public void setWithUI(boolean fbValue){
        p_bWithUI = fbValue;
    }
    
    public int getEditMode(){
        return p_nEditMode;
    }
    
    
    private void createMaster() throws SQLException{
        RowSetMetaData meta = new RowSetMetaDataImpl();

        meta.setColumnCount(27);

        meta.setColumnName(1, "sBatchNox");
        meta.setColumnLabel(1, "sBatchNox");
        meta.setColumnType(1, Types.VARCHAR);
        meta.setColumnDisplaySize(1, 12);

        meta.setColumnName(2, "dTransact");
        meta.setColumnLabel(2, "dTransact");
        meta.setColumnType(2, Types.DATE);
        
        meta.setColumnName(3, "sRemarksx");
        meta.setColumnLabel(3, "sRemarksx");
        meta.setColumnType(3, Types.VARCHAR);
        meta.setColumnDisplaySize(3, 256);
        
        meta.setColumnName(4, "nEntryNox");
        meta.setColumnLabel(4, "nEntryNox");
        meta.setColumnType(4, Types.INTEGER);
        
        meta.setColumnName(5, "nEntryByx");
        meta.setColumnLabel(5, "nEntryByx");
        meta.setColumnType(5, Types.VARCHAR);
        meta.setColumnDisplaySize(5, 10);
        
        meta.setColumnName(6, "dEntryDte");
        meta.setColumnLabel(6, "dEntryDte");
        meta.setColumnType(6, Types.TIMESTAMP);
        
        meta.setColumnName(7, "dPickedUp");
        meta.setColumnLabel(7, "dPickedUp");
        meta.setColumnType(7, Types.TIMESTAMP);
        
        meta.setColumnName(8, "sPickedBy");
        meta.setColumnLabel(8, "sPickedBy");
        meta.setColumnType(8, Types.VARCHAR);
        meta.setColumnDisplaySize(8, 10);
        
        meta.setColumnName(9, "cTranStat");
        meta.setColumnLabel(9, "cTranStat");
        meta.setColumnType(9, Types.CHAR);
        meta.setColumnDisplaySize(9, 1);
        
        meta.setColumnName(10, "dModified");
        meta.setColumnLabel(10, "dModified");
        meta.setColumnType(10, Types.TIMESTAMP);
        
        p_oMaster = new CachedRowSetImpl();
        p_oMaster.setMetaData(meta);
    }
    
    private void createWaybill() throws SQLException{
        RowSetMetaData meta = new RowSetMetaDataImpl();

        meta.setColumnCount(8);

        meta.setColumnName(1, "sTransNox");
        meta.setColumnLabel(1, "sTransNox");
        meta.setColumnType(1, Types.VARCHAR);
        meta.setColumnDisplaySize(1, 12);

        meta.setColumnName(2, "sCompnyNm");
        meta.setColumnLabel(2, "sCompnyNm");
        meta.setColumnType(2, Types.VARCHAR);
        
        meta.setColumnName(3, "sOrderNox");
        meta.setColumnLabel(3, "sOrderNox");
        meta.setColumnType(3, Types.VARCHAR);
        
        meta.setColumnName(4, "sTrackrNo");
        meta.setColumnLabel(4, "sTrackrNo");
        meta.setColumnType(4, Types.VARCHAR);
        
        meta.setColumnName(5, "sPackngDs");
        meta.setColumnLabel(5, "sPackngDs");
        meta.setColumnType(5, Types.VARCHAR);
        
        meta.setColumnName(6, "dTransact");
        meta.setColumnLabel(6, "dTransact");
        meta.setColumnType(6, Types.DATE);
        
        meta.setColumnName(7, "sBatchNox");
        meta.setColumnLabel(7, "sBatchNox");
        meta.setColumnType(7, Types.VARCHAR);
        
        meta.setColumnName(8, "xReferNox");
        meta.setColumnLabel(8, "xReferNox");
        meta.setColumnType(8, Types.VARCHAR);
        
        p_oWaybill = new CachedRowSetImpl();
        p_oWaybill.setMetaData(meta);
    }
       
    public int getItemCount() throws SQLException{
        p_oMaster.last();
        return p_oMaster.getRow();
    }
     public Object getMaster(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oMaster.first();
        return p_oMaster.getObject(fnIndex);
    }
    
    public Object getMaster(String fsIndex) throws SQLException{
        return getMaster(getColumnIndex(p_oMaster, fsIndex));
    }
    
    public Object getDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oMaster.absolute(fnRow);
        return p_oMaster.getObject(fnIndex);
    }
    
    public Object getDetail(int fnRow, String fsIndex) throws SQLException{
        return getDetail(fnRow, getColumnIndex(p_oMaster, fsIndex));
    }
    
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        if (p_nEditMode != EditMode.ADDNEW && p_nEditMode != EditMode.UPDATE) {
            System.out.println("Invalid Edit Mode Detected.");
            return;
        }
        
        p_oMaster.first();
        
        switch (fnIndex){
            case 2:
            case 4:
                if (foValue instanceof Date){
                    p_oMaster.updateDate(fnIndex, SQLUtil.toDate((Date) foValue));
                } else
                    p_oMaster.updateDate(fnIndex, SQLUtil.toDate(p_oApp.getServerDate()));
                
                p_oMaster.updateRow();
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
            case 1: 
            case 3:
            case 5:
                p_oMaster.updateString(fnIndex, (String) foValue);
                p_oMaster.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
            
            case 6: 
            
                if (foValue instanceof Double)
                    p_oMaster.updateDouble(fnIndex, (double) foValue);
                else 
                    p_oMaster.updateDouble(fnIndex, 0.000);
                
                
                p_oMaster.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
          
        }
    }
     public void setMaster(String fsIndex, Object foValue) throws SQLException{
        setMaster(getColumnIndex(p_oMaster, fsIndex), foValue);
    }
    public int getItemCountWaybill() throws SQLException{
        p_oWaybill.last();
        return p_oWaybill.getRow();
    }
    public Object getWaybill(int fnRow, String fnIndex) throws SQLException{
        return getWaybill(fnRow,getColumnIndex(p_oWaybill, fnIndex));
    }
    
    public Object getWaybill(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oWaybill.absolute(fnRow);
        return p_oWaybill.getObject(fnIndex);
    }
    
    
    public String getMessage(){
        return p_sMessage;
    }
    public boolean LoadList() throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";    
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
    public boolean OpenTransaction(String fsTransNox) throws SQLException{
        p_nEditMode = EditMode.UNKNOWN;
        
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
         p_sMessage = "";  
        String lsSQL = "";
        lsSQL = getSQ_Waybill()+ " AND b.sBatchNox = " + SQLUtil.toSQL(fsTransNox);
        lsSQL = lsSQL + getSQ_Waybill1()+ " AND b.sBatchNox = " + SQLUtil.toSQL(fsTransNox);
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
       
        RowSetFactory factory = RowSetProvider.newFactory();
        p_oWaybill = factory.createCachedRowSet();
        p_oWaybill.populate(loRS);
        MiscUtil.close(loRS);  
        
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
            lsCondition = "a.cTranStat IN (" + lsSQL.substring(2) + ")";
        } else 
            lsCondition = "a.cTranStat = " + SQLUtil.toSQL(lsStat);
          
        lsSQL = "SELECT " +
                    " IFNULL(a.sBatchNox,'') sBatchNox " +
                    ", IFNULL(a.dTransact,'') dTransact " +
                    ", IFNULL(a.sRemarksx,'') sRemarksx " +
                    ", IFNULL(a.dPickedUp,'') dPickedUp " +
                    ", IFNULL(a.sPickedBy,'') sPickedBy " +
                    ", IFNULL(a.cTranStat,0) cTranStat " +
                " FROM ECommerce_Pickup_Master a   " +
                ", ECommerce_Order_Master b   " +
                ", Client_Master c   " +
                ", App_User_Profile d   " +
                " WHERE  a.cTranStat IN ('0')  " +
                " AND a.sBatchNox = b.sBatchNox  " +
                " AND c.sClientID = b.sClientID  " +
                " UNION SELECT " +
                    "  IFNULL(a.sBatchNox,'') sBatchNox " +
                    ", IFNULL(a.dTransact,'') dTransact " +
                    ", IFNULL(a.sRemarksx,'') sRemarksx " +
                    ", IFNULL(a.dPickedUp,'') dPickedUp " +
                    ", IFNULL(a.sPickedBy,'') sPickedBy " +
                    ", IFNULL(a.cTranStat,0) cTranStat " +
                    " FROM ECommerce_Pickup_Master a   " +
                ", ECommerce_Order_Master b   " +
                ", App_User_Profile c   " +
                " WHERE  a.cTranStat IN ('0')  " +
                " AND a.sBatchNox = b.sBatchNox  " +
                " AND c.sUserIDxx = b.sAppUsrID  " +
                " ORDER BY dTransact DESC";
               
        return lsSQL;
    }
     public String getSQ_Waybill(){
        String lsSQL = "";
           
        lsSQL = "SELECT" +
            "  IFNULL(a.sTransNox, '') sTransNox, " +
            "  CONCAT(IFNULL(c.sFrstName,''), ' ', IFNULL(c.sMiddName,''),' ',IFNULL(c.sLastName,'')) AS sCompnyNm " +
            ", IFNULL(b.sOrderNox, '') sOrderNox " +
            ", IFNULL(a.sTrackrNo, '') sTrackrNo " +
            ", IFNULL(d.sPackngDs, '') sPackngDs " +
            ", IFNULL(b.dTransact, '') dTransact " +
            ", IFNULL(b.sBatchNox, '') sBatchNox " +
            ", IFNULL(b.sTransNox, '') xReferNox " +
            " FROM ECommerce_Order_Waybill a" +
            ", ECommerce_Order_Master b" +
            ", Client_Master c" +
            ", ECommerce_Packaging d" +
         " WHERE a.sTransNox = b.sWaybilNo " +
            " AND b.sClientID = c.sClientID " +
            " AND a.sPackngCD = d.sPackngCD "  ;
               
        return lsSQL;
    }
     public String getSQ_Waybill1(){
        String lsSQL = "";
           
        lsSQL = " UNION SELECT" +
            "  IFNULL(a.sTransNox, '') sTransNox, " +
            "  CONCAT(IFNULL(c.sFrstName,''), ' ', IFNULL(c.sMiddName,''),' ',IFNULL(c.sLastName,'')) AS sCompnyNm " +
            ", IFNULL(b.sOrderNox, '') sOrderNox " +
            ", IFNULL(a.sTrackrNo, '') sTrackrNo " +
            ", IFNULL(d.sPackngDs, '') sPackngDs " +
            ", IFNULL(b.dTransact, '') dTransact " +
            ", IFNULL(b.sBatchNox, '') sBatchNox " +
            ", IFNULL(b.sTransNox, '') xReferNox " +
            " FROM ECommerce_Order_Waybill a" +
            ", ECommerce_Order_Master b" +
            ", App_User_Profile c" +
            ", ECommerce_Packaging d" +
         " WHERE a.sTransNox = b.sWaybilNo " +
            " AND b.sAppUsrID = c.sUserIDxx " +
            " AND a.sPackngCD = d.sPackngCD "  ;
               
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
}