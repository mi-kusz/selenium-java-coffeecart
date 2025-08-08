import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CartTest
{
    private static FirefoxOptions firefoxOptions;
    private WebDriver webDriver;
    private WebDriverWait wait;

    private static final String menuUrl = "https://coffee-cart.app/";
    private static final String cartUrl = "https://coffee-cart.app/cart";

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
    }

    @AfterEach
    public void teardown()
    {
        webDriver.quit();
    }

    private void goToCartTab()
    {
        WebElement link = webDriver.findElement(By.cssSelector("a[href='/cart']"));
        link.click();
    }

    @Test
    public void testEmptyCart()
    {
        webDriver.get(cartUrl);

        WebElement paragraph = webDriver.findElement(By.cssSelector("div.list p"));

        assertEquals("No coffee, go add some.", paragraph.getText());
    }

    private void addEveryCoffeeToCart()
    {
        webDriver.get(menuUrl);
        List<WebElement> entryButtons = webDriver.findElements(By.cssSelector("li[data-v-a9662a08]"))
                .stream()
                .map(element -> element.findElement(By.cssSelector("div div.cup")))
                .toList();

        for (WebElement coffeeButton : entryButtons)
        {
            coffeeButton.click();
        }

        goToCartTab();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
    }

    @Test
    public void testListHeaderInCart()
    {
        addEveryCoffeeToCart();

        WebElement header = webDriver.findElement(By.cssSelector("li.list-header"));
        List<WebElement> columns = header.findElements(By.tagName("div"));

        assertTrue(columns.size() >= 3);
        assertEquals("Item", columns.get(0).getText());
        assertEquals("Unit", columns.get(1).getText());
        assertEquals("Total", columns.get(2).getText());
    }

    private List<WebElement> getOrderedItemsEntries()
    {
        return webDriver.findElements(By.cssSelector("ul:not(.cart-preview) li.list-item"));
    }

    @Test
    public void testEntriesNumber()
    {
        addEveryCoffeeToCart();
        List<WebElement> entryRows = getOrderedItemsEntries();

        assertEquals(9, entryRows.size());
    }

    private String getEntryName(WebElement entry)
    {
        WebElement nameDiv = entry.findElement(By.tagName("div"));

        return nameDiv.getText();
    }

    private BigDecimal getEntryUnitPrice(WebElement entry)
    {
        WebElement priceSpan = entry.findElement(By.cssSelector("div span.unit-desc"));
        String priceWithAmount = priceSpan.getText();

        String priceText = priceWithAmount.split("x")[0].trim();

        return new BigDecimal(priceText.substring(1));
    }

    private int getEntryAmount(WebElement entry)
    {
        WebElement amountSpan = entry.findElement(By.cssSelector("div span.unit-desc"));
        String priceWithAmount = amountSpan.getText();

        String amountText = priceWithAmount.split("x")[1].trim();

        return Integer.parseInt(amountText);
    }

    private WebElement getAddButton(WebElement entry)
    {
        return entry.findElement(By.cssSelector("div div.unit-controller button"));
    }

    private WebElement getRemoveButton(WebElement entry)
    {
        return entry.findElements(By.cssSelector("div div.unit-controller button")).get(1);
    }

    private BigDecimal getEntryTotalPrice(WebElement entry)
    {
        WebElement totalPriceDiv = entry.findElements(By.cssSelector(":scope > div")).get(2);
        String totalPriceText = totalPriceDiv.getText().substring(1);

        return new BigDecimal(totalPriceText);
    }

    private WebElement getRemoveEntryButton(WebElement entry)
    {
        return entry.findElement(By.cssSelector("div button[class='delete']"));
    }

    @Test
    public void testEntryNamesAreDisplayed()
    {
        addEveryCoffeeToCart();

        List<WebElement> cartEntries = getOrderedItemsEntries();

        for (WebElement entry : cartEntries)
        {
            assertTrue(entry.isDisplayed());
        }
    }

    @Test
    public void testUnitPricesAreNonNegative()
    {
        addEveryCoffeeToCart();

        List<WebElement> cartEntries = getOrderedItemsEntries();

        for (WebElement entry : cartEntries)
        {
            BigDecimal unitPrice = getEntryUnitPrice(entry);

            assertTrue(unitPrice.signum() >= 0);
        }
    }

    @Test
    public void testEntryAmountIsPositive()
    {
        addEveryCoffeeToCart();

        List<WebElement> cartEntries = getOrderedItemsEntries();

        for (WebElement entry : cartEntries)
        {
            int amount = getEntryAmount(entry);

            assertTrue(amount > 0);
        }
    }

    @Test
    public void testAddButtonsAreDisplayed()
    {
        addEveryCoffeeToCart();

        List<WebElement> cartEntries = getOrderedItemsEntries();

        for (WebElement entry : cartEntries)
        {
            WebElement addButton = getAddButton(entry);

            assertTrue(addButton.isDisplayed());
        }
    }

    @Test
    public void testRemoveButtonsAreDisplayed()
    {
        addEveryCoffeeToCart();

        List<WebElement> cartEntries = getOrderedItemsEntries();

        for (WebElement entry : cartEntries)
        {
            WebElement removeButton = getRemoveButton(entry);

            assertTrue(removeButton.isDisplayed());
        }
    }

    @Test
    public void testTotalEntryPriceIsValidInitially()
    {
        addEveryCoffeeToCart();

        List<WebElement> cartEntries = getOrderedItemsEntries();

        for (WebElement entry : cartEntries)
        {
            BigDecimal unitPrice = getEntryUnitPrice(entry);
            BigDecimal totalPrice = getEntryTotalPrice(entry);

            assertEquals(unitPrice, totalPrice);
        }
    }

    @Test
    public void testAddingCoffeesChangesAmountAndTotalEntryPrice()
    {
        int repeats = 3;

        addEveryCoffeeToCart();

        List<WebElement> cartEntries = getOrderedItemsEntries();

        for (WebElement entry : cartEntries)
        {
            WebElement addButton = getAddButton(entry);
            BigDecimal unitPrice = getEntryUnitPrice(entry);

            BigDecimal totalPrice;
            int amount;

            BigDecimal expectedPrice;

            for (int expectedAmount = 1; expectedAmount <= repeats; ++expectedAmount)
            {
                expectedPrice = unitPrice.multiply(new BigDecimal(expectedAmount));
                totalPrice = getEntryTotalPrice(entry);
                amount = getEntryAmount(entry);

                assertEquals(expectedAmount, amount);
                assertEquals(expectedPrice, totalPrice);

                addButton.click();
            }
        }
    }

    @Test
    public void testRemovingCoffeesChangesAmountAndTotalPrice()
    {
        int repeats = 3;

        addEveryCoffeeToCart();

        List<WebElement> cartEntries = getOrderedItemsEntries();

        for (WebElement entry : cartEntries)
        {
            WebElement addButton = getAddButton(entry);

            for (int i = 0; i < repeats - 1; ++i)
            {
                addButton.click();
            }

            WebElement removeButton = getRemoveButton(entry);
            BigDecimal unitPrice = getEntryUnitPrice(entry);

            BigDecimal totalPrice;
            int amount;

            BigDecimal expectedPrice;

            for (int expectedAmount = repeats; expectedAmount > 0; --expectedAmount)
            {
                expectedPrice = unitPrice.multiply(new BigDecimal(expectedAmount));
                totalPrice = getEntryTotalPrice(entry);
                amount = getEntryAmount(entry);

                assertEquals(expectedAmount, amount);
                assertEquals(expectedPrice, totalPrice);

                removeButton.click();
            }
        }
    }

    @Test
    public void testRemoveEntryButtonDeletesEntireEntry()
    {
        int repeats = 2;

        addEveryCoffeeToCart();

        List<WebElement> cartEntries = getOrderedItemsEntries();
        int expectedSize = cartEntries.size();

        while (!cartEntries.isEmpty())
        {
            assertEquals(expectedSize, cartEntries.size());

            WebElement entry = cartEntries.getFirst();

            WebElement addButton = getAddButton(entry);
            WebElement removeEntryButton = getRemoveEntryButton(entry);

            for (int i = 0; i < repeats; ++i)
            {
                addButton.click();
            }

            removeEntryButton.click();
            --expectedSize;
            cartEntries = getOrderedItemsEntries();
        }
    }

    @Test
    public void testRemovingSingleItemRemovesEntireEntry()
    {
        addEveryCoffeeToCart();

        List<WebElement> cartEntries = getOrderedItemsEntries();
        int expectedEntriesCount = cartEntries.size();

        while (!cartEntries.isEmpty())
        {
            assertEquals(expectedEntriesCount, cartEntries.size());

            WebElement entry = cartEntries.getFirst();
            WebElement removeButton = getRemoveButton(entry);

            removeButton.click();
            --expectedEntriesCount;
            cartEntries = getOrderedItemsEntries();
        }
    }

    @Test
    public void testTotalPriceOfCartIsValid()
    {
        int repeats = 3;
        BigDecimal expectedTotalCartPrice = BigDecimal.ZERO;

        addEveryCoffeeToCart();

        List<WebElement> cartEntries = getOrderedItemsEntries();

        for (WebElement entry : cartEntries)
        {
            WebElement addButton = getAddButton(entry);

            for (int i = 0; i < repeats; ++i)
            {
                addButton.click();
            }

            expectedTotalCartPrice = expectedTotalCartPrice.add(getEntryTotalPrice(entry));
        }

        String totalPriceText = webDriver.findElement(By.cssSelector("div.pay-container button.pay")).getText();
        BigDecimal totalPrice = new BigDecimal(totalPriceText.substring("Total: $".length())); // Remove "Total: $" preceding text

        assertEquals(expectedTotalCartPrice, totalPrice);
    }
}
