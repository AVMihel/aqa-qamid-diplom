package page;

import com.codeborne.selenide.SelenideElement;
import data.DataHelper;
import java.time.Duration;

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
    private final SelenideElement anyNotification = $(".notification");
    private final SelenideElement successNotificationContent = $(".notification_status_ok .notification__content");
    private final SelenideElement errorNotificationContent = $(".notification_status_error .notification__content");
    private final SelenideElement invalidFormatError = $(".input__sub");

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
        successNotificationContent.shouldHave(text("Операция одобрена Банком."));
    }

    public SelenideElement getSuccessNotification() {
        return successNotification;
    }

    public SelenideElement getErrorNotification() {
        return errorNotification;
    }

    public SelenideElement getAnyNotification() {
        return anyNotification;
    }
}