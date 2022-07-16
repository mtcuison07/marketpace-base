
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
    public void test02LoadClient() {
        try {
            if (trans.OpenRecord("GAP022000848")){  
            
//                if("0".equals((String) trans.getMaster("cVerified")) ||
//                    "0".equals(trans.getUserPicture("cVerified")) ||
//                    "0".equals(trans.getUserProfile("cVerified")) ||
//                    "0".equals(trans.getUserEmail("cVerified")) ||
//                    "0".equals(trans.getUserMobileNo("cVerified"))){
//                    System.out.println("Master ID --> " + (String) trans.getMaster("cVerified"));
//                    System.out.println("User Picture --> " + (String) trans.getUserPicture("cVerified"));
//                    System.out.println("User Profile --> " + (String) trans.getUserProfile("cVerified"));
//                    System.out.println("User Email --> " + (String) trans.getUserEmail("cVerified"));
//                    System.out.println("User Mobile No --> " + (String) trans.getUserMobileNo("cVerified"));
//                
//                }else{
//                    System.out.println("Status --> FULLY VERIFIED");
//                }
//                    
            } else
                fail(trans.getMessage());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }   
    }
    
   
}