package data;

import com.github.javafaker.Faker;
import lombok.Value;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;

public class DataHelper {
    private static final Faker fakerEn = new Faker(new Locale("en"));
    private static final Faker fakerRu = new Faker(new Locale("ru"));

    @Value
    public static class CardInfo {
        String number;
        String month;
        String year;
        String holder;
        String cvc;
    }

    public static String getApprovedCardNumber() {
        return "1111 2222 3333 4444";
    }

    public static String getDeclinedCardNumber() {
        return "5555 6666 7777 8888";
    }

    public static String getInvalidCardNumber() {
        return "1111 2222 3333 4445";
    }

    public static String getShortCardNumber() {
        return "1111 2222 3333 444";
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
        return LocalDate.now().minusYears(1).format(DateTimeFormatter.ofPattern("yy"));
    }

    public static String getLatinHolder() {
        return fakerEn.name().fullName().toUpperCase();
    }

    public static String getCyrillicHolder() {
        return fakerRu.name().fullName();
    }

    public static String getInvalidHolder() {
        return "123!@#";
    }

    public static String getCVC() {
        return String.format("%03d", new Random().nextInt(1000));
    }

    public static String getInvalidCVC() {
        return String.format("%02d", new Random().nextInt(100));
    }

    public static CardInfo getValidCardInfo() {
        return new CardInfo(getApprovedCardNumber(), getMonth(1), getYear(1), getLatinHolder(), getCVC());
    }

    public static CardInfo getDeclinedCardInfo() {
        return new CardInfo(getDeclinedCardNumber(), getMonth(1), getYear(1), getLatinHolder(), getCVC());
    }


}
