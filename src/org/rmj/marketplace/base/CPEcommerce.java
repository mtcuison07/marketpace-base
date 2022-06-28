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
import org.json.simple.JSONObject;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.ui.showFXDialog;
import org.rmj.appdriver.constants.EditMode;
import org.rmj.appdriver.constants.UserRight;

/**
 *
 * @author User
 */
public class CPEcommerce {
    private final String MASTER_TABLE = "sales_order_master";
    private final String DETAIL_TABLE = "sales_order_detail";
    private final String PAYMENT_TABLE = "other_payment_trans";
    
    private final GRider p_oApp;
    private final boolean p_bWithParent;
    
    private String p_sBranchCd;
    
    private int p_nEditMode;
    private int p_nTranStat;

    private String p_sMessage;
    private boolean p_bWithUI = true;

    private CachedRowSet p_oMaster;
    private CachedRowSet p_oDetail;
    private CachedRowSet p_oIssuance;
    private CachedRowSet p_oPayment;
    private LTransaction p_oListener;
    private LResult p_oResult;
   
    public CPEcommerce(GRider foApp, String fsBranchCd, boolean fbWithParent){        
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
    
    public boolean LoadList(String fsTransNox, boolean fbByCode) throws SQLException{
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
    public boolean LoadOrderDetail(String fsTransNox, boolean fbByCode) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";    
        
        String lsSQL = "";
        lsSQL = getSQ_Detail()+ " WHERE a.sTransNox = " + SQLUtil.toSQL(fsTransNox);
       
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
            case 2:
                if (foValue instanceof Date){
                    p_oMaster.updateDate(fnIndex, SQLUtil.toDate((Date) foValue));
                } else
                    p_oMaster.updateDate(fnIndex, SQLUtil.toDate(p_oApp.getServerDate()));
                
                p_oMaster.updateRow();
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
            case 1: 
            case 3:
            case 11:
                p_oMaster.updateString(fnIndex, (String) foValue);
                p_oMaster.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
            case 4:
            case 5:
            case 6: 
            case 7:
            case 8:
            case 9:
                if (foValue instanceof Double)
                    p_oMaster.updateDouble(fnIndex, (double) foValue);
                else 
                    p_oMaster.updateDouble(fnIndex, 0.000);
                
                
                p_oMaster.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
            case 10:
                if (foValue instanceof Integer)
                    p_oMaster.updateInt(fnIndex, (int) foValue);
                else 
                    p_oMaster.updateInt(fnIndex, 0);
                
                p_oMaster.updateRow();
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
    
    public int getDetailItemCount() throws SQLException{
        p_oDetail.last();
        return p_oDetail.getRow();
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
    
    public int getPaymentItemCount() throws SQLException{
        if (p_oPayment == null) return 0;
        
        p_oPayment.last();
        return p_oPayment.getRow();
    }
    
    public Object getPayment(int fnRow, int fnIndex) throws SQLException{
        if (getPaymentItemCount()  == 0) return null;
        
        if (getPaymentItemCount() == 0 || fnRow > getPaymentItemCount()) return null;   
       
        p_oPayment.absolute(fnRow);
        return p_oPayment.getObject(fnIndex);
        
    }
    
    public Object getPayment(int fnRow, String fsIndex) throws SQLException{
        return getPayment(fnRow, getColumnIndex(p_oPayment, fsIndex));
    }
    
    public void setPayment(int fnRow, String fsIndex, Object foValue) throws SQLException{
        setPayment(fnRow, getColumnIndex(p_oPayment, fsIndex), foValue);
    }
    
    public void setPayment(int fnRow, int fnIndex, Object foValue) throws SQLException{
        if (p_nEditMode != EditMode.ADDNEW && p_nEditMode != EditMode.UPDATE) {
            System.out.println("Invalid Edit Mode Detected.");
            return;
        }
        //p_oPayment.first();
        p_oPayment.absolute(fnRow);
        
        switch (fnIndex){
            case 6: //sRemarksx
                p_oPayment.updateString(fnIndex, (String) foValue);
                p_oPayment.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oPayment.getString(fnIndex));
                break;
            case 9:
                if (foValue instanceof Integer){
                    p_oPayment.updateInt(fnIndex, (int) foValue);
                    p_oPayment.updateRow();
                }                
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oPayment.getString(fnIndex));
                break;
            case 10:
                if (foValue instanceof Date){
                    p_oPayment.updateDate(fnIndex, SQLUtil.toDate((Date) foValue));
                } else
                    p_oPayment.updateDate(fnIndex, SQLUtil.toDate(p_oApp.getServerDate()));
                
                p_oPayment.updateRow();
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oPayment.getString(fnIndex));
                break;
        }
    }
    
    private boolean loadPayment(String fsTransNox) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";    
        String lsSQL = getSQ_Payment()+ " AND sSourceNo = " + SQLUtil.toSQL(fsTransNox);
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
        
        RowSetFactory factory = RowSetProvider.newFactory();
        p_oPayment = factory.createCachedRowSet();
        
        if (MiscUtil.RecordCount(loRS) == 0){
            MiscUtil.close(loRS);
            p_sMessage = "No record found for the given criteria.";
            return false;
        }
        
        p_oPayment.populate(loRS);
        MiscUtil.close(loRS);
        return true;
    }
    
    private boolean isEntryOK(int lnRow) throws SQLException{           
        //validate master               
        if ("".equals((String) getPayment(lnRow,"sTransNox"))){
            p_sMessage = "No order was selected to list.";
            return false;
        }
        
//        if ("".equals((String) getPayment(lnRow,"sRemarksx"))){
//            p_sMessage = "Payment remarks is not set.";
//            return false;
//        }
        
        return true;
    }
    public String getSQ_Payment(){
        String lsSQL = "";
        lsSQL = "SELECT " +
                "  IFNULL(sTransNox, '') sTransNox" +
                ", IFNULL(dTransact, '') dTransact" +
                ", IFNULL(sReferCde, '') sReferCde" +
                ", IFNULL(sReferNox, '') sReferNox" +
                ", IFNULL(nAmountxx, '') nAmountxx" +
                ", IFNULL(sRemarksx, '') sRemarksx" +
                ", IFNULL(sSourceCd, '') sSourceCd" +
                ", IFNULL(sSourceNo, '') sSourceNo" +
                ", IFNULL(cTranStat, '') cTranStat" +
                ", IFNULL(dModified, '') dModified" +
                "  FROM " + PAYMENT_TABLE +
                " WHERE sSourceCD = 'MPSO' ";
        return lsSQL;
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
                "  c.sTownName" +
                "  FROM " + MASTER_TABLE +" a " +
                "LEFT JOIN Client_Master b " +
                "	ON a.sClientID = b.sClientID " +
                "LEFT JOIN TownCity c " + 
                "ON b.sTownIDxx = c.sTownIDxx " + 
                " WHERE " + lsCondition;
        return lsSQL;
    }
    
    public String getSQ_Detail(){
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
                    "FROM "+DETAIL_TABLE+" a " +
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
    public String getItem(){
        String lsSQL = "";
        lsSQL = "SELECT " +
            "  h.sSerialNo, " +
            "  d.sBarrcode xBarCodex, " +
            "  d.sDescript xDescript, " +
            "  IFNULL(b.nQtyOnHnd,0) nQtyOnHnd, " +
            "  IFNULL(a.nQuantity,1) nQuantity, " +
            "  IFNULL(b.nUnitPrce,0) nUnitPrce, " +
            "  h.sSerialID,  e.sBrandNme, " +
            "  f.sModelNme, " +
            "  g.sColorNme  " +
            " FROM mp_inv_master b " +
            " LEFT JOIN sales_order_detail a " +
            "    ON b.sStockIDx = a.sStockIDx " +
            "  LEFT JOIN inv_category c " +
            "    ON b.sCategrID = c.sCategrID " +
            "  LEFT JOIN CP_Inventory d " +
            "    ON d.sStockIDx = b.sStockIDx " +
            "  LEFT JOIN CP_Brand e " +
            "    ON d.sBrandIDx = e.sBrandIDx " +
            "  LEFT JOIN CP_Model f " +
            "    ON d.sModelIDx = f.sModelIDx " +
            "  LEFT JOIN color g " +
            "    ON d.sColorIDx = g.sColorIDx, " +
            "  CP_Inventory_Serial h " +
            " WHERE d.sStockIDx = h.sStockIDx " +
            "    AND d.sModelIDx = f.sModelIDx " +
            "    AND d.sBrandIDx = e.sBrandIDx ";
        return lsSQL;
    }
    public String getGiveaways(){
        String lsSQL = "";
        lsSQL = "SELECT " +
            "  h.sSerialNo, " +
            "  d.sBarrcode xBarCodex, " +
            "  d.sDescript xDescript, " +
            "  IFNULL(b.nQtyOnHnd,0) nQtyOnHnd, " +
            "  IFNULL(a.nQuantity,1) nQuantity, " +
            "  IFNULL(b.nUnitPrce,0) nUnitPrce, " +
            "  h.sSerialID,  e.sBrandNme, " +
            "  f.sModelNme, " +
            "  g.sColorNme  " +
            " FROM mp_inv_master b " +
            " LEFT JOIN sales_order_detail a " +
            "    ON b.sStockIDx = a.sStockIDx " +
            "  LEFT JOIN inv_category c " +
            "    ON b.sCategrID = c.sCategrID " +
            "  LEFT JOIN CP_Inventory d " +
            "    ON d.sStockIDx = b.sStockIDx " +
            "  LEFT JOIN CP_Brand e " +
            "    ON d.sBrandIDx = e.sBrandIDx " +
            "  LEFT JOIN CP_Model f " +
            "    ON d.sModelIDx = f.sModelIDx " +
            "  LEFT JOIN color g " +
            "    ON d.sColorIDx = g.sColorIDx, " +
            "  CP_Inventory_Serial h " +
            " WHERE d.sStockIDx = h.sStockIDx " +
            "    AND d.sModelIDx = f.sModelIDx " +
            "    AND d.sBrandIDx = e.sBrandIDx ";
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
        
        
        
        int lnCtr = 1;
        int lnRow;
        String lsSQL;
        
        lnRow = getPaymentItemCount();
        while(lnCtr <= lnRow ){
            setPayment(lnCtr, "dModified", p_oApp.getServerDate().toString());
            String transNox = (String)getPayment(lnCtr, "sTransNox");
            
            if (!isEntryOK(lnCtr)) return false;
            lsSQL = MiscUtil.rowset2SQL(p_oPayment, 
                                        PAYMENT_TABLE, 
                                        "",
                                        " sTransNox = " + SQLUtil.toSQL(transNox) 
                                        + " AND sSourceNo = " + SQLUtil.toSQL(getPayment(lnCtr, "sSourceNo")));
            
            if (!lsSQL.isEmpty()){
                
                if (!p_bWithParent) p_oApp.beginTrans();
                if (!lsSQL.isEmpty()){
                    if (p_oApp.executeQuery(lsSQL, MASTER_TABLE, p_sBranchCd, transNox.substring(0, 4)) <= 0){
                        if (!p_bWithParent) p_oApp.rollbackTrans();
                        p_sMessage = p_oApp.getMessage() + ";" + p_oApp.getErrMsg();
                        return false;
                    }
                }
                
                p_nEditMode = EditMode.UNKNOWN;
                
                if (!p_bWithParent) p_oApp.commitTrans();
                if (p_oResult != null) p_oResult.OnSave("Transaction save successfully.");
                return true;
            }
            lnCtr++;
        }
            
        
        return true;
    }
    
    public boolean SearchTransaction(String fsValue, boolean fbByCode) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";    
        
        String lsSQL = "";
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(getItem(), " d.sBarrcode = " + SQLUtil.toSQL(fsValue));   
        else {
            if (!fsValue.isEmpty()) {
                lsSQL = MiscUtil.addCondition(getItem(), " d.sBarrcode LIKE " + SQLUtil.toSQL(fsValue + "%")); 
                
            }
        }
        if (p_bWithUI){
            JSONObject loJSON = showFXDialog.jsonSearch(
                                p_oApp, 
                                getItem(), 
                                fsValue,
                                "Serial No.»Barrcode»Description»Qty on Hand", 
                                "h.sSerialNo»xBarCodex»xDescript»nQtyOnHnd", 
                                "h.sSerialNo»d.sBarrcode»d.sDescript»b.nQtyOnHnd", 
                                fbByCode ? 0 : 1);
            
            if (loJSON != null) 
                return OpenItem((String) loJSON.get("sSerialNo"));
            else {
                p_sMessage = "No record selected.";
                return false;
            }
        }
        
       
        
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
        
        if (!loRS.next()){
            MiscUtil.close(loRS);
            p_sMessage = "No transaction found for the givern criteria.";
            return false;
        }
        
        lsSQL = loRS.getString("sSerialNo");
        MiscUtil.close(loRS);
        
        return OpenItem(lsSQL);
    }
    
    
    public boolean SearchItem(String fsValue, boolean fbByCode) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";    
        
        String lsSQL = "";
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(getItem(), " d.sBarrcode = " + SQLUtil.toSQL(fsValue));   
        else {
            if (!fsValue.isEmpty()) {
                lsSQL = MiscUtil.addCondition(getItem(), " d.sBarrcode LIKE " + SQLUtil.toSQL(fsValue + "%")); 
                
            }
        }
        if (p_bWithUI){
            JSONObject loJSON = showFXDialog.jsonSearch(
                                p_oApp, 
                                getItem(), 
                                fsValue,
                                "Serial No.»Barrcode»Description»Qty on Hand", 
                                "h.sSerialNo»xBarCodex»xDescript»nQtyOnHnd", 
                                "h.sSerialNo»d.sBarrcode»d.sDescript»b.nQtyOnHnd", 
                                fbByCode ? 0 : 1);
            
            if (loJSON != null) 
                return OpenItem((String) loJSON.get("sSerialNo"));
            else {
                p_sMessage = "No record selected.";
                return false;
            }
        }
        
       
        
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
        
        if (!loRS.next()){
            MiscUtil.close(loRS);
            p_sMessage = "No transaction found for the givern criteria.";
            return false;
        }
        
        lsSQL = loRS.getString("sSerialNo");
        MiscUtil.close(loRS);
        
        return OpenItem(lsSQL);
    }
    
    public boolean SearchGiveaways(String fsValue, boolean fbByCode) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";    
        
        String lsSQL = "";
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(getGiveaways(), " d.sBarrcode = " + SQLUtil.toSQL(fsValue));   
        else {
            if (!fsValue.isEmpty()) {
                lsSQL = MiscUtil.addCondition(getGiveaways(), " d.sBarrcode LIKE " + SQLUtil.toSQL(fsValue + "%")); 
                
            }
        }
        if (p_bWithUI){
            JSONObject loJSON = showFXDialog.jsonSearch(
                                p_oApp, 
                                getGiveaways(), 
                                fsValue,
                                "Serial No.»Barrcode»Description»Qty on Hand", 
                                "h.sSerialNo»xBarCodex»xDescript»nQtyOnHnd", 
                                "h.sSerialNo»d.sBarrcode»d.sDescript»b.nQtyOnHnd", 
                                fbByCode ? 0 : 1);
            
            if (loJSON != null) 
                return OpenItem((String) loJSON.get("sSerialNo"));
            else {
                p_sMessage = "No record selected.";
                return false;
            }
        }
        
       
        
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
        
        if (!loRS.next()){
            MiscUtil.close(loRS);
            p_sMessage = "No transaction found for the givern criteria.";
            return false;
        }
        
        lsSQL = loRS.getString("sSerialNo");
        MiscUtil.close(loRS);
        
        return OpenItem(lsSQL);
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
        
        lsSQL = getSQ_Detail()+ " WHERE a.sTransNox = " + SQLUtil.toSQL(fsTransNox);
        
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
        
        loadPayment(fsTransNox);
        
        p_nEditMode = EditMode.READY;
        
        return true;
    }
    
    public boolean OpenItem(String fsTransNox) throws SQLException{
        p_nEditMode = EditMode.UNKNOWN;
        
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        
        p_sMessage = "";
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        lsSQL = getItem()+ " AND h.sSerialNo LIKE " + SQLUtil.toSQL(fsTransNox + "%");
        
        //open master
        loRS = p_oApp.executeQuery(lsSQL);
        p_oIssuance = factory.createCachedRowSet();
        p_oIssuance.populate(loRS);
        MiscUtil.close(loRS);
        
        p_oIssuance.last();
        if (p_oIssuance.getRow() <= 0) {
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
    public int getIssuanceItemCount() throws SQLException{
        if (p_oIssuance == null) return 0;
        
        p_oIssuance.last();
        return p_oIssuance.getRow();
    }
    public Object getIssuance(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oIssuance.first();
        return p_oIssuance.getObject(fnIndex);
    }
    
    public Object getIssuance(String fsIndex) throws SQLException{
        return getIssuance(getColumnIndex(p_oIssuance, fsIndex));
    }
    public Object getIssuance(int fnRow, String fsIndex) throws SQLException{
        return getIssuance(getColumnIndex(p_oIssuance, fsIndex));
    }
    
    public void setIssuance(int fnRow, String fsIndex, Object foValue) throws SQLException{
        setPayment(fnRow, getColumnIndex(p_oIssuance, fsIndex), foValue);
    }
    
    public void setIssuance(int fnRow, int fnIndex, Object foValue) throws SQLException{
        if (p_nEditMode != EditMode.ADDNEW && p_nEditMode != EditMode.UPDATE) {
            System.out.println("Invalid Edit Mode Detected.");
            return;
        }
        //p_oPayment.first();
        p_oIssuance.absolute(fnRow);
        
        switch (fnIndex){
            case 4: //nQtyOnHnd
            case 5: //nQuantity
                if (foValue instanceof Integer){
                    p_oIssuance.updateInt(fnIndex, (int) foValue);
                }else 
                    p_oMaster.updateDouble(fnIndex, 0);       
                
                 p_oIssuance.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oIssuance.getString(fnIndex));
                break;
                
            case 6: //nUnitPrce
                if (foValue instanceof Double)
                    p_oMaster.updateDouble(fnIndex, (double) foValue);
                else 
                    p_oMaster.updateDouble(fnIndex, 0.000);
                
                p_oIssuance.updateRow();
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oIssuance.getString(fnIndex));
                break;
                
            case 1:
            case 2:
            case 3:
            case 7:
            case 8:
            case 9:
            case 10:
                p_oIssuance.updateString(fnIndex, (String) foValue);
                p_oIssuance.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oIssuance.getString(fnIndex));
                break;
            
        }
    }
    
}
      