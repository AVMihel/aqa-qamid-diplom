package data;

import com.github.javafaker.Faker;
import lombok.Value;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;

public class DataHelper {
    private DataHelper() {
    }

    public static Faker faker = new Faker(new Locale("en"));

    @Value
    public static class CardInfo {
        String number;
        String month;
        String year;
        String holder;
        String cvc;
    }

    public static String getApprovedCardNumber() {
        return "4444 4444 4444 4441";
    }

    public static String getDeclinedCardNumber() {
        return "4444 4444 4444 4442";
    }

    public static String getInvalidCardNumber() {
        return "4444 4444 4444 4443";
    }

    public static String getShortCardNumber() {
        return "4444 4444 4444 444";
    }

    public static String getMonth(int shift) {
        return LocalDate.now().plusMonths(shift).format(DateTimeFormatter.ofPattern("MM"));
    }

    public static String getInvalidMonth() {
        return "13";
    }

    public static String getYear(int shift) {
        return LocalDate.now().plusYears(shift).format(DateTimeFormatter.ofPattern("yy"));
    }

    public static String getInvalidYear() {
        return "00";
    }

    public static String getHolder() {
        return faker.name().fullName();
    }

    public static String getInvalidHolder() {
        return "Иван Иванов";
    }

    public static String getCVC() {
        return String.format("%03d", new Random().nextInt(1000));
    }

    public static String getInvalidCVC() {
        return String.format("%02d", new Random().nextInt(100));
    }

    public static CardInfo getValidCardInfo() {
        return new CardInfo(getApprovedCardNumber(), getMonth(1), getYear(1), getHolder(), getCVC());
    }

    public static CardInfo getDeclinedCardInfo() {
        return new CardInfo(getDeclinedCardNumber(), getMonth(1), getYear(1), getHolder(), getCVC());
    }
}
