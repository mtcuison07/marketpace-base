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
import javax.sql.rowset.RowSetProvider;
import javax.sql.rowset.RowSetMetaDataImpl;
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
public class ClientProfiling {
    
    private final String MASTER_ID_TABLE = "App_User_Master_ID";
    private final String MASTER_TABLE = "App_User_Master";
    private final String USER_PICTURE_TABLE = "App_User_Picture";
    private final String USER_PROFILE_TABLE = "App_User_Profile";
    
    private final String USER_EMAIL_TABLE = "App_User_Email";
    private final String USER_MOBILE_TABLE = "App_User_Mobile";
    
    private final GRider p_oApp;
    private final boolean p_bWithParent;
    
    private String p_sBranchCd;
    
    private int p_nEditMode;
    private int p_nTranStat;

    private String p_sMessage;
    private boolean p_bWithUI = true;

    private CachedRowSet p_oMaster;
    private CachedRowSet p_oDetail;
    private CachedRowSet p_oVerify;
    private CachedRowSet p_oMasterID;
    private CachedRowSet p_oPicture;
    private CachedRowSet p_oProfile;
    private CachedRowSet p_oEmail;
    private CachedRowSet p_oMobile;
    private LTransaction p_oListener;
    private LResultTown p_oTownListener;
    
    public ClientProfiling(GRider foApp, String fsBranchCd, boolean fbWithParent){        
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
    public void setListener(LResultTown foValue){
        p_oTownListener = foValue;
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
            case 1: //sUserIDxx
            case 2: //sIDCodex1
            case 3: //sIDCodex2
                p_oMaster.updateString(fnIndex, (String) foValue);
                p_oMaster.updateRow();

                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMaster.getString(fnIndex));
                break;
        }
    }
    
    public void setMaster(String fsIndex, Object foValue) throws SQLException{
        setMaster(getColumnIndex(p_oMaster, fsIndex), foValue);
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
    
    private boolean createDetail() throws SQLException{
        RowSetMetaData meta = new RowSetMetaDataImpl();        

        meta.setColumnCount(11);
        
        meta.setColumnName(1, "sUserIDxx");
        meta.setColumnLabel(1, "sUserIDxx");
        meta.setColumnType(1, Types.VARCHAR);
        meta.setColumnDisplaySize(1, 12);
        
        meta.setColumnName(2, "sUserName");
        meta.setColumnLabel(2, "sUserName");
        meta.setColumnType(2, Types.VARCHAR);
        meta.setColumnDisplaySize(1, 64);
        
        meta.setColumnName(3, "sEmailAdd");
        meta.setColumnLabel(3, "sEmailAdd");
        meta.setColumnType(3, Types.VARCHAR);
        meta.setColumnDisplaySize(3, 64);
        
        
        meta.setColumnName(4, "cIDVerify");
        meta.setColumnLabel(4, "cIDVerify");
        meta.setColumnType(4, Types.VARCHAR);
        meta.setColumnDisplaySize(4, 1);
        
        
        meta.setColumnName(5, "cPrVerify");
        meta.setColumnLabel(5, "cPrVerify");
        meta.setColumnType(5, Types.VARCHAR);
        meta.setColumnDisplaySize(5, 1);
        
        meta.setColumnName(6, "cPcVerify");
        meta.setColumnLabel(6, "cPcVerify");
        meta.setColumnType(6, Types.VARCHAR);
        meta.setColumnDisplaySize(6, 1);
        
        meta.setColumnName(7, "cEmVerify");
        meta.setColumnLabel(7, "cEmVerify");
        meta.setColumnType(7, Types.VARCHAR);
        meta.setColumnDisplaySize(7, 1);
        
        meta.setColumnName(8, "cMoVerify");
        meta.setColumnLabel(8, "cMoVerify");
        meta.setColumnType(8, Types.VARCHAR);
        meta.setColumnDisplaySize(8, 1);
        
        meta.setColumnName(9, "sMobileNo");
        meta.setColumnLabel(9, "sMobileNo");
        meta.setColumnType(9, Types.VARCHAR);
        meta.setColumnDisplaySize(9, 13);
        
        meta.setColumnName(10, "sAddress1");
        meta.setColumnLabel(10, "sAddress1");
        meta.setColumnType(10, Types.VARCHAR);
        meta.setColumnDisplaySize(10, 256);
        
        meta.setColumnName(11, "sAddress2");
        meta.setColumnLabel(11, "sAddress2");
        meta.setColumnType(11, Types.VARCHAR);
        meta.setColumnDisplaySize(11, 256);
        
        p_oDetail = new CachedRowSetImpl();
        p_oDetail.setMetaData(meta);        
        
        
        p_sMessage = ""; 
        
        String lsSQL = "SELECT" +
                        "  sUserIDxx" +
                        " FROM App_User_Master " +
                        " WHERE sProdctID = 'GuanzonApp' " +
                        " AND cMPCLient = 1 " +
                        " ORDER BY dTimeStmp DESC";
        
//        p_oMaster.first();
//        if (!p_oMaster.getString("sUserIDxx").isEmpty())
//        lsSQL = MiscUtil.addCondition(lsSQL, "sUserIDxx = " + SQLUtil.toSQL(p_oMaster.getString("sUserIDxx")));
        
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
        
        int lnRow = 1;
        
        while (loRS.next()){
            
            ResultSet loRS1 = p_oApp.executeQuery(getSQ_Master(loRS.getString("sUserIDxx")));
            while(loRS1.next()){
                p_oDetail.last();
                p_oDetail.moveToInsertRow(); //add new
                
                MiscUtil.initRowSet(p_oDetail);    //clear data    
                p_oDetail.updateString("sUserIDxx", loRS1.getString("sUserIDxx"));
                p_oDetail.updateString("sUserName", loRS1.getString("sUserName"));
                p_oDetail.updateString("sEmailAdd", loRS1.getString("sEmailAdd"));
                p_oDetail.updateString("cIDVerify", loRS1.getString("cIDVerify"));
                p_oDetail.updateString("cPrVerify", loRS1.getString("cPrVerify"));
                p_oDetail.updateString("cPcVerify", loRS1.getString("cPcVerify"));
                p_oDetail.updateString("cEmVerify", loRS1.getString("cEmVerify"));
                p_oDetail.updateString("cMoVerify", loRS1.getString("cMoVerify"));
                p_oDetail.updateString("sMobileNo", loRS1.getString("sMobileNo"));
                p_oDetail.updateString("sAddress1", loRS1.getString("sAddress1"));
                p_oDetail.updateString("sAddress2", loRS1.getString("sAddress2"));

                p_oDetail.insertRow();
                p_oDetail.moveToCurrentRow(); //save data
                
            }
            
        
           
            
            MiscUtil.close(loRS1);
            lnRow++;
        }
        if (MiscUtil.RecordCount(loRS) == 0){
            MiscUtil.close(loRS);
            p_sMessage = "No record found for the given criteria.";
            return false;
        }
        MiscUtil.close(loRS);
        return true;
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
    public boolean UpdateRecord() throws SQLException{
        if (p_nEditMode != EditMode.READY){
            p_sMessage = "Invalid edit mode.";
            return false;
        }
        
        p_nEditMode = EditMode.UPDATE;
        return true;
    }
    
    private String getSQ_Master(String fsValue){
        String lsSQL = "";
        lsSQL = "SELECT   " +
                "  a.sUserIDxx,  " +
                "  a.sUserName,    " +
                "  a.sEmailAdd,  " +
                "  IFNULL(b.cVerified,'0') cPrVerify,  " +
                "  IFNULL(c.cVerified,'0') cPcVerify,  " +
                "  IFNULL(d.cVerified,'0') cIDVerify,  " +
                "  IFNULL(e.cVerified,'0') cEmVerify,  " +
                "  IFNULL(f.cVerified,'0') cMoVerify,  " +
                "  IFNULL(f.sMobileNo,'') sMobileNo,  " +
                "  IFNULL(b.sAddress1,'') sAddress1,   " +
                "  IFNULL(b.sAddress2,'') sAddress2 " +
                "FROM App_User_Master a  " +
                "  LEFT JOIN (SELECT  " +
                "     g.sUserIDxx, " +
                "     g.cVerified, " +
                "     CONCAT(IFNULL(g.sHouseNo1,''), ' ', IFNULL(g.sAddress1,''),' ',IFNULL(i.sBrgyName,''), ' ',IFNULL(h.sTownName,'')) AS sAddress1, " +
                "     CONCAT(IFNULL(g.sHouseNo2,''), ' ', IFNULL(g.sAddress2,''),' ',IFNULL(k.sBrgyName,''), ' ',IFNULL(j.sTownName,'')) AS sAddress2  " +
                "   FROM App_User_Profile g " +
                "     LEFT JOIN TownCity h " +
                "       ON  g.sTownIDx1  = h.sTownIDxx " +
                "     LEFT JOIN Barangay i " +
                "       ON  g.sBrgyIDx1  = i.sBrgyIDxx " +
                "     LEFT JOIN TownCity j " +
                "       ON  g.sTownIDx2  = j.sTownIDxx " +
                "     LEFT JOIN Barangay k " +
                "       ON  g.sBrgyIDx2  = k.sBrgyIDxx " +
                "     WHERE sUserIDxx =  "+ SQLUtil.toSQL(fsValue) + "  ORDER BY dTransact DESC LIMIT 1) AS b " +
                "      ON a.sUserIDxx = b.sUserIDxx     " +
                "  LEFT JOIN (SELECT sUserIDxx,cVerified FROM App_User_Picture WHERE sUserIDxx = "+ SQLUtil.toSQL(fsValue) + "  ORDER BY dTransact DESC LIMIT 1) c " +
                "    ON a.sUserIDxx = c.sUserIDxx     " +
                "  LEFT JOIN (SELECT sUserIDxx,cVerified FROM App_User_Master_ID WHERE sUserIDxx = "+ SQLUtil.toSQL(fsValue) + "  ORDER BY dTransact DESC LIMIT 1) d " +
                "    ON a.sUserIDxx = d.sUserIDxx     " +
                "  LEFT JOIN (SELECT sUserIDxx,cVerified FROM App_User_Email WHERE sUserIDxx = "+ SQLUtil.toSQL(fsValue) + " ORDER BY dTransact DESC LIMIT 1) e " +
                "    ON a.sUserIDxx = e.sUserIDxx     " +
                "  LEFT JOIN (SELECT sUserIDxx,sMobileNo,cVerified FROM App_User_Mobile WHERE sUserIDxx = "+ SQLUtil.toSQL(fsValue) + " ORDER BY dTransact DESC LIMIT 1) f " +
                "    ON a.sUserIDxx = f.sUserIDxx     " +
                "WHERE a.sProdctID = 'GuanzonApp' " +
                "  AND a.sUserIDxx = " + SQLUtil.toSQL(fsValue);
        return lsSQL;
    }
    public boolean LoadList() throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }  
        
        return createDetail();
    }
    

    public Object getMasterID(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oMasterID.absolute(fnRow);
        return p_oMasterID.getObject(fnIndex);
    }
    
    public Object getMasterID(int fnRow, String fsIndex) throws SQLException{
        return getMasterID(fnRow, getColumnIndex(p_oMasterID, fsIndex));
    }
    
    public Object getMasterID(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oMasterID.first();
        return p_oMasterID.getObject(fnIndex);
    }
    
    public Object getMasterID(String fsIndex) throws SQLException{
        return getMasterID(getColumnIndex(p_oMasterID, fsIndex));
    }
    
    public void setMasterID(int fnIndex, Object foValue) throws SQLException{
        if (p_nEditMode != EditMode.ADDNEW && p_nEditMode != EditMode.UPDATE) {
            System.out.println("Invalid Edit Mode Detected.");
            return;
        }
        
        p_oMasterID.first();
        
        switch (fnIndex){
            case 2: //dTransact
            case 12: //dVerified
                if (foValue instanceof Date){
                    p_oMasterID.updateDate(fnIndex, SQLUtil.toDate((Date) foValue));
                } else
                    p_oMasterID.updateDate(fnIndex, SQLUtil.toDate(p_oApp.getServerDate()));
                
                p_oMasterID.updateRow();
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMasterID.getString(fnIndex));
                break;
            case 1: //sUserIDxx
            case 3: //sIDCodex1
            case 4: //sIDNoxxx1
            case 5: //sIDFrntx1
            case 6: //sIDBackx1
            case 7: //sIDCodex2
            case 8: //sIDNoxxx2
            case 9: //sIDFrntx2
            case 10: //sIDBackx2
            case 13: //sVerified
                p_oMasterID.updateString(fnIndex, (String) foValue);
                p_oMasterID.updateRow();

                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMasterID.getString(fnIndex));
                break;
            case 11: //cVerified
                if (foValue instanceof Integer)
                    p_oMasterID.updateInt(fnIndex, (int) foValue);
                else 
                    p_oMasterID.updateInt(fnIndex, 0);
                
                p_oMasterID.updateRow();
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMasterID.getString(fnIndex));
                break;
        }
    }
    
    public int getMasterIDItemCount() throws SQLException{
        if (p_oMasterID == null) return 0;
        
        p_oMasterID.last();
        return p_oMasterID.getRow();
    }
    
    public void setMasterID(String fsIndex, Object foValue) throws SQLException{
        setMasterID(getColumnIndex(p_oMasterID, fsIndex), foValue);
    }
    private String getSQ_MasterID(){
        String lsSQL = "";
        
        String lsCondition = "";
        String lsStat = String.valueOf(p_nTranStat);
        
        if (lsStat.length() > 1){
            for (int lnCtr = 0; lnCtr <= lsStat.length()-1; lnCtr++){
                lsSQL += ", " + SQLUtil.toSQL(Character.toString(lsStat.charAt(lnCtr)));
            }
            lsCondition = " cVerified IN (" + lsSQL.substring(2) + ")";
        } else 
            lsCondition = " cVerified = " + SQLUtil.toSQL(lsStat);
           
        
        lsSQL = "SELECT " +
                    "  IFNULL(a.sUserIDxx, '') sUserIDxx " +
                    ", IFNULL(a.dTransact, '') dTransact " +
                    ", IFNULL(a.sIDCodex1, '') sIDCodex1 " +
                    ", IFNULL(a.sIDNoxxx1, '') sIDNoxxx1 " +
                    ", IFNULL(a.sIDFrntx1, '') sIDFrntx1 " +
                    ", IFNULL(a.sIDBackx1, '') sIDBackx1 " +
                    ", IFNULL(a.dIDExpry1, '') dIDExpry1 " +
                    ", IFNULL(a.sIDCodex2, '') sIDCodex2 " +
                    ", IFNULL(a.sIDNoxxx2, '') sIDNoxxx2 " +
                    ", IFNULL(a.sIDFrntx2, '') sIDFrntx2 " +
                    ", IFNULL(a.sIDBackx2, '') sIDBackx2 " +
                    ", IFNULL(a.dIDExpry2, '') dIDExpry2 " +
                    ", IFNULL(a.cVerified, '0') cVerified " +
                    ", IFNULL(a.dVerified, '') dVerified " +
                    ", IFNULL(a.sVerified, '') sVerified " + 
                    ", IFNULL(b.sIDNamexx, '') sIDNamex1 " +
                    ", IFNULL(c.sIDNamexx, '') sIDNamex2 " + 
                " FROM " + MASTER_ID_TABLE + " a " +
                "   LEFT JOIN Identification b " +
                "       ON a.sIDCodex1 = b.sIDCodexx " +
                "   LEFT JOIN Identification c " +
                "       ON a.sIDCodex2 = c.sIDCodexx " +
                " WHERE ";
        return lsSQL;
    }
    

    public Object getUserPicture(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oPicture.absolute(fnRow);
        return p_oPicture.getObject(fnIndex);
    }
    
    public Object getUserPicture(int fnRow, String fsIndex) throws SQLException{
        return getUserPicture(fnRow, getColumnIndex(p_oPicture, fsIndex));
    }
    
    public Object getUserPicture(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oPicture.first();
        return p_oPicture.getObject(fnIndex);
    }
    
    public Object getUserPicture(String fsIndex) throws SQLException{
        return getUserPicture(getColumnIndex(p_oPicture, fsIndex));
    }
    
    public int getUserPictureItemCount() throws SQLException{
        if (p_oPicture == null) return 0;
        
        p_oPicture.last();
        return p_oPicture.getRow();
    }
    
    public void setUserPicture(int fnIndex, Object foValue) throws SQLException{
        if (p_nEditMode != EditMode.ADDNEW && p_nEditMode != EditMode.UPDATE) {
            System.out.println("Invalid Edit Mode Detected.");
            return;
        }
        
        p_oPicture.first();
        
        switch (fnIndex){
            case 2: //dTransact
            case 6: //dImgeDate
            case 9: //dVerified
                if (foValue instanceof Date){
                    p_oPicture.updateDate(fnIndex, SQLUtil.toDate((Date) foValue));
                } else
                    p_oPicture.updateDate(fnIndex, SQLUtil.toDate(p_oApp.getServerDate()));
                
                p_oPicture.updateRow();
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oPicture.getString(fnIndex));
                break;
            case 1: //sUserIDxx
            case 3: //sLastName
            case 4: //sFrstName
            case 5: //sMiddName
            case 10: //sBirthPlc
                p_oPicture.updateString(fnIndex, (String) foValue);
                p_oPicture.updateRow();

                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oPicture.getString(fnIndex));
                break;
            case 7: //cGenderCd
            case 8: //cCvilStat
                if (foValue instanceof Integer)
                    p_oPicture.updateInt(fnIndex, (int) foValue);
                else 
                    p_oPicture.updateInt(fnIndex, 0);
                
                p_oPicture.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oPicture.getString(fnIndex));
                break;
        }
    }
    
    public void setUserPicture(String fsIndex, Object foValue) throws SQLException{
        setUserPicture(getColumnIndex(p_oPicture, fsIndex), foValue);
    }
    private String getSQ_Picture(){
        String lsSQL = "";
        
        String lsCondition = "";
        String lsStat = String.valueOf(p_nTranStat);
        
        if (lsStat.length() > 1){
            for (int lnCtr = 0; lnCtr <= lsStat.length()-1; lnCtr++){
                lsSQL += ", " + SQLUtil.toSQL(Character.toString(lsStat.charAt(lnCtr)));
            }
            lsCondition = " cVerified IN (" + lsSQL.substring(2) + ")";
        } else 
            lsCondition = " cVerified = " + SQLUtil.toSQL(lsStat);
           
        lsSQL =  "SELECT" +
                    "  IFNULL(sUserIDxx, '') sUserIDxx " +
                    ", IFNULL(dTransact, '') dTransact " +
                    ", IFNULL(sImageNme, '') sImageNme " +
                    ", IFNULL(sMD5Hashx, '') sMD5Hashx " +
                    ", IFNULL(sImagePth, '') sImagePth " +
                    ", IFNULL(dImgeDate, '') dImgeDate " +
                    ", IFNULL(cImgeStat, '0') cImgeStat " +
                    ", IFNULL(cVerified, '0') cVerified " +
                    ", IFNULL(dVerified, '') dVerified " +
                    ", IFNULL(sVerified, '') sVerified " +
                " FROM " + USER_PICTURE_TABLE + 
                " WHERE ";
        return lsSQL;
    }
    
    
    public Object getUserProfile(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oProfile.absolute(fnRow);
        return p_oProfile.getObject(fnIndex);
    }
    
    public Object getUserProfile(int fnRow, String fsIndex) throws SQLException{
        return getUserProfile(fnRow, getColumnIndex(p_oProfile, fsIndex));
    }
    public Object getUserProfile(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oProfile.first();
        return p_oProfile.getObject(fnIndex);
    }
    
    public Object getUserProfile(String fsIndex) throws SQLException{
        return getUserProfile(getColumnIndex(p_oProfile, fsIndex));
    }
    
    public int getUserProfileItemCount() throws SQLException{
        if (p_oProfile == null) return 0;
        
        p_oProfile.last();
        return p_oProfile.getRow();
    }
    
    public void setUserProfile(int fnIndex, Object foValue) throws SQLException{
        if (p_nEditMode != EditMode.ADDNEW && p_nEditMode != EditMode.UPDATE) {
            System.out.println("Invalid Edit Mode Detected.");
            return;
        }
        
        p_oProfile.first();
        
        switch (fnIndex){
            case 2: //dTransact
            case 10: //dBirthDte
            case 22: //dVerified
                if (foValue instanceof Date){
                    p_oProfile.updateDate(fnIndex, SQLUtil.toDate((Date) foValue));
                } else
                    p_oProfile.updateDate(fnIndex, SQLUtil.toDate(p_oApp.getServerDate()));
                
                p_oProfile.updateRow();
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oProfile.getString(fnIndex));
                break;
            case 1: //sUserIDxx
            case 3: //sLastName
            case 4: //sFrstName
            case 5: //sMiddName
            case 6: //sMaidenNm
            case 7: //sSuffixNm
            case 11: //sBirthPlc
            case 12: //sHouseNo1
            case 13: //sAddress1
            case 14: //sBrgyIDx1
            case 15: //sTownIDx1
            case 16: //sHouseNo2
            case 17: //sAddress2
            case 18: //sBrgyIDx2
            case 19: //sTownIDx2
            case 20: //sClientID
            case 23: //sVerified
            case 24: //sTownNme1 
            case 25: //sTownNme2 
            case 26: //sBrgyNme1 
            case 27: //sBrgyNme2 
            case 28: //strBrhPlc 
                p_oProfile.updateString(fnIndex, (String) foValue);
                p_oProfile.updateRow();

                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oProfile.getString(fnIndex));
                break;
            case 8: //cGenderCd
            case 9: //cCvilStat
            case 21: //cVerified
                if (foValue instanceof Integer)
                    p_oProfile.updateInt(fnIndex, (int) foValue);
                else 
                    p_oProfile.updateInt(fnIndex, 0);
                
                p_oProfile.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oProfile.getString(fnIndex));
                break;
        }
    }
    
    public void setUserProfile(String fsIndex, Object foValue) throws SQLException{
        setUserProfile(getColumnIndex(p_oProfile, fsIndex), foValue);
    }
    private String getSQ_Profile(){
        String lsSQL = "";
        
        String lsCondition = "";
        String lsStat = String.valueOf(p_nTranStat);
        
        if (lsStat.length() > 1){
            for (int lnCtr = 0; lnCtr <= lsStat.length()-1; lnCtr++){
                lsSQL += ", " + SQLUtil.toSQL(Character.toString(lsStat.charAt(lnCtr)));
            }
            lsCondition = " cVerified IN (" + lsSQL.substring(2) + ")";
        } else 
            lsCondition = " cVerified = " + SQLUtil.toSQL(lsStat);
           
        lsSQL = "SELECT " +
                    "  IFNULL(a.sUserIDxx, '') sUserIDxx " +
                    ", IFNULL(a.dTransact, '') dTransact " +
                    ", IFNULL(a.sLastName, '') sLastName " +
                    ", IFNULL(a.sFrstName, '') sFrstName " +
                    ", IFNULL(a.sMiddName, '') sMiddName " +
                    ", IFNULL(a.sMaidenNm, '') sMaidenNm " +
                    ", IFNULL(a.sSuffixNm, '') sSuffixNm " +
                    ", IFNULL(a.cGenderCd, '0') cGenderCd " +
                    ", IFNULL(a.cCvilStat, '0') cCvilStat " +
                    ", IFNULL(a.dBirthDte,'') dBirthDte " +
                    ", IFNULL(a.sBirthPlc, '') sBirthPlc " +
                    ", IFNULL(a.sHouseNo1, '') sHouseNo1 " +
                    ", IFNULL(a.sAddress1, '') sAddress1 " +
                    ", IFNULL(a.sBrgyIDx1, '') sBrgyIDx1 " +
                    ", IFNULL(a.sTownIDx1, '') sTownIDx1 " +
                    ", IFNULL(a.sClientID, '') sClientID " +
                    ", IFNULL(a.sHouseNo2, '') sHouseNo2 " +
                    ", IFNULL(a.sAddress2, '') sAddress2 " +
                    ", IFNULL(a.sBrgyIDx2, '') sBrgyIDx2 " +
                    ", IFNULL(a.sTownIDx2, '') sTownIDx2 " +
                    ", IFNULL(a.cVerified, '0') cVerified " +
                    ", IFNULL(a.dVerified, '') dVerified " +
                    ", IFNULL(a.sVerified, '') sVerified " + 
                    ", IFNULL(e.sBrgyName, '') sBrgyNme1 " +
                    ", IFNULL(b.sTownName, '') sTownNme1 " +
                    ", IFNULL(f.sBrgyName, '') sBrgyNme2 " +
                    ", IFNULL(c.sTownName, '') sTownNme2 " +
                    ", IFNULL(d.sTownName, '') strBrhPlc " +
                " FROM " + USER_PROFILE_TABLE  + " a "+
                "   LEFT JOIN TownCity b " + 
                "     ON a.sTownIDx1 = b.sTownIDxx " + 
                "   LEFT JOIN TownCity c " + 
                "     ON a.sTownIDx2 = c.sTownIDxx " + 
                "   LEFT JOIN TownCity d " + 
                "     ON a.sBirthPlc = d.sTownIDxx " + 
                "   LEFT JOIN Barangay e " + 
                "     ON a.sBrgyIDx1 = e.sBrgyIDxx " + 
                "   LEFT JOIN Barangay f " + 
                "     ON a.sBrgyIDx2 = f.sBrgyIDxx " + 
                " WHERE ";
        return lsSQL;
    }
    
    public Object getUserEmail(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oEmail.absolute(fnRow);
        return p_oEmail.getObject(fnIndex);
    }
    
    public Object getUserEmail(int fnRow, String fsIndex) throws SQLException{
        return getUserEmail(fnRow, getColumnIndex(p_oEmail, fsIndex));
    }
    public Object getUserEmail(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oEmail.first();
        return p_oEmail.getObject(fnIndex);
    }
    
    public Object getUserEmail(String fsIndex) throws SQLException{
        return getUserEmail(getColumnIndex(p_oEmail, fsIndex));
    }
    
    public int getUserEmailItemCount() throws SQLException{
        if (p_oEmail == null) return 0;
        
        p_oEmail.last();
        return p_oEmail.getRow();
    }
    public void setUserEmail(int fnIndex, Object foValue) throws SQLException{
        if (p_nEditMode != EditMode.ADDNEW && p_nEditMode != EditMode.UPDATE) {
            System.out.println("Invalid Edit Mode Detected.");
            return;
        }
        
        p_oEmail.first();
        
        switch (fnIndex){
            case 2:  //dTransact
            case 5:  //dVerified
                if (foValue instanceof Date){
                    p_oEmail.updateDate(fnIndex, SQLUtil.toDate((Date) foValue));
                } else
                    p_oEmail.updateDate(fnIndex, SQLUtil.toDate(p_oApp.getServerDate()));
                
                p_oEmail.updateRow();
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oEmail.getString(fnIndex));
                break;
            case 7:  //cRecdStat
                if (foValue instanceof Integer)
                    p_oEmail.updateInt(fnIndex, (int) foValue);
                else 
                    p_oEmail.updateInt(fnIndex, 0);
                
                p_oEmail.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oEmail.getString(fnIndex));
                break;
            case 1: //sImagesxx
            case 3: //sImagesxx
            case 6: //sImagesxx
                p_oEmail.updateString(fnIndex, (String) foValue);
                p_oEmail.updateRow();

                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oEmail.getString(fnIndex));
                break;
        }
    }
    
    public void setUserEmail(String fsIndex, Object foValue) throws SQLException{
        setUserEmail(getColumnIndex(p_oEmail, fsIndex), foValue);
    }
    private String getSQ_Email(){
        String lsSQL = "";
        
        String lsCondition = "";
        String lsStat = String.valueOf(p_nTranStat);
        
        if (lsStat.length() > 1){
            for (int lnCtr = 0; lnCtr <= lsStat.length()-1; lnCtr++){
                lsSQL += ", " + SQLUtil.toSQL(Character.toString(lsStat.charAt(lnCtr)));
            }
            lsCondition = " cVerified IN (" + lsSQL.substring(2) + ")";
        } else 
            lsCondition = " cVerified = " + SQLUtil.toSQL(lsStat);
           
        lsSQL = "SELECT " +
                    "  IFNULL(sUserIDxx, '') sUserIDxx " +
                    ", IFNULL(dTransact, '') dTransact " +
                    ", IFNULL(sEmailAdd, '') sEmailAdd " +
                    ", IFNULL(cVerified, '0') cVerified " +
                    ", IFNULL(dVerified, '') dVerified " +
                    ", IFNULL(sVerified, '') sVerified " +
                    ", IFNULL(cRecdStat, '0') cRecdStat " + 
                "FROM " + USER_EMAIL_TABLE + 
                " WHERE ";
        return lsSQL;
    }
    
    public Object getUserMobileNo(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oMobile.absolute(fnRow);
        return p_oMobile.getObject(fnIndex);
    }
    
    public Object getUserMobileNo(int fnRow, String fsIndex) throws SQLException{
        return getUserMobileNo(fnRow, getColumnIndex(p_oMobile, fsIndex));
    }
    public Object getUserMobileNo(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oMobile.first();
        return p_oMobile.getObject(fnIndex);
    }
    
    public Object getUserMobileNo(String fsIndex) throws SQLException{
        return getUserMobileNo(getColumnIndex(p_oMobile, fsIndex));
    }
    
    public int getUserMobileNoItemCount() throws SQLException{
        if (p_oMobile == null) return 0;
        
        p_oMobile.last();
        return p_oMobile.getRow();
    }
    public void setUserMobileNo(int fnIndex, Object foValue) throws SQLException{
        if (p_nEditMode != EditMode.ADDNEW && p_nEditMode != EditMode.UPDATE) {
            System.out.println("Invalid Edit Mode Detected.");
            return;
        }
        
        p_oMobile.first();
        
        switch (fnIndex){
            case 2:  //dTransact
            case 6:  //dUserVrfd
            case 8:  //dVerified
                if (foValue instanceof Date){
                    p_oMobile.updateDate(fnIndex, SQLUtil.toDate((Date) foValue));
                } else
                    p_oMobile.updateDate(fnIndex, SQLUtil.toDate(p_oApp.getServerDate()));
                
                p_oMobile.updateRow();
                
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMobile.getString(fnIndex));
                break;
            case 5:  //cUserVrfd
            case 7:  //cVerified
            case 10: //cRecdStat
                if (foValue instanceof Integer)
                    p_oMobile.updateInt(fnIndex, (int) foValue);
                else 
                    p_oMobile.updateInt(fnIndex, 0);
                
                p_oMobile.updateRow();
                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMobile.getString(fnIndex));
                break;
            case 1: //sImagesxx
            case 3: //sImagesxx
            case 4: //sImagesxx
            case 9: //sImagesxx
                p_oMobile.updateString(fnIndex, (String) foValue);
                p_oMobile.updateRow();

                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oMobile.getString(fnIndex));
                break;
            
        }
    }
    
    public void setUserMobileNo(String fsIndex, Object foValue) throws SQLException{
        setUserMobileNo(getColumnIndex(p_oMobile, fsIndex), foValue);
    }
    private String getSQ_MobileNo(){
        String lsSQL = "";
        
        String lsCondition = "";
        String lsStat = String.valueOf(p_nTranStat);
        
        if (lsStat.length() > 1){
            for (int lnCtr = 0; lnCtr <= lsStat.length()-1; lnCtr++){
                lsSQL += ", " + SQLUtil.toSQL(Character.toString(lsStat.charAt(lnCtr)));
            }
            lsCondition = " cVerified IN (" + lsSQL.substring(2) + ")";
        } else 
            lsCondition = " cVerified = " + SQLUtil.toSQL(lsStat);
           
       
        lsSQL = "SELECT" +
                    "  IFNULL(sUserIDxx, '') sUserIDxx " +
                    ", IFNULL(dTransact, '') dTransact " +
                    ", IFNULL(sMobileNo, '') sMobileNo " +
                    ", IFNULL(sOTPasswd, '') sOTPasswd " +
                    ", IFNULL(cUserVrfd, '0') cUserVrfd " +
                    ", IFNULL(dUserVrfd, '') dUserVrfd " +
                    ", IFNULL(cVerified, '0') cVerified " +
                    ", IFNULL(dVerified, '') dVerified " +
                    ", IFNULL(sVerified, '') sVerified " +
                    ", IFNULL(cRecdStat, '0') cRecdStat " + 
                " FROM " + USER_MOBILE_TABLE + 
                " WHERE " ;
        return lsSQL;
                
    }
    
    public boolean OpenRecord(String fsValue) throws SQLException{
        p_nEditMode = EditMode.UNKNOWN;
        
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        loadMasterID(fsValue);
        loadUserPicture(fsValue);
        loadUserProfile(fsValue);
        loadUserEmail(fsValue);
        loadUserMobile(fsValue);
        p_nEditMode = EditMode.READY;
        
        return true;
    }
    
    public boolean isVerefied() throws SQLException{
        return isIDVerefied() &&
                    isPictureVerefied() &&
                    isProfileVerefied() &&
                    isEmailVerefied() &&
                    isMobileNoVerefied();
    }
    public boolean isIDVerefied() throws SQLException{
        if(getMasterIDItemCount() == 0){
            return false;
        }
        return "1".equals(getMasterID("cVerified"));
    }
    public boolean isPictureVerefied() throws SQLException{
        if(getUserPictureItemCount()== 0){
            return false;
        }
        return "1".equals(getUserPicture("cVerified"));
    }
    public boolean isProfileVerefied() throws SQLException{
        if(getUserProfileItemCount()== 0){
            return false;
        }
        return "1".equals(getUserProfile("cVerified"));
    }
    public boolean isEmailVerefied() throws SQLException{
        if(getUserEmailItemCount()== 0){
            return false;
        }
        return "1".equals(getUserEmail("cVerified"));
    }
    public boolean isMobileNoVerefied() throws SQLException{
        if(getUserMobileNoItemCount()== 0){
            return false;
        }
        return "1".equals(getUserMobileNo("cVerified"));
    }
    private String getSQ_Town(){
        String lsSQL = "";
        
        lsSQL = "SELECT " +
                    "  IFNULL(sTownIDxx, '') sTownIDxx " +
                    ", IFNULL(sTownName, '') sTownName " +
                    
                " FROM TownCity " +
                " WHERE ";
        return lsSQL;
    }
    public boolean SearchTown(int index,String fsValue, boolean fbByCode) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";    
        
        String lsSQL = "";
        if (fbByCode)
            lsSQL = getSQ_Town() + " sTownIDxx = " + SQLUtil.toSQL(fsValue);   
        else {
            lsSQL = getSQ_Town() + " sTownName LIKE " + SQLUtil.toSQL(fsValue + "%"); 
        }
        if (p_bWithUI){
            JSONObject loJSON = showFXDialog.jsonSearch(
                                p_oApp, 
                                lsSQL, 
                                fsValue, 
                                "Town ID.»TownName", 
                                "sTownIDxx»sTownName", 
                                "sTownIDxx»sTownName", 
                                fbByCode ? 0 : 1);
            
            if (loJSON != null){
                if(index == 1){
                    setUserProfile("sTownIDx1", (String) loJSON.get("sTownIDxx"));
                    setUserProfile("sTownNme1", (String) loJSON.get("sTownName"));
                    if (p_oListener != null) p_oListener.MasterRetreive(24, (String) loJSON.get("sTownName"));
                }else{
                    setUserProfile("sTownIDx2", (String) loJSON.get("sTownIDxx"));
                    setUserProfile("sTownNme2", (String) loJSON.get("sTownName"));
                    if (p_oListener != null) p_oListener.MasterRetreive(25, (String) loJSON.get("sTownName"));
                }
               
                
                return true;
            }
                
            else {
                p_sMessage = "No record selected.";
                return false;
            }
        }
        
       
//        
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
        
        if (!loRS.next()){
            MiscUtil.close(loRS);
            p_sMessage = "No transaction found for the givern criteria.";
            return false;
        }
        
        lsSQL = loRS.getString("sTownIDxx");
        if(index == 1){
            setUserProfile("sTownIDx1", (String) loRS.getString("sTownIDxx"));
            setUserProfile("sTownNme1", (String) loRS.getString("sTownName"));
        }else{
            setUserProfile("sTownIDx2", (String) loRS.getString("sTownIDxx"));
            setUserProfile("sTownNme2", (String) loRS.getString("sTownName"));
        }
        MiscUtil.close(loRS);
//        
        return true;
    }
    private String getSQ_Barangay(){
        String lsSQL = "";
        
        lsSQL = "SELECT " +
                    "  IFNULL(a.sBrgyIDxx, '') sBrgyIDxx " +
                    ", IFNULL(a.sBrgyName, '') sBrgyName " +
                    ", IFNULL(b.sTownIDxx, '') sTownIDxx " +
                    ", IFNULL(b.sTownName, '') sTownName " +
                    
                " FROM Barangay a " +
                "   LEFT JOIN TownCity b " +
                "     ON a.sTownIDxx = b.sTownIDxx " +
                " WHERE ";
        return lsSQL;
    }
        
    public boolean SearchBarangay(int index,String fsValue, boolean fbByCode) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";    
        
        String lsSQL = "";
        if (fbByCode)
            lsSQL = getSQ_Barangay() + " sBrgyIDxx = " + SQLUtil.toSQL(fsValue);   
        else {
            lsSQL = getSQ_Barangay()+ " sBrgyName LIKE " + SQLUtil.toSQL(fsValue + "%"); 
        }
        if (p_bWithUI){
            JSONObject loJSON = showFXDialog.jsonSearch(
                                p_oApp, 
                                lsSQL, 
                                fsValue, 
                                "Brgy ID.»Brgy Name» Town Name", 
                                "sBrgyIDxx»sBrgyName»sTownName", 
                                "sBrgyIDxx»sBrgyName»sTownName", 
                                fbByCode ? 0 : 1);
            
            if (loJSON != null){
                if(index == 1){
                    setUserProfile("sBrgyIDx1", (String) loJSON.get("sBrgyIDxx"));
                    setUserProfile("sBrgyNme1", (String) loJSON.get("sBrgyName")); 
                    setUserProfile("sTownIDx1", (String) loJSON.get("sTownIDxx"));
                    setUserProfile("sTownNme1", (String) loJSON.get("sTownName"));
                    if (p_oTownListener != null) {
                        p_oTownListener.MasterTownRetreive(24, (String) loJSON.get("sTownName"));
                        p_oTownListener.MasterTownRetreive(25, (String) loJSON.get("sBrgyName"));
                    }
                }else{
                    setUserProfile("sBrgyIDx2", (String) loJSON.get("sBrgyIDxx"));
                    setUserProfile("sBrgyNme2", (String) loJSON.get("sBrgyName"));
                    setUserProfile("sTownIDx2", (String) loJSON.get("sTownIDxx"));
                    setUserProfile("sTownNme2", (String) loJSON.get("sTownName"));
                    if (p_oTownListener != null) {
                        p_oTownListener.MasterTownRetreive(26, (String) loJSON.get("sTownName"));
                        p_oTownListener.MasterTownRetreive(27, (String) loJSON.get("sBrgyName"));
                    }
//                    if (p_oListener != null) p_oListener.MasterRetreive(27, (String) loJSON.get("sBrgyName"));
                }
               
                
                return true;
            }
                
            else {
                p_sMessage = "No record selected.";
                return false;
            }
        }
        
       
//        
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
        
        if (!loRS.next()){
            MiscUtil.close(loRS);
            p_sMessage = "No transaction found for the givern criteria.";
            return false;
        }
        
        lsSQL = loRS.getString("sTownIDxx");
        if(index == 1){
            setUserProfile("sTownIDx1", (String) loRS.getString("sTownIDxx"));
            setUserProfile("sTownNme1", (String) loRS.getString("sTownName"));
        }else{
            setUserProfile("sTownIDx2", (String) loRS.getString("sTownIDxx"));
            setUserProfile("sTownNme2", (String) loRS.getString("sTownName"));
        }
        MiscUtil.close(loRS);
//        
        return true;
    }
    public boolean loadMasterID(String fsValue) throws SQLException{
        
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        String lsSQL;
        ResultSet loRS;
        //open master
        
        lsSQL = getSQ_MasterID() + " a.sUserIDxx = " + SQLUtil.toSQL(fsValue) +  " ORDER BY dTransact DESC LIMIT 1" ;
        loRS = p_oApp.executeQuery(lsSQL);
        RowSetFactory factory = RowSetProvider.newFactory();
        p_oMasterID = factory.createCachedRowSet();
        p_oMasterID.populate(loRS);
        MiscUtil.close(loRS);
        
        p_oMasterID.last();
        if (p_oMasterID.getRow() <= 0) {
            p_sMessage = "No record was loaded.";
            return false;
        }
        return true;
    }
    public boolean loadUserPicture(String fsValue) throws SQLException{
        
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
        lsSQL = getSQ_Picture() + " sUserIDxx = " + SQLUtil.toSQL(fsValue) +  " ORDER BY dTransact DESC LIMIT 1" ;
        
//        lsSQL = MiscUtil.addCondition(getSQ_Picture(), " sUserIDxx = " + SQLUtil.toSQL(fsValue));
        loRS = p_oApp.executeQuery(lsSQL);
        p_oPicture = factory.createCachedRowSet();
        p_oPicture.populate(loRS);
        MiscUtil.close(loRS);
        
        p_oPicture.last();
        if (p_oPicture.getRow() <= 0) {
            p_sMessage = "No record was loaded.";
            return false;
        }
        
        return true;
    }

    public boolean loadUserProfile(String fsValue) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
        lsSQL = getSQ_Profile() + " sUserIDxx = " + SQLUtil.toSQL(fsValue) +  " ORDER BY dTransact DESC LIMIT 1" ;
        
//        lsSQL = MiscUtil.addCondition(getSQ_Profile(), " sUserIDxx = " + SQLUtil.toSQL(fsValue));
        loRS = p_oApp.executeQuery(lsSQL);
        p_oProfile = factory.createCachedRowSet();
        p_oProfile.populate(loRS);
        MiscUtil.close(loRS);
        
        p_oProfile.last();
        if (p_oProfile.getRow() <= 0) {
            p_sMessage = "No record was loaded.";
            return false;
        }
        
        return true;
    }
//    public boolean SearchRecord(String fsValue, boolean fbByCode) throws SQLException{
//        if (p_oApp == null){
//            p_sMessage = "Application driver is not set.";
//            return false;
//        }
//        
//        p_sMessage = "";
//        
//        String lsSQL = getSQ_Record();
//        
//        if (p_bWithUI){
//            JSONObject loJSON = showFXDialog.jsonSearch(
//                                p_oApp, 
//                                lsSQL, 
//                                fsValue, 
//                                "ID Code»ID Name", 
//                                "sIDCodexx»sIDNamexx", 
//                                "sIDCodexx»sIDNamexx", 
//                                fbByCode ? 0 : 1);
//            
//            if (loJSON != null) 
//                return OpenRecord((String) loJSON.get("sIDCodexx"));
//            else {
//                p_sMessage = "No record selected.";
//                return false;
//            }
//        }
//        
//        if (fbByCode)
//            lsSQL = MiscUtil.addCondition(lsSQL, "sIDCodexx = " + SQLUtil.toSQL(fsValue));   
//        else {
//            lsSQL = MiscUtil.addCondition(lsSQL, "sIDNamexx LIKE " + SQLUtil.toSQL(fsValue + "%")); 
//            lsSQL += " LIMIT 1";
//        }
//        
//        ResultSet loRS = p_oApp.executeQuery(lsSQL);
//        
//        if (!loRS.next()){
//            MiscUtil.close(loRS);
//            p_sMessage = "No transaction found for the givern criteria.";
//            return false;
//        }
//        
//        lsSQL = loRS.getString("sIDCodexx");
//        MiscUtil.close(loRS);
//        
//        return OpenRecord(lsSQL);
//    }
    
    public boolean loadUserEmail(String fsValue) throws SQLException{
        
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
        lsSQL = getSQ_Email() + " sUserIDxx = " + SQLUtil.toSQL(fsValue) +  " ORDER BY dTransact DESC LIMIT 1" ;
        
//        lsSQL = MiscUtil.addCondition(getSQ_Email(), " sUserIDxx = " + SQLUtil.toSQL(fsValue));
        loRS = p_oApp.executeQuery(lsSQL);
        p_oEmail = factory.createCachedRowSet();
        p_oEmail.populate(loRS);
        MiscUtil.close(loRS);
        
        p_oEmail.last();
        if (p_oEmail.getRow() <= 0) {
            p_sMessage = "No record was loaded.";
            return false;
        }
        
        return true;
    }

    public boolean loadUserMobile(String fsValue) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
        lsSQL = getSQ_MobileNo() + " sUserIDxx = " + SQLUtil.toSQL(fsValue) +  " ORDER BY dTransact DESC LIMIT 1" ;
        
//        lsSQL = MiscUtil.addCondition(getSQ_MobileNo(), " sUserIDxx = " + SQLUtil.toSQL(fsValue));
        loRS = p_oApp.executeQuery(lsSQL);
        p_oMobile = factory.createCachedRowSet();
        p_oMobile.populate(loRS);
        MiscUtil.close(loRS);
        
        p_oMobile.last();
        if (p_oMobile.getRow() <= 0) {
            p_sMessage = "No record was loaded.";
            return false;
        }
        
        return true;
    }
    
    public boolean SaveMasterID() throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        String lsSQL;
        if (p_nEditMode != EditMode.ADDNEW &&
            p_nEditMode != EditMode.UPDATE){
            p_sMessage = "Invalid edit mode detected.";
            return false;
        } 
        String lsUserID = (String) getMasterID("sUserIDxx");
        p_oMasterID.updateObject("cVerified", "1");
        p_oMasterID.updateObject("sVerified", p_oApp.getUserID());
        p_oMasterID.updateObject("dVerified", p_oApp.getServerDate());

        lsSQL = MiscUtil.rowset2SQL(p_oMasterID, 
                                    MASTER_ID_TABLE, 
                                    "", 
                                    "sUserIDxx = " + SQLUtil.toSQL(lsUserID));

        if (!lsSQL.isEmpty()){
            if (!p_bWithParent) p_oApp.beginTrans();
            if (!lsSQL.isEmpty()){
                if (p_oApp.executeQuery(lsSQL, MASTER_ID_TABLE, p_sBranchCd, lsUserID.substring(0, 4)) <= 0){
                    if (!p_bWithParent) p_oApp.rollbackTrans();
                    p_sMessage = p_oApp.getMessage() + ";" + p_oApp.getErrMsg();
                    return false;
                }
            }
            if (!p_bWithParent) p_oApp.commitTrans();

            p_nEditMode = EditMode.READY;
            return true;
        } else{
            p_sMessage = "No record to save.";
            return false;
        }
    }
    public boolean SaveUserPicture() throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        String lsSQL;
        if (p_nEditMode != EditMode.ADDNEW &&
            p_nEditMode != EditMode.UPDATE){
            p_sMessage = "Invalid edit mode detected.";
            return false;
        } 
        
        String lsUserID = (String) getUserPicture("sUserIDxx");
        p_oPicture.updateObject("cVerified", "1");
        p_oPicture.updateObject("sVerified", p_oApp.getUserID());
        p_oPicture.updateObject("dVerified", p_oApp.getServerDate());
        p_oPicture.updateRow();
        lsSQL = MiscUtil.rowset2SQL(p_oPicture, 
                                    USER_PICTURE_TABLE, 
                                    "", 
                                    "sUserIDxx = " + SQLUtil.toSQL(lsUserID));

        if (!lsSQL.isEmpty()){
            if (!p_bWithParent) p_oApp.beginTrans();
            if (!lsSQL.isEmpty()){
                if (p_oApp.executeQuery(lsSQL, USER_PICTURE_TABLE, p_sBranchCd, lsUserID.substring(0, 4)) <= 0){
                    if (!p_bWithParent) p_oApp.rollbackTrans();
                    p_sMessage = p_oApp.getMessage() + ";" + p_oApp.getErrMsg();
                    return false;
                }
            }
            if (!p_bWithParent) p_oApp.commitTrans();

            p_nEditMode = EditMode.READY;
            return true;
        } else{
            p_sMessage = "No record to save.";
            return false;
        }
    }
    public boolean SaveUserProfile() throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        String lsSQL;
        if (p_nEditMode != EditMode.ADDNEW &&
            p_nEditMode != EditMode.UPDATE){
            p_sMessage = "Invalid edit mode detected.";
            return false;
        } 
//        String lsUserID = (String) getUserProfile("sUserIDxx");

        lsSQL = MiscUtil.rowset2SQL(p_oProfile, 
                                        USER_PROFILE_TABLE, 
                                        "sBrgyNme1;sTownNme1;sBrgyNme2;sTownNme2", 
                                        "sUserIDxx = " + SQLUtil.toSQL(p_oProfile.getString("sUserIDxx")));
//        lsSQL = MiscUtil.rowset2SQL(p_oProfile, 
//                                    USER_PROFILE_TABLE, 
//                                    "", 
//                                    "sUserIDxx = " + SQLUtil.toSQL(lsUserID));

        if (!lsSQL.isEmpty()){
            if (!p_bWithParent) p_oApp.beginTrans();
            if (!lsSQL.isEmpty()){
                if (p_oApp.executeQuery(lsSQL, USER_PROFILE_TABLE, p_sBranchCd, "") <= 0){
                if (!p_bWithParent) p_oApp.rollbackTrans();
                p_sMessage = p_oApp.getErrMsg() + ";" + p_oApp.getMessage();
                p_nEditMode = EditMode.UPDATE;
                return false;
            }
            }
            if (!p_bWithParent) p_oApp.commitTrans();

            p_nEditMode = EditMode.READY;
            return true;
        } else{
            p_sMessage = "No record to save.";
            return false;
        }
    }
    public boolean VerifyUserProfile() throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        String lsSQL;
//        if (p_nEditMode != EditMode.ADDNEW &&
//            p_nEditMode != EditMode.UPDATE){
//            p_sMessage = "Invalid edit mode detected.";
//            return false;
//        } 
        String lsUserID = (String) getUserProfile("sUserIDxx");
        p_oProfile.updateObject("cVerified", "1");
        p_oProfile.updateObject("sVerified", p_oApp.getUserID());
        p_oProfile.updateObject("dVerified", p_oApp.getServerDate());
        p_oProfile.updateRow();
        lsSQL = MiscUtil.rowset2SQL(p_oProfile, 
                                    USER_PROFILE_TABLE, 
                                    "", 
                                    "sUserIDxx = " + SQLUtil.toSQL(lsUserID));

        if (!lsSQL.isEmpty()){
            if (!p_bWithParent) p_oApp.beginTrans();
            if (!lsSQL.isEmpty()){
                if (p_oApp.executeQuery(lsSQL, USER_PROFILE_TABLE, p_sBranchCd, lsUserID.substring(0, 4)) <= 0){
                    if (!p_bWithParent) p_oApp.rollbackTrans();
                    p_sMessage = p_oApp.getMessage() + ";" + p_oApp.getErrMsg();
                    return false;
                }
            }
            if (!p_bWithParent) p_oApp.commitTrans();

//            p_nEditMode = EditMode.UNKNOWN;
            return true;
        } else{
            p_sMessage = "No record to save.";
            return false;
        }
    }
    
    public boolean SaveUserEmail() throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        String lsSQL;
        if (p_nEditMode != EditMode.ADDNEW &&
            p_nEditMode != EditMode.UPDATE){
            p_sMessage = "Invalid edit mode detected.";
            return false;
        } 
        String lsUserID = (String) getUserPicture("sUserIDxx");
        p_oEmail.updateObject("sVerified", p_oApp.getUserID());
        p_oEmail.updateObject("dVerified", p_oApp.getServerDate());
        p_oEmail.updateRow();
        lsSQL = MiscUtil.rowset2SQL(p_oEmail, 
                                    USER_EMAIL_TABLE, 
                                    "", 
                                    "sUserIDxx = " + SQLUtil.toSQL(lsUserID));

        if (!lsSQL.isEmpty()){
            if (!p_bWithParent) p_oApp.beginTrans();
            if (!lsSQL.isEmpty()){
                if (p_oApp.executeQuery(lsSQL, USER_EMAIL_TABLE, p_sBranchCd, lsUserID.substring(0, 4)) <= 0){
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
    
    public boolean SaveUserMobileNo() throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        String lsSQL;
        if (p_nEditMode != EditMode.ADDNEW &&
            p_nEditMode != EditMode.UPDATE){
            p_sMessage = "Invalid edit mode detected.";
            return false;
        } 
        String lsUserID = (String) getUserPicture("sUserIDxx");
        p_oMobile.updateObject("sVerified", p_oApp.getUserID());
        p_oMobile.updateObject("dVerified", p_oApp.getServerDate());
        p_oMobile.updateRow();
        lsSQL = MiscUtil.rowset2SQL(p_oMobile, 
                                    USER_MOBILE_TABLE, 
                                    "", 
                                    "sUserIDxx = " + SQLUtil.toSQL(lsUserID));

        if (!lsSQL.isEmpty()){
            if (!p_bWithParent) p_oApp.beginTrans();
            if (!lsSQL.isEmpty()){
                if (p_oApp.executeQuery(lsSQL, USER_MOBILE_TABLE, p_sBranchCd, lsUserID.substring(0, 4)) <= 0){
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
