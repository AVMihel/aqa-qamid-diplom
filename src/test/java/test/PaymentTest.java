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

    private void logTestData(DataHelper.CardInfo cardInfo) {
        System.out.println("\n=== TEST DATA USED ===");
        System.out.println("Card number: " + maskCardNumber(cardInfo.getNumber()));
        System.out.println("Month: " + cardInfo.getMonth());
        System.out.println("Year: " + cardInfo.getYear());
        System.out.println("Cardholder: " + cardInfo.getHolder());
        System.out.println("CVC: " + (cardInfo.getCvc().isEmpty() ? "<empty>" : "***"));
        System.out.println("=====================\n");
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "<empty>";
        }
        if (cardNumber.length() <= 8) {
            return cardNumber;
        }
        return cardNumber.substring(0, 4) + " **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    private void executeTestWithLogging(DataHelper.CardInfo cardInfo, Runnable testAction) {
        try {
            testAction.run();
        } catch (AssertionError | Exception e) {
            logTestData(cardInfo);
            throw e;
        }
    }

    @Test
    @DisplayName("Успешная оплата одобренной картой с именем на латинице")
    void shouldSuccessPaymentWithApprovedCard() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            PaymentPage paymentPage = new MainPage().goToPaymentPage();
            paymentPage.fillForm(cardInfo);
            paymentPage.verifySuccessNotification();
            assertEquals("APPROVED", SQLHelper.getPaymentStatus());
        });
    }

    @Test
    @DisplayName("Отказ в оплате отклоненной картой")
    void shouldFailPaymentWithDeclinedCard() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getDeclinedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            PaymentPage paymentPage = new MainPage().goToPaymentPage();
            paymentPage.fillForm(cardInfo);
            paymentPage.verifyErrorNotification();
            assertEquals("DECLINED", SQLHelper.getPaymentStatus());
        });
    }

    @Test
    @DisplayName("Успешная оплата с минимально допустимым сроком действия карты")
    void shouldSuccessPaymentWithMinValidExpiryDate() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(0),
                DataHelper.getYear(0),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            PaymentPage paymentPage = new MainPage().goToPaymentPage();
            paymentPage.fillForm(cardInfo);
            paymentPage.verifySuccessNotification();
            assertEquals("APPROVED", SQLHelper.getPaymentStatus());
        });
    }

    // Остальные тесты оформляем аналогично:

    @Test
    @DisplayName("Отказ в оплате с истекшим сроком действия карты (месяц)")
    void shouldFailPaymentWithExpiredMonth() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(-1),
                DataHelper.getYear(0),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            PaymentPage paymentPage = new MainPage().goToPaymentPage();
            paymentPage.fillForm(cardInfo);
            paymentPage.verifyCardExpired();
        });
    }

    @Test
    @DisplayName("Отказ в оплате с неверным форматом года (одна цифра)")
    void shouldFailPaymentWithSingleDigitYear() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                "1",
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            PaymentPage paymentPage = new MainPage().goToPaymentPage();
            paymentPage.fillForm(cardInfo);
            paymentPage.verifyInvalidFormat();
        });
    }

    @Test
    @DisplayName("Отказ в оплате с истекшим сроком действия карты (год)")
    void shouldFailPaymentWithExpiredYear() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(0),
                DataHelper.getYear(-1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            PaymentPage paymentPage = new MainPage().goToPaymentPage();
            paymentPage.fillForm(cardInfo);
            paymentPage.verifyCardExpired();
        });
    }

    @Test
    @DisplayName("Отказ в оплате с неверным форматом месяца (13)")
    void shouldFailPaymentWithInvalidMonth() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getInvalidMonth(),
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            PaymentPage paymentPage = new MainPage().goToPaymentPage();
            paymentPage.fillForm(cardInfo);
            paymentPage.verifyInvalidCardExpirationDate();
        });
    }

    @Test
    @DisplayName("Отказ в оплате с пустым номером карты")
    void shouldFailPaymentWithEmptyCardNumber() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                "",
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            PaymentPage paymentPage = new MainPage().goToPaymentPage();
            paymentPage.fillForm(cardInfo);
            paymentPage.verifyAnyErrorMessage("Неверный формат", "Поле обязательно для заполнения");
        });
    }

    @Test
    @DisplayName("Отказ в оплате с пустым полем месяца")
    void shouldFailPaymentWithEmptyMonth() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                "",
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            PaymentPage paymentPage = new MainPage().goToPaymentPage();
            paymentPage.fillForm(cardInfo);
            paymentPage.verifyAnyErrorMessage("Неверный формат", "Поле обязательно для заполнения");
        });
    }

    @Test
    @DisplayName("Отказ в оплате с пустым полем года")
    void shouldFailPaymentWithEmptyYear() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                "",
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            PaymentPage paymentPage = new MainPage().goToPaymentPage();
            paymentPage.fillForm(cardInfo);
            paymentPage.verifyAnyErrorMessage("Неверный формат", "Поле обязательно для заполнения");
        });
    }

    @Test
    @DisplayName("Отказ в оплате с пустым полем владельца")
    void shouldFailPaymentWithEmptyHolder() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                "",
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            PaymentPage paymentPage = new MainPage().goToPaymentPage();
            paymentPage.fillForm(cardInfo);
            paymentPage.verifyAnyErrorMessage("Поле обязательно для заполнения");
        });
    }

    @Test
    @DisplayName("Отказ в оплате с пустым полем CVC")
    void shouldFailPaymentWithEmptyCVC() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                "");

        executeTestWithLogging(cardInfo, () -> {
            PaymentPage paymentPage = new MainPage().goToPaymentPage();
            paymentPage.fillForm(cardInfo);
            paymentPage.verifyAnyErrorMessage("Неверный формат", "Поле обязательно для заполнения");
        });
    }

    @Test
    @DisplayName("Отказ в оплате с несуществующим номером карты")
    void shouldFailPaymentWithNonExistentCard() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getInvalidCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            PaymentPage paymentPage = new MainPage().goToPaymentPage();
            paymentPage.fillForm(cardInfo);
            paymentPage.verifyErrorNotification();
        });
    }

    @Test
    @DisplayName("Отказ в оплате с неполным номером карты")
    void shouldFailPaymentWithShortCardNumber() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getShortCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            PaymentPage paymentPage = new MainPage().goToPaymentPage();
            paymentPage.fillForm(cardInfo);
            paymentPage.verifyInvalidFormat();
        });
    }

    @Test
    @DisplayName("Успешная оплата при вводе кириллического имени владельца")
    void shouldAcceptPaymentWithCyrillicHolder() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getCyrillicHolder(), // Кириллическое имя
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            PaymentPage paymentPage = new MainPage().goToPaymentPage();
            paymentPage.fillForm(cardInfo);

            paymentPage.verifyHolderAcceptsInput();
            paymentPage.verifySuccessNotification();

            assertEquals("APPROVED", SQLHelper.getPaymentStatus());
        });
    }

    @Test
    @DisplayName("Оплата с минимальным кириллическим именем (2 символа)")
    void shouldAcceptMinimalCyrillicName() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                "Ян",
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            PaymentPage paymentPage = new MainPage().goToPaymentPage();
            paymentPage.fillForm(cardInfo);
            paymentPage.verifyHolderAcceptsInput();
            paymentPage.verifySuccessNotification();
        });
    }

    @Test
    @DisplayName("Отказ в оплате с недопустимыми символами в имени владельца")
    void shouldFailPaymentWithInvalidHolder() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getInvalidHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            PaymentPage paymentPage = new MainPage().goToPaymentPage();
            paymentPage.fillForm(cardInfo);
            paymentPage.verifyInvalidHolderFormat();
        });
    }

    @Test
    @DisplayName("Отказ в оплате с неполным CVC")
    void shouldFailPaymentWithShortCVC() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getInvalidCVC());

        executeTestWithLogging(cardInfo, () -> {
            PaymentPage paymentPage = new MainPage().goToPaymentPage();
            paymentPage.fillForm(cardInfo);
            paymentPage.verifyInvalidFormat();
        });
    }

    @Test
    @DisplayName("Отказ в оплате с нулевым месяцем")
    void shouldFailPaymentWithZeroMonth() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                "00",
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            PaymentPage paymentPage = new MainPage().goToPaymentPage();
            paymentPage.fillForm(cardInfo);
            paymentPage.verifyInvalidCardExpirationDate();
        });
    }

    @Test
    @DisplayName("Отказ в оплате с годом больше текущего на 6 лет")
    void shouldFailPaymentWithTooDistantYear() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(6),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            PaymentPage paymentPage = new MainPage().goToPaymentPage();
            paymentPage.fillForm(cardInfo);
            paymentPage.verifyInvalidCardExpirationDate();
        });
    }
}