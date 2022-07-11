/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.rmj.marketplace.base;

import java.sql.ResultSet;
import java.sql.SQLException;
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
public class ClientProfiling {
    
    private final String MASTER_TABLE = "App_User_Master_ID";
    private final String CLIENT_TABLE = "Client_Master";
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

    private CachedRowSet p_oMasterID;
    private CachedRowSet p_oPicture;
    private CachedRowSet p_oProfile;
    private CachedRowSet p_oEmail;
    private CachedRowSet p_oMobile;
    private LTransaction p_oListener;
    
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
        
        p_oMasterID.first();
        return p_oMasterID.getObject(fnIndex);
    }
    
    public Object getMaster(String fsIndex) throws SQLException{
        return getMaster(getColumnIndex(p_oMasterID, fsIndex));
    }
    
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
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
    
    public void setMaster(String fsIndex, Object foValue) throws SQLException{
        setMaster(getColumnIndex(p_oMasterID, fsIndex), foValue);
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
    
    private String getSQ_MasterID(){
        String lsSQL = "";
        String lsCondition = "";
        
        lsSQL = "SELECT " +
                    "sUserIDxx " +
                    ", dTransact " +
                    ", sIDCodex1 " +
                    ", sIDNoxxx1 " +
                    ", sIDFrntx1 " +
                    ", sIDBackx1 " +
                    ", sIDCodex2 " +
                    ", sIDNoxxx2 " +
                    ", sIDFrntx2 " +
                    ", sIDBackx2 " +
                    ", cVerified " +
                    ", dVerified " +
                    ", sVerified " + 
                " FROM " + MASTER_TABLE + 
                " WHERE " + 
                " ORDER BY dTransact DESC LIMIT 1";
        return lsSQL;
    }
    
    public Object getUserPicture(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oPicture.first();
        return p_oPicture.getObject(fnIndex);
    }
    
    public Object getUserPicture(String fsIndex) throws SQLException{
        return getUserPicture(getColumnIndex(p_oPicture, fsIndex));
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
      
        lsSQL =  "SELECT" +
                    " sUserIDxx " +
                    ", dTransact " +
                    ", sImageNme " +
                    ", sMD5Hashx " +
                    ", sImagePth " +
                    ", dImgeDate " +
                    ", cImgeStat " +
                    ", cVerified " +
                    ", dVerified " +
                    ", sVerified " +
                " FROM " + USER_PICTURE_TABLE + 
                " WHERE " + 
                " ORDER BY dTransact DESC LIMIT 1";
        return lsSQL;
    }
    
    
    public Object getUserProfile(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oProfile.first();
        return p_oProfile.getObject(fnIndex);
    }
    
    public Object getUserProfile(String fsIndex) throws SQLException{
        return getUserProfile(getColumnIndex(p_oProfile, fsIndex));
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
            case 20: //dVerified
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
            case 14: //sTownIDx1
            case 15: //sClientID
            case 16: //sHouseNo2
            case 17: //sAddress2
            case 18: //sTownIDx2
            case 21: //sVerified
                p_oProfile.updateString(fnIndex, (String) foValue);
                p_oProfile.updateRow();

                if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oProfile.getString(fnIndex));
                break;
            case 8: //cGenderCd
            case 9: //cCvilStat
            case 19: //cVerified
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
        lsSQL = "SELECT " +
                    "sUserIDxx " +
                    ", dTransact " +
                    ", sLastName " +
                    ", sFrstName " +
                    ", sMiddName " +
                    ", sMaidenNm " +
                    ", sSuffixNm " +
                    ", cGenderCd " +
                    ", cCvilStat " +
                    ", dBirthDte " +
                    ", sBirthPlc " +
                    ", sHouseNo1 " +
                    ", sAddress1 " +
                    ", sTownIDx1 " +
                    ", sClientID " +
                    ", sHouseNo2 " +
                    ", sAddress2 " +
                    ", sTownIDx2 " +
                    ", cVerified " +
                    ", dVerified " +
                    ", sVerified " + 
                " FROM " + USER_PROFILE_TABLE  + 
                " WHERE " + 
                " ORDER BY dTransact DESC LIMIT 1";
        return lsSQL;
    }
    public Object getUserEmail(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oEmail.first();
        return p_oEmail.getObject(fnIndex);
    }
    
    public Object getUserEmail(String fsIndex) throws SQLException{
        return getUserEmail(getColumnIndex(p_oEmail, fsIndex));
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
       
        lsSQL = "SELECT " +
                    "sUserIDxx " +
                    ", dTransact " +
                    ", sEmailAdd " +
                    ", cVerified " +
                    ", dVerified " +
                    ", sVerified " +
                    ", cRecdStat " + 
                "FROM " + USER_EMAIL_TABLE + 
                " WHERE " + 
                " ORDER BY dTransact DESC LIMIT 1";
        return lsSQL;
    }
    
    public Object getUserMobileNo(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oMobile.first();
        return p_oMobile.getObject(fnIndex);
    }
    
    public Object getUserMobileNo(String fsIndex) throws SQLException{
        return getUserMobileNo(getColumnIndex(p_oMobile, fsIndex));
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
       
        lsSQL = "SELECT" +
                    " sUserIDxx " +
                    ", dTransact " +
                    ", sMobileNo " +
                    ", sOTPasswd " +
                    ", cUserVrfd " +
                    ", dUserVrfd " +
                    ", cVerified " +
                    ", dVerified " +
                    ", sVerified " +
                    ", cRecdStat " + 
                " FROM " + USER_MOBILE_TABLE + 
                " WHERE " +
                " ORDER BY dTransact DESC LIMIT 1";
        return lsSQL;
                
    }
    
    
    public boolean OpenRecord(String fsValue, boolean fbByUserID) throws SQLException{
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
        return "1".equals(getMaster("cVerified"));
    }
    public boolean isPictureVerefied() throws SQLException{
        return "1".equals(getUserPicture("cVerified"));
    }
    public boolean isProfileVerefied() throws SQLException{
        return "1".equals(getUserProfile("cVerified"));
    }
    public boolean isEmailVerefied() throws SQLException{
        return "1".equals(getUserEmail("cVerified"));
    }
    public boolean isMobileNoVerefied() throws SQLException{
        return "1".equals(getUserMobileNo("cVerified"));
    }
    private boolean loadMasterID(String fsValue) throws SQLException{
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
        lsSQL = MiscUtil.addCondition(getSQ_MasterID(), " sUserIDxx = " + SQLUtil.toSQL(fsValue));
        loRS = p_oApp.executeQuery(lsSQL);
        p_oMasterID = factory.createCachedRowSet();
        p_oMasterID.populate(loRS);
        MiscUtil.close(loRS);
        
        p_oMasterID.last();
        if (p_oMasterID.getRow() <= 0) {
            p_sMessage = "No record was loaded.";
            return false;
        }
        p_nEditMode = EditMode.READY;
        
        return true;
    }
    private boolean loadUserPicture(String fsValue) throws SQLException{
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
        lsSQL = MiscUtil.addCondition(getSQ_Picture(), " sUserIDxx = " + SQLUtil.toSQL(fsValue));
        loRS = p_oApp.executeQuery(lsSQL);
        p_oPicture = factory.createCachedRowSet();
        p_oPicture.populate(loRS);
        MiscUtil.close(loRS);
        
        p_oPicture.last();
        if (p_oPicture.getRow() <= 0) {
            p_sMessage = "No record was loaded.";
            return false;
        }
        p_nEditMode = EditMode.READY;
        
        return true;
    }

    private boolean loadUserProfile(String fsValue) throws SQLException{
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
        lsSQL = MiscUtil.addCondition(getSQ_Profile(), " sUserIDxx = " + SQLUtil.toSQL(fsValue));
        loRS = p_oApp.executeQuery(lsSQL);
        p_oProfile = factory.createCachedRowSet();
        p_oProfile.populate(loRS);
        MiscUtil.close(loRS);
        
        p_oProfile.last();
        if (p_oProfile.getRow() <= 0) {
            p_sMessage = "No record was loaded.";
            return false;
        }
        p_nEditMode = EditMode.READY;
        
        return true;
    }

    private boolean loadUserEmail(String fsValue) throws SQLException{
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
        lsSQL = MiscUtil.addCondition(getSQ_Email(), " sUserIDxx = " + SQLUtil.toSQL(fsValue));
        loRS = p_oApp.executeQuery(lsSQL);
        p_oEmail = factory.createCachedRowSet();
        p_oEmail.populate(loRS);
        MiscUtil.close(loRS);
        
        p_oEmail.last();
        if (p_oEmail.getRow() <= 0) {
            p_sMessage = "No record was loaded.";
            return false;
        }
        p_nEditMode = EditMode.READY;
        
        return true;
    }

    private boolean loadUserMobile(String fsValue) throws SQLException{
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
        lsSQL = MiscUtil.addCondition(getSQ_MobileNo(), " sUserIDxx = " + SQLUtil.toSQL(fsValue));
        loRS = p_oApp.executeQuery(lsSQL);
        p_oMobile = factory.createCachedRowSet();
        p_oMobile.populate(loRS);
        MiscUtil.close(loRS);
        
        p_oMobile.last();
        if (p_oMobile.getRow() <= 0) {
            p_sMessage = "No record was loaded.";
            return false;
        }
        p_nEditMode = EditMode.READY;
        
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
        String lsUserID = (String) getMaster("sUserIDxx");
        p_oMasterID.updateObject("sVerified", p_oApp.getUserID());
        p_oMasterID.updateObject("dVerified", p_oApp.getServerDate());

        lsSQL = MiscUtil.rowset2SQL(p_oMasterID, 
                                    MASTER_TABLE, 
                                    "", 
                                    "sUserIDxx = " + SQLUtil.toSQL(lsUserID));

        if (!lsSQL.isEmpty()){
            if (!p_bWithParent) p_oApp.beginTrans();
            if (!lsSQL.isEmpty()){
                if (p_oApp.executeQuery(lsSQL, MASTER_TABLE, p_sBranchCd, lsUserID.substring(0, 4)) <= 0){
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

            p_nEditMode = EditMode.UNKNOWN;
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
        String lsUserID = (String) getUserPicture("sUserIDxx");
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

            p_nEditMode = EditMode.UNKNOWN;
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
