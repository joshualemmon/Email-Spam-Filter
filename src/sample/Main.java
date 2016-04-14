package sample;

import java.text.DecimalFormat;
import java.util.*;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.event.*;
import javafx.util.StringConverter;

import java.io.*;

/**
 * @author  Joshua  Lemmon  joshua.lemmon@uoit.net
 * @author  Tony    Wu      tony.wu@uoit.net
 * @version 1.7
 * @since   2016-03-02
 */

public class Main extends Application
{
    private TableView<TestFile> tableArea;
    private BorderPane displayResults;
    private HashMap<String, Integer> trainHamFreq;
    private HashMap<String, Integer> trainSpamFreq;
    private HashMap<String, Double> probabilitySpam;
    private HashMap<String, Double> probabilityHam;
    private HashMap<String, Double> wordSpamProbability;
    private File mainDirectory;

    @Override
    public void start(final Stage primaryStage) throws Exception
    {
        primaryStage.setTitle("Spam Master 3000");

        //creating the gridpane that will hold the directory and the train/test buttons
        GridPane editArea = new GridPane();
        editArea.setPadding(new Insets(10, 10, 10, 10));
        editArea.setVgap(5);
        editArea.setHgap(5);


        Label directoryLabel = new Label("Directory with training and testing files: ");
        editArea.add(directoryLabel, 7, 7);

        final Label trainingLabel = new Label("");
        editArea.add(trainingLabel, 7, 14);

        final Label testingLabel = new Label("");
        editArea.add(testingLabel, 7, 15);

        final Label chooseDir = new Label("");
        editArea.add(chooseDir, 7, 9);


        /**
         * This button handles the location/directory train and test folders.
         */
        Button dirButton = new Button("Choose Directory");
        dirButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(new File("."));
                mainDirectory = directoryChooser.showDialog(primaryStage);
                chooseDir.setText("Directory chosen to have testing and training files:\n ../" + mainDirectory.getName());
            }
        });
        editArea.add(dirButton, 7, 8);


        /**
         * This button handles the training of the spam detector.
         */
        Button startTrain = new Button("Train");
        startTrain.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                String spamPath = "../" + mainDirectory.getName() + "/training/spam";
                String hamPath = "../" + mainDirectory.getName() + "/training/ham";
                trainSpamFreq = new HashMap<>();
                trainHamFreq = new HashMap<>();
                TrainFile.trainEmailSet(trainSpamFreq, spamPath);
                TrainFile.trainEmailSet(trainHamFreq, hamPath);
                probabilitySpam = new HashMap<>();
                probabilitySpam = TrainFile.getWordProbability(trainSpamFreq, 500);
                probabilityHam = new HashMap<>();
                probabilityHam = TrainFile.getWordProbability(trainHamFreq, 2500);
                wordSpamProbability = new HashMap<>();
                wordSpamProbability = TrainFile.getWordSpamProbability(probabilitySpam, probabilityHam);
                trainingLabel.setText("Done Training");
            }
        });
        editArea.add(startTrain, 7, 12);


        /**
         * This button handles the testing of the test files, then creates a new window to display
         * the results in.
         */
        Button startTest = new Button("Test");
        startTest.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                testingLabel.setText("Testing in progress...");
                String spamPath = "../" + mainDirectory.getName() + "/testing/spam";
                String hamPath = "../" + mainDirectory.getName() + "/testing/ham";
                TestFile[] spamTestFiles = testingFiles(spamPath, "SPAM");
                TestFile[] hamTestFiles = testingFiles(hamPath, "HAM");
                TestFile[] files = concFileList(hamTestFiles, spamTestFiles);

                DecimalFormat df = new DecimalFormat("0.000000");
                displayResults = new BorderPane();
                tableArea = displayResults(files);
                tableArea.setMinHeight(675);
                displayResults.setTop(tableArea);
                HBox stats = new HBox(5);
                Label accuracyLabel = new Label("Accuracy:");
                TextField accuracyField = new TextField(df.format(calcAccuracy(files)));
                accuracyField.setEditable(false);
                Label precisionLabel = new Label("Precision:");
                TextField precisionField = new TextField(df.format(calcPrecision(files)));
                precisionField.setEditable(false);
                stats.getChildren().addAll(accuracyLabel, accuracyField, precisionLabel, precisionField);
                stats.setMinHeight(25);
                testingLabel.setText("Done Testing");
                displayResults.setCenter(stats);
                Scene displayScene = new Scene(displayResults, 700, 700);
                Stage displayStage = new Stage();
                displayStage.setTitle("Spam Master 3000 Results");
                displayStage.setScene(displayScene);
                displayStage.show();
            }
        });
        editArea.add(startTest, 7, 13);

        Scene scene = new Scene(editArea, 425, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * This function creates a TableView to output the results of the
     * spam detector.
     *
     * @param files an array of email test files
     * @return      returns the TableView object for use in result displaying
     */
    public TableView<TestFile> displayResults(TestFile[] files)
    {
        TableView<TestFile> tv = new TableView<>();
        TableColumn fileNameCol = new TableColumn("File");
        fileNameCol.setMinWidth(300.0);
        fileNameCol.setCellValueFactory(new PropertyValueFactory<TestFile, String>("filename"));
        TableColumn actualClassCol = new TableColumn("Actual Class");
        actualClassCol.setMinWidth(100.0);
        actualClassCol.setCellValueFactory(new PropertyValueFactory<TestFile, String>("actualClass"));
        TableColumn spamProbCol = new TableColumn("Spam Probability");
        spamProbCol.setMinWidth(300.0);
        spamProbCol.setCellValueFactory(new PropertyValueFactory<TestFile, Double>("spamProbFormatted"));
        final ObservableList<TestFile> fileList = FXCollections.observableArrayList(files);
        tv.getColumns().addAll(fileNameCol, actualClassCol, spamProbCol);
        tv.setItems(fileList);
        return tv;
    }

    /**
     * This function concatenates two TestFile arrays
     *
     * @param t1    The first TestFile array
     * @param t2    The second TestFile array
     * @return      The concatenated TestFile array
     */
    public TestFile[] concFileList(TestFile[] t1, TestFile[] t2)
    {
        TestFile[] files = new TestFile[t1.length + t2.length];
        for(int i = 0; i < t1.length; i++)
        {
            files[i] = t1[i];
        }
        for(int i = 0; i < t2.length; i++ )
        {
            files[i+t1.length] = t2[i];
        }
        return files;
    }

    /**
     * This function calculates and formats the spam email probability
     * for each email in the testing folders.
     *
     * @param path  The directory path for the chosen spam folder
     * @param type  The type of the emails currently being tested, is either
     *              ham or spam.
     * @return      returns an array of TestFiles
     */
    public TestFile[] testingFiles(String path, String type)
    {
        File emailDir = new File(path);
        File[] emailDirListing = emailDir.listFiles();

        if (emailDirListing == null) {
            System.exit(0);
        }

        TestFile[] testFiles = new TestFile[emailDirListing.length];
        int i = 0;

        Arrays.sort(emailDirListing); //Sorts Emails in Numeric Order
        for(File email : emailDirListing) {
            testFiles[i] = new TestFile(email.getName(), 0.0, type);
            testFiles[i].getSpamEmailProbability(wordSpamProbability, email);
            testFiles[i].getSpamProbRounded();
            i++;
        }

        return testFiles;
    }

    /**
     * This function calculates the precision of the spam detector.
     * Uses the formula Precision = #correct spam guesses / #of total spam guesses
     *
     * @param testfiles An array of every TestFile object that has been tested
     * @return          A double value that holds the precision of the spam detector
     */
    public double calcPrecision(TestFile[] testfiles)
    {
        int spamGuesses = 0;
        int correctSpam = 0;

        for (TestFile testfile : testfiles)
        {
            if (testfile.getSpamProbability() > 0.5)
            {
                if (testfile.getActualClass().equalsIgnoreCase("ham"))
                {
                    spamGuesses++;
                }
                else
                {
                    spamGuesses++;
                    correctSpam++;
                }
            }
        }
        return (double)correctSpam/((double)spamGuesses);
    }

    /**
     * This function calculates the accuracy of the spam detector.
     * Uses the formula Accuracy = #correct guesses / #total guesses
     *
     * @param testfiles An array of every TestFile object that has been tested
     * @return          A double value that holds the accuracy of the spam detector.
     */
    public double calcAccuracy(TestFile[] testfiles) {

        int correct = 0;
        for (TestFile testfile : testfiles)
        {
            if (testfile.getSpamProbability() > 0.5)
            {
                if (testfile.getActualClass().equalsIgnoreCase("spam"))
                {
                    correct++;
                }
            } else
            {
                if (testfile.getActualClass().equalsIgnoreCase("ham"))
                {
                    correct++;
                }
            }
        }
        return (double)correct/(double)testfiles.length;
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}