
import java.sql.SQLException;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rmj.appdriver.GRider;
import org.rmj.marketplace.base.FAQuestions;
import org.rmj.marketplace.base.LTransaction;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author User
 */
public class FAQuestionsTest {
    static GRider instance = new GRider();
    static FAQuestions trans;
    static LTransaction listener;
    
    public FAQuestionsTest() {
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
        trans = new FAQuestions(instance, instance.getBranchCode(), false);
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
                    System.out.println("Question : " + (String) trans.getDetail(lnCtr,"sQuestion"));
                    System.out.println("Customer Name: " + trans.getDetail(lnCtr,"sCompnyNm"));
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
            if (trans.OpenTransaction("C00122000002", "2")){
                System.out.println("Listing ID: " + (String) trans.getMaster("sListngID"));
                System.out.println("Question : " + (String) trans.getMaster("sQuestion"));
                System.out.println("Customer Name: " + trans.getMaster("sCompnyNm"));
                
                if (trans.UpdateTransaction()){
                    trans.setMaster("sReplyxxx", "Salamat po.");
                    assertTrue(String.valueOf(trans.getMaster("sReplyxxx")).equalsIgnoreCase("Salamat po."));
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
