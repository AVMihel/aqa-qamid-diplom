# Отчёт о тестировании платежной системы

## 1. Общая информация

### 1.1. Технические детали
**Стек технологий:**
- Java 11 + JUnit 5
- Selenide для UI-тестирования
- Docker для развертывания
- MySQL 8.0 для проверки статусов
- Allure Report для визуализации

**Окружение:**
- Windows 10 Pro (сборка 19045.5737)
- IntelliJ IDEA 2024.3.2.1 (Community Edition)
- Chrome 125 (headless режим)
- Локальный сервер (localhost:8080)

### 1.2. Объем тестирования
- **Всего тестов:** 39
- **Время выполнения:** 2 мин 2 сек
- **Тест-сьюты:**
   - `PaymentTest` (20 тестов)
   - `CreditTest` (19 тестов)

## 2. Результаты тестирования

### 2.1. Статистика выполнения

| Тип тестирования       | Всего | Успешно | Провалено | Успешность |
|------------------------|-------|---------|-----------|------------|
| Оплата по карте        | 20    | 17      | 3         | 85%        |
| Кредитные операции     | 19    | 16      | 3         | 84.2%      |
| **Итого**             | **39**| **33**  | **6**     | **84.6%**  |

### 2.2. Детализация ошибок

#### 2.2.1. Оплата по карте (PaymentTest)
1. **DECLINED карта** (Критический)
   - Тест: `shouldFailPaymentWithDeclinedCard`
   - Ожидалось: статус `DECLINED`
   - Фактически: статус `APPROVED`  

2. **Невалидный владелец** (Высокий)
   - Тест: `shouldFailWithInvalidHolder`
   - Ожидалось: сообщение об ошибке
   - Ошибка: `NoSuchElementException` (не найден элемент `.input__sub`) 

3. **Нулевой месяц** (Высокий)
   - Тест: `shouldFailWithZeroMonth`
   - Ошибка: аналогично предыдущей   

#### 2.2.2. Кредитные операции (CreditTest)
1. **DECLINED карта** (Критический)
   - Тест: `shouldFailCreditWithDeclinedCard`
   - Ожидалось: уведомление об ошибке
   - Фактически: уведомление скрыто (`displayed:false`)   

2. **Невалидный владелец** (Высокий)
   - Тест: `shouldFailCreditWithInvalidHolder`
   - Ошибка: `NoSuchElementException`   

3. **Нулевой месяц** (Высокий)
   - Тест: `shouldFailCreditWithZeroMonth`
   - Ошибка: аналогично предыдущей   

## 3. Анализ проблем

### 3.1. Основные паттерны ошибок
1. **Логика обработки карт:**
   - Система некорректно обрабатывает DECLINED-карты
   - Статус APPROVED вместо DECLINED

2. **Валидация полей:**
   - Не отображаются сообщения об ошибках
   - Проблемы с элементами `.input__sub` и `.notification_status_error`

3. **Технические проблемы:**
   - Элементы интерфейса не найдены (`NoSuchElementException`)
   - Уведомления остаются скрытыми

## 4. Рекомендации

### 4.1. Критические исправления
1. Исправить обработку отклонённых карт - добавить проверку статуса в БД перед одобрением
2. Реализовать валидацию поля "Владелец" - запрет спецсимволов и цифр
3. Исправить отображение сообщений об ошибках

### 4.2. Улучшения
1. Добавить - проверку достаточности средств        
2. Улучшить обработку ошибок:
    - чёткие сообщения для пользователя
    - подсказки по формату полей

## 5. Заключение
**Общий результат: 84.6% успешных тестов**

Ключевые проблемы:

1. Некорректная обработка DECLINED-карт (2 теста)
2. Проблемы валидации (4 теста)

**Приложения**:
1. Allure-отчёт
   ![Screenshot_2](https://github.com/user-attachments/assets/53fd4ac0-8c65-4f55-aa92-d529b80c9fc8)

