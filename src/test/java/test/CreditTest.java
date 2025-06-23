package test;

import com.codeborne.selenide.logevents.SelenideLogger;
import data.DataHelper;
import data.SQLHelper;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;
import page.CreditPage;
import page.MainPage;

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
    @DisplayName("Успешное оформление кредита одобренной картой")
    void shouldSuccessCreditWithApprovedCard() {
        DataHelper.CardInfo cardInfo = DataHelper.getValidCardInfo();

        executeTestWithLogging(cardInfo, () -> {
            CreditPage creditPage = new MainPage().goToCreditPage();
            creditPage.fillForm(cardInfo);
            creditPage.verifySuccessNotification();
            assertEquals("APPROVED", SQLHelper.getCreditStatus());
        });
    }

    @Test
    @DisplayName("Отказ в кредите отклоненной картой")
    void shouldFailCreditWithDeclinedCard() {
        DataHelper.CardInfo cardInfo = DataHelper.getDeclinedCardInfo();

        executeTestWithLogging(cardInfo, () -> {
            CreditPage creditPage = new MainPage().goToCreditPage();
            creditPage.fillForm(cardInfo);
            creditPage.verifyErrorNotification();
            assertEquals("DECLINED", SQLHelper.getCreditStatus());
        });
    }

    @Test
    @DisplayName("Успешный кредит с минимально допустимым сроком действия карты")
    void shouldSuccessCreditWithMinValidExpiryDate() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(0),
                DataHelper.getYear(0),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            CreditPage creditPage = new MainPage().goToCreditPage();
            creditPage.fillForm(cardInfo);
            creditPage.verifySuccessNotification();
            assertEquals("APPROVED", SQLHelper.getCreditStatus());
        });
    }

    @Test
    @DisplayName("Отказ в кредите с истекшим сроком действия карты (месяц)")
    void shouldFailCreditWithExpiredMonth() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(-1),
                DataHelper.getYear(0),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            CreditPage creditPage = new MainPage().goToCreditPage();
            creditPage.fillForm(cardInfo);
            creditPage.verifyCardExpired();
        });
    }

    @Test
    @DisplayName("Отказ в кредите с неверным форматом года (одна цифра)")
    void shouldFailCreditWithSingleDigitYear() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                "1",
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            CreditPage creditPage = new MainPage().goToCreditPage();
            creditPage.fillForm(cardInfo);
            creditPage.verifyInvalidFormat();
        });
    }

    @Test
    @DisplayName("Отказ в кредите с истекшим сроком действия карты (год)")
    void shouldFailCreditWithExpiredYear() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(0),
                DataHelper.getYear(-1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            CreditPage creditPage = new MainPage().goToCreditPage();
            creditPage.fillForm(cardInfo);
            creditPage.verifyCardExpired();
        });
    }

    @Test
    @DisplayName("Отказ в кредите с неверным форматом месяца (13)")
    void shouldFailCreditWithInvalidMonth() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getInvalidMonth(),
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            CreditPage creditPage = new MainPage().goToCreditPage();
            creditPage.fillForm(cardInfo);
            creditPage.verifyInvalidCardExpirationDate();
        });
    }

    @Test
    @DisplayName("Отказ в кредите с пустым номером карты")
    void shouldFailCreditWithEmptyCardNumber() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                "",
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            CreditPage creditPage = new MainPage().goToCreditPage();
            creditPage.fillForm(cardInfo);
            creditPage.verifyAnyErrorMessage("Неверный формат", "Поле обязательно для заполнения");
        });
    }

    @Test
    @DisplayName("Отказ в кредите с пустым полем месяца")
    void shouldFailCreditWithEmptyMonth() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                "",
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            CreditPage creditPage = new MainPage().goToCreditPage();
            creditPage.fillForm(cardInfo);
            creditPage.verifyAnyErrorMessage("Неверный формат", "Поле обязательно для заполнения");
        });
    }

    @Test
    @DisplayName("Отказ в кредите с пустым полем года")
    void shouldFailCreditWithEmptyYear() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                "",
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            CreditPage creditPage = new MainPage().goToCreditPage();
            creditPage.fillForm(cardInfo);
            creditPage.verifyAnyErrorMessage("Неверный формат", "Поле обязательно для заполнения");
        });
    }

    @Test
    @DisplayName("Отказ в кредите с пустым полем владельца")
    void shouldFailCreditWithEmptyHolder() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                "",
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            CreditPage creditPage = new MainPage().goToCreditPage();
            creditPage.fillForm(cardInfo);
            creditPage.verifyAnyErrorMessage("Поле обязательно для заполнения");
        });
    }

    @Test
    @DisplayName("Отказ в кредите с пустым полем CVC")
    void shouldFailCreditWithEmptyCVC() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                "");

        executeTestWithLogging(cardInfo, () -> {
            CreditPage creditPage = new MainPage().goToCreditPage();
            creditPage.fillForm(cardInfo);
            creditPage.verifyAnyErrorMessage("Неверный формат", "Поле обязательно для заполнения");
        });
    }

    @Test
    @DisplayName("Отказ в кредите с несуществующим номером карты")
    void shouldFailCreditWithNonExistentCard() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getInvalidCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            CreditPage creditPage = new MainPage().goToCreditPage();
            creditPage.fillForm(cardInfo);
            creditPage.verifyErrorNotification();
        });
    }

    @Test
    @DisplayName("Отказ в кредите с неполным номером карты")
    void shouldFailCreditWithShortCardNumber() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getShortCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            CreditPage creditPage = new MainPage().goToCreditPage();
            creditPage.fillForm(cardInfo);
            creditPage.verifyInvalidFormat();
        });
    }

    @Test
    @DisplayName("Успешный кредит при вводе кириллического имени владельца")
    void shouldAcceptCreditWithCyrillicHolder() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getCyrillicHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            CreditPage creditPage = new MainPage().goToCreditPage();
            creditPage.fillForm(cardInfo);
            creditPage.verifyHolderAcceptsInput();
            creditPage.verifySuccessNotification();
            assertEquals("APPROVED", SQLHelper.getCreditStatus());
        });
    }

    @Test
    @DisplayName("Отказ в кредите с недопустимыми символами в имени владельца")
    void shouldFailCreditWithInvalidHolder() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getInvalidHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            CreditPage creditPage = new MainPage().goToCreditPage();
            creditPage.fillForm(cardInfo);
            creditPage.verifyInvalidHolderFormat();
        });
    }

    @Test
    @DisplayName("Отказ в кредите с неполным CVC")
    void shouldFailCreditWithShortCVC() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getInvalidCVC());

        executeTestWithLogging(cardInfo, () -> {
            CreditPage creditPage = new MainPage().goToCreditPage();
            creditPage.fillForm(cardInfo);
            creditPage.verifyInvalidFormat();
        });
    }

    @Test
    @DisplayName("Отказ в кредите с нулевым месяцем")
    void shouldFailCreditWithZeroMonth() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                "00",
                DataHelper.getYear(1),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            CreditPage creditPage = new MainPage().goToCreditPage();
            creditPage.fillForm(cardInfo);
            creditPage.verifyInvalidCardExpirationDate();
        });
    }

    @Test
    @DisplayName("Отказ в кредите с годом больше текущего на 6 лет")
    void shouldFailCreditWithTooDistantYear() {
        DataHelper.CardInfo cardInfo = new DataHelper.CardInfo(
                DataHelper.getApprovedCardNumber(),
                DataHelper.getMonth(1),
                DataHelper.getYear(6),
                DataHelper.getLatinHolder(),
                DataHelper.getCVC());

        executeTestWithLogging(cardInfo, () -> {
            CreditPage creditPage = new MainPage().goToCreditPage();
            creditPage.fillForm(cardInfo);
            creditPage.verifyInvalidCardExpirationDate();
        });
    }
}
