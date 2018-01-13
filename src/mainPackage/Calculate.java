package mainPackage;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileReader;
import java.io.IOException;


public class Calculate {
    private static int helpIndex = 0;

    public static void main(String[] args) {
        if(!checkConfigs()) return;

        Coin[] coins = createCoinsList();
        Card[] cards = createCardsList(coins);
        fillCardsFromJson(cards);
        for (int i = 0; i < cards.length; ++i)
        {
            sortCoinsInCard(cards[i]);
            printCardCoins(cards[i]); // унести в createCardsList
        }
        //todo если ограничиваем количество карт, то сортим монеты в картах и работаем. если не ограничиваем, то монеты не сортим.

        СardCoinCombination[] cardCoinCombinations = createCardCoinCombinations();
        fillAllCombinations(coins, cards, cardCoinCombinations);
        СardCoinCombination[] tmpArray = new СardCoinCombination[cardCoinCombinations.length]; //сделать отдельным методом
        cardCoinCombinations = mergeSort(cardCoinCombinations,tmpArray,0,cardCoinCombinations.length - 1); //сделать отдельным методом
        printBestCoins(cardCoinCombinations);
    }

    private static boolean checkConfigs()
    {
        boolean isOk = false;

        if (Configs.getCardNames().length == Configs.getCardHashratesEquihash().length &&
                Configs.getCardNames().length == Configs.getCardHashratesCryptonight().length) isOk = true;
        else {System.out.println("Длины массивов с настройками КАРТ не совпадают!");
            isOk = false;}

        if (Configs.getCoinNames().length == Configs.getCoinCodes().length &&
                Configs.getCoinNames().length == Configs.getCoinAlgoritms().length) isOk = true;
        else {System.out.println("Длины массивов с настройками МОНЕТ не совпадают!");
            isOk = false;}

        return isOk;
    }

    public static Coin [] createCoinsList() //create Coins list from Config.java
    {
        Coin [] coins = new Coin[Configs.getCoinNames().length];
        for (int i = 0; i < Configs.getCoinNames().length; i++) {
            coins[i] = new Coin();
            coins[i].setName(Configs.getCoinNames()[i]);
            coins[i].setCode(Configs.getCoinCodes()[i]);
            coins[i].setAlgorithm(Configs.getCoinAlgoritms()[i]);
        }
        return coins;
    }

    public static Card [] createCardsList(Coin[] coins) //create Cards list from Config.java for each coins
    {
        Card[] cardsList = new Card[Configs.getCardNames().length];
        for (int i = 0; i < cardsList.length; i++) {
            cardsList[i] = new Card();
            cardsList[i].setName(Configs.getCardNames()[i]);
            cardsList[i].hashRateEquihash = Configs.getCardHashratesEquihash()[i];
            cardsList[i].hashRateCryptonote = Configs.getCardHashratesCryptonight()[i];
            cardsList[i].coins = new Coin[coins.length];
            cardsList[i].coinBtcRewards24 = new double[coins.length];
            for (int j = 0; j < coins.length; j++) {
                cardsList[i].coins[j] = coins[j];
                cardsList[i].coinBtcRewards24[j] = 0;
            }
        }
        return cardsList;
    }

    private static СardCoinCombination[] createCardCoinCombinations()
    {
        int numberOfCombinations = countNumberOfCombinations();
        СardCoinCombination[] cardCoinCombinations = new СardCoinCombination[numberOfCombinations];
        for(int i = 0; i < numberOfCombinations; ++i)
        {
            cardCoinCombinations[i] = new СardCoinCombination();
            cardCoinCombinations[i].cards = new Card[Configs.getCardNames().length];
            cardCoinCombinations[i].coins = new Coin[Configs.getCardNames().length];
            cardCoinCombinations[i].totalBtcReward24 = 0;
        }
        return cardCoinCombinations;
    }

    public static int countNumberOfCombinations()
    {
        int numberOfCombinations = 1;
        int coinsLimitInCominations;
        if (Configs.getCoinsLimitInCominations() == -1) coinsLimitInCominations = Configs.getCoinNames().length;
        else coinsLimitInCominations = Configs.getCoinsLimitInCominations();

        for (int i = 0; i < Configs.getCardNames().length; ++i) numberOfCombinations = numberOfCombinations * coinsLimitInCominations;
        return numberOfCombinations;
    }

    public static void fillCardsFromJson(Card[] cardsList)
    {
        JSONParser parser = new JSONParser();
        Object fileObject = null;
        try {
            fileObject = parser.parse(new FileReader(Configs.getDefaultRewardsPath()));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = (JSONObject) fileObject;
        JSONObject allCoins = (JSONObject) jsonObject.get("coins");

        for (int i = 0; i < Configs.getCoinNames().length; ++i) //идем по списку монет
        {
            JSONObject currentCoin = (JSONObject) allCoins.get(Configs.getCoinNames()[i]);
            String stringER24 = (String) currentCoin.get("btc_revenue24");
            double doubleER24 = Double.parseDouble(stringER24);
            for(int j = 0; j < cardsList.length; ++j)
            {
                double normilizedReward = normilizeCoinRewardForCard (i, j, doubleER24); //нормализуем прибыль для монеты i у карты j
                cardsList[j].coinBtcRewards24[i] = normilizedReward;
//                System.out.println(cardsList[j].coins[i].getName() + " rewards using " + cardsList[j].getName() + " is: " + cardsList[j].coinBtcRewards24[i]);
            }
//            System.out.print("\n");
        }
    }

    public static double normilizeCoinRewardForCard (int coinIndex, int cardIndex, double reward)
    {
        double factor = -1;
        if (Configs.getCoinAlgoritms()[coinIndex] == "Equihash")
            factor = (double) Configs.getCardHashratesEquihash()[cardIndex] / Configs.getDefaultHashrateEquihash();
        if (Configs.getCoinAlgoritms()[coinIndex] == "CryptoNight")
            factor = (double) Configs.getCardHashratesCryptonight()[cardIndex] / Configs.getDefaultHashrateCryptonight();
        return reward * factor;
    }

    public static void fillAllCombinations(Coin[] coins, Card[] cards, СardCoinCombination[] cardCoinCombinations)
    {
        int [] combinationIndexes = new int[cards.length]; //конкретная комбинация карт-монет. позиция индекса это индекс карты, значение - индекс монеты
        //вариант оптимизации - для карточек посортить монеты по реварду и потом собрать комбинации топ2/топ3 монет для всех карт
        int coinsLimitInCominations;
        if (Configs.getCoinsLimitInCominations() == -1) coinsLimitInCominations = Configs.getCoinNames().length;
        else coinsLimitInCominations = Configs.getCoinsLimitInCominations();

        for (int i = 0; i < cards.length; ++i)
        {
            int j;
            if (i == 0) {
                j = 0;
            }
            else j = 1;

            while (j < coinsLimitInCominations)
            {
                resetPrevColumns(i, combinationIndexes);
                if (i == 0)
                {
                    combinationIndexes[i] = j;
                    putCombination(coins, cards, combinationIndexes, cardCoinCombinations);
                }
                else
                {
                    combinationIndexes[i] = j;
                    iteratePrevColumns(i, combinationIndexes, cardCoinCombinations, coins, cards, true);
                }
                ++j;
            }
        }
    }

    public static void resetPrevColumns (int currentColumn, int [] combinationIndexes)
    {
        for (int i = 0; i < currentColumn; ++i)
            combinationIndexes[i] = 0;
    }

    public static void iteratePrevColumns (int currentColumn, int [] combinationIndexes, СardCoinCombination[] cardCoinCombinations, Coin[] coins, Card[] cards, boolean firstIteration)
    {
        if (currentColumn != 0)
        {
            int i = 0;
            if (firstIteration) i = 0;
            else i = 1;
            int coinsLimitInCominations;
            if (Configs.getCoinsLimitInCominations() == -1) coinsLimitInCominations = Configs.getCoinNames().length;
            else coinsLimitInCominations = Configs.getCoinsLimitInCominations();

            while (i < coinsLimitInCominations)
            {
                resetPrevColumns(currentColumn, combinationIndexes);
                combinationIndexes[currentColumn - 1] = i;
                putCombination(coins, cards, combinationIndexes, cardCoinCombinations);
                iteratePrevColumns(currentColumn - 1, combinationIndexes, cardCoinCombinations, coins, cards, false);
                ++i;
            }
        }
    }

    public static void putCombination(Coin[] coins, Card[] cards, int[] combinationIndexes, СardCoinCombination[] cardCoinCombinations)
    {
        for (int i = 0; i < Configs.getCardNames().length; ++i)
            cardCoinCombinations[helpIndex].coins[i] = coins[combinationIndexes[i]];
        for (int i = 0; i < Configs.getCardNames().length; ++i)
        {
            cardCoinCombinations[helpIndex].cards[i] = cards[i];
            cardCoinCombinations[helpIndex].totalBtcReward24 += cardCoinCombinations[helpIndex].cards[i].coinBtcRewards24[combinationIndexes[i]];
        }
        helpIndex += 1;
//        printIntArray(combinationIndexes);
    }

    public static void printIntArray(int[] array)
    {
        System.out.print("combination:");
        for (int i: array) System.out.print(" " + i);
        System.out.print("\n");
    }

    public static void printBestCoins(СardCoinCombination[] cardCoinCombinations)
    {
        int topCombinationsNumberForPrint;
        System.out.println();

        if (Configs.getTopCombinationsNumberForPrint() == -1)
        {
            topCombinationsNumberForPrint = cardCoinCombinations.length;
            System.out.printf("Current BTC/USD rate: $%.2f \nRewards (all possible):\n", Configs.getRateBTCtoUSD());
        }
        else
        {
            topCombinationsNumberForPrint = Integer.min(Configs.getTopCombinationsNumberForPrint(),countNumberOfCombinations());
            System.out.printf("Current BTC/USD rate: $%.2f \nRewards (TOP %d):\n", Configs.getRateBTCtoUSD(),topCombinationsNumberForPrint);
        }

        for (int i = 0; i < topCombinationsNumberForPrint; ++i)
        {
            System.out.printf("$%.2f (%.5f): ", cardCoinCombinations[i].totalBtcReward24 * Configs.getRateBTCtoUSD(), cardCoinCombinations[i].totalBtcReward24);
            for (int j = 0; j < Configs.getCardNames().length; ++j)
                System.out.print(cardCoinCombinations[i].cards[j].getName() + "(" + cardCoinCombinations[i].coins[j].getCode() + ") ");
            System.out.println();
        }
        if (Configs.getTopCombinationsNumberForPrint() != -1) System.out.printf("and %d more...", cardCoinCombinations.length - topCombinationsNumberForPrint);
    }


    public static void sortCoinsInCard (Card card)
    {
        for (int i = card.coins.length - 1; i > 0; --i)
        {
            for (int j = 0; j < i; ++j)
            {
                if (card.coinBtcRewards24[j] < card.coinBtcRewards24[j+1])
                {
                    Coin tmpCoin;
                    double tmpCoinBtcRewards24;
                    tmpCoin = card.coins[j];
                    card.coins[j] = card.coins[j+1];
                    card.coins[j+1] = tmpCoin;
                    tmpCoinBtcRewards24 = card.coinBtcRewards24[j];
                    card.coinBtcRewards24[j] = card.coinBtcRewards24[j+1];
                    card.coinBtcRewards24[j+1] = tmpCoinBtcRewards24;
                }
            }
        }
    }

    public static СardCoinCombination[] mergeSort (СardCoinCombination[] leftArray, СardCoinCombination[] rightArray, int leftIndex, int rightIndex)
    {
        if (leftIndex == rightIndex)
        {
            rightArray[leftIndex] = leftArray[leftIndex];
            return rightArray;
        }

        int middleIndex = (leftIndex + rightIndex) / 2;
        СardCoinCombination[] leftHelpArray = mergeSort(leftArray, rightArray, leftIndex, middleIndex);
        СardCoinCombination[] rightHelpArray = mergeSort(leftArray, rightArray, middleIndex + 1, rightIndex);

        СardCoinCombination[] targetArray;
        if (leftHelpArray == leftArray) targetArray = rightArray;
        else targetArray = leftArray;

        int currentLeft = leftIndex;
        int currentRight = middleIndex + 1;

        for (int i = leftIndex; i <= rightIndex; ++i)
        {
            if (currentLeft <= middleIndex && currentRight <= rightIndex)
            {
                if (leftHelpArray[currentLeft].totalBtcReward24 > rightHelpArray[currentRight].totalBtcReward24)
                {
                    targetArray[i] = leftHelpArray[currentLeft];
                    ++currentLeft;
                }
                else
                {
                    targetArray[i] = rightHelpArray[currentRight];
                    ++currentRight;
                }
            }
            else if (currentLeft <= middleIndex)
            {
                targetArray[i] = leftHelpArray[currentLeft];
                ++currentLeft;
            }
            else
            {
                targetArray[i] = rightHelpArray[currentRight];
                ++currentRight;
            }
        }
        return targetArray;
    }

    public static int[] mergeSort (int[] leftArray, int[] rightArray, int leftIndex, int rightIndex)
    {
        if (leftIndex == rightIndex)
        {
            rightArray[leftIndex] = leftArray[leftIndex];
            return rightArray;
        }

        int middleIndex = (leftIndex + rightIndex) / 2;
        int[] leftHelpArray = mergeSort(leftArray, rightArray, leftIndex, middleIndex);
        int[] rightHelpArray = mergeSort(leftArray, rightArray, middleIndex + 1, rightIndex);

        int[] targetArray;
        if (leftHelpArray == leftArray) targetArray = rightArray;
        else targetArray = leftArray;

        int currentLeft = leftIndex;
        int currentRight = middleIndex + 1;

        for (int i = leftIndex; i <= rightIndex; ++i)
        {
            if (currentLeft <= middleIndex && currentRight <= rightIndex)
            {
                if (leftHelpArray[currentLeft] < rightHelpArray[currentRight])
                {
                    targetArray[i] = leftHelpArray[currentLeft];
                    ++currentLeft;
                }
                else
                {
                    targetArray[i] = rightHelpArray[currentRight];
                    ++currentRight;
                }
            }
            else if (currentLeft <= middleIndex)
            {
                targetArray[i] = leftHelpArray[currentLeft];
                ++currentLeft;
            }
            else
            {
                targetArray[i] = rightHelpArray[currentRight];
                ++currentRight;
            }
        }
        return targetArray;
    }

    public static void printCardCoins(Card card)
    {
        System.out.println();
        System.out.println(card.getName() + " coin's rewards:");
        for (int i = 0; i < card.coins.length; ++i)
            System.out.printf("%s - $%.2f\n", card.coins[i].getCode(), card.coinBtcRewards24[i] * Configs.getRateBTCtoUSD());
    }
}