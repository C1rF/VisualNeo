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
    private static final String PREFIX = "src/main/resources/hkust/edu/visualneo/data/worldcup/";
    private static final String DATA_NAME = "worldcup.csv";
    private static final String PARTITION_NAME = "graph_partition.txt";
    private static final String OUTPUT_NAME = "subgraph_collection.txt";
    private static final String OUTPUT_NODE_LABEL_NAME = "node_label_map.txt";
    private static final String OUTPUT_RELATION_LABEL_NAME = "relation_label_map.txt";
    private static final String GRAPH_DATA = PREFIX +  DATA_NAME;
    private static final String GRAPH_PARTITION =  PREFIX +  PARTITION_NAME;
    private static final String GRAPH_SUBGRAPH_COLLECTION = PREFIX + OUTPUT_NAME;
    private static final String NODE_LABEL_MAP = PREFIX + OUTPUT_NODE_LABEL_NAME;
    private static final String RELATION_LABEL_MAP = PREFIX + OUTPUT_RELATION_LABEL_NAME;
    private static final long nodeNum = 2486;
    private static final long relationNum = 14799;
    private static final int firstNodeIdx = 9;
    private static final int secondNodeIdx = firstNodeIdx + 1;
    private static final int relationLabelIdx = firstNodeIdx + 2;
    private static final int nodeLabelIdx = 1;
    private static final int groupNum = 50;

    public static void partitionGraph(){
        partitionGraph(GRAPH_PARTITION);
    }
    private static void partitionGraph(String group_file){
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

            // Get all the relations, node labels and relation labels
            List<List<Integer>> relations = new ArrayList<>();
            for (int i = 0; i < relationNum; i++) relations.add(new ArrayList<>());
            List<String> node_labels = new ArrayList<>();
            List<String> relation_labels = new ArrayList<>();
            int relationId = 0;
            for(int i = 0; i < graphData.size(); i++){
                String[] row = graphData.get(i);
                if(i < nodeNum){
                    // For each node
                    String thisNodeLabel = row[nodeLabelIdx];
                    if(!node_labels.contains(thisNodeLabel)) node_labels.add(thisNodeLabel);
                }
                else{
                    // For each relation
                    String thisRelationLabel = row[relationLabelIdx];
                    if(!relation_labels.contains(thisRelationLabel)) relation_labels.add(thisRelationLabel);
                    int firstNode = Integer.parseInt(row[firstNodeIdx]);
                    int secondNode = Integer.parseInt(row[secondNodeIdx]);
                    relations.get(relationId).add(firstNode);
                    relations.get(relationId).add(secondNode);
                    relations.get(relationId).add(relation_labels.indexOf(thisRelationLabel));
                    ++relationId;
                }
            }

            System.out.println("#Relations: " + relations.size());
            System.out.println("#Node Labels: " + node_labels.size());
            System.out.println("#Relation Labels: " + relation_labels.size());

            // Filter out all the relations that have been cut off
            List<Integer> idxToBeRemoved = new ArrayList<>();
            for(int i = 0; i < relations.size(); i++){
                int firstNode = relations.get(i).get(0);
                int secondNode = relations.get(i).get(1);
                // Check whether these two nodes are in the same group
                boolean same_group = false;
                for(List<Integer> group : groups){
                    if(group.contains(firstNode) && group.contains(secondNode))
                        same_group = true;
                }
                // If they are not in the same group, record them
                if(!same_group) idxToBeRemoved.add(i);
            }
            // Remove all the relations that have been cut off
            for(int j = idxToBeRemoved.size()-1; j >= 0; j--){
                int idx = idxToBeRemoved.get(j);
                relations.remove(idx);
            }
            System.out.println("#Remaining Relations: " + relations.size());

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
                    // Write the relation
                    for(int k = 0; k < relations.size(); k++){
                        int firstNode = relations.get(k).get(0);
                        int secondNode = relations.get(k).get(1);
                        int labelId = relations.get(k).get(2);
                        if(vertex_id_this_group.contains(firstNode)){
                            // This relation is in this subgraph
                            int firstNodeIdxInGroup = vertex_id_this_group.indexOf(firstNode);
                            int secondNodeIdxInGroup = vertex_id_this_group.indexOf(secondNode);
                            String relation_line = "e " + firstNodeIdxInGroup + " " + secondNodeIdxInGroup + " " + labelId + '\n';
                            myWriter.write(relation_line);
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

            try {
                File node_label_map = new File(NODE_LABEL_MAP);
                FileWriter myWriter = new FileWriter(node_label_map);
                for(int i = 0; i < node_labels.size(); i++)
                    myWriter.write(i + node_labels.get(i) + '\n');
                myWriter.close();
                File relation_label_map = new File(RELATION_LABEL_MAP);
                FileWriter myWriter2 = new FileWriter(relation_label_map);
                for(int i = 0; i < relation_labels.size(); i++)
                    myWriter2.write(i + ":" + relation_labels.get(i) + '\n');
                myWriter2.close();
                System.out.println("Successfully wrote to label map files");

            } catch (IOException e) {
                System.out.println("Error when writing to label map files");
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            System.out.println("Error when reading " + PARTITION_NAME);
            e.printStackTrace();
        }
    }
}
