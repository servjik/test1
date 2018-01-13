package mainPackage;

public class Configs {
// Coins ---------------
    private static String [] coinNames = {"Hush", "Monero", "Sumokoin", "Zcash", "Zclassic"};
    private static String [] coinCodes = {"HUSH", "XMR", "SUMO", "ZEC", "ZCL"};
    private static String [] coinAlgoritms = {"Equihash", "CryptoNight", "CryptoNight", "Equihash", "Equihash"};
//    private static String [] coinNames = {"Hush", "Monero", "Sumokoin"};
//    private static String [] coinCodes = {"HUSH", "XMR", "SUMO"};
//    private static String [] coinAlgoritms = {"Equihash", "CryptoNight", "CryptoNight"};

// Use 'TOP N' coins in card for calculate ---------------
    private static int coinsLimitInCominations = -1; //(must be INTEGER!) "-1" - all, "N" - TOP N

// Cards ---------------
//    private static String [] cardNames = {"rx480", "1080ti", "vega56"};
//    private static int [] cardHashratesEquihash = {300, 720, 450};
//    private static int [] cardHashratesCryptonight = {600, 850, 1800};
    private static String [] cardNames = {"rx480", "1080ti"};
    private static int [] cardHashratesEquihash = {300, 720};
    private static int [] cardHashratesCryptonight = {600, 850};
    private static int defaultHashrateEquihash = 870; //hashrate for 3x480
    private static int defaultHashrateCryptonight = 2190; //hashrate for 3x480

// File path ---------------
    private static String defaultRewardsPath = "z:\\downloads\\overclock\\!mine\\defaultRewards.json"; //json with rewards for 3x480
    private static String testPath = "c:\\Users\\servj\\IdeaProjects\\!vjikTest\\src\\test.json";

// Current rate BTC/USD in USD ---------------
    private static double rateBTCtoUSD = 14000;

// How many combinations should be printed ---------------
    private static int topCombinationsNumberForPrint = 15; //(must be INTEGER!) "-1" - print all, "N" - print first N

    public static String[] getCoinNames() {
        return coinNames;
    }

    public static String[] getCoinCodes() {
        return coinCodes;
    }

    public static String[] getCoinAlgoritms() {
        return coinAlgoritms;
    }

    public static int getCoinsLimitInCominations() {
        return coinsLimitInCominations;
    }

    public static String[] getCardNames() {
        return cardNames;
    }

    public static int[] getCardHashratesEquihash() {
        return cardHashratesEquihash;
    }

    public static int[] getCardHashratesCryptonight() {
        return cardHashratesCryptonight;
    }

    public static int getDefaultHashrateEquihash() {
        return defaultHashrateEquihash;
    }

    public static int getDefaultHashrateCryptonight() {
        return defaultHashrateCryptonight;
    }

    public static String getDefaultRewardsPath() {
        return defaultRewardsPath;
    }

    public static String getTestPath() {
        return testPath;
    }

    public static double getRateBTCtoUSD() {
        return rateBTCtoUSD;
    }

    public static int getTopCombinationsNumberForPrint() {
        return topCombinationsNumberForPrint;
    }
}
