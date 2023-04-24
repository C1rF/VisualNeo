package hkust.edu.visualneo.utils.graphprocessor;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * GraphParser will be the first step after getting the CSV file.
 * It will parse the csv file and output graph_adj_lists.txt which contains all adjacency lists
 * */

public class GraphParser {

    private static final String PREFIX = "src/main/resources/hkust/edu/visualneo/data/";
    private static final String DATA_NAME = "worldcup.csv";
    private static final String OUTPUT_NAME = "graph_adj_lists.txt";
    private static final String GRAPH_DATA = PREFIX + DATA_NAME;
    private static final String GRAPH_ADJ_LISTS = PREFIX + OUTPUT_NAME;
    private static final long nodeNum = 2486;
    private static final long edgeNum = 14799;
    private static final int firstNodeIdx = 9;
    private static final int secondNodeIdx = firstNodeIdx + 1;

    public static void parseGraph(){
        readGraphFile(GRAPH_DATA);
    }

    private static void readGraphFile(String file)
    {
        try {
            // Read the graph file
            FileReader filereader = new FileReader(file);
            CSVReader csvReader = new CSVReaderBuilder(filereader)
                    .withSkipLines(1)
                    .build();
            List<String[]> allData = csvReader.readAll();

            // Process the Adjacency lists
            List<List<Integer>> adj_lists = new ArrayList<>();
            for (int i = 0; i < nodeNum; i++)  {
                adj_lists.add(new ArrayList<>());
            }
            int rowCount = 0;
            for (String[] row : allData) {
                if(rowCount++ < nodeNum) continue;
                int firstNode = Integer.parseInt(row[firstNodeIdx]);
                int secondNode = Integer.parseInt(row[secondNodeIdx]);
                adj_lists.get(firstNode).add(secondNode);
                adj_lists.get(secondNode).add(firstNode);
            }

            // Write the adjacency lists to the file
            try {
                File myObj = new File(GRAPH_ADJ_LISTS);
                FileWriter myWriter = new FileWriter(myObj);
                // Write the first line
                String firstLine = String.valueOf(nodeNum) + ' ' + edgeNum + '\n';
                myWriter.write(firstLine);
                // Write the remaining lines
                for(int i = 0; i < nodeNum; i++){
                    List<Integer> adj_list = adj_lists.get(i);
                    String new_line = "";
                    for(Integer num : adj_list){
                        new_line += String.valueOf(num+1) + ' ';
                    }
                    if(i != nodeNum-1) new_line += '\n';
                    myWriter.write(new_line);
                }
                // Close the writer
                myWriter.close();
                System.out.println("Successfully wrote to " + OUTPUT_NAME);
            } catch (IOException e) {
                System.out.println("Error when writing to " + OUTPUT_NAME);
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            System.out.println("Error when reading " + DATA_NAME);
            e.printStackTrace();
        }
    }
}
