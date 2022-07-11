
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
import org.rmj.marketplace.base.LMasDetTrans;
import org.rmj.marketplace.base.LTransaction;
import org.rmj.marketplace.base.parameter.Identification;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IdentificationTest {
 static GRider instance = new GRider();
    static org.rmj.marketplace.base.parameter.Identification trans;
    
    public IdentificationTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {        
        if (!instance.logUser("gRider", "M001111122")){
            System.err.println(instance.getMessage() + instance.getErrMsg());
            System.exit(1);
        }
        
        trans = new org.rmj.marketplace.base.parameter.Identification(instance, instance.getBranchCode(), false);
        trans.setWithUI(false);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void test01NewTransaction() {
        try {
            if (trans.NewRecord()){
                trans.displayMasFields();
            } else {
                fail(trans.getMessage());
            }
        } catch (SQLException e) {
            fail(e.getMessage());
        }
        
    }
    
    @Test 
    public void test02SetMaster(){
        try {
            trans.setMaster("sIDCodexx", "00001");
            assertEquals("00001", (String) trans.getMaster("sIDCodexx"));
            
            trans.setMaster("sIDNamexx", "UMID");
            assertEquals("UMID", (String) trans.getMaster("sIDNamexx"));
            
            
        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }
    
    @Test 
    public void test03SaveRecord(){
        try {
            if (!trans.SaveRecord())
                fail(trans.getMessage());
        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }
    
    @Test 
    public void test04Search(){
        try {
            if (trans.SearchRecord("UMID", false)){
                if (trans.UpdateRecord()){
                   
                } else
                    fail(trans.getMessage());
            } else
                fail(trans.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    @Test 
    public void test05SaveRecord(){
        try {
            if (!trans.SaveRecord())
                fail(trans.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    @Test 
    public void test06Search(){
        try {
            if (trans.SearchRecord("UMID", false)){
                if (trans.DeactivateRecord()){
                } else
                    fail(trans.getMessage());
            } else
                fail(trans.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    @Test 
    public void test07Search(){
        try {
            if (trans.SearchRecord("UMID", false)){
                if (trans.ActivateRecord()){
                } else
                    fail(trans.getMessage());
            } else
                fail(trans.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}