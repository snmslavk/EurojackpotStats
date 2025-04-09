package euro.jackpot;

public class Main {
    public static void main(String[] args) {
        StatsCalculation statsCalculation = new StatsCalculation();
        statsCalculation.downloadStats();
        statsCalculation.calculate();
    }
}

