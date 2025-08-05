import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NavigationTest
{
    private static FirefoxOptions firefoxOptions;
    private WebDriver webDriver;

    private static final String menuUrl = "https://coffee-cart.app/";
    private static final String cartUrl = "https://coffee-cart.app/cart";
    private static final String githubUrl = "https://coffee-cart.app/github";

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
    }

    @AfterEach
    public void teardown()
    {
        webDriver.quit();
    }

    private WebElement getNavigation()
    {
        return webDriver.findElement(By.cssSelector("#app ul[data-v-bb7b5941]"));
    }

    private List<WebElement> getNavigationLinks()
    {
        WebElement navigation = getNavigation();

        return navigation.findElements(By.cssSelector("li[data-v-bb7b5941]"));
    }

    private static Stream<String> urlProvider()
    {
        return Stream.of(
                menuUrl,
                cartUrl,
                githubUrl
        );
    }

    @ParameterizedTest
    @MethodSource("urlProvider")
    public void testNavigationIsDisplayed(String url)
    {
        webDriver.get(url);

        WebElement navigation = getNavigation();

        assertTrue(navigation.isDisplayed());
    }

    @ParameterizedTest
    @MethodSource("urlProvider")
    public void testNavigationLinksNumber(String url)
    {
        webDriver.get(url);

        List<WebElement> links = getNavigationLinks();

        assertEquals(3, links.size());
    }

    @ParameterizedTest
    @MethodSource("urlProvider")
    public void testNavigationLinksAreDisplayed(String url)
    {
        webDriver.get(url);

        List<WebElement> links = getNavigationLinks();

        for (WebElement link : links)
        {
            assertTrue(link.isDisplayed());
        }
    }

    @ParameterizedTest
    @MethodSource("urlProvider")
    public void testNavigationLinksContainValidTextInitially(String url)
    {
        webDriver.get(url);

        List<WebElement> links = getNavigationLinks();

        WebElement menuLink = links.get(0);
        WebElement cartLink = links.get(1);
        WebElement githubLink = links.get(2);

        assertEquals("menu", menuLink.getText());
        assertEquals("cart (0)", cartLink.getText());
        assertEquals("github", githubLink.getText());

        // cart (X)
        // where X is non-negative number
        // String regex = "cart \\(\\d+\\)";
        // assertTrue(Pattern.matches(regex, cartLink.getText()));
    }

    @ParameterizedTest
    @MethodSource("urlProvider")
    public void testNavigationLinksAreValid(String url)
    {
        final List<String> urlOrder = List.of(menuUrl, cartUrl, githubUrl);

        for (int linkIndex = 0; linkIndex < 3; ++linkIndex)
        {
            webDriver.get(url);

            List<WebElement> links = getNavigationLinks();

            WebElement link = links.get(linkIndex);
            link.click();

            assertEquals(urlOrder.get(linkIndex), webDriver.getCurrentUrl());
        }
    }

    private String getLinkColor(WebElement link)
    {
        return link.findElement(By.tagName("a")).getCssValue("color");
    }

    @ParameterizedTest
    @MethodSource("urlProvider")
    public void testCurrentPageIsInDifferentColor(String url)
    {
        webDriver.get(url);

        List<WebElement> links = getNavigationLinks();

        for (WebElement link : links)
        {
            // Current page
            if (link.findElement(By.tagName("a")).getAttribute("href").equals(url))
            {
                assertEquals("rgb(218, 165, 32)", getLinkColor(link));
            }
            else
            {
                assertEquals("rgb(0, 0, 0)", getLinkColor(link));
            }
        }
    }
}
