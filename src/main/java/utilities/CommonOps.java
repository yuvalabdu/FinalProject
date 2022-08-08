package utilities;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.windows.WindowsDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.restassured.RestAssured;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.sikuli.script.Screen;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import workflows.ElectronFlows;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.lang.reflect.Method;


public class CommonOps extends Base {

    /*
    #####################################################################################
     Method Name: getData
     Method Description: This Method gets the data from xml configuration file
     Method Parameters: String
     Method Return: String
     #####################################################################################
     */
    public static String getData(String nodeName) {
        DocumentBuilder dBuilder;
        Document doc = null;
        File fXmlFile = new File("./Configuration/DataConfig.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(fXmlFile);
        }
        catch(Exception e) {
            System.out.println("Exception in reading XML file: " + e);
        }
        doc.getDocumentElement().normalize();
        return doc.getElementsByTagName(nodeName).item(0).getTextContent();
    }


    /*
    #####################################################################################
    Method Name: initBrowser
    Method Description: This Method referring to the right driver for web clients (chrome, fireFox, internetExplorer)
    Method Parameters: String
    Method Return: --
    #####################################################################################
    */
    public static void initBrowser(String browserType) {
        if (browserType.equalsIgnoreCase("chrome"))
            driver = initChromeDriver();
        else if (browserType.equalsIgnoreCase("firefox"))
            driver = initFirefoxDriver();
        else if (browserType.equalsIgnoreCase("ie"))
            driver = initIEDriver();
        else
            throw new RuntimeException("Invalid browser type");

        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Long.parseLong(getData("Timeout")), TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, Long.parseLong(getData("Timeout")));
        driver.get(getData("url"));
        ManagePages.initSauceDemo();
        action = new Actions(driver);
    }

    /*
    #####################################################################################
    Method Name: initChromeDriver
    Method Description: This Method initializing the driver for Chrome web clients
    Method Parameters: --
    Method Return: WebDriver
    #####################################################################################
    */
    public static WebDriver initChromeDriver() {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        return driver;
    }


    /*
    #####################################################################################
    Method Name: initFirefoxDriver
    Method Description: This Method initializing the driver for fireFox web clients
    Method Parameters: --
    Method Return: WebDriver
    #####################################################################################
    */
    public static WebDriver initFirefoxDriver() {
        WebDriverManager.firefoxdriver().setup();
        WebDriver driver = new FirefoxDriver();
        return driver;
    }


    /*
    #####################################################################################
    Method Name: initIEDriver
    Method Description: This Method initializing the driver for Internet Explorer web clients
    Method Parameters: --
    Method Return: WebDriver
    #####################################################################################
    */
    public static WebDriver initIEDriver() {
        WebDriverManager.iedriver().setup();
        WebDriver driver = new InternetExplorerDriver();
        return driver;
    }


    /*
    #####################################################################################
    Method Name: initMobile
    Method Description: This Method initializing the driver for mobile clients
    Method Parameters: --
    Method Return: --
    #####################################################################################
    */
     public static void initMobile(){
         dc.setCapability(MobileCapabilityType.UDID, getData("UDID"));
         dc.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, getData("AppPackage"));
         dc.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, getData("AppActivity"));
         try {
             mobileDriver = new AndroidDriver(new URL(getData("AppiumServer")), dc);
         } catch (Exception e) {
             System.out.println("Can not connect to appium server, see details: " + e);
         }
         ManagePages.initMortgageCalc();
         mobileDriver.manage().timeouts().implicitlyWait(Long.parseLong(getData("Timeout")), TimeUnit.SECONDS);
         wait = new WebDriverWait(mobileDriver, Long.parseLong(getData("Timeout")));
     }


    /*
   #####################################################################################
   Method Name: initAPI
   Method Description: This Method connects to the server
   Method Parameters: --
   Method Return: --
   #####################################################################################
   */
    public static void initAPI() {
        RestAssured.baseURI= getData("urlAPI");
        httpRequest = RestAssured.given();
    }


    /*
   #####################################################################################
   Method Name: initElectron
   Method Description: This Method initializing the driver for Electron Application clients
   Method Parameters: --
   Method Return: --
   #####################################################################################
   */
    public static void initElectron() {
        System.setProperty("webdriver.chrome.driver", getData("ElectronDriverPath"));
        ChromeOptions opt = new ChromeOptions();
        opt.setBinary(getData("ElectronAppPath"));
        dc.setCapability("chromeOptions", opt);
        dc.setBrowserName("chrome");
        driver = new ChromeDriver(dc);
        ManagePages.initToDo();
        driver.manage().timeouts().implicitlyWait(Long.parseLong(getData("Timeout")), TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, Long.parseLong(getData("Timeout")));
        action = new Actions(driver);
    }


    /*
   #####################################################################################
   Method Name: initDesktop
   Method Description: This Method initializing the driver for Desktop Application clients
   Method Parameters: --
   Method Return: --
   #####################################################################################
   */
    public static void initDesktop() {
        dc.setCapability("app", getData("CalculatorApp"));
        try {
            driver = new WindowsDriver(new URL(getData("AppiumServerDesktop")), dc);
        } catch (Exception e) {
            System.out.println("Can not Connect to Appium Server, See Details: " + e);
        }
        driver.manage().timeouts().implicitlyWait(Long.parseLong(getData("Timeout")), TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, Long.parseLong(getData("Timeout")));
        ManagePages.initCalculator();
    }



    @BeforeClass
    @Parameters({"PlatformName"})
    public void startSession(String PlatformName) {
        platform = PlatformName;
        if (platform.equalsIgnoreCase("web"))
            initBrowser(getData("BrowserName"));
        else if(platform.equalsIgnoreCase("mobile"))
            initMobile();
        else if(platform.equalsIgnoreCase("api"))
            initAPI();
        else if(platform.equalsIgnoreCase("electron"))
            initElectron();
        else if(platform.equalsIgnoreCase("desktop"))
            initDesktop();
        else
            throw new RuntimeException("Invalid platform name");

        softAssert = new SoftAssert();
        screen = new Screen();

        ManageDB.openConnection(getData("DBURL"), getData("DBUserName"), getData("DBPassword"));
    }

    @AfterClass
    public void closeSession() {
        ManageDB.closeConnection();
        if (!platform.equalsIgnoreCase("api")){
            if(!platform.equalsIgnoreCase("mobile"))
                driver.quit();
            else
                mobileDriver.quit();
        }
    }

    @AfterMethod
    public void afterMethod(){
        if(platform.equalsIgnoreCase("web"))
            driver.get(getData("url"));
        else if(platform.equalsIgnoreCase("electron"))
            ElectronFlows.emptyList();
    }

    @BeforeMethod
    public void beforeMethod(Method method){
        if (!platform.equalsIgnoreCase("api")){
            try {
                MonteScreenRecorder.startRecord(method.getName());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}