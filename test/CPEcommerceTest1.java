
import java.sql.SQLException;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rmj.appdriver.GRider;
import org.rmj.marketplace.base.LResult;
import org.rmj.marketplace.base.LTransaction;
import org.rmj.marketplace.base.CPEcommerce;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author User
 */
public class CPEcommerceTest1 {
    static GRider instance = new GRider();
    static CPEcommerce trans;
    static LTransaction listener;
    static LResult olistener;
    
    public CPEcommerceTest1() {
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
        olistener = new LResult() {
            @Override
            public void OnSave(String message) {
                System.out.println("OnSave --> " + message);
            }

            @Override
            public void OnCancel(String message) {
                System.out.println("OnCancel --> " + message);
            }
        };
        trans = new CPEcommerce(instance, instance.getBranchCode(), false);
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
                
                 System.out.println();
                 System.out.println("---------- ORDER LIST ----------");
                 System.out.println();
                for (int lnCtr = 1; lnCtr <= trans.getItemCount(); lnCtr++){
                   
                    System.out.println("No.: " + lnCtr);
                    System.out.println("TransNox: " + (String) trans.getDetail(lnCtr,"sTransNox"));
                    System.out.println("Customer Name: " + (String) trans.getDetail(lnCtr,"sCompnyNm"));
                    System.out.println("Total Amount: " + trans.getDetail(lnCtr,"nTranTotl").toString());
                }
                
                System.out.println();
            } else {
                System.out.println(trans.getMessage());
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
                if(trans.LoadOrderDetail("MX0122000001", true)){
                     for (int lnCtr = 1; lnCtr <= trans.getDetailItemCount(); lnCtr++){
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
                }else{
                    System.out.println(trans.getMessage());
                }
               
            } else {
                System.out.println(trans.getMessage());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
     @Test
    public void test03PaymentProcessing(){
        try {
            
            System.out.println();
            System.out.println("---------- PAYMENT PROCESSING ----------");
           
           if (trans.OpenTransaction("MX0122000001")){
               if(trans.getPaymentItemCount() > 0){
                    for (int lnCtr = 1; lnCtr <= trans.getPaymentItemCount(); lnCtr++){
                        System.out.println("No.: " + lnCtr);
                        System.out.println("TransNox : " + trans.getPayment(lnCtr,"sTransNox").toString());
                        System.out.println("Reference Code : " + trans.getPayment(lnCtr,"sReferCde").toString());
                        System.out.println("Reference No : " + (String) trans.getPayment(lnCtr,"sReferNox"));
                        System.out.println("Amount : " + trans.getPayment(lnCtr,"nAmountxx").toString());
                        System.out.println("Date Transaction : " + trans.getPayment(lnCtr,"dTransact").toString());
                        System.out.println("Remarks : " + (String) trans.getPayment(lnCtr,"sRemarksx"));
                        System.out.println("TranStat : " + (String) trans.getPayment(lnCtr,"cTranStat"));
                    }
               }else{
                    System.out.println(trans.getMessage());
//                    System.out.println(trans.getMessage());
               }
                
            }else{
                System.out.println(trans.getMessage());
                System.out.println(trans.getMessage());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }catch (NullPointerException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    
     @Test
    public void test04SavePaymentProcessing(){
       
        try {
            
            System.out.println();
            System.out.println("---------- SAVE PAYMENT PROCESSING ----------");
            if(trans.OpenTransaction("MX0122000001")){
                if(trans.UpdateTransaction()){
                    trans.setPayment(1, "sRemarksx", "Save payment processing.");
                    trans.setPayment(1, "cTranStat", 1);
                     if(trans.SaveTransaction()){

                    }else{
                        System.out.println(trans.getMessage());
                    } 
                }else{
                    System.out.println(trans.getMessage());
                } 
            }else{
                System.out.println(trans.getMessage());
            } 
            
        } catch (SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }catch (NullPointerException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
     @Test
    public void test05SearchProduct(){
       try {
            if (trans.OpenTransaction("MX0122000001")){
                double ntotal = 0;
                System.out.println();
                System.out.println("---------- ORDER ISSUANCE ----------");
                if(trans.LoadOrderDetail("MX0122000001", true)){
                     
                    if(trans.OpenItem("OPPOCPH2365SILVER")){
                        System.out.println("Serial No : " + trans.getIssuance(1,"sSerialNo").toString());
                        
                    }else{
                        System.out.println(trans.getMessage());
                    }
                }else{
                    System.out.println(trans.getMessage());
                }
               
            } else {
                System.out.println(trans.getMessage());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
}
