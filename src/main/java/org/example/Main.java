package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        StatsCalculation statsCalculation = new StatsCalculation();
        statsCalculation.calculate();
    }
}

class StatsCalculation {
    public void calculate() {
        Map<Integer, Integer> usualDraw = new HashMap<>();
        Map<Integer, Integer> additionalTwo = new HashMap<>();
        for (int i = 1; i < 51; i++) {
            usualDraw.put(i,0);
        }
        for (int i = 1; i < 13; i++) {
            additionalTwo.put(i,0);
        }
        Path statsData = Paths.get("StatsData");
        try {
            Files.walk(statsData)
                    .filter(Files::isRegularFile)
                    .forEach(file-> {
                        try {
                            String content = Files.readString(file);
                            content = content.substring(content.indexOf("\"draws\":") + 8,content.indexOf(",\"addonDraw\":"));
                            content = content.replace("[","").replace("]","");
                            List<Integer> numbers = Arrays.stream(content.split(",")).map(Integer::valueOf).toList();
                            usualDraw.put(numbers.get(0),usualDraw.get(numbers.get(0))+1);
                            usualDraw.put(numbers.get(1),usualDraw.get(numbers.get(1))+1);
                            usualDraw.put(numbers.get(2),usualDraw.get(numbers.get(2))+1);
                            usualDraw.put(numbers.get(3),usualDraw.get(numbers.get(3))+1);
                            usualDraw.put(numbers.get(4),usualDraw.get(numbers.get(4))+1);

                            additionalTwo.put(numbers.get(5),additionalTwo.get(numbers.get(5))+1);
                            additionalTwo.put(numbers.get(6),additionalTwo.get(numbers.get(6))+1);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        long numberOfDraws;
        try (Stream<Path> files = Files.list(statsData)) {
            numberOfDraws = files.count();
            System.out.println("number of draws:" + numberOfDraws);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Map<Integer, Integer> sortedUsualDraw = usualDraw.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        System.out.println();
        System.out.println("Sorted number by frequency in usual draw:");
        sortedUsualDraw.forEach((key, value) -> System.out.println("number:"+key + " frequency:" + value + " percentage:" + (((double)value)/numberOfDraws)*100 + "%"));

        Map<Integer, Integer> sortedAdditionalTwo = additionalTwo.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        System.out.println();
        System.out.println("Sorted number by frequency in additional two:");
        sortedAdditionalTwo.forEach((key, value) -> System.out.println("number:"+key + " frequency:" + value + " percentage:" + (((double)value)/numberOfDraws)*100 + "%"));
        System.out.println();
        System.out.println("Chances to win and amount of money");
        double chances5_2 = (((double)sortedUsualDraw.get(20))/numberOfDraws)
                *(((double)sortedUsualDraw.get(34))/numberOfDraws)
                *(((double)sortedUsualDraw.get(49))/numberOfDraws)
                *(((double)sortedUsualDraw.get(16))/numberOfDraws)
                *(((double)sortedUsualDraw.get(11))/numberOfDraws)
                *(((double)sortedAdditionalTwo.get(3))/numberOfDraws)
                *(((double)sortedAdditionalTwo.get(5))/numberOfDraws);
        System.out.println("5+2 chances:" + String.format("%.8f", chances5_2*100) + "% prize:1 573 179 300 Kč");

        double chances5_1 = (((double)sortedUsualDraw.get(20))/numberOfDraws)
                *(((double)sortedUsualDraw.get(34))/numberOfDraws)
                *(((double)sortedUsualDraw.get(49))/numberOfDraws)
                *(((double)sortedUsualDraw.get(16))/numberOfDraws)
                *(((double)sortedUsualDraw.get(11))/numberOfDraws)
                *(((double)sortedAdditionalTwo.get(3))/numberOfDraws);
        System.out.println("5+1 chances:" + String.format("%.8f", chances5_1*100) + "% prize:15 032 602 Kč");

        double chances5_0 = (((double)sortedUsualDraw.get(20))/numberOfDraws)
                *(((double)sortedUsualDraw.get(34))/numberOfDraws)
                *(((double)sortedUsualDraw.get(49))/numberOfDraws)
                *(((double)sortedUsualDraw.get(16))/numberOfDraws)
                *(((double)sortedUsualDraw.get(11))/numberOfDraws);
        System.out.println("5+0 chances:" + String.format("%.8f", chances5_0*100) + "% prize:3 767 862 Kč");

        double chances4_2 = (((double)sortedUsualDraw.get(20))/numberOfDraws)
                *(((double)sortedUsualDraw.get(34))/numberOfDraws)
                *(((double)sortedUsualDraw.get(49))/numberOfDraws)
                *(((double)sortedUsualDraw.get(16))/numberOfDraws)
                *(((double)sortedAdditionalTwo.get(3))/numberOfDraws)
                *(((double)sortedAdditionalTwo.get(5))/numberOfDraws);
        System.out.println("4+2 chances:" + String.format("%.8f", chances4_2*100) + "% prize:124 301 Kč");

        double chances4_1 = (((double)sortedUsualDraw.get(20))/numberOfDraws)
                *(((double)sortedUsualDraw.get(34))/numberOfDraws)
                *(((double)sortedUsualDraw.get(49))/numberOfDraws)
                *(((double)sortedUsualDraw.get(16))/numberOfDraws)
                *(((double)sortedAdditionalTwo.get(3))/numberOfDraws);
        System.out.println("4+1 chances:" + String.format("%.8f", chances4_1*100) + "% prize:7 769 Kč");
    }

    public static void downloadStats() {
        for (int i = 2015; i < 2026; i++) {
            String requestParams = i + "";
            for (int j = 1; j < 55; j++) {
                String week = j < 10 ? "0" + j : j + "";
                String tuesday = requestParams + week + 2;
                String friday = requestParams + week + 5;
                getResponse(tuesday);
                getResponse(friday);
            }
        }
    }

    public static void getResponse(String requestParams) {
        try {
            URL url = new URL("https://www.sazka.cz/api/draw-info/draws/universal/eurojackpot/" + requestParams);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                Files.writeString(Path.of("StatsData/"+requestParams+".json"), content);
                System.out.println(content);
            }
            con.disconnect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}