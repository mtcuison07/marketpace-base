package org.rmj.marketplace.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.json.simple.JSONObject;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.ui.showFXDialog;
import org.rmj.appdriver.constants.EditMode;

/**
 * @author Michael Cuison
 * @since
 */
public class Clients {
    private final String MASTER_TABLE = "App_User_Master";
    private final String CLIENT_TABLE = "Client_Master";
    private final String ORDER_MASTER_TABLE = "sales_order_master";
    private final String ORDER_DETAIL_TABLE = "sales_order_detail";
    
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
    private LTransaction p_oListener;
   
    public Clients(GRider foApp, String fsBranchCd, boolean fbWithParent){        
        p_oApp = foApp;
        p_sBranchCd = fsBranchCd;
        p_bWithParent = fbWithParent;        
                
        if (p_sBranchCd.isEmpty()) p_sBranchCd = p_oApp.getBranchCode();
        
        p_nTranStat = 0;
        p_nEditMode = EditMode.UNKNOWN;
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
    
    public String getMessage(){
        return p_sMessage;
    }
    
    public boolean NewRecord(){
        p_sMessage = "This feature is not supported yet.";
        return false;
    }
    
    public boolean UpdateRecord(){
        p_sMessage = "This feature is not supported yet.";
        return false;
    }
    
    public boolean SaveRecord(){
        p_sMessage = "This feature is not supported yet.";
        return false;
    }
    
    public boolean LoadList(String fsValue) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";    
        
        String lsSQL = MiscUtil.addCondition(getSQ_Master(), "b.sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%"));
        
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
    
    public boolean SearchRecord(String fsValue, boolean fbByCode) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";    
        
        String lsSQL = getSQ_Master();
        
        if (p_bWithUI){
            JSONObject loJSON = showFXDialog.jsonSearch(
                                p_oApp, 
                                lsSQL, 
                                fsValue, 
                                "User ID»Name»", 
                                "sUserIDxx»xCompnyNm»sEmailAdd", 
                                "a.sUserIDxx»b.sCompnyNm»a.sEmailAdd", 
                                fbByCode ? 0 : 1);
            
            if (loJSON != null) 
                return OpenRecord((String) loJSON.get("sUserIDxx"), true);
            else {
                p_sMessage = "No record selected.";
                return false;
            }
        }
        
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sUserIDxx = " + SQLUtil.toSQL(fsValue));   
        else {
            if (!fsValue.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL, "b.sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%")); 
                lsSQL += " LIMIT 1";
            }
        }
        
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
        
        if (!loRS.next()){
            MiscUtil.close(loRS);
            p_sMessage = "No record found for the given criteria.";
            return false;
        }
        
        lsSQL = loRS.getString("sUserIDxx");
        MiscUtil.close(loRS);
        
        return OpenRecord(lsSQL, true);
    }
    
    public boolean OpenRecord(String fsValue, boolean fbByUserID) throws SQLException{
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
        if (fbByUserID)
            lsSQL = MiscUtil.addCondition(getSQ_Master(), "a.sUserIDxx = " + SQLUtil.toSQL(fsValue));
        else 
            lsSQL = MiscUtil.addCondition(getSQ_Master(), "sListngID = " + SQLUtil.toSQL(fsValue));
        
        loRS = p_oApp.executeQuery(lsSQL);
        p_oMaster = factory.createCachedRowSet();
        p_oMaster.populate(loRS);
        MiscUtil.close(loRS);
        
        p_oMaster.last();
        if (p_oMaster.getRow() <= 0) {
            p_sMessage = "No record was loaded.";
            return false;
        }
        
        p_nEditMode = EditMode.READY;
        
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
    public Object getDetailOrder(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oOrder.absolute(fnRow);
        return p_oOrder.getObject(fnIndex);
    }
    
    public Object getDetailOrder(int fnRow, String fsIndex) throws SQLException{
        return getDetailOrder(fnRow, getColumnIndex(p_oOrder, fsIndex));
    }
    
    public int getOrderDetailItemCount() throws SQLException{
        p_oOrder.last();
        return p_oOrder.getRow();
    }
    
    public void displayMasFields() throws SQLException{
        if (p_nEditMode != EditMode.ADDNEW && 
            p_nEditMode != EditMode.UPDATE &&
            p_nEditMode != EditMode.READY) return;
        
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
    
    private String getSQ_Master(){
        return "SELECT" +
                    "  a.sUserIDxx" +
                    ", a.sEmployNo" +
                    ", b.sLastName" +
                    ", b.sFrstName" +
                    ", b.sMiddName" +
                    ", b.sSuffixNm" +
                    ", b.cGenderCd" +
                    ", b.cCvilStat" +
                    ", b.sCitizenx" +
                    ", b.dBirthDte" +
                    ", b.sBirthPlc" +
                    ", b.sHouseNox" +
                    ", b.sAddressx" +
                    ", b.sTownIDxx" +
                    ", b.sBrgyIDxx" +
                    ", a.sMobileNo" +
                    ", a.sEmailAdd" +
                    ", b.sTaxIDNox" +
                    ", b.sCompnyNm xClientNm" +
                    ", IFNULL(c.sTownName, '') xTownName" + 
                    ", IFNULL(d.sTownName, '') xBirthPlc" +
                    ", IFNULL(e.sBrgyName, '') xBrgyName" + 
                    ", IFNULL(f.sNational, '') xNational" + 
                " FROM App_User_Master a" +
                    ", "+ CLIENT_TABLE +" b" +
                        " LEFT JOIN TownCity c ON b.sTownIDxx = c.sTownIDxx" +
                        " LEFT JOIN TownCity d ON b.sBirthPlc = d.sTownIDxx" +
                        " LEFT JOIN Barangay e ON b.sBrgyIDxx = e.sBrgyIDxx" +
                        " LEFT JOIN Country f ON b.sCitizenx = f.sCntryCde" +
                      
                " WHERE a.sEmployNo = b.sClientID" +
                    " AND a.sProdctID = 'GuanzonApp'";
    }
    
     private String getSQ_OrderMaster(){
        return "SELECT " +
                "  sTransNox," +
                "  dTransact, " +
                "  sTermCode," +
                "  nTranTotl," +
                "  nVATRatex," +
                "  nDiscount," +
                "  nAddDiscx," +
                "  nFreightx," +
                "  nAmtPaidx," +
                "  cTranStat," +
                "  sRemarksx" +
                "  FROM " + ORDER_MASTER_TABLE +
                " WHERE cTranStat = 2 ";
         
    }
    public boolean LoadOrder(String fsCLientID) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";    
        String lsSQL = "";
        lsSQL = getSQ_OrderMaster()+ " AND sClientID = " + SQLUtil.toSQL(fsCLientID);
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
    public boolean LoadOrderDetail(String fsTransNox) throws SQLException{
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
     
    private String getSQ_OrderDetail(){
        return "SELECT " +
                    "  a.sTransNox, " +
                    "  IFNULL(d.sBarrcode, '') xBarCodex, " +
                    "  IFNULL(d.sDescript, '') xDescript, " +
                    "  IFNULL(e.sBrandNme, '') xBrandNme, " +
                    "  IFNULL(f.sModelNme, '') xModelNme, " +
                    "  IFNULL(g.sColorNme, '') xColorNme, " +
                    "  a.nEntryNox, " +
                    "  a.nQuantity, " +
                    "  a.nUnitPrce, " +
                    "  a.sReferNox, " +
                    "  a.nIssuedxx " +
                    "FROM " + ORDER_DETAIL_TABLE + " a " +
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
