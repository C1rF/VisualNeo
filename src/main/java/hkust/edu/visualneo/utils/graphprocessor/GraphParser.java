package hkust.edu.visualneo.utils.graphprocessor;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GraphParser {

    private static final String HEALTH_DATA = "src/main/resources/hkust/edu/visualneo/data/health.csv";
    private static final String HEALTH_GRAPH = "src/main/resources/hkust/edu/visualneo/data/health_graph2.txt";

    public static void parseGraph(){
        readAllDataAtOnce(HEALTH_DATA, 11381, 61453, 16);
    }

    public static void readAllDataAtOnce(String file, long nodeNum, long edgeNum, int colIdx)
    {
        try {
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
                int firstNode = Integer.parseInt(row[colIdx]);
                int secondNode = Integer.parseInt(row[colIdx+1]);
                adj_lists.get(firstNode).add(secondNode);
                adj_lists.get(secondNode).add(firstNode);
            }


            try {
                File myObj = new File(HEALTH_GRAPH);
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
                    new_line += '\n';
                    myWriter.write(new_line);
                }
                // Close the writer
                myWriter.close();
                System.out.println("Successfully wrote to the file.");
            } catch (IOException e) {
                System.out.println("Error when writing the txt file!");
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            System.out.println("Error when reading the graph!");
            e.printStackTrace();
        }
    }
}
