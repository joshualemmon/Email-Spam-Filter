package sample;
import java.io.*;
import java.text.NumberFormat;
import java.util.HashMap;

/**
 * @author  Joshua  Lemmon  joshua.lemmon@uoit.net
 * @author  Tony    Wu      tony.wu@uoit.net
 * @version 1.6
 * @since   2016-03-02
 */

public class TestFile
{
    private String filename;
    private double spamProbability;
    private String spamProbFormatted;
    private String actualClass;

    /**
     * Testfile constructor
     *
     * @param filename        Name of file
     * @param spamProbability Spam probability
     * @param actualClass     Ham or Spam, the actual class/folder that file is in
     */
    public TestFile(String filename, double spamProbability, String actualClass)
    {
        this.filename = filename;
        this.spamProbability = spamProbability;
        this.actualClass = actualClass;
        this.spamProbFormatted = " ";
    }

    /**
     * Get and Set methods
     */
    public String getFilename()
    {
        return this.filename;
    }

    public String getSpamProbFormatted()
    {
        return this.spamProbFormatted;
    }

    public double getSpamProbability()
    {
        return this.spamProbability;
    }

    public String getActualClass()
    {
        return this.actualClass;
    }

    public void setFilename(String value)
    {
        this.filename = value;
    }

    public void setSpamProbability(double val)
    {
        this.spamProbability = val;
    }

    public void setActualClass(String value)
    {
        this.actualClass = value;
    }

    public void setSpamProbFormatted(String value)
    {
        this.spamProbFormatted = value;
    }

    /**
     * This function formats the spam probability to six decimal places
     */
    public void getSpamProbRounded()
    {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(6);
        nf.setMinimumFractionDigits(6);
        this.spamProbFormatted = nf.format(spamProbability);
    }

    /**
     * Gets the probability that an email is spam
     *
     * @param probMap   A hashmap of probable spam words
     * @param email     The email being analyzed
     * @return          The probability of a spam file
     */
    public void getSpamEmailProbability(HashMap<String, Double> probMap, File email)
    {
        double eta = 0.0;
        String line;

        try
        {
            FileReader fileChecker = new FileReader(email);
            BufferedReader check = new BufferedReader(fileChecker);

            while ((line = check.readLine()) != null)
            {
                String[] currLine = line.split(" ");
                for (int i = 0; i < currLine.length; i++)
                {
                    if ((probMap.get(currLine[i]) != null) && (probMap.get(currLine[i]) != 0.0))
                    {
                        eta += (Math.log(1 - probMap.get(currLine[i])) - Math.log(probMap.get(currLine[i])));
                    }
                }
            }
        } catch (IOException ioex)
        {
            System.out.println(ioex.getMessage());
        }

        this.spamProbability = 1.0 / (1.0 + Math.pow(Math.E, eta));
    }
}