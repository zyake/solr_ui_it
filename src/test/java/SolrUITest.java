import com.google.common.base.Predicate;
import org.junit.Test;
import javax.annotation.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class SolrUITest extends AbstractWebTest {

    @Test
    public void testInitialLoad() throws InterruptedException {
        getDriver().get(getAppUrl());

        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver webDriver) {
                WebElement fieldFacets = webDriver.findElement(By.className("field_facets"));

                List<WebElement> h3Elems = fieldFacets.findElements(By.tagName("h3"));
                boolean allCategoriesAreLoaded = h3Elems.size()  == 2;

                List<WebElement> liElems = fieldFacets.findElements(By.tagName("li"));
                boolean allIndexesAreLoaded = liElems.size() == 5;

                return allCategoriesAreLoaded && allIndexesAreLoaded;
            }
        }, "The initial contents should exist.");
    }

    @Test
    public void testSearchDocThatExistsMoreThanInitialItemCount() throws InterruptedException {
        getDriver().get(getAppUrl());

        WebElement searchBox = getDriver().findElement(By.cssSelector("#searchBox .input"));
        searchBox.sendKeys("BLogic");

        WebElement submit = getDriver().findElement(By.cssSelector("#searchBox .submit"));
        submit.click();

        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver webDriver) {
                WebElement resultSummary = webDriver.findElement(By.cssSelector("#searchBox .resultSummary"));
                boolean isExpectedSummary = resultSummary.getText().startsWith(
                        "検索が完了しました.(マッチしたもの=7, 取得したもの=5, 検索時間=");

                List<WebElement> docElems = webDriver.findElements(By.cssSelector(".content .document"));
                boolean isExpectedDocElemCount = docElems.size() == 5;

                return isExpectedSummary & isExpectedDocElemCount;
            }
        }, "The search result should be matched.");
    }

    @Test
    public void downloadDocument() throws IOException, NoSuchAlgorithmException {
        getDriver().get(getAppUrl());

        WebElement searchBox = getDriver().findElement(By.cssSelector("#searchBox .input"));

        // determine only one document.
        searchBox.sendKeys("id:*アーキテクチャ説明書.pdf AND id:*Rich版*");

        WebElement submit = getDriver().findElement(By.cssSelector("#searchBox .submit"));
        submit.click();

        ExpectedConditions.textToBePresentInElementValue(
                By.cssSelector("#searchBox .resultSummary"),
                "検索が完了しました.");

        WebElement docElem = getDriver().findElement(By.cssSelector(".content .document"));
        WebElement anchorElem = docElem.findElement(By.tagName("a"));
        String href = anchorElem.getAttribute("href");

        // compare file digests.
        URL url = new URL(href);
        byte[]  loadedFileDigest = getDigest(url.openStream());
        byte[]  fileDigest = getDigest(
                   new FileInputStream("resource/expectedFile.pdf"));

        assertArrayEquals(fileDigest, loadedFileDigest);
    }

    private byte[] getDigest(InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        DigestInputStream digestInputStream = new DigestInputStream(inputStream, md5);
        try {
            while ( digestInputStream.read() != -1 ) {
            }
        } finally {
            digestInputStream.close();
        }

        return md5.digest();
    }
}
