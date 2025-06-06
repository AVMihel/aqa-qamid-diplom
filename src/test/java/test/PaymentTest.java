package test;

import com.codeborne.selenide.logevents.SelenideLogger;
import data.DataHelper;
import data.SQLHelper;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;
import page.MainPage;
import page.PaymentPage;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaymentTest {
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
    @DisplayName("Успешная оплата одобренной картой")
    void shouldSuccessPaymentWithApprovedCard() {
        MainPage mainPage = new MainPage();
        PaymentPage paymentPage = mainPage.goToPaymentPage();
        paymentPage.fillForm(DataHelper.getValidCardInfo());
        paymentPage.verifySuccessNotification();

        assertEquals("APPROVED", SQLHelper.getPaymentStatus());
    }

    @Test
    @DisplayName("Отказ в оплате отклоненной картой")
    void shouldFailPaymentWithDeclinedCard() {
        MainPage mainPage = new MainPage();
        PaymentPage paymentPage = mainPage.goToPaymentPage();
        paymentPage.fillForm(DataHelper.getDeclinedCardInfo());
        paymentPage.verifyErrorNotification();

        assertEquals("DECLINED", SQLHelper.getPaymentStatus());
    }

    @Test
    @DisplayName("Отправка формы с несуществующим номером карты")
    void shouldFailWithInvalidCardNumber() {
        MainPage mainPage = new MainPage();
        PaymentPage paymentPage = mainPage.goToPaymentPage();
        paymentPage.fillForm(new DataHelper.CardInfo(
                DataHelper.getInvalidCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getHolder(),
                DataHelper.getCVC()));
        paymentPage.verifyErrorNotification();
    }

    @Test
    @DisplayName("Отправка формы с коротким номером карты")
    void shouldShowErrorWithShortCardNumber() {
        MainPage mainPage = new MainPage();
        PaymentPage paymentPage = mainPage.goToPaymentPage();
        paymentPage.fillForm(new DataHelper.CardInfo(
                DataHelper.getShortCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getHolder(),
                DataHelper.getCVC()));
        paymentPage.verifyInvalidFormat();
    }

    @Test
    @DisplayName("Отправка формы с неверным месяцем")
    void shouldShowErrorWithInvalidMonth() {
        MainPage mainPage = new MainPage();
        PaymentPage paymentPage = mainPage.goToPaymentPage();
        paymentPage.fillForm(new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getInvalidMonth(),
                DataHelper.getYear(1),
                DataHelper.getHolder(),
                DataHelper.getCVC()));
        paymentPage.verifyInvalidCardExpirationDate();
    }

    @Test
    @DisplayName("Отправка формы с истекшим сроком действия карты")
    void shouldShowErrorWithExpiredCard() {
        MainPage mainPage = new MainPage();
        PaymentPage paymentPage = mainPage.goToPaymentPage();
        paymentPage.fillForm(new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(-1),
                DataHelper.getYear(0),
                DataHelper.getHolder(),
                DataHelper.getCVC()));
        paymentPage.verifyCardExpired();
    }

    @Test
    @DisplayName("Отправка формы с неверным годом")
    void shouldShowErrorWithInvalidYear() {
        MainPage mainPage = new MainPage();
        PaymentPage paymentPage = mainPage.goToPaymentPage();
        paymentPage.fillForm(new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getInvalidYear(),
                DataHelper.getHolder(),
                DataHelper.getCVC()));
        paymentPage.verifyInvalidCardExpirationDate();
    }

    @Test
    @DisplayName("Отправка формы с кириллическим именем владельца")
    void shouldShowErrorWithCyrillicHolder() {
        MainPage mainPage = new MainPage();
        PaymentPage paymentPage = mainPage.goToPaymentPage();
        paymentPage.fillForm(new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getInvalidHolder(),
                DataHelper.getCVC()));
        paymentPage.verifyInvalidFormat();
    }

    @Test
    @DisplayName("Отправка формы с коротким CVC")
    void shouldShowErrorWithShortCVC() {
        MainPage mainPage = new MainPage();
        PaymentPage paymentPage = mainPage.goToPaymentPage();
        paymentPage.fillForm(new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getHolder(),
                DataHelper.getInvalidCVC()));
        paymentPage.verifyInvalidFormat();
    }

    @Test
    @DisplayName("Отправка пустой формы")
    void shouldShowErrorsWithEmptyForm() {
        MainPage mainPage = new MainPage();
        PaymentPage paymentPage = mainPage.goToPaymentPage();
        paymentPage.fillForm(new DataHelper.CardInfo(
                "",
                "",
                "",
                "",
                ""));
        paymentPage.verifyRequiredField();
    }
}
