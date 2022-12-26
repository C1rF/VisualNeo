package hkust.edu.visualneo.utils.graphprocessor;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GraphPartitioner {
    private static final String PREFIX = "src/main/resources/hkust/edu/visualneo/data/";
    private static final String DATA_NAME = "worldcup.csv";
    private static final String PARTITION_NAME = "graph_partition.txt";
    private static final String OUTPUT_NAME = "subgraph_collection.txt";
    private static final String GRAPH_DATA = PREFIX +  DATA_NAME;
    private static final String GRAPH_PARTITION =  PREFIX +  PARTITION_NAME;
    private static final String GRAPH_SUBGRAPH_COLLECTION = PREFIX + OUTPUT_NAME;
    private static final long nodeNum = 2486;
    private static final long edgeNum = 14799;
    private static final int firstNodeIdx = 9;
    private static final int secondNodeIdx = firstNodeIdx + 1;
    private static final int edgeLabelIdx = firstNodeIdx + 2;
    private static final int nodeLabelIdx = 1;
    private static final int groupNum = 50;

    public static void partitionGraph(){
        partitionGraph(GRAPH_PARTITION);
    }
    public static void partitionGraph(String group_file){
        try {
            // Read the partition information
            File file = new File(group_file);
            Scanner sc = new Scanner(file);
            List<String> partitionData = new ArrayList<>();
            while (sc.hasNextLine())
                partitionData.add(sc.nextLine());

            // Process the group id
            List<List<Integer>> groups = new ArrayList<>();
            for (int i = 0; i < groupNum; i++)  {
                groups.add(new ArrayList<>());
            }
            for (int vertexId = 0; vertexId < partitionData.size(); vertexId++) {
                int group_id = Integer.parseInt( partitionData.get(vertexId) );
                groups.get(group_id).add(vertexId);
            }

            System.out.println("#Group: " + groups.size());

            // Read the original graph data
            FileReader fileReader2 = new FileReader(GRAPH_DATA);
            CSVReader csvReader2 = new CSVReaderBuilder(fileReader2)
                    .withSkipLines(1)
                    .build();
            List<String[]> graphData = csvReader2.readAll();

            // Get all the edges, node labels and edge labels
            List<List<Integer>> edges = new ArrayList<>();
            for (int i = 0; i < edgeNum; i++) edges.add(new ArrayList<>());
            List<String> node_labels = new ArrayList<>();
            List<String> edge_labels = new ArrayList<>();
            int edgeId = 0;
            for(int i = 0; i < graphData.size(); i++){
                String[] row = graphData.get(i);
                if(i < nodeNum){
                    // For each node
                    String thisNodeLabel = row[nodeLabelIdx];
                    if(!node_labels.contains(thisNodeLabel)) node_labels.add(thisNodeLabel);
                }
                else{
                    // For each edge
                    String thisEdgeLabel = row[edgeLabelIdx];
                    if(!edge_labels.contains(thisEdgeLabel)) edge_labels.add(thisEdgeLabel);
                    int firstNode = Integer.parseInt(row[firstNodeIdx]);
                    int secondNode = Integer.parseInt(row[secondNodeIdx]);
                    edges.get(edgeId).add(firstNode);
                    edges.get(edgeId).add(secondNode);
                    edges.get(edgeId).add(edge_labels.indexOf(thisEdgeLabel));
                    ++edgeId;
                }
            }

            System.out.println("#Edges: " + edges.size());
            System.out.println("#Node Labels: " + node_labels.size());
            System.out.println("#Edge Labels: " + edge_labels.size());

            // Filter out all the edges that have been cut off
            List<Integer> idxToBeRemoved = new ArrayList<>();
            for(int i = 0; i < edges.size(); i++){
                int firstNode = edges.get(i).get(0);
                int secondNode = edges.get(i).get(1);
                // Check whether these two nodes are in the same group
                boolean same_group = false;
                for(List<Integer> group : groups){
                    if(group.contains(firstNode) && group.contains(secondNode))
                        same_group = true;
                }
                // If they are not in the same group, record them
                if(!same_group) idxToBeRemoved.add(i);
            }
            // Remove all the edges that have been cut off
            for(int j = idxToBeRemoved.size()-1; j >= 0; j--){
                int idx = idxToBeRemoved.get(j);
                edges.remove(idx);
            }
            System.out.println("#Remaining Edge: " + edges.size());

            // Use groups to generate TED input file
            try {
                File myObj = new File(GRAPH_SUBGRAPH_COLLECTION);
                FileWriter myWriter = new FileWriter(myObj);

                // Write each subgraph sequentially
                for(int i = 0; i < groupNum; i++){
                    List<Integer> vertex_id_this_group = groups.get(i);
                    // Write the first line
                    String head_line = "t # " + i + " " + vertex_id_this_group.size() + '\n';
                    myWriter.write(head_line);
                    // Write the vertex
                    for(int j = 0; j < vertex_id_this_group.size(); j++){
                        String vertex_label = graphData.get(vertex_id_this_group.get(j))[1];
                        int vertex_label_id = node_labels.indexOf(vertex_label);
                        String vertex_line = "v " + j + " " + vertex_label_id + '\n';
                        myWriter.write(vertex_line);
                    }
                    // Write the edge
                    for(int k = 0; k < edges.size(); k++){
                        int firstNode = edges.get(k).get(0);
                        int secondNode = edges.get(k).get(1);
                        int labelId = edges.get(k).get(2);
                        if(vertex_id_this_group.contains(firstNode)){
                            // This edge is in this subgraph
                            int firstNodeIdxInGroup = vertex_id_this_group.indexOf(firstNode);
                            int secondNodeIdxInGroup = vertex_id_this_group.indexOf(secondNode);
                            String edge_line = "e " + firstNodeIdxInGroup + " " + secondNodeIdxInGroup + " " + labelId + '\n';
                            myWriter.write(edge_line);
                        }
                    }
                    myWriter.write('\n');
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
            System.out.println("Error when reading " + PARTITION_NAME);
            e.printStackTrace();
        }
    }
}
