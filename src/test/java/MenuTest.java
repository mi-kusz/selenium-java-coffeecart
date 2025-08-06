import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class MenuTest
{
    private static FirefoxOptions firefoxOptions;
    private WebDriver webDriver;
    private WebDriverWait wait;

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
        wait = new WebDriverWait(webDriver, Duration.ofSeconds(5));
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

    private String getEntryPriceText(WebElement element)
    {
        WebElement header = getEntryHeader(element);

        return header.findElement(By.tagName("small")).getText();
    }

    private BigDecimal getEntryPrice(WebElement entryElement)
    {
        String priceText = getEntryPriceText(entryElement);
        String number = priceText.substring(1); // Delete $ sign

        return new BigDecimal(number);
    }

    private String getEntryName(WebElement element)
    {
        WebElement header = getEntryHeader(element);

        String nameWithPrice = header.getText();

        String priceText = getEntryPriceText(element);
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

    private void assertPriceOnButtonIsEqual(BigDecimal expectedPrice)
    {
        WebElement payButton = getPayButton();
        String staticText = "Total: $";

        BigDecimal buttonValue = new BigDecimal(payButton.getText().substring(staticText.length()));

        assertEquals(expectedPrice.stripTrailingZeros(), buttonValue.stripTrailingZeros());
    }

    private WebElement getCartPreview()
    {
        return webDriver.findElement(By.cssSelector("ul.cart-preview"));
    }

    private List<WebElement> getCartPreviewEntries()
    {
        WebElement cartPreview = getCartPreview();

        return cartPreview.findElements(By.tagName("li"));
    }

    private String getCartPreviewEntryName(WebElement cartPreviewEntry)
    {
        WebElement span = cartPreviewEntry.findElement(By.tagName("span"));

        wait.until(driver -> !span.getText().isEmpty());

        return span.getText();
    }

    private int getCartPreviewEntryCount(WebElement cartPreviewEntry)
    {
        WebElement entryCount = cartPreviewEntry.findElement(By.cssSelector("span.unit-desc"));

        // Omit "x" character
        return Integer.parseInt(entryCount.getText().substring(1).trim());
    }

    private WebElement getAddButton(WebElement cartPreviewEntry)
    {
        return cartPreviewEntry.findElement(By.cssSelector("div.unit-controller button"));
    }

    private WebElement getRemoveButton(WebElement cartPreviewEntry)
    {
        return cartPreviewEntry.findElements(By.cssSelector("div.unit-controller button")).get(1);
    }

    private WebElement getPromoElement()
    {
        return webDriver.findElement(By.className("promo"));
    }

    private WebElement getAcceptPromoButton()
    {
        WebElement promo = getPromoElement();

        return promo.findElement(By.cssSelector("div.buttons button.yes"));
    }

    private WebElement getDiscardPromoButton()
    {
        WebElement promo = getPromoElement();

        return promo.findElements(By.cssSelector("div.buttons button")).get(1);
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
        List<String> prices = getMenuEntries().stream().map(this::getEntryPriceText).toList();

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
    public void testPayButtonIsDisplayed()
    {
        WebElement payButton = getPayButton();

        assertTrue(payButton.isDisplayed());
    }

    @Test
    public void testPriceIsZeroInitially()
    {
        assertPriceOnButtonIsEqual(BigDecimal.ZERO);
    }

    @Test
    public void testAddingCoffeesIncreasePrice()
    {
        List<WebElement> menuEntries = getMenuEntries();
        BigDecimal expectedPrice = BigDecimal.ZERO;

        for (WebElement menuEntry : menuEntries)
        {
            WebElement cupElement = getEntryCup(menuEntry);
            BigDecimal coffeePrice = getEntryPrice(menuEntry);

            cupElement.click();
            expectedPrice = expectedPrice.add(coffeePrice);

            assertPriceOnButtonIsEqual(expectedPrice);
        }
    }

    @Test
    public void testAddingTheSameCoffeeToCartGivesValidPrice()
    {
        final int repeats = 10;
        final int cupsNumber = 9;

        for (int cupIndex = 0; cupIndex < cupsNumber; ++cupIndex)
        {
            webDriver.navigate().refresh();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("li[data-v-a9662a08]")));

            WebElement menuEntry = getMenuEntries().get(cupIndex);
            BigDecimal expectedPrice = BigDecimal.ZERO;

            WebElement cupElement = getEntryCup(menuEntry);
            BigDecimal coffeePrice = getEntryPrice(menuEntry);

            for (int i = 0; i < repeats; ++i)
            {
                cupElement.click();
                expectedPrice = expectedPrice.add(coffeePrice);

                assertPriceOnButtonIsEqual(expectedPrice);
            }
        }
    }

    @Test
    public void testCartPreviewDoesNotShowUpWithoutHover()
    {
        // Empty cart
        assertThrows(NoSuchElementException.class, this::getCartPreview);

        WebElement menuEntry = getMenuEntries().getFirst();
        WebElement cupElement = getEntryCup(menuEntry);

        cupElement.click();

        WebElement cartPreview = getCartPreview();

        // Non-empty cart, but without hovering
        assertFalse(cartPreview.isDisplayed());
    }

    @Test
    public void testCartPreviewDoesNotShowUpWhenCartIsEmpty()
    {
        WebElement payButton = getPayButton();

        new Actions(webDriver)
                .moveToElement(payButton)
                .perform();

        assertThrows(NoSuchElementException.class, this::getCartPreview);
    }

    @Test
    public void testOrderedElementsShowUpInCartPreview()
    {
        List<WebElement> cupElements = getMenuEntries().stream().map(this::getEntryCup).toList();
        WebElement payButton = getPayButton();

        for (WebElement cup : cupElements)
        {
            cup.click();
        }

        new Actions(webDriver)
                .moveToElement(payButton)
                .perform();

        List<WebElement> cartPreviewEntries = getCartPreviewEntries();

        assertEquals(9, cartPreviewEntries.size());

        for (WebElement previewEntry : cartPreviewEntries)
        {
            String name = getCartPreviewEntryName(previewEntry);
            int count = getCartPreviewEntryCount(previewEntry);

            assertTrue(validEnglishNames.contains(name));
            assertEquals(1, count);
        }
    }

    @Test
    public void testPlusAndMinusButtonsAreDisplayedInCartPreview()
    {
        List<WebElement> cupElements = getMenuEntries().stream().map(this::getEntryCup).toList();
        WebElement payButton = getPayButton();

        for (WebElement cup : cupElements)
        {
            cup.click();
        }

        new Actions(webDriver)
                .moveToElement(payButton)
                .perform();

        List<WebElement> cartPreviewEntries = getCartPreviewEntries();

        for (WebElement cartPreviewEntry : cartPreviewEntries)
        {
            WebElement plusButton = getAddButton(cartPreviewEntry);
            WebElement minusButton = getRemoveButton(cartPreviewEntry);

            assertTrue(plusButton.isDisplayed());
            assertTrue(minusButton.isDisplayed());
        }
    }

    @Test
    public void testPlusAndMinusButtonsAddAndRemoveElementsFromCart()
    {
        List<WebElement> cupElements = getMenuEntries().stream().map(this::getEntryCup).toList();
        WebElement payButton = getPayButton();

        for (WebElement cup : cupElements)
        {
            cup.click();
        }

        new Actions(webDriver)
                .moveToElement(payButton)
                .perform();

        List<WebElement> cartPreviewEntries = getCartPreviewEntries();

        for (WebElement cartPreviewEntry : cartPreviewEntries)
        {
            WebElement plusButton = getAddButton(cartPreviewEntry);
            WebElement minusButton = getRemoveButton(cartPreviewEntry);

            plusButton.click();

            int count = getCartPreviewEntryCount(cartPreviewEntry);
            assertEquals(2, count);

            minusButton.click();

            count = getCartPreviewEntryCount(cartPreviewEntry);
            assertEquals(1, count);
        }
    }

    @Test
    public void testRemovingSingleElementsFromPreviewDeleteEntry()
    {
        List<WebElement> cupElements = getMenuEntries().stream().map(this::getEntryCup).toList();
        WebElement payButton = getPayButton();

        for (WebElement cup : cupElements)
        {
            cup.click();
        }

        new Actions(webDriver)
                .moveToElement(payButton)
                .perform();

        List<WebElement> cartPreviewEntries = getCartPreviewEntries();
        int expectedEntries = cartPreviewEntries.size();

        while (true)
        {
            assertEquals(expectedEntries, cartPreviewEntries.size());

            WebElement firstEntry = cartPreviewEntries.getFirst();
            WebElement deleteButton = getRemoveButton(firstEntry);

            deleteButton.click();
            --expectedEntries;

            if (expectedEntries == 0)
            {
                assertThrows(NoSuchElementException.class, this::getCartPreviewEntries);
                break;
            }
            else
            {
                cartPreviewEntries = getCartPreviewEntries();
            }
        }
    }

    @Test
    public void testPromoIsNotDisplayedInitially()
    {
        assertThrows(NoSuchElementException.class, this::getPromoElement);
    }

    @Test
    public void testOrderingThreeCoffeesShowsPromo()
    {
        List<WebElement> menuEntries = getMenuEntries();
        int counter = 0;

        for (WebElement menuEntry : menuEntries)
        {
            if (counter != 0 && counter % 3 == 0)
            {
                assertDoesNotThrow(this::getPromoElement);
            }
            else
            {
                assertThrows(NoSuchElementException.class, this::getPromoElement);
            }

            WebElement cupElement = getEntryCup(menuEntry);
            cupElement.click();

            ++counter;
        }
    }

    @Test
    public void testAcceptAndDiscardPromoButtonsAreDisplayed()
    {
        WebElement menuEntry = getMenuEntries().getFirst();
        WebElement cupElement = getEntryCup(menuEntry);

        for (int i = 0; i < 3; ++i)
        {
            cupElement.click();
        }

        WebElement acceptButton = getAcceptPromoButton();
        WebElement discardButton = getDiscardPromoButton();

        assertTrue(acceptButton.isDisplayed());
        assertTrue(discardButton.isDisplayed());
    }

    @Test
    public void testDiscardPromoButtonDoesNotChangeAnything()
    {
        WebElement menuEntry = getMenuEntries().getFirst();
        WebElement cupElement = getEntryCup(menuEntry);
        BigDecimal entryPrice = getEntryPrice(menuEntry);

        BigDecimal expectedPrice = BigDecimal.ZERO;

        for (int i = 0; i < 3; ++i)
        {
            expectedPrice = expectedPrice.add(entryPrice);
            cupElement.click();
        }

        WebElement discardButton = getDiscardPromoButton();
        discardButton.click();

        assertThrows(NoSuchElementException.class, this::getPromoElement);
        assertPriceOnButtonIsEqual(expectedPrice);
    }

    // TODO: Accept promo adds discounted mocha (price is 4)
    // TODO: Discounted item is always on top of the preview
    // TODO: Discounted item could be removed, but not added
}
