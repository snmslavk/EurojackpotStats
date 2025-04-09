package euro.jackpot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatsCalculation {
    public static final String STATS_DATA_FOLDER = "StatsData";
    public static final String STATS_API_ENDPOINT = "https://www.sazka.cz/api/draw-info/draws/universal/eurojackpot/";
    private final Map<Integer, Double> usualDraw = new HashMap<>();
    private final Map<Integer, Double> additionalTwo = new HashMap<>();
    private final Path statsData = Paths.get(STATS_DATA_FOLDER);

    public void calculate() {
        fillChancesMap();
        printNumberOfDraws();

        Map<Integer, Double> sortedUsualDraw = usualDraw.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        System.out.println("<br />");
        System.out.println("Chances the number will NOT be in the next usual draw:<br />");
        sortedUsualDraw.forEach((key, value) -> System.out.println("number:" + key + " chance:" + value * 100 + "%  "));

        List<Double> topUsualChances = new ArrayList<>(sortedUsualDraw.values());
        List<Integer> topUsualNumbers = new ArrayList<>(sortedUsualDraw.keySet());

        Map<Integer, Double> sortedAdditionalTwo = additionalTwo.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        System.out.println("<br />");
        System.out.println("Chances the number will NOT be in the next additional draw:  ");
        sortedAdditionalTwo.forEach((key, value) -> System.out.println("number:" + key + " chance:" + value * 100 + "%  "));
        System.out.println();
        System.out.println("Chances to win and amount of money:  ");

        List<Double> topExtraChances = new ArrayList<>(sortedAdditionalTwo.values());
        List<Integer> topExtraNumbers = new ArrayList<>(sortedAdditionalTwo.keySet());

        System.out.println("Your lucky numbers with the best chances:");
        System.out.println(topUsualNumbers.get(0) + " " + topUsualNumbers.get(1) + " " + topUsualNumbers.get(2) + " "
                + topUsualNumbers.get(3) + " " + topUsualNumbers.get(4) + "  ");
        System.out.println(topExtraNumbers.get(0) + " " + topExtraNumbers.get(1));

        calculatePrintChancesPrizes(topUsualChances, topExtraChances);
    }

    private void calculatePrintChancesPrizes(List<Double> topUsualChances, List<Double> topExtraChances) {
        double chances5_2 = (1 - topUsualChances.get(0))
                * (1 - topUsualChances.get(1))
                * (1 - topUsualChances.get(2))
                * (1 - topUsualChances.get(3))
                * (1 - topUsualChances.get(4))
                * (1 - topExtraChances.get(0))
                * (1 - topExtraChances.get(1));
        System.out.println("5+2 chances:" + String.format("%.8f", chances5_2 * 100) + "% prize:1 573 179 300 Kč  ");

        double chances5_1 = (1 - topUsualChances.get(0))
                * (1 - topUsualChances.get(1))
                * (1 - topUsualChances.get(2))
                * (1 - topUsualChances.get(3))
                * (1 - topUsualChances.get(4))
                * (1 - topExtraChances.get(0));
        System.out.println("5+1 chances:" + String.format("%.8f", chances5_1 * 100) + "% prize:15 032 602 Kč  ");

        double chances5_0 = (1 - topUsualChances.get(0))
                * (1 - topUsualChances.get(1))
                * (1 - topUsualChances.get(2))
                * (1 - topUsualChances.get(3))
                * (1 - topUsualChances.get(4));
        System.out.println("5+0 chances:" + String.format("%.8f", chances5_0 * 100) + "% prize:3 767 862 Kč  ");

        double chances4_2 = (1 - topUsualChances.get(0))
                * (1 - topUsualChances.get(1))
                * (1 - topUsualChances.get(2))
                * (1 - topUsualChances.get(3))
                * (1 - topExtraChances.get(0))
                * (1 - topExtraChances.get(1));
        System.out.println("4+2 chances:" + String.format("%.8f", chances4_2 * 100) + "% prize:124 301 Kč  ");

        double chances4_1 = (1 - topUsualChances.get(0))
                * (1 - topUsualChances.get(1))
                * (1 - topUsualChances.get(2))
                * (1 - topUsualChances.get(3))
                * (1 - topExtraChances.get(0));
        System.out.println("4+1 chances:" + String.format("%.8f", chances4_1 * 100) + "% prize:7 769 Kč  ");
    }

    private void printNumberOfDraws() {
        try (Stream<Path> files = Files.list(statsData)) {
            System.out.println("The number of draws:" + files.count() + "<br />");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillChancesMap() {
        for (int i = 1; i < 51; i++) {
            usualDraw.put(i, 1.0);
        }
        for (int i = 1; i < 13; i++) {
            additionalTwo.put(i, 1.0);
        }

        try (Stream<Path> paths = Files.walk(statsData)) {
            paths.filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            String content = Files.readString(file);
                            content = content.substring(content.indexOf("\"draws\":") + 8, content.indexOf(",\"addonDraw\":"));
                            content = content.replace("[", "").replace("]", "");
                            List<Integer> numbers = Arrays.stream(content.split(",")).map(Integer::valueOf).toList();
                            usualDraw.put(numbers.get(0), 0.98);
                            usualDraw.put(numbers.get(1), 0.98);
                            usualDraw.put(numbers.get(2), 0.98);
                            usualDraw.put(numbers.get(3), 0.98);
                            usualDraw.put(numbers.get(4), 0.98);

                            for (int i = 1; i < 51; i++) {
                                if (i != numbers.get(0)
                                        && i != numbers.get(1)
                                        && i != numbers.get(2)
                                        && i != numbers.get(3)
                                        && i != numbers.get(4)) {
                                    usualDraw.put(i, usualDraw.get(i) * 0.98);
                                }
                            }

                            additionalTwo.put(numbers.get(5), 0.917);
                            additionalTwo.put(numbers.get(6), 0.917);

                            for (int i = 1; i < 13; i++) {
                                if (i != numbers.get(5)
                                        && i != numbers.get(6)) {
                                    additionalTwo.put(i, additionalTwo.get(i) * 0.917);
                                }
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void downloadStats() {
        int currentYear = Year.now().getValue();
        ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        int currentWeek = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        for (int i = 2015; i <= currentYear; i++) {
            String requestParams = i + "";
            for (int j = 1; j < 53; j++) {
                if (i == currentYear && j > currentWeek) {
                    break;
                }
                String week = j < 10 ? "0" + j : j + "";
                String tuesday = requestParams + week + 2;
                String friday = requestParams + week + 5;
                if (i > 2021) {
                    getResponse(tuesday);
                }
                getResponse(friday);
            }
        }
    }

    private void getResponse(String requestParams) {
        try {
            Path savedResponsePath = Path.of(STATS_DATA_FOLDER + "/" + requestParams + ".json");
            if (Files.exists(savedResponsePath)) {
                return;
            }
            URL url = new URL(STATS_API_ENDPOINT + requestParams);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                Files.writeString(savedResponsePath, content);
                System.out.println(content);
            }
            con.disconnect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
