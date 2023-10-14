import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    static final int NUMBER_OF_TEXTS = 10_000;
    static final int NUMBER_OF_LETTERS_IN_LINE = 100_000;
    static final int NUMBER_IN_QUEUE = 100;
    static final String TEXT = "Максимальное количество: - символов: ";

    static ArrayBlockingQueue<String> a = new ArrayBlockingQueue<>(NUMBER_IN_QUEUE); // Создаем отдельныю защищннную Очередь для каждого потока. Длинной 100
    static ArrayBlockingQueue<String> b = new ArrayBlockingQueue<>(NUMBER_IN_QUEUE);
    static ArrayBlockingQueue<String> c = new ArrayBlockingQueue<>(NUMBER_IN_QUEUE);
    static ConcurrentHashMap<String, Integer> mapOfMaxInt = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, String> mapOfStr = new ConcurrentHashMap<>();

    static {
        mapOfMaxInt.put("a", 0);
        mapOfMaxInt.put("b", 0);
        mapOfMaxInt.put("c", 0);
        mapOfStr.put("a", "");
        mapOfStr.put("b", "");
        mapOfStr.put("c", "");
    }

    public static void main(String[] args) {

        new Thread(() -> { // Запускаем отдельный поток который будет генерировать текст 10_000 итераций по 100_000 строк
            for (int i = 0; i < NUMBER_OF_TEXTS; i++) {
                String str = generateText("abc", NUMBER_OF_LETTERS_IN_LINE);// Объявляем новую стринговую переменныую str в которую копируем сгенерированный текст
                try {
                    a.put(str); // Сгененрированный текст трижды копируем в каждую Очередь
                    b.put(str);
                    c.put(str);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }).start();

        new Thread(() -> {// Запускаем три потока для подсчета трех разных букв
            String letter = "a"; // Задаем переменную и указываем букву которую будет искать этот поток
            for (int i = 0; i < NUMBER_OF_TEXTS; i++) {
                searchMaximumText(letter, a);
            }
            printResult(letter);
        }, " А").start();

        new Thread(() -> {
            String letter = "b";
            for (int i = 0; i < NUMBER_OF_TEXTS; i++) {
                searchMaximumText(letter, b);
            }
            printResult(letter);
        }, " В").start();

        new Thread(() -> {
            String letter = "c";
            for (int i = 0; i < NUMBER_OF_TEXTS; i++) {
                searchMaximumText(letter, c);
            }
            printResult(letter);
        }, " С").start();

    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    public static int countLetters(String letter, String text) {
        String str = text.replaceAll(String.format("[^%s]", letter), "");
        return str.length();
    }

    public static void searchMaximumText(String letter, ArrayBlockingQueue<String> queue) {
        String str;
        int sumLatters;
        int max = mapOfMaxInt.get(letter);
        try {
            str = queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        sumLatters = countLetters(letter, str);
        if (max < sumLatters) {
            mapOfMaxInt.put(letter, sumLatters);
            mapOfStr.put(letter, str);
        }
    }

    public static void printResult(String letter) {
        System.out.printf(TEXT, Thread.currentThread().getName(), letter, mapOfMaxInt.get(letter), mapOfStr.get(letter));
    }
}
