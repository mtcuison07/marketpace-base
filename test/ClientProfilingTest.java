
import java.sql.SQLException;
import java.util.Date;
import org.json.simple.parser.ParseException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.marketplace.base.LTransaction;
import org.rmj.marketplace.base.ClientProfiling;
import org.rmj.marketplace.base.LResult;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientProfilingTest {
    static GRider instance = new GRider();
    static ClientProfiling trans;
    static LTransaction listener;
    
    public ClientProfilingTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {        
        if (!instance.logUser("gRider", "M001111122")){
            System.err.println(instance.getMessage() + instance.getErrMsg());
            System.exit(1);
        }
        
        listener = new LTransaction() {
            @Override
            public void MasterRetreive(int fnIndex, Object foValue) {
                System.out.println(fnIndex + "-->" + foValue);
            }

           

        };
        
        trans = new ClientProfiling(instance, instance.getBranchCode(), false);
        trans.setListener(listener);
        trans.setWithUI(false);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Test
    public void test01LoadClient() {
        try {
            if(trans.LoadList()){
                System.out.println("---------- DISPLAY ALL USERS ----------" );
                if(trans.getItemCount()>0){
                    for(int x = 1; x<= trans.getItemCount(); x++){
                        System.out.println("No : " + x);
                        System.out.println("User ID --> " + (String) trans.getDetail(x,"sUserIDxx"));
                        System.out.println("User Name --> " + (String) trans.getDetail(x,"sUserName"));
                        System.out.println("User Email --> " + (String) trans.getDetail(x,"cIDVerify"));
                        System.out.println("User Profile verify --> " + (String) trans.getDetail(x,"cPrVerify"));
                        System.out.println("User Picture verify --> " + (String) trans.getDetail(x,"cPcVerify"));
                        System.out.println("User Email verify --> " + (String) trans.getDetail(x,"cEmVerify"));
                        System.out.println("User Mobile No verify --> " + (String) trans.getDetail(x,"cMoVerify")); 
                        System.out.println("");

                    }
                }else{
                    System.out.println("No Record Found...");
                }
            }else
                 System.out.println(trans.getMessage());
//                fail(trans.getMessage());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }   
    }
    @Test
    public void test02LoadMasterID() {
        try {
            if(trans.loadMasterID("GAP0190554")){
                 System.out.println("---------- DISPLAY MASTER ID ----------" );
                 if(trans.getMasterIDItemCount()>0){
                    for(int x = 1; x<= trans.getMasterIDItemCount(); x++){
                        System.out.println("No : " + x);
                        System.out.println("User ID --> " + (String) trans.getMasterID(x,"sUserIDxx"));
                        System.out.println("Date Transaction --> " + (String) trans.getMasterID(x,"dTransact"));
                        System.out.println("User ID Code 1 --> " + (String) trans.getMasterID(x,"sIDCodex1"));
                        System.out.println("User ID No 1 --> " + (String) trans.getMasterID(x,"sIDNoxxx1"));
                        System.out.println("User ID Front 1 --> " + (String) trans.getMasterID(x,"sIDFrntx1"));
                        System.out.println("User ID Back 1 --> " + (String) trans.getMasterID(x,"sIDBackx1"));
                        System.out.println("User ID Code 2 --> " + (String) trans.getMasterID(x,"sIDCodex2"));
                        System.out.println("User ID No 2 --> " + (String) trans.getMasterID(x,"sIDNoxxx2"));
                        System.out.println("User ID Front 2 --> " + (String) trans.getMasterID(x,"sIDFrntx2"));
                        System.out.println("User ID Back 2 --> " + (String) trans.getMasterID(x,"sIDBackx2")); 
                        System.out.println("User ID Verify --> " + (String) trans.getMasterID(x,"cVerified"));
                        System.out.println("Date Verified --> " + (String) trans.getMasterID(x,"dVerified")); 
                        System.out.println("User Verified by--> " + (String) trans.getMasterID(x,"sVerified")); 
                        System.out.println("");

                    }
                }else{
                    System.out.println("No Record Found...");
                }
                
            }else
                 System.out.println(trans.getMessage());
//                fail(trans.getMessage());    
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }   
    }
    
    @Test
    public void test03LoadUserProfile() {
        try {
            if(trans.loadUserProfile("GAP0190554")){
                 System.out.println("---------- DISPLAY USER PROFILE ----------" );
                 System.out.println("Count : " + trans.getUserProfileItemCount());
                 if(trans.getUserProfileItemCount()>0){
                    for(int x = 1; x<= trans.getUserProfileItemCount(); x++){
                        System.out.println("No : " + x);
                        System.out.println("User ID --> " + (String) trans.getUserProfile(x,"sUserIDxx"));
                        System.out.println("Date Transaction --> " + (String) trans.getUserProfile(x,"dTransact"));
                        System.out.println("Las tName--> " + (String) trans.getUserProfile(x,"sLastName"));
                        System.out.println("First Name --> " + (String) trans.getUserProfile(x,"sFrstName"));
                        System.out.println("Middle Name --> " + (String) trans.getUserProfile(x,"sMiddName"));
                        System.out.println("Maiden Name --> " + (String) trans.getUserProfile(x,"sMaidenNm"));
                        System.out.println("Suffix Name --> " + (String) trans.getUserProfile(x,"sSuffixNm"));
                        System.out.println("GenderCd --> " + (String) trans.getUserProfile(x,"cGenderCd")); 
                        System.out.println("Civil Stat--> " + (String) trans.getUserProfile(x,"cCvilStat")); 
                        System.out.println("Birth Date --> " + (String) trans.getUserProfile(x,"dBirthDte"));
                        System.out.println("Birth Place --> " + (String) trans.getUserProfile(x,"sBirthPlc"));
                        System.out.println("House No 1 --> " + (String) trans.getUserProfile(x,"sHouseNo1"));
                        System.out.println("Address 1--> " + (String) trans.getUserProfile(x,"sAddress1"));
                        System.out.println("Town ID 1 --> " + (String) trans.getUserProfile(x,"sTownIDx1"));
                        System.out.println("Client ID --> " + (String) trans.getUserProfile(x,"sClientID"));
                        System.out.println("House No 2 --> " + (String) trans.getUserProfile(x,"sHouseNo2"));
                        System.out.println("Address 2 --> " + (String) trans.getUserProfile(x,"sAddress2"));
                        System.out.println("Town ID 2 --> " + (String) trans.getUserProfile(x,"sTownIDx2"));
                        System.out.println("cVerified --> " + (String) trans.getUserProfile(x,"cVerified"));
                        System.out.println("dVerified --> " + (String) trans.getUserProfile(x,"dVerified"));
                        System.out.println("sVerified --> " + (String) trans.getUserProfile(x,"sVerified"));
                        System.out.println("");

                    }
                }else{
                    System.out.println("No Record Found...");
                }
                
            }else
                 System.out.println(trans.getMessage());
//                fail(trans.getMessage());    
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }   
        
    }
    
    
    @Test
    public void test04LoadUserPicture() {
        try {
            if(trans.loadMasterID("GAP0190554")){
                 System.out.println("---------- DISPLAY USER PICTURE ----------" );
                if(trans.getUserPictureItemCount() > 0){
                    for(int x = 1; x<= trans.getUserPictureItemCount(); x++){
                        System.out.println("No : " + x);
                        System.out.println("User ID --> " + (String) trans.getUserPicture(x,"sUserIDxx"));
                        System.out.println("Date Transaction --> " + (String) trans.getUserPicture(x,"dTransact"));
                        System.out.println("Image Name --> " + (String) trans.getUserPicture(x,"sImageNme"));
                        System.out.println("MD5 Hash --> " + (String) trans.getUserPicture(x,"sMD5Hashx"));
                        System.out.println("Image Path --> " + (String) trans.getUserPicture(x,"sImagePth"));
                        System.out.println("Imge Date --> " + (String) trans.getUserPicture(x,"dImgeDate"));
                        System.out.println("Image Stat --> " + (String) trans.getUserPicture(x,"cImgeStat"));
                        System.out.println("cVerified --> " + (String) trans.getUserPicture(x,"cVerified"));
                        System.out.println("dVerified --> " + (String) trans.getUserPicture(x,"dVerified"));
                        System.out.println("sVerified --> " + (String) trans.getUserPicture(x,"sVerified"));
                        System.out.println("");
                    }
                }else{
                    System.out.println("No Record Found...");
                }
                
            }else
                 System.out.println(trans.getMessage());
//                fail(trans.getMessage());    
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }   
    }
    
    
    @Test
    public void test06LoadUserMobileNo() {
        try {
            if(trans.loadMasterID("GAP0190554")){
                 System.out.println("---------- DISPLAY USER MOBILE NO ----------" );
                if(trans.getUserMobileNoItemCount()> 0){
                    for(int x = 1; x<= trans.getUserMobileNoItemCount(); x++){
                        System.out.println("No : " + x);
                        System.out.println("User ID --> " + (String) trans.getUserMobileNo(x,"sUserIDxx"));
                        System.out.println("Date Transaction --> " + (String) trans.getUserMobileNo(x,"dTransact"));
                        System.out.println("Mobile No --> " + (String) trans.getUserMobileNo(x,"sMobileNo"));
                        System.out.println("OTP Password--> " + (String) trans.getUserMobileNo(x,"sOTPasswd"));
                        System.out.println("User Date Verified --> " + (String) trans.getUserMobileNo(x,"dUserVrfd"));
                        System.out.println("User Verified --> " + (String) trans.getUserMobileNo(x,"cUserVrfd"));
                        System.out.println("cVerified --> " + (String) trans.getUserMobileNo(x,"cVerified"));
                         System.out.println("dVerified --> " + (String) trans.getUserMobileNo(x,"dVerified"));
                        System.out.println("sVerified --> " + (String) trans.getUserMobileNo(x,"sVerified"));
                        System.out.println("cRecdStat --> " + (String) trans.getUserMobileNo(x,"cRecdStat"));
                        System.out.println("");
                   
                    }
                }else{
                    System.out.println("No Record Found...");
                }
                
            }else
                System.out.println(trans.getMessage());
//                fail(trans.getMessage());    
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }   
    }
    
    @Test
    public void test05LoadUserEmail() {
        try {
            System.out.println("---------- DISPLAY USER EMAIL ----------" );
            if(trans.loadMasterID("GAP022000006")){
                if(trans.getUserPictureItemCount() > 0){
                    for(int x = 1; x<= trans.getUserEmailItemCount(); x++){
                        System.out.println("No : " + x);
                        System.out.println("User ID --> " + (String) trans.getUserEmail(x,"sUserIDxx"));
                        System.out.println("Date Transaction --> " + (String) trans.getUserEmail(x,"dTransact"));
                        System.out.println("Email Address --> " + (String) trans.getUserEmail(x,"sEmailAdd"));
                        System.out.println("cVerified --> " + (String) trans.getUserEmail(x,"cVerified"));
                        System.out.println("dVerified --> " + (String) trans.getUserEmail(x,"dVerified"));
                        System.out.println("sVerified --> " + (String) trans.getUserEmail(x,"sVerified"));
                        System.out.println("cRecdStat --> " + (String) trans.getUserEmail(x,"cRecdStat"));
                        System.out.println("");
                    }
                }else{
                    System.out.println("No Record Found...");
                }
                
            }else
                 System.out.println(trans.getMessage());
//                fail(trans.getMessage());    
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }   
    }
   
}