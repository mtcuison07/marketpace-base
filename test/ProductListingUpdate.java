import java.sql.SQLException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
public class ProductListingUpdate {
    static GRider instance = new GRider();
    static ProductListing trans;
    static LTransaction listener;
    
    public ProductListingUpdate() {
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
        trans.setTranStat(1230);
        trans.setWithUI(true);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Test
    public void test01List() {
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
    public void test02Update(){
        try {
            if (trans.OpenTransaction("M00122000001")){
                System.out.println("Listing ID: " + (String) trans.getMaster("sListngID"));
                System.out.println("Barcode: " + (String) trans.getMaster("xBarCodex"));
                System.out.println("Description: " + (String) trans.getMaster("sBriefDsc"));
                
                if (trans.UpdateTransaction()){
                    JSONObject loJSON;
                    JSONParser loParser = new JSONParser();
                    JSONArray loArray = (JSONArray) loParser.parse((String) trans.getMaster("sImagesxx"));
                    
                    if (!loArray.isEmpty()){
                        for (int lnCtr = 0; lnCtr <= loArray.size()-1; lnCtr++){
                            loJSON = (JSONObject) loArray.get(lnCtr);
                            System.out.println("Image: " + (String) loJSON.get("sImageURL"));
                        }
                    }
                    
                    trans.setMaster("nUnitPrce", (double) 2000.00);
                    assertTrue(Double.valueOf(String.valueOf(trans.getMaster("nUnitPrce"))) == (double) 2000.00);
                    
                    trans.addImage("Image 2");
                } else {
                    fail(trans.getMessage());
                }
            } else {
                fail(trans.getMessage());
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    @Test
    public void test03SaveTransaction() {
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
}