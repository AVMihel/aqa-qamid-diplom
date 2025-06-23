package page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import data.DataHelper;

import java.time.Duration;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class CreditPage {
    private final SelenideElement heading = $$(".heading").find(exactText("Кредит по данным карты"));
    private final SelenideElement cardNumberField = $("input[placeholder='0000 0000 0000 0000']");
    private final SelenideElement monthField = $("input[placeholder='08']");
    private final SelenideElement yearField = $("input[placeholder='22']");
    private final SelenideElement holderField = $$(".input__control").get(3);
    private final SelenideElement cvcField = $("input[placeholder='999']");
    private final SelenideElement continueButton = $$("button").find(exactText("Продолжить"));

    private final SelenideElement successNotification = $(".notification_status_ok");
    private final SelenideElement errorNotification = $(".notification_status_error");
    private final ElementsCollection errorMessages = $$(".input__sub").filter(visible);
    private final SelenideElement firstErrorMessage = $(".input__sub");

    public CreditPage() {
        heading.shouldBe(visible);
    }

    public void fillForm(DataHelper.CardInfo cardInfo) {
        cardNumberField.setValue(cardInfo.getNumber());
        monthField.setValue(cardInfo.getMonth());
        yearField.setValue(cardInfo.getYear());
        holderField.setValue(cardInfo.getHolder());
        cvcField.setValue(cardInfo.getCvc());
        continueButton.click();
    }

    public void verifySuccessNotification() {
        successNotification.shouldBe(visible, Duration.ofSeconds(15));
        successNotification.$(".notification__content")
                .shouldHave(exactText("Операция одобрена Банком."));
    }

    public void verifyErrorNotification() {
        errorNotification.shouldBe(visible, Duration.ofSeconds(15));
        errorNotification.$(".notification__content")
                .shouldHave(exactText("Ошибка! Банк отказал в проведении операции."));
    }

    public void verifyAnyErrorMessage(String... expectedTexts) {
        firstErrorMessage.shouldBe(visible, Duration.ofSeconds(5));
        boolean found = false;
        for (String text : expectedTexts) {
            if (firstErrorMessage.getText().contains(text)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new AssertionError(String.format(
                    "Ожидался один из текстов: %s, но найдено: '%s'",
                    String.join(", ", expectedTexts),
                    firstErrorMessage.getText()
            ));
        }
    }

    public void verifyInvalidFormat() {
        verifyAnyErrorMessage("Неверный формат");
    }

    public void verifyInvalidCardExpirationDate() {
        verifyAnyErrorMessage(
                "Истёк срок действия карты",
                "Неверно указан срок действия карты"
        );
    }

    public void verifyCardExpired() {
        verifyAnyErrorMessage(
                "Истёк срок действия карты",
                "Неверно указан срок действия карты"
        );
    }

    public void verifyHolderAcceptsInput() {
        errorMessages.shouldHave(size(0));
    }

    public void verifyInvalidHolderFormat() {
        verifyAnyErrorMessage("Неверный формат");
    }
}