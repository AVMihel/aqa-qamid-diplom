package page;

import com.codeborne.selenide.SelenideElement;
import data.DataHelper;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class CreditPage {
    private SelenideElement heading = $$(".heading").find(exactText("Кредит по данным карты"));
    private SelenideElement cardNumberField = $("input[placeholder='0000 0000 0000 0000']");
    private SelenideElement monthField = $("input[placeholder='08']");
    private SelenideElement yearField = $("input[placeholder='22']");
    private SelenideElement holderField = $$(".input__control").get(3);
    private SelenideElement cvcField = $("input[placeholder='999']");
    private SelenideElement continueButton = $$("button").find(exactText("Продолжить"));
    private SelenideElement successNotification = $(".notification_status_ok");
    private SelenideElement errorNotification = $(".notification_status_error");
    private SelenideElement invalidFormatError = $(".input__sub");

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
        successNotification.waitUntil(visible, 15000);
    }

    public void verifyErrorNotification() {
        errorNotification.waitUntil(visible, 15000);
    }

    public void verifyInvalidFormat() {
        invalidFormatError.shouldBe(visible).shouldHave(text("Неверный формат"));
    }

    public void verifyRequiredField() {
        invalidFormatError.shouldBe(visible).shouldHave(text("Поле обязательно для заполнения"));
    }

    public void verifyInvalidCardExpirationDate() {
        invalidFormatError.shouldBe(visible).shouldHave(text("Неверно указан срок действия карты"));
    }

    public void verifyCardExpired() {
        invalidFormatError.shouldBe(visible).shouldHave(text("Истёк срок действия карты"));
    }
}
