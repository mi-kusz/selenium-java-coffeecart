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

    private static final int itemsToPromo = 3;

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

    private void hoverOverPayButton()
    {
        WebElement payButton = getPayButton();

        new Actions(webDriver)
                .moveToElement(payButton)
                .perform();
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
        hoverOverPayButton();

        assertThrows(NoSuchElementException.class, this::getCartPreview);
    }

    @Test
    public void testOrderedElementsShowUpInCartPreview()
    {
        List<WebElement> cupElements = getMenuEntries().stream().map(this::getEntryCup).toList();

        for (WebElement cup : cupElements)
        {
            cup.click();
        }

        hoverOverPayButton();

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

        for (WebElement cup : cupElements)
        {
            cup.click();
        }

        hoverOverPayButton();

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

        for (WebElement cup : cupElements)
        {
            cup.click();
        }

        hoverOverPayButton();

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

        for (WebElement cup : cupElements)
        {
            cup.click();
        }

        hoverOverPayButton();

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
            if (counter != 0 && counter % itemsToPromo == 0)
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

    private BigDecimal addItemsToCartToShowPromo()
    {
        WebElement menuEntry = getMenuEntries().getFirst();
        WebElement cupElement = getEntryCup(menuEntry);
        BigDecimal entryPrice = getEntryPrice(menuEntry);

        BigDecimal expectedPrice = BigDecimal.ZERO;

        for (int i = 0; i < itemsToPromo; ++i)
        {
            expectedPrice = expectedPrice.add(entryPrice);
            cupElement.click();
        }

        return expectedPrice;
    }

    @Test
    public void testAcceptAndDiscardPromoButtonsAreDisplayed()
    {
        addItemsToCartToShowPromo();

        WebElement acceptButton = getAcceptPromoButton();
        WebElement discardButton = getDiscardPromoButton();

        assertTrue(acceptButton.isDisplayed());
        assertTrue(discardButton.isDisplayed());
    }

    @Test
    public void testDiscardPromoButtonDoesNotChangeAnything()
    {
        BigDecimal expectedPrice = addItemsToCartToShowPromo();

        WebElement discardButton = getDiscardPromoButton();
        discardButton.click();

        assertThrows(NoSuchElementException.class, this::getPromoElement);
        assertPriceOnButtonIsEqual(expectedPrice);
    }

    @Test
    public void testAcceptPromoButtonAddsPrice()
    {
        BigDecimal discountedPrice = new BigDecimal("4.00");
        BigDecimal expectedPrice = addItemsToCartToShowPromo();

        WebElement acceptButton = getAcceptPromoButton();
        acceptButton.click();

        expectedPrice = expectedPrice.add(discountedPrice);

        assertPriceOnButtonIsEqual(expectedPrice);
    }

    @Test
    public void testAcceptPromoAddsDiscountedMochaToPreviewOnTheFirstPlace()
    {
        addItemsToCartToShowPromo();

        WebElement acceptButton = getAcceptPromoButton();
        acceptButton.click();

        hoverOverPayButton();

        WebElement cartPreviewFirstEntry = getCartPreviewEntries().getFirst();
        String entryName = getCartPreviewEntryName(cartPreviewFirstEntry);

        assertEquals("(Discounted) Mocha", entryName);
    }

    private boolean isSorted(List<String> strings)
    {
        for (int i = 1; i < strings.size(); ++i)
        {
            if (strings.get(i - 1).compareTo(strings.get(i)) > 0)
            {
                return false;
            }
        }

        return true;
    }

    @Test
    public void testItemsInCartAreSortedAlphabetically()
    {
        List<WebElement> cupElements = getMenuEntries().stream().map(this::getEntryCup).toList();

        for (WebElement cup : cupElements)
        {
            cup.click();
        }

        hoverOverPayButton();

        List<String> cartPreviewEntryNames = getCartPreviewEntries().stream().map(this::getCartPreviewEntryName).toList();

        assertTrue(isSorted(cartPreviewEntryNames));
    }

    @Test
    @Disabled("I think if a user gets a discounted item for ordering 3 items, the user should not be allowed to increase the number of discounted items without limit.")
    public void testDiscountedItemsCannotBeAddedInCartPreview()
    {
        addItemsToCartToShowPromo();

        WebElement acceptPromoButton = getAcceptPromoButton();
        acceptPromoButton.click();

        hoverOverPayButton();

        WebElement discountedEntry = getCartPreviewEntries().getFirst();
        assertThrows(NoSuchElementException.class, () -> getAddButton(discountedEntry));
    }

    @Test
    public void testDiscountedItemsCanBeRemovedInCartPreview()
    {
        addItemsToCartToShowPromo();

        WebElement acceptPromoButton = getAcceptPromoButton();
        acceptPromoButton.click();

        hoverOverPayButton();

        List<WebElement> cartPreviewEntries = getCartPreviewEntries();
        int initialCartPreviewSize = cartPreviewEntries.size();

        WebElement discountedEntry = cartPreviewEntries.getFirst();
        WebElement removeButton = getRemoveButton(discountedEntry);
        removeButton.click();

        cartPreviewEntries = getCartPreviewEntries();

        assertEquals(initialCartPreviewSize - 1, cartPreviewEntries.size());
    }

    @Test
    public void testPromoShowsUpEveryThreeBasicItemsOrderedWithoutPromoItems()
    {
        for (int i = 0; i < 3; ++i)
        {
            addItemsToCartToShowPromo();

            assertTrue(getPromoElement().isDisplayed());
        }
    }

    @Test
    @Disabled("Discounted items counts to the promo counter. Example: 3 basic items -> Get promo item -> Need to order 2 (instead of 3) another items to get another promo.")
    public void testPromoShowsUpEveryThreeBasicItemsOrderedWithPromoItems()
    {
        for (int i = 0; i < 3; ++i)
        {
            addItemsToCartToShowPromo();

            assertTrue(getPromoElement().isDisplayed());

            getAcceptPromoButton().click();
        }
    }

    @Test
    @Disabled("There is no upper limit for discounted items")
    public void testNumberOfDiscountedItemsIsLimitedByNumberOfBasicItems()
    {
        for (int i = 0; i < 3; ++i)
        {
            addItemsToCartToShowPromo();
            getAcceptPromoButton().click();
        }

        // Currently in cart: 9 basic items and 3 discounted

        hoverOverPayButton();

        List<WebElement> cartPreviewEntries = getCartPreviewEntries();
        WebElement discountedItemEntry = cartPreviewEntries.getFirst();
        WebElement basicItemEntry = cartPreviewEntries.get(1);

        WebElement removeButton = getRemoveButton(basicItemEntry);

        int basicItemCount = getCartPreviewEntryCount(basicItemEntry);
        int discountedItemCount;
        int maximumOfDiscountedItems;

        while (basicItemCount > 0)
        {
            maximumOfDiscountedItems = basicItemCount / itemsToPromo;

            if (maximumOfDiscountedItems == 0)
            {
                assertThrows(StaleElementReferenceException.class, () -> getCartPreviewEntryCount(discountedItemEntry));
                break;
            }
            else
            {
                discountedItemCount = getCartPreviewEntryCount(discountedItemEntry);
            }

            assertTrue(discountedItemCount <= maximumOfDiscountedItems);

            removeButton.click();

            --basicItemCount;
        }
    }

    private WebElement getModalElement()
    {
        return webDriver.findElement(By.cssSelector("div.modal-content"));
    }

    private WebElement getModalNameInput()
    {
        WebElement modal = getModalElement();

        return modal.findElement(By.cssSelector("input#name"));
    }

    private WebElement getModalEmailInput()
    {
        WebElement modal = getModalElement();

        return modal.findElement(By.cssSelector("input#email"));
    }

    private WebElement getModalPromotionCheckbox()
    {
        WebElement modal = getModalElement();

        return modal.findElement(By.cssSelector("input#promotion"));
    }

    private WebElement getSubmitPaymentButton()
    {
        WebElement modal = getModalElement();

        return modal.findElement(By.cssSelector("button#submit-payment"));
    }

    @Test
    public void testModalIsNotDisplayedInitially()
    {
        WebElement modal = getModalElement();

        assertFalse(modal.isDisplayed());
    }

    @Test
    public void testModalShowsUpAfterPayButtonClick()
    {
        WebElement payButton = getPayButton();
        WebElement modal = getModalElement();

        payButton.click();

        assertTrue(modal.isDisplayed());
    }

    @Test
    public void testModalNameInputIsDisplayed()
    {
        WebElement payButton = getPayButton();
        payButton.click();

        WebElement nameInput = getModalNameInput();

        assertTrue(nameInput.isDisplayed());
    }

    @Test
    public void testModalEmailInputIsDisplayed()
    {
        WebElement payButton = getPayButton();
        payButton.click();

        WebElement emailInput = getModalEmailInput();

        assertTrue(emailInput.isDisplayed());
    }

    @Test
    public void testModalPromoInputIsDisplayed()
    {
        WebElement payButton = getPayButton();
        payButton.click();

        WebElement promoCheckbox = getModalPromotionCheckbox();

        assertTrue(promoCheckbox.isDisplayed());
    }

    @Test
    public void testModalSubmitInputIsDisplayed()
    {
        WebElement payButton = getPayButton();
        payButton.click();

        WebElement submitButton = getSubmitPaymentButton();

        assertTrue(submitButton.isDisplayed());
    }

    @Test
    public void testModalNameAndEmailCannotBeEmpty()
    {
        WebElement payButton = getPayButton();
        payButton.click();

        WebElement submitButton = getSubmitPaymentButton();
        submitButton.click();

        // Modal does not disappear
        assertTrue(getModalElement().isDisplayed());
    }

    @Test
    public void testModalEmailCannotBeEmpty()
    {
        WebElement payButton = getPayButton();
        payButton.click();

        WebElement nameInput = getModalNameInput();
        nameInput.sendKeys("Test name");

        WebElement submitButton = getSubmitPaymentButton();
        submitButton.click();

        // Modal does not disappear
        assertTrue(getModalElement().isDisplayed());
    }

    @Test
    public void testModalNameCannotBeEmpty()
    {
        WebElement payButton = getPayButton();
        payButton.click();

        WebElement emailInput = getModalEmailInput();
        emailInput.sendKeys("test@test.com");

        WebElement submitButton = getSubmitPaymentButton();
        submitButton.click();

        // Modal does not disappear
        assertTrue(getModalElement().isDisplayed());
    }

    @Test
    public void testModalEmailMustBeValid()
    {
        WebElement payButton = getPayButton();
        payButton.click();

        WebElement nameInput = getModalNameInput();
        nameInput.sendKeys("Test name");

        WebElement emailInput = getModalEmailInput();
        emailInput.sendKeys("test");

        WebElement submitButton = getSubmitPaymentButton();
        submitButton.click();

        // Modal does not disappear
        assertTrue(getModalElement().isDisplayed());
    }

    @Test
    public void testModalDisappearsWithValidData()
    {
        WebElement payButton = getPayButton();
        payButton.click();

        WebElement nameInput = getModalNameInput();
        nameInput.sendKeys("Test name");

        WebElement emailInput = getModalEmailInput();
        emailInput.sendKeys("test@test.com");

        WebElement submitButton = getSubmitPaymentButton();
        submitButton.click();

        // Modal does not disappear
        assertFalse(getModalElement().isDisplayed());
    }

    @Test
    public void testModalCheckboxCanBeSelectedAndUnselected()
    {
        WebElement payButton = getPayButton();
        payButton.click();

        WebElement checkboxInput = getModalPromotionCheckbox();

        assertFalse(checkboxInput.isSelected());

        checkboxInput.click();
        assertTrue(checkboxInput.isSelected());

        checkboxInput.click();
        assertFalse(checkboxInput.isSelected());
    }

    private WebElement getSnackbarElement()
    {
        return webDriver.findElement(By.cssSelector("div.snackbar.success"));
    }

    @Test
    public void testSnackbarIsNotDisplayedInitially()
    {
        assertThrows(NoSuchElementException.class, this::getSnackbarElement);
    }

    @Test
    public void testSnackBarShowsUpAfterPurchase()
    {
        getPayButton().click();

        getModalNameInput().sendKeys("Test name");
        getModalEmailInput().sendKeys("test@test.com");
        getSubmitPaymentButton().click();

        assertTrue(getSnackbarElement().isDisplayed());
    }

    @Test
    public void testSnackbarDisappearsAfterTime()
    {
        getPayButton().click();

        getModalNameInput().sendKeys("Test name");
        getModalEmailInput().sendKeys("test@test.com");
        getSubmitPaymentButton().click();

        WebElement snackbar = getSnackbarElement();

        wait.until(ExpectedConditions.invisibilityOf(snackbar));

        assertFalse(snackbar.isDisplayed());
    }
}
