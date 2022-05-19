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
import org.rmj.marketplace.base.LTransaction;
import org.rmj.marketplace.base.Clients;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientLoad {
    static GRider instance = new GRider();
    static Clients trans;
    static LTransaction listener;
    
    public ClientLoad() {
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
        
        trans = new Clients(instance, instance.getBranchCode(), false);
        trans.setListener(listener);
        trans.setWithUI(false);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void test01LoadClientList() {
        try {
            if (trans.LoadList("")){                
                System.out.println("List count -->" + trans.getItemCount());
                for (int lnCtr = 1; lnCtr <= trans.getItemCount(); lnCtr++){
                    System.out.println("No. --> " + lnCtr);
                    System.out.println("Name --> " + (String) trans.getDetail(lnCtr, 19));
                    System.out.println("Email --> " + (String) trans.getDetail(lnCtr, 17));
                }
            } else
                fail(trans.getMessage());
        } catch (SQLException e) {
            fail(e.getMessage());
        }   
    }
    
    @Test
    public void test02LoadClient() {
        try {
            if (trans.SearchRecord("GAP020202528", true)){
                trans.displayMasFields();   
                
                System.out.println("Last Name --> " + (String) trans.getMaster("sLastName"));
                System.out.println("First Name --> " + (String) trans.getMaster("sFrstName"));
                System.out.println("Middle Name --> " + (String) trans.getMaster("sMiddName"));
                System.out.println("Suffix Name --> " + (String) trans.getMaster("sSuffixNm"));
                System.out.println("Gender --> " + (String) trans.getMaster("cGenderCd"));
                System.out.println("Civil Status --> " + (String) trans.getMaster("cCvilStat"));
                System.out.println("Citizenship --> " + (String) trans.getMaster("sCitizenx"));
                System.out.println("Birth Date --> " + (Date) trans.getMaster("dBirthDte"));
                System.out.println("Birth Place --> " + (String) trans.getMaster("xBirthPlc"));
                System.out.println("House No. --> " + (String) trans.getMaster("sHouseNox"));
                System.out.println("Address --> " + (String) trans.getMaster("sAddressx"));
                System.out.println("Barangay --> " + (String) trans.getMaster("xBrgyName"));
                System.out.println("Town Name --> " + (String) trans.getMaster("xTownName"));
                System.out.println("Mobile No. --> " + (String) trans.getMaster("sMobileNo"));
                System.out.println("Email Add --> " + (String) trans.getMaster("sEmailAdd"));
                System.out.println("TIN --> " + (String) trans.getMaster("sTaxIDNox"));
            } else
                fail(trans.getMessage());
        } catch (SQLException e) {
            fail(e.getMessage());
        }   
    }
}