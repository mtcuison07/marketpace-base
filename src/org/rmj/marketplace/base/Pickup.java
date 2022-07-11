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
    private final String ORDER_TABLE = "sales_order_master";
    private final String ODETAIL_TABLE = "sales_order_detail";
    
    private final GRider p_oApp;
    private final boolean p_bWithParent;    
    
    private String p_sBranchCd;
    
    private int p_nEditMode;
    private int p_nTranStat;

    private String p_sMessage;
    private boolean p_bWithUI = true;

    private CachedRowSet p_oMaster;
    private CachedRowSet p_oDetail;
    private CachedRowSet p_oOrder;
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
    
    public boolean NewTransaction() throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        createMaster();

        p_nEditMode = EditMode.ADDNEW;
        return true;
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
        
        p_oMaster.last();
        p_oMaster.moveToInsertRow();
        
        MiscUtil.initRowSet(p_oMaster);       
        
        p_oMaster.updateObject("sBatchNox", MiscUtil.getNextCode(MASTER_TABLE, "sBatchNox", true, p_oApp.getConnection(), p_sBranchCd));
        p_oMaster.updateObject("dPickedUp", p_oApp.getServerDate());
        p_oMaster.updateObject("sRemarksx", "");
        p_oMaster.updateObject("dTransact", p_oApp.getServerDate());
        p_oMaster.updateObject("cTranStat", "2");
        
        p_oMaster.insertRow();
        p_oMaster.moveToCurrentRow();
    }
    public boolean LoadOrder(String fsTransNox) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";    
        ResultSet loRS = p_oApp.executeQuery(getSQ_OrderMaster());
        if (MiscUtil.RecordCount(loRS) == 0){
            MiscUtil.close(loRS);
            p_sMessage = "No record found for the given criteria.";
            return false;
        }
        
        RowSetFactory factory = RowSetProvider.newFactory();
        p_oOrder = factory.createCachedRowSet();
        p_oOrder.populate(loRS);
        MiscUtil.close(loRS);
        
        return true;
    }
    public boolean LoadOrderDetail(String fsTransNox, boolean fbByCode) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";    
        
        String lsSQL = "";
        lsSQL = getSQ_OrderDetail()+ " WHERE a.sTransNox = " + SQLUtil.toSQL(fsTransNox);
       
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
        if (MiscUtil.RecordCount(loRS) == 0){
            MiscUtil.close(loRS);
            p_sMessage = "No record found for the given criteria.";
            return false;
        }
        
        RowSetFactory factory = RowSetProvider.newFactory();
        p_oOrder = factory.createCachedRowSet();
        p_oOrder.populate(loRS);
        MiscUtil.close(loRS);
        
        return true;
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
        
        switch (fnIndex){
           
            case 1: //sBatchNox
            case 3: //sRemarksx
            case 5: //sEntryByx
            case 8: //sPickedBy
                p_oMaster.updateString(fnIndex, (String) foValue);
                p_oMaster.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
            case 4:
            case 9:
                if (foValue instanceof Integer)
                    p_oMaster.updateInt(fnIndex, (int) foValue);
                else 
                    p_oMaster.updateInt(fnIndex, 0);
                
                p_oMaster.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
             case 2: //dTransact
             case 6: //dEntryDte
             case 7: //dPickedUp
             case 10: //dModified
                if (foValue instanceof Date){
                    p_oMaster.updateDate(fnIndex, SQLUtil.toDate((Date) foValue));
                } else
                    p_oMaster.updateDate(fnIndex, SQLUtil.toDate(p_oApp.getServerDate()));
                
                p_oMaster.updateRow();
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
        }
    }
     public void setMaster(String fsIndex, Object foValue) throws SQLException{
        setMaster(getColumnIndex(p_oMaster, fsIndex), foValue);
    }
    
    
    
    public void setOrder(int fnIndex, Object foValue) throws SQLException{
        if (p_nEditMode != EditMode.ADDNEW && p_nEditMode != EditMode.UPDATE) {
            System.out.println("Invalid Edit Mode Detected.");
            return;
        }
        
        p_oOrder.first();
        
        switch (fnIndex){
            case 2:
                if (foValue instanceof Date){
                    p_oOrder.updateDate(fnIndex, SQLUtil.toDate((Date) foValue));
                } else
                    p_oOrder.updateDate(fnIndex, SQLUtil.toDate(p_oApp.getServerDate()));
                
                p_oOrder.updateRow();
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oOrder.getString(fnIndex));
                break;
            case 1: 
            case 3:
            case 11:
                p_oOrder.updateString(fnIndex, (String) foValue);
                p_oOrder.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oOrder.getString(fnIndex));
                break;
            case 4:
            case 5:
            case 6: 
            case 7:
            case 8:
            case 9:
                if (foValue instanceof Double)
                    p_oOrder.updateDouble(fnIndex, (double) foValue);
                else 
                    p_oOrder.updateDouble(fnIndex, 0.000);
                
                
                p_oOrder.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oOrder.getString(fnIndex));
                break;
            case 10:
                if (foValue instanceof Integer)
                    p_oOrder.updateInt(fnIndex, (int) foValue);
                else 
                    p_oOrder.updateInt(fnIndex, 0);
                
                p_oOrder.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oOrder.getString(fnIndex));
                break;
        }
    }
     public void setOrder(String fsIndex, Object foValue) throws SQLException{
        setOrder(getColumnIndex(p_oOrder, fsIndex), foValue);
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
   
      public Object getOrder(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oOrder.absolute(fnRow);
        return p_oOrder.getObject(fnIndex);
    }
    
    public Object getOrder(int fnRow, String fsIndex) throws SQLException{
        return getOrder(fnRow, getColumnIndex(p_oOrder, fsIndex));
    }
    public int getOrderItemCount() throws SQLException{
        p_oOrder.last();
        return p_oOrder.getRow();
    }
    
    public Object getOrderDetail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oOrder.absolute(fnRow);
        return p_oOrder.getObject(fnIndex);
    }
    
    public Object getOrderDetail(int fnRow, String fsIndex) throws SQLException{
        return getOrderDetail(fnRow, getColumnIndex(p_oOrder, fsIndex));
    }
    public int getDetailItemCount() throws SQLException{
        p_oOrder.last();
        return p_oOrder.getRow();
    }
    
    
    public Object getWaybill(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oWaybill.first();
        return p_oWaybill.getObject(fnIndex);
    }
    
    public Object getWaybill(String fsIndex) throws SQLException{
        return getWaybill(getColumnIndex(p_oWaybill, fsIndex));
    }
    
    public void setWaybill(int fnIndex, Object foValue) throws SQLException{
        if (p_nEditMode != EditMode.ADDNEW && p_nEditMode != EditMode.UPDATE) {
            System.out.println("Invalid Edit Mode Detected.");
            return;
        }
        
        p_oWaybill.first();
        
        switch (fnIndex){
           
            case 1: //sBatchNox
            case 3: //sRemarksx
            case 5: //sEntryByx
            case 8: //sPickedBy
                p_oWaybill.updateString(fnIndex, (String) foValue);
                p_oWaybill.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oWaybill.getString(fnIndex));
                break;
            case 4:
            case 9:
                if (foValue instanceof Integer)
                    p_oWaybill.updateInt(fnIndex, (int) foValue);
                else 
                    p_oWaybill.updateInt(fnIndex, 0);
                
                p_oWaybill.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oWaybill.getString(fnIndex));
                break;
             case 2: //dTransact
             case 6: //dEntryDte
             case 7: //dPickedUp
             case 10: //dModified
                if (foValue instanceof Date){
                    p_oWaybill.updateDate(fnIndex, SQLUtil.toDate((Date) foValue));
                } else
                    p_oWaybill.updateDate(fnIndex, SQLUtil.toDate(p_oApp.getServerDate()));
                
                p_oWaybill.updateRow();
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oWaybill.getString(fnIndex));
                break;
        }
    }
     public void setWaybill(String fsIndex, Object foValue) throws SQLException{
        setWaybill(getColumnIndex(p_oWaybill, fsIndex), foValue);
    }
    
    public int getWaybillItemCount() throws SQLException{
        if (p_oWaybill == null) return 0;
        
        p_oWaybill.last();
        return p_oWaybill.getRow();
    }
    
    public Object getWaybill(int fnRow, int fnIndex) throws SQLException{
        if (getWaybillItemCount()  == 0) return null;
        
        if (getWaybillItemCount() == 0 || fnRow > getWaybillItemCount()) return null;   
       
        p_oWaybill.absolute(fnRow);
        return p_oWaybill.getObject(fnIndex);
        
    }
    
    public Object getWaybill(int fnRow, String fsIndex) throws SQLException{
        return getWaybill(fnRow, getColumnIndex(p_oWaybill, fsIndex));
    }
    
    public void setWaybill(int fnRow, String fsIndex, Object foValue) throws SQLException{
        setWaybill(fnRow, getColumnIndex(p_oWaybill, fsIndex), foValue);
    }
    
    public void setWaybill(int fnRow, int fnIndex, Object foValue) throws SQLException{
        if (p_nEditMode != EditMode.ADDNEW && p_nEditMode != EditMode.UPDATE) {
            System.out.println("Invalid Edit Mode Detected.");
            return;
        }
        //p_oWaybill.first();
        p_oWaybill.absolute(fnRow);
        
        switch (fnIndex){
            case 6: //sRemarksx
                p_oWaybill.updateString(fnIndex, (String) foValue);
                p_oWaybill.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oWaybill.getString(fnIndex));
                break;
            case 9:
                if (foValue instanceof Integer){
                    p_oWaybill.updateInt(fnIndex, (int) foValue);
                    p_oWaybill.updateRow();
                }                
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oWaybill.getString(fnIndex));
                break;
            case 10:
                if (foValue instanceof Date){
                    p_oWaybill.updateDate(fnIndex, SQLUtil.toDate((Date) foValue));
                } else
                    p_oWaybill.updateDate(fnIndex, SQLUtil.toDate(p_oApp.getServerDate()));
                
                p_oWaybill.updateRow();
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oWaybill.getString(fnIndex));
                break;
        }
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
    
    private boolean loadWaybill(String fsValue) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";    
        System.out.println();
        String lsSQL = getSQ_Detail() + " AND b.sTransNox = " + SQLUtil.toSQL(fsValue);
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
        
        RowSetFactory factory = RowSetProvider.newFactory();
        p_oWaybill = factory.createCachedRowSet();
        
        if (MiscUtil.RecordCount(loRS) == 0){
            MiscUtil.close(loRS);
            p_sMessage = "No record found for the given criteria.";
            return false;
        }
        
        p_oWaybill.populate(loRS);
        MiscUtil.close(loRS);
        return true;
    }
    public String getSQ_OrderMaster(){
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
                "  a.sTransNox," +
                "  a.dTransact, " +
                "  a.sTermCode," +
                "  a.nTranTotl," +
                "  a.nVATRatex," +
                "  a.nDiscount," +
                "  a.nAddDiscx," +
                "  a.nFreightx," +
                "  a.nAmtPaidx," +
                "  a.cTranStat," +
                "  a.sRemarksx," +
                "  CONCAT(b.sFrstName, ' ', b.sMiddName,' ', b.sLastName) AS sCompnyNm," +
                "  b.sAddressx," +
                "  c.sTownName," +
                "  b.sMobileNo," +
                "  b.sEmailAdd  " +
                "  FROM " + ORDER_TABLE +" a " +
                "LEFT JOIN Client_Master b " +
                "	ON a.sClientID = b.sClientID " +
                "LEFT JOIN TownCity c " + 
                "ON b.sTownIDxx = c.sTownIDxx " + 
                " WHERE " + lsCondition ;
        return lsSQL;
    }
    public String getSQ_Master(){
        String lsSQL = "";
          
        lsSQL = "SELECT" +
                  " sBatchNox" +
                  ", dTransact" +
                  ", sRemarksx" +
                  ", dPickedUp" +
                  ", sPickedBy" +
                  ", cTranStat" +
               " FROM ECommerce_Pickup_Master a" +
                  " LEFT JOIN Ecommerce_Packaging b" +
                     " ON a.sPackngCD = b.sPackngCD ";
               
        return lsSQL;
    }
    
    public String getSQ_Detail(){
        String lsSQL = "";
        
        lsSQL = "SELECT" +
               "  a.sTransNox" +
               ",  CONCAT(c.sFrstName, ' ', c.sMiddName,' ', c.sLastName) AS sCompnyNm" +
               ", d.sPackngDs" +
               ", b.dTransact" +
               ", b.sTransNox xReferNox" +
            " FROM ECommerce_Order_Waybill a" +
               ", sales_order_master b" +
               ", Client_Master c" +
               ", ECommerce_Packaging d" +
            " WHERE a.sTransNox = b.sTransNox" +
               " AND b.sClientID = c.sClientID" +
               " AND a.sPackngCD = d.sPackngCD" +
               " AND b.cTranStat = '3'";
               
        return lsSQL;
    }
    
    public String getSQ_OrderDetail(){
        String lsSQL = "";
        lsSQL = "SELECT " +
                    "  a.sTransNox, " +
                    "  d.sBarrcode xBarCodex, " +
                    "  d.sDescript xDescript, " +
                    "  IFNULL(e.sBrandNme, '') xBrandNme, " +
                    "  IFNULL(f.sModelNme, '') xModelNme, " +
                    "  IFNULL(g.sColorNme, '') xColorNme, " +
                    "  a.nEntryNox, " +
                    "  a.nQuantity, " +
                    "  a.nUnitPrce, " +
                    "  a.sReferNox, " +
                    "  a.nIssuedxx " +
                    "FROM "+ODETAIL_TABLE+" a " +
                    "LEFT JOIN mp_inv_master b " +
                    "	ON a.sStockIDx = b.sListngID " +
                    "LEFT JOIN inv_category c " +
                    "	ON b.sCategrID = c.sCategrID " +
                    "LEFT JOIN  CP_Inventory d " +
                    "	ON d.sStockIDx = a.sStockIDx " +
                    "LEFT JOIN CP_Brand e " +
                    "	ON d.sBrandIDx = e.sBrandIDx " +
                    "LEFT JOIN CP_Model f " +
                    "	ON d.sModelIDx = f.sModelIDx " +
                    "LEFT JOIN color g " +
                    "	ON d.sColorIDx = g.sColorIDx";
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
    
    public boolean SearchOrderTransaction(String fsValue, boolean fbByCode) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";    
        
        String lsSQL = MiscUtil.addCondition(getSQ_OrderMaster(), "sBranchCd = " + SQLUtil.toSQL(p_sBranchCd));
        
        if (p_bWithUI){
            JSONObject loJSON = showFXDialog.jsonSearch(
                                p_oApp, 
                                getSQ_OrderMaster(),  
                                fsValue, 
                                "Order No.»Customer Name", 
                                "sTransNox»sCompnyNm", 
                                "sTransNox»sCompnyNm", 
                                fbByCode ? 0 : 1);
            
            if (loJSON != null) 
                return OpenOrderTransaction((String) loJSON.get("sTransNox"));
            else {
                p_sMessage = "No record selected.";
                return false;
            }
        }
        
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "sTransNox = " + SQLUtil.toSQL(fsValue));   
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
        
        lsSQL = loRS.getString("sTransNox");
        MiscUtil.close(loRS);
        
        return OpenOrderTransaction(lsSQL);
    }
    
    public boolean OpenOrderTransaction(String fsTransNox) throws SQLException{
        p_nEditMode = EditMode.UNKNOWN;
        
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        
        p_sMessage = "";
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        lsSQL = getSQ_OrderDetail()+ " WHERE a.sTransNox = " + SQLUtil.toSQL(fsTransNox);
        
        //open master
        loRS = p_oApp.executeQuery(lsSQL);
        p_oOrder = factory.createCachedRowSet();
        p_oOrder.populate(loRS);
        MiscUtil.close(loRS);
        
        p_oOrder.last();
        if (p_oOrder.getRow() <= 0) {
            p_sMessage = "No transaction was loaded.";
            return false;
        }
        
        loadWaybill(fsTransNox);
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


    private boolean isEntryOK() throws SQLException{           
        //validate master               
        if ("".equals((String) getMaster("sBatchNox"))){
            p_sMessage = "Invalid Batch Number Detected!!! \n Verify your Entries then Try Again!!!";
            return false;
        }
        
        
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
        
        
        int lnCtr = 1;
        int lnRow;
        String lsSQL;
        
        lnRow = getOrderItemCount();
//        while(lnCtr <= lnRow ){
//            setOrder(lnCtr, "dModified", p_oApp.getServerDate().toString());
//            String transNox = (String)getPayment(lnCtr, "sTransNox");
//            
//            if (!isEntryOK(lnCtr)) return false;
//            lsSQL = MiscUtil.rowset2SQL(p_oPayment, 
//                                        PAYMENT_TABLE, 
//                                        "",
//                                        " sTransNox = " + SQLUtil.toSQL(transNox) 
//                                        + " AND sSourceNo = " + SQLUtil.toSQL(getPayment(lnCtr, "sSourceNo")));
//            
//            if (!lsSQL.isEmpty()){
//                
//                if (!p_bWithParent) p_oApp.beginTrans();
//                if (!lsSQL.isEmpty()){
//                    if (p_oApp.executeQuery(lsSQL, MASTER_TABLE, p_sBranchCd, transNox.substring(0, 4)) <= 0){
//                        if (!p_bWithParent) p_oApp.rollbackTrans();
//                        p_sMessage = p_oApp.getMessage() + ";" + p_oApp.getErrMsg();
//                        return false;
//                    }
//                }
//                
//                p_nEditMode = EditMode.UNKNOWN;
//                
//                if (!p_bWithParent) p_oApp.commitTrans();
//                if (p_oResult != null) p_oResult.OnSave("Transaction save successfully.");
//                return true;
//            }
//            lnCtr++;
//        }
            
        
        return true;
    }

}
