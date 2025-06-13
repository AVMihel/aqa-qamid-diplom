package test;

import com.codeborne.selenide.logevents.SelenideLogger;
import data.DataHelper;
import data.SQLHelper;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;
import page.CreditPage;
import page.MainPage;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreditTest {
    @BeforeAll
    static void setUpAll() {
        SelenideLogger.addListener("allure", new AllureSelenide());
    }

    @AfterAll
    static void tearDownAll() {
        SelenideLogger.removeListener("allure");
        SQLHelper.cleanDatabase();
    }

    @BeforeEach
    void setUp() {
        open("http://localhost:8080");
    }

    @Test
    @DisplayName("Успешное оформление кредита одобренной картой")
    void shouldSuccessCreditWithApprovedCard() {
        MainPage mainPage = new MainPage();
        CreditPage creditPage = mainPage.goToCreditPage();
        creditPage.fillForm(DataHelper.getValidCardInfo());
        creditPage.verifySuccessNotification();
        assertEquals("APPROVED", SQLHelper.getCreditStatus());
    }

    @Test
    @DisplayName("Отказ в кредите отклоненной картой")
    void shouldFailCreditWithDeclinedCard() {
        MainPage mainPage = new MainPage();
        CreditPage creditPage = mainPage.goToCreditPage();
        creditPage.fillForm(DataHelper.getDeclinedCardInfo());
        creditPage.getSuccessNotification().shouldNotBe(visible, Duration.ofSeconds(10));
        assertEquals("DECLINED", SQLHelper.getCreditStatus());
        creditPage.getAnyNotification().shouldBe(visible, Duration.ofSeconds(10));
    }
}
