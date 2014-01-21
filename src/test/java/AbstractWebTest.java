import com.google.common.base.Predicate;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * PhantomJSを使うWebテストケースの基底クラス。
 *
 * <p>
 * システムプロパティから以下の値を読み込み、テストケース内で使用する。
 * </p>
 * <ul>
 *     <li>my.apps.test.timeout: DOM要素の出現を待ち受ける時間(ミリ秒)</li>
 *     <li>my.apps.test.appurl: テスト対象アプリのURL</li>
 *     <li>my.apps.test.phantomjspath: PhantomJSのパス</li>
 * </ul>
 */
public abstract class AbstractWebTest {

    private long timeout;

    private String appUrl;

    private String phantomJsPath;

    private WebDriver driver;

    public AbstractWebTest() {
        Properties sysProps = System.getProperties();

        String timeout = sysProps.getProperty("my.apps.test.timeout", "15");
        this.timeout = Long.parseLong(timeout);

        appUrl = sysProps.getProperty("my.apps.test.appurl");

        phantomJsPath = sysProps.getProperty("my.apps.test.phantomjspath", "resource/phantomjs_windows");
    }

    @Before
    public void before() {
        driver = createWebDriver();
    }

    @After
    public void after() {
        driver.close();
    }

    public long getTimeout() {
        return timeout;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void waitUntil(Predicate<WebDriver> predicate, String msg) {
        WebDriverWait webDriverWait = new WebDriverWait(driver, timeout);
        webDriverWait.withMessage(msg);
        webDriverWait.until(predicate);
    }

    private WebDriver createWebDriver() {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                phantomJsPath);
        caps.setJavascriptEnabled(true);
        WebDriver driver = new PhantomJSDriver(caps);
        driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);

        return driver;
    }
}
