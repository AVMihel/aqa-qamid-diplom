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
        return "1111 2222 3333 4444"; // APPROVED карта
    }

    public static String getDeclinedCardNumber() {
        return "5555 6666 7777 8888"; // DECLINED карта
    }

    public static String getInvalidCardNumber() {
        return "1111 2222 3333 4445"; // Несуществующая карта
    }

    public static String getShortCardNumber() {
        return "1111 2222 3333 444"; // Короткий номер (15 цифр)
    }

    public static String getMonth(int shift) {
        return LocalDate.now().plusMonths(shift).format(DateTimeFormatter.ofPattern("MM"));
    }

    public static String getInvalidMonth() {
        return "13"; // Неверный месяц
    }

    public static String getYear(int shift) {
        return LocalDate.now().plusYears(shift).format(DateTimeFormatter.ofPattern("yy"));
    }

    public static String getInvalidYear() {
        return LocalDate.now().minusYears(1).format(DateTimeFormatter.ofPattern("yy")); // Прошлый год
    }

    public static String getHolder() {
        return faker.name().fullName().toUpperCase(); // Имя в верхнем регистре
    }

    public static String getInvalidHolder() {
        return "Иван Иванов"; // Кириллица
    }

    public static String getCVC() {
        return String.format("%03d", new Random().nextInt(1000)); // 3 цифры
    }

    public static String getInvalidCVC() {
        return String.format("%02d", new Random().nextInt(100)); // 2 цифры
    }

    public static CardInfo getValidCardInfo() {
        return new CardInfo(getApprovedCardNumber(), getMonth(1), getYear(1), getHolder(), getCVC());
    }

    public static CardInfo getDeclinedCardInfo() {
        return new CardInfo(getDeclinedCardNumber(), getMonth(1), getYear(1), getHolder(), getCVC());
    }
}
