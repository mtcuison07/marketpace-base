
import java.sql.SQLException;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rmj.appdriver.GRider;
import org.rmj.marketplace.base.LTransaction;
import org.rmj.marketplace.base.SalesOrder;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author User
 */
public class SalesOrderTest {
    static GRider instance = new GRider();
    static SalesOrder trans;
    static LTransaction listener;
    
    public SalesOrderTest() {
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
        trans = new SalesOrder(instance, instance.getBranchCode(), false);
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
                 System.out.println("---------- ORDER LIST ----------");
                for (int lnCtr = 1; lnCtr <= trans.getItemCount(); lnCtr++){
                    System.out.println("No.: " + lnCtr);
                    System.out.println("TransNox: " + (String) trans.getMaster("sTransNox"));
                    System.out.println("Customer Name: " + (String) trans.getMaster("sCompnyNm"));
                    System.out.println("Total Amount: " + trans.getMaster("nTranTotl").toString());
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
            if (trans.OpenTransaction("MX0122000001")){
                double ntotal = 0;
                System.out.println();
                System.out.println("---------- ORDER DETAIL ----------");
                
                for (int lnCtr = 1; lnCtr <= trans.getItemCount(); lnCtr++){
                    System.out.println("No.: " + lnCtr);
                    System.out.println("BarCode: " + (String) trans.getDetail(lnCtr,"xBarCodex"));
                    System.out.println("Description: " + (String) trans.getDetail(lnCtr,"xDescript"));
                    System.out.println("Brand Name: " + (String) trans.getDetail(lnCtr,"xBrandNme"));
                    System.out.println("Model Name: " + (String) trans.getDetail(lnCtr,"xModelNme"));
                    System.out.println("ColorName: " + (String) trans.getDetail(lnCtr,"xColorNme"));
                    System.out.println("Unit Price: " + trans.getDetail(lnCtr,"nUnitPrce").toString());
                    System.out.println("Quantity: " + trans.getDetail(lnCtr,"nQuantity").toString());
                    ntotal += (Double.parseDouble(trans.getDetail(lnCtr,"nUnitPrce").toString())) * (Double.parseDouble(trans.getDetail(lnCtr,"nQuantity").toString()));
                }
                System.out.println();
                System.out.println("Total Amount: " + ntotal);
            } else {
                fail(trans.getMessage());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
}
