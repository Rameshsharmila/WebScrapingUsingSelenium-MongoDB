import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.github.bonigarcia.wdm.WebDriverManager;

public class WebScrappingTest {
	
	WebDriver driver;
	MongoCollection<Document> webCollection;
	
	@BeforeSuite
	public void connectMongoDB() {
		Logger log = Logger.getLogger("org.mongodb.driver");
		
		//Create mongo client
		MongoClient mongoclient = MongoClients.create("mongodb://localhost:27017");
	
		//Connect to database
		MongoDatabase mongodatabase = mongoclient.getDatabase("automationdb");
		
		//Create a collection under the chosed database
		webCollection = mongodatabase.getCollection("web");
		
	}
	
	@BeforeTest
	public void setUp() {
		WebDriverManager.chromedriver().setup();
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");
		driver = new ChromeDriver(options);
		
	}

	@DataProvider
	public Object[][] getData() {
		return new Object[][] {
			{"https://www.amazon.com"},
			{"https://www.ebay.com"}
		};
		
	}
	
	
	@Test (dataProvider="getData")
	public void webScrapeTest(String appUrl) {
		driver.get(appUrl);
		String url = driver.getCurrentUrl();
		String title = driver.getTitle();
		
		int linksCount = driver.findElements(By.tagName("a")).size();
		int imagesCount = driver.findElements(By.tagName("img")).size();
		
		List<WebElement> linkslist = driver.findElements(By.tagName("a"));
		List<String> linksAttrList = new ArrayList<String>();

		List<WebElement> imagesList = driver.findElements(By.tagName("img"));
		List<String> ImageSrcList = new ArrayList<String>();

		//Add the scraped data to the document in key-value pairs
		Document d1 = new Document();
		d1.append("url", url);
		d1.append("title", title);
		d1.append("totalLinks", linksCount);
		d1.append("totalImages", imagesCount);
		
		for(WebElement e:linkslist) {
			String hrefval = e.getAttribute("href");
			linksAttrList.add(hrefval);
					
		}
		
		for(WebElement e:imagesList) {
			String srcval=e.getAttribute("src");
			ImageSrcList.add(srcval);
		}
		
		d1.append("linksAttribute", linksAttrList);
		d1.append("SourceValue", ImageSrcList);
		
		List<Document> docsList = new ArrayList<Document>();
		docsList.add(d1);
		
		//Insert the document to the collection
		webCollection.insertMany(docsList);
	}
	
	
	@AfterTest
	public void tearDown() {
		driver.quit();
	}
}
