package sample;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author  Joshua  Lemmon  joshua.lemmon@uoit.net
 * @author  Tony    Wu      tony.wu@uoit.net
 * @version 1.5
 * @since   2016-03-02
 */

public class TrainFile
{
    //array of ignore characters, so word will be ignored if they contain one of these
    //useful to remove email code tags
    private static char[] ignoreChars = {'<','>','{','}', ',', ':', '/', '\'', '\"', '@', '\t', '\n', '\r', '(', ')',
                                         '$'};
    //array of common english words/ common email code tags to ignore if they are encountered
    private static String[] ignoreStrings = {"and", "to", "some", "a", "the", "for", "be", "of", "in", "have", "not",
                                             ".com", ".net", ".ca", "buy", "clearance", "clearances", "order", "orders",
                                             "shopper", "meet", "single", "singles", "make", "making", "earn",
                                             "earnings", "money", "income", "additional", "business", "boss",
                                             "employment", "home", "extra", "cash", "opportunity", "university",
                                             "college", "online", "degree", "friends", "dating", "expect", "loans",
                                             "$$$", "best", "price", "bargain", "cheap", "collect", "fast", "credit",
                                             "cost", "free", "discount", "dollar", "rates", "charges", "easy",
                                             "insurance", "deal", "mortgage", "claim", "affordable", "only", "profit",
                                             "profits", "quote", "debt", "save", "refinance", "finance", "bankruptcy",
                                             "paid", "get", "lower", "interest", "payment", "bad", "investment",
                                             "stock", "refund", "full", "alert", "solution", "success", "satisfaction"};

    /**
     * Record a word and increase occurrence by 1 if the word is in the email
     *
     * @param hMap  The Hashmap that will hold words and their occurrence in an email set
     * @param email The email that will be analyzed
     * @return      A Hashmap filled with words and their occurrences
     */
    public static HashMap<String, Integer> scanEmail(HashMap<String, Integer> hMap, File email)
    {
        HashMap<String, Integer> compare = new HashMap<>();
        try
        {
            FileReader fileReader = new FileReader(email);
            BufferedReader input = new BufferedReader(fileReader);
            String line;
            try
            {
                while ((line = input.readLine()) != null)
                {
                    String[] currLine = line.split(" ");
                    for(String word : currLine)
                    {
                        if(!containsIgnoreChar(word) && !inIgnoreStrings(word))
                        {
                            if (hMap.get(word) == null)
                            {
                                hMap.put(word, 1);
                                compare.put(word, 1);

                            }

                            if (compare.get(word) == null)
                            {
                                hMap.put(word, hMap.get(word) + 1);
                                compare.put(word, 1);
                            }
                        }
                    }
                }
            } catch(IOException ioex)
            {
                System.out.println(ioex.getMessage());
            }
        } catch(FileNotFoundException e)
        {
            System.out.println("Error, file not found. Please verify paths.");
        }
        return hMap;
    }

    /**
     * Gets the probability/frequency of a word appearing in an email
     *
     * @param hMap  A completed Hashmap filled with words and their occurrences
     * @param files The number of files in the email set
     * @return      A probability map that is a record of words and their frequency
     */
    public static HashMap<String, Double> getWordProbability(HashMap<String, Integer> hMap, int files)
    {
        HashMap<String, Double> probabilityMap = new HashMap<>();

        Map<String, Integer> map = hMap;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String word = entry.getKey();
            int occurrences = entry.getValue();
            probabilityMap.put(word, (double)occurrences/(double)files);
        }
        return probabilityMap;
    }

    /**
     * Generates the probability of a spam word
     * Using equation Pr(S|Wi) = PR(Wi|S)/(Pr(Wi|S)+Pr(Wi|H))
     *
     * @param spam  A completed Hashmap filled with words and their frequency in spam emails
     * @param ham   A completed Hashmap filled with words and their frequency in ham emails
     * @return      A probability map that is a record of words and their probability of being spam
     */
    public static HashMap<String, Double> getWordSpamProbability(HashMap<String, Double> spam, HashMap<String, Double> ham)
    {
        HashMap<String, Double> probMap = new HashMap<>();

        Map<String, Double> map = spam;
        for(Map.Entry<String, Double> entry : map.entrySet())
        {
            String word = entry.getKey();
            if(ham.get(word) == null)
            {
                //if word only ever appears in spam then Pr(S|Wi) simplifies to 1
                probMap.put(word, 1.0);
            }
            else
            {
                double spamicity = spam.get(word)/(ham.get(word)+spam.get(word));
                //if spamicity is near 0.5 then it contributes little to an accurate decision
                if(spamicity < 0.45 || spamicity > 0.6)
                {
                    probMap.put(word, spamicity);
                }
            }
        }
        return probMap;
    }

    /**
     * Determines whether an ignore character is used
     *
     * @param word  A word in an email
     * @return      True if there is, False if there's not
     */
    private static boolean containsIgnoreChar(String word)
    {
        for(char c : ignoreChars)
        {
            if(word.indexOf(c) >= 0)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines weather an ignore word is used
     *
     * @param word  A word in an email
     * @return      True if word is in ignore list, false if not
     */
    private static boolean inIgnoreStrings(String word)
    {
        for (String s : ignoreStrings)
        {
            if (word.equals(s)) //equalsIgnoreCase?
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Runs through a train email set to record words and their occurrence
     *
     * @param emailMap      The email Hashmap that will record words and their occurrence in an email set
     * @param directoryPath The location of emails
     */
    public static void trainEmailSet(HashMap<String, Integer> emailMap, String directoryPath)
    {
        File emailDir = new File(directoryPath);
        File[] emailDirListing = emailDir.listFiles();

        if (emailDirListing != null)
        {
            Arrays.sort(emailDirListing);
            for (File email : emailDirListing)
            {
                emailMap = scanEmail(emailMap, email);
            }
        }
    }
}