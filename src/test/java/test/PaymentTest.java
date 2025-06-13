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
    @DisplayName("Успешная оплата одобренной картой с именем на латинице")
    void shouldSuccessPaymentWithApprovedCard() {
        PaymentPage paymentPage = new MainPage().goToPaymentPage();
        paymentPage.fillForm(new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC()));

        paymentPage.verifySuccessNotification();
        assertEquals("APPROVED", SQLHelper.getPaymentStatus());
    }

    @Test
    @DisplayName("Отказ в оплате отклоненной картой")
    void shouldFailPaymentWithDeclinedCard() {
        PaymentPage paymentPage = new MainPage().goToPaymentPage();
        paymentPage.fillForm(new DataHelper.CardInfo(
                DataHelper.getDeclinedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC()));

        paymentPage.verifyErrorNotification();
        assertEquals("DECLINED", SQLHelper.getPaymentStatus());
    }

    @Test
    @DisplayName("Успешная оплата с кириллическим именем владельца")
    void shouldAcceptCyrillicHolder() {
        PaymentPage paymentPage = new MainPage().goToPaymentPage();
        paymentPage.fillForm(new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getCyrillicHolder(),
                DataHelper.getCVC()));

        paymentPage.verifyHolderAcceptsInput();
    }

    @Test
    @DisplayName("Отправка формы с недопустимыми символами в имени")
    void shouldShowErrorWithInvalidHolder() {
        PaymentPage paymentPage = new MainPage().goToPaymentPage();
        paymentPage.fillForm(new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getInvalidHolder(),
                DataHelper.getCVC()));

        paymentPage.verifyInvalidHolderFormat();
    }

    @Test
    @DisplayName("Отправка формы с несуществующим номером карты")
    void shouldFailWithInvalidCardNumber() {
        PaymentPage paymentPage = new MainPage().goToPaymentPage();
        paymentPage.fillForm(new DataHelper.CardInfo(
                DataHelper.getInvalidCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC()));

        paymentPage.verifyErrorNotification();
    }

    @Test
    @DisplayName("Отправка формы с коротким номером карты")
    void shouldShowErrorWithShortCardNumber() {
        PaymentPage paymentPage = new MainPage().goToPaymentPage();
        paymentPage.fillForm(new DataHelper.CardInfo(
                DataHelper.getShortCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC()));

        paymentPage.verifyInvalidFormat();
    }

    @Test
    @DisplayName("Отправка формы с неверным месяцем")
    void shouldShowErrorWithInvalidMonth() {
        PaymentPage paymentPage = new MainPage().goToPaymentPage();
        paymentPage.fillForm(new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getInvalidMonth(),
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC()));

        paymentPage.verifyInvalidCardExpirationDate();
    }

    @Test
    @DisplayName("Отправка формы с истекшим сроком действия карты")
    void shouldShowErrorWithExpiredCard() {
        PaymentPage paymentPage = new MainPage().goToPaymentPage();
        paymentPage.fillForm(new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(-1),
                DataHelper.getYear(0),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC()));

        paymentPage.verifyCardExpired();
    }

    @Test
    @DisplayName("Отправка формы с неверным годом")
    void shouldShowErrorWithInvalidYear() {
        PaymentPage paymentPage = new MainPage().goToPaymentPage();
        paymentPage.fillForm(new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getInvalidYear(),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC()));

        paymentPage.verifyInvalidCardExpirationDate();
    }

    @Test
    @DisplayName("Отправка формы с коротким CVC")
    void shouldShowErrorWithShortCVC() {
        PaymentPage paymentPage = new MainPage().goToPaymentPage();
        paymentPage.fillForm(new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getInvalidCVC()));

        paymentPage.verifyInvalidFormat();
    }

    @Test
    @DisplayName("Отправка пустой формы")
    void shouldShowErrorsWithEmptyForm() {
        PaymentPage paymentPage = new MainPage().goToPaymentPage();
        paymentPage.fillForm(new DataHelper.CardInfo("", "", "", "", ""));

        // Проверяем оба возможных сообщения
        paymentPage.verifyAnyErrorMessage(
                "Неверный формат",
                "Поле обязательно для заполнения"
        );
    }
}