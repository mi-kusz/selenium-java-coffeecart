import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MenuTest
{
    private static FirefoxOptions firefoxOptions;
    private WebDriver webDriver;

    private static final String menuUrl = "https://coffee-cart.app/";

    private static final List<String> validEnglishNames = List.of(
            "Espresso",
            "Espresso Macchiato",
            "Cappuccino",
            "Mocha",
            "Flat White",
            "Americano",
            "Cafe Latte",
            "Espresso Con Panna",
            "Cafe Breve"
    );

    private static final List<String> validChineseNames = List.of(
            "特浓咖啡",
            "浓缩玛奇朵",
            "卡布奇诺",
            "摩卡",
            "平白咖啡",
            "美式咖啡",
            "拿铁",
            "浓缩康宝蓝",
            "半拿铁"
    );

    @BeforeAll
    public static void setupClass()
    {
        WebDriverManager.firefoxdriver().setup();

        firefoxOptions = new FirefoxOptions();
        firefoxOptions.addArguments("--headless");
    }

    @BeforeEach
    public void setup()
    {
        webDriver = new FirefoxDriver(firefoxOptions);
        webDriver.get(menuUrl);
    }

    @AfterEach
    public void teardown()
    {
        webDriver.quit();
    }

    private void doubleClick(WebElement element)
    {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        String scrollScript = "arguments[0].scrollIntoView({behavior: 'auto', block: 'center'});";
        javascriptExecutor.executeScript(scrollScript, element);

        new Actions(webDriver)
                .doubleClick(element)
                .perform();
    }

    private List<WebElement> getMenuEntries()
    {
        return webDriver.findElements(By.cssSelector("li[data-v-a9662a08]"));
    }

    private WebElement getEntryHeader(WebElement element)
    {
        return element.findElement(By.tagName("h4"));
    }

    private WebElement getEntryCup(WebElement element)
    {
        return element.findElement(By.cssSelector("div div.cup"));
    }

    private String getEntryPrice(WebElement element)
    {
        WebElement header = getEntryHeader(element);

        return header.findElement(By.tagName("small")).getText();
    }

    private String getEntryName(WebElement element)
    {
        WebElement header = getEntryHeader(element);

        String nameWithPrice = header.getText();

        String priceText = getEntryPrice(element);
        int lengthWithoutPrice = nameWithPrice.length() - priceText.length();

        return nameWithPrice.substring(0, lengthWithoutPrice).trim();
    }

    private List<String> getMenuEntriesNames(List<WebElement> menuEntries)
    {
        return menuEntries.stream()
                .map(this::getEntryName)
                .toList();
    }

    private WebElement getPayButton()
    {
        return webDriver.findElement(By.cssSelector("button.pay"));
    }

    @Test
    public void testMenuEntriesAreDisplayed()
    {
        List<WebElement> menuEntries = getMenuEntries();

        for (WebElement menuEntry : menuEntries)
        {
            assertTrue(menuEntry.isDisplayed());
        }
    }

    @Test
    public void testMenuHeadersEnglishNamesAreValid()
    {
        List<WebElement> menuEntries = getMenuEntries();
        List<String> names = getMenuEntriesNames(menuEntries);

        assertEquals(validEnglishNames, names);
    }

    @Test
    public void testMenuHeadersChangeToChineseOnDoubleClick()
    {
        List<WebElement> menuEntries = getMenuEntries();
        List<WebElement> menuHeaders = menuEntries.stream().map(this::getEntryHeader).toList();

        for (WebElement menuHeader : menuHeaders)
        {
            doubleClick(menuHeader);
        }

        List<String> names = getMenuEntriesNames(menuEntries);

        assertEquals(validChineseNames, names);
    }

    @Test
    public void testMenuHeadersComeBackToEnglishOnDoubleClick()
    {
        List<WebElement> menuEntries = getMenuEntries();
        List<WebElement> menuHeaders = menuEntries.stream().map(this::getEntryHeader).toList();

        for (WebElement menuHeader : menuHeaders)
        {
            doubleClick(menuHeader);
            doubleClick(menuHeader);
        }

        List<String> names = getMenuEntriesNames(menuEntries);

        assertEquals(validEnglishNames, names);
    }

    @Test
    public void testMenuHeadersChangeColorOnHover()
    {
        List<WebElement> menuHeaders = getMenuEntries().stream().map(this::getEntryHeader).toList();

        for (WebElement menuHeader : menuHeaders)
        {
            String colorBefore = menuHeader.getCssValue("color");

            new Actions(webDriver)
                    .moveToElement(menuHeader)
                    .perform();

            String colorOnHover = menuHeader.getCssValue("color");

            new Actions(webDriver)
                    .moveToLocation(0, 0)
                    .perform();

            String colorOnMouseOff = menuHeader.getCssValue("color");

            assertEquals("rgb(0, 0, 0)", colorBefore);
            assertEquals("rgb(218, 165, 32)", colorOnHover);
            assertEquals("rgb(0, 0, 0)", colorOnMouseOff);
        }
    }

    @Test
    public void testPricesAreValid()
    {
        List<String> prices = getMenuEntries().stream().map(this::getEntryPrice).toList();

        String regex = "^\\$[0-9]+\\.[0-9]{2}$";

        for (String price : prices)
        {
            assertTrue(Pattern.matches(regex, price));
        }
    }

    @Test
    public void testCupsRotateOnHover()
    {
        List<WebElement> cups = getMenuEntries().stream().map(this::getEntryCup).toList();

        for (WebElement cup : cups)
        {
            String transformBefore = cup.getCssValue("transform");

            new Actions(webDriver)
                    .moveToElement(cup)
                    .perform();

            String transformOnHover = cup.getCssValue("transform");

            new Actions(webDriver)
                    .moveToLocation(0, 0)
                    .perform();

            String transformOnMouseOff = cup.getCssValue("transform");

            assertEquals("none", transformBefore);
            assertTrue(transformOnHover.contains("matrix"));
            assertEquals("none", transformOnMouseOff);
        }
    }

    @Test
    public void testPriceIsZeroInitially()
    {
        WebElement payButton = getPayButton();
        String payText = "Total: $";
        String payValue = payButton.getText().substring(payText.length());

        assertEquals("0.00", payValue);
    }
}
