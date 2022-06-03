
import java.sql.SQLException;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rmj.appdriver.GRider;
import org.rmj.marketplace.base.LTransaction;
import org.rmj.marketplace.base.ProductReviews;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author User
 */
public class ProductReviewsTest {
    static GRider instance = new GRider();
    static ProductReviews trans;
    static LTransaction listener;
    
    public ProductReviewsTest() {
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
        trans = new ProductReviews(instance, instance.getBranchCode(), false);
        trans.setListener(listener);
        trans.setTranStat(12340);
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
                    System.out.println("Listing ID: " + (String) trans.getDetail(lnCtr,"sListngID"));
                    System.out.println("Ratings: " + (String) trans.getDetail(lnCtr,"nRatingxx").toString());
                    System.out.println("Remarks: " + (String) trans.getDetail(lnCtr,"sRemarksx"));
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
    public void test02OpenTransaction(){
        try {
            if (trans.OpenTransaction("C00122000002", "1")){
                System.out.println("Listing ID: " + (String) trans.getMaster("sListngID"));
                System.out.println("Ratings: " + trans.getMaster("nRatingxx").toString());
                System.out.println("Remarks: " + (String) trans.getMaster("sRemarksx"));
                
                if (trans.UpdateTransaction()){
                    trans.setMaster("sReplyxxx", "Sample Reply...");
                    assertTrue(String.valueOf(trans.getMaster("sReplyxxx")).equalsIgnoreCase("Sample Reply..."));
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
    @Test
    public void test03SaveTransaction() {
        try {
            if (trans.SaveTransaction()){
                System.out.println("Reply to " + (String) trans.getMaster("sCompnyNm") + " has been sent.");
            } else {
                fail(trans.getMessage());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        
    }
}
