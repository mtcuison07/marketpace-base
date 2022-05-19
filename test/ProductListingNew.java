import java.sql.SQLException;
import org.json.simple.parser.ParseException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.rmj.appdriver.GRider;
import org.rmj.marketplace.base.LTransaction;
import org.rmj.marketplace.base.ProductListing;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProductListingNew {
    static GRider instance = new GRider();
    static ProductListing trans;
    static LTransaction listener;
    
    public ProductListingNew() {
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
        
        trans = new ProductListing(instance, instance.getBranchCode(), false);
        trans.setListener(listener);
        trans.setWithUI(true);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void test01NewTransaction() {
        try {
            if (trans.NewTransaction()){
                trans.displayMasFields();   
                
                if (!trans.searchItem("%", false, false)){
                    fail("No item was loaded.");
                }
                
                trans.setMaster("nUnitPrce", (double) 10000.00);
                assertTrue((double) trans.getMaster("nUnitPrce") == (double) 10000.00);
                
                trans.addDescription("Description 1", true);
                trans.addDescription("Description 2", false);
                trans.addDescription("Description 3", false);
                
                trans.delDescription(1);
                trans.addDescription("Description 2", false);
                
                trans.setDescriptPriority(2, true); //move up
                trans.setDescriptPriority(0, true); //move up
                trans.setDescriptPriority(2, false); //move down
                trans.setDescriptPriority(0, false); //move down
                trans.setDescriptPriority(1, false); //move down
                
                //adding image
                trans.addImage("Image 1");
                trans.addImage("Image 2");
                trans.addImage("Image 3");
                
                trans.setImagePriority(2, true);
                trans.setImagePriority(1, true);
                trans.setImagePriority(0, false);
                
                trans.delImage(2);
            } else {
                fail(trans.getMessage());
            }
        } catch (SQLException | ParseException e) {
            fail(e.getMessage());
        }   
    }
    
    @Test
    public void test02SaveTransaction() {
        try {
            if (trans.SaveTransaction()){
            } else {
                fail(trans.getMessage());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        
    }
    
    @Test
    public void test03List() {
        try {
            if (trans.LoadList("", false)){//true if by barcode; false if by description
                for (int lnCtr = 1; lnCtr <= trans.getItemCount(); lnCtr++){
                    System.out.println("No.: " + lnCtr);
                    System.out.println("Listing ID: " + (String) trans.getDetail(lnCtr, "sListngID"));
                    System.out.println("Barcode: " + (String) trans.getDetail(lnCtr, "xBarCodex"));
                    System.out.println("Description: " + (String) trans.getDetail(lnCtr, "xDescript"));
                }
            } else {
                fail(trans.getMessage());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    @Test
    public void test04OpenTransaction(){
        try {
            if (trans.OpenTransaction("M00122000001")){
                System.out.println("Listing ID: " + (String) trans.getMaster("sListngID"));
                System.out.println("Barcode: " + (String) trans.getMaster("xBarCodex"));
                System.out.println("Description: " + (String) trans.getMaster("sBriefDsc"));
                
                if (trans.UpdateTransaction()){
                    trans.setMaster("nUnitPrce", (double) 10000.00);
                    assertTrue(Double.valueOf(String.valueOf(trans.getMaster("nUnitPrce"))) == (double) 10000.00);
                } else {
                    fail(trans.getMessage());
                }
            } else {
                fail(trans.getMessage());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}