
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
    
    @Test
    public void test03LoadClientOrder() {
        try {
            if (trans.SearchRecord("GAP0190554", true)){
                trans.displayMasFields();   
                
                System.out.println("Last Name --> " + (String) trans.getMaster("sLastName"));
                System.out.println("First Name --> " + (String) trans.getMaster("sFrstName"));
                System.out.println("Middle Name --> " + (String) trans.getMaster("sMiddName"));
                System.out.println("Suffix Name --> " + (String) trans.getMaster("sSuffixNm"));
                System.out.println("Address --> " + (String) trans.getMaster("sAddressx"));
                System.out.println("Barangay --> " + (String) trans.getMaster("xBrgyName"));
                System.out.println("Town Name --> " + (String) trans.getMaster("xTownName"));
                System.out.println("Mobile No. --> " + (String) trans.getMaster("sMobileNo"));
                System.out.println("Email Add --> " + (String) trans.getMaster("sEmailAdd"));
                System.out.println("Client ID --> " + (String) trans.getMaster("sEmployNo"));
                if(trans.LoadOrder("GAP022000002")){
                    for (int lnCtr = 1; lnCtr <= trans.getOrderItemCount(); lnCtr++){
                        System.out.println("No.: " + lnCtr);
                        System.out.println("TransNox: " + (String) trans.getOrder(lnCtr,"sTransNox"));
                        System.out.println("Date Transaction: " + trans.getOrder(lnCtr,"dTransact").toString());
                        System.out.println("Total Amount: " + trans.getOrder(lnCtr,"nTranTotl").toString());
                    }
                }else{
                    fail(trans.getMessage());
                }
            } else
                fail(trans.getMessage());
        } catch (SQLException e) {
            fail(e.getMessage());
        }   
    }
    @Test
    public void test04LoadClientOrderDetail() {
       
        
        try {
            if (trans.SearchRecord("GAP0190554", true)){
               
                if(trans.LoadOrder((String) trans.getMaster("sEmployNo"))){
                    if(trans.LoadOrderDetail("MX0122000006")){
                        double ntotal = 0;
                        System.out.println();
                        System.out.println("---------- ORDER DETAIL ----------");
                        for (int lnCtr = 1; lnCtr <= trans.getOrderDetailItemCount(); lnCtr++){
                            System.out.println("No.: " + lnCtr);
                            System.out.println("BarCode: " + (String) trans.getDetailOrder(lnCtr,"xBarCodex"));
                            System.out.println("Description: " + (String) trans.getDetailOrder(lnCtr,"xDescript"));
                            System.out.println("Brand Name: " + (String) trans.getDetailOrder(lnCtr,"xBrandNme"));
                            System.out.println("Model Name: " + (String) trans.getDetailOrder(lnCtr,"xModelNme"));
                            System.out.println("Color Name: " + (String) trans.getDetailOrder(lnCtr,"xColorNme"));
                            System.out.println("Unit Price: " + trans.getDetailOrder(lnCtr,"nUnitPrce").toString());
                            System.out.println("Quantity: " + trans.getDetailOrder(lnCtr,"nQuantity").toString());
                            ntotal += (Double.parseDouble(trans.getDetailOrder(lnCtr,"nUnitPrce").toString())) * (Double.parseDouble(trans.getDetailOrder(lnCtr,"nQuantity").toString()));
                        }
                        System.out.println();
                        System.out.println("Total Amount: " + ntotal);
                    }else{
                        fail(trans.getMessage());
                    }
                }else{
                    fail(trans.getMessage());
                }
            } else
                fail(trans.getMessage());
        } catch (SQLException e) {
            fail(e.getMessage());
        }   catch (NullPointerException e) {
            fail(e.getMessage());
        }   
    }
}