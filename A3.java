
package a3;

import org.json.simple.JSONArray; 
import org.json.simple.JSONObject; 
import org.json.simple.parser.*; 
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class A3
{

    private final Map<String, Set<String>> adjacencyLists = new HashMap<>();

    private static ArrayList<String> shortestPath = new ArrayList<String>();

    public void addVertex(final String name) {
        this.adjacencyLists.put(name, new HashSet<>());
    }

    public void addEdge(final String source, final String destination) {
        this.adjacencyLists.get(destination).add(source);
        this.adjacencyLists.get(source).add(destination);
    }

    public ArrayList<String> getNeighbors(final String name) {
        return new ArrayList<>(this.adjacencyLists.get(name));
    }

    public boolean isNeighbor(final String source, final String destination) {
        return this.adjacencyLists.get(source).contains(destination);
    }

    public int size() {
        return this.adjacencyLists.size();
    }

    public static ArrayList<String> BFS(A3 graph, String source, String destination) {
        shortestPath.clear();
        ArrayList<String> path = new ArrayList<String>();

        if (source.equals(destination)) { //If the same name return null
            path.add(source);
            return path;
        }
        
        ArrayDeque<String> queue = new ArrayDeque<String>(); //Nodes to visit
        ArrayDeque<String> visited = new ArrayDeque<String>(); //Nodes visited

        queue.offer(source);
        while (!queue.isEmpty()) {
            String name = queue.poll();
            visited.offer(name);

            ArrayList<String> neighborsList = graph.getNeighbors(name); 
            
            for(String neighbor : neighborsList) {
                path.add(neighbor);
                path.add(name);
                if (neighbor.equals(destination)) {
                    return getShortestPath(source, destination, path);
                } else {
                    if (!visited.contains(neighbor)) { 
                        queue.offer(neighbor);
                    }
                }
            }
        }
        return null;
    }

    private static ArrayList<String> getShortestPath(String src, String destination, ArrayList<String> path) {

        int index = path.indexOf(destination);
        String source = path.get(index + 1);

        shortestPath.add(0, destination); 

        if (source.equals(src)) {
            shortestPath.add(0, src);
            return shortestPath;
        } else {
            return getShortestPath(src, source, path); 
        }
    }

    public String getPath(A3 graph){ 
        Scanner reader = new Scanner(System.in);
        System.out.print("Enter Actor 1: ");
        String source = (reader.nextLine()).toLowerCase();
        if(!graph.adjacencyLists.containsKey(source))
            return "No such actor.";
        System.out.print("Enter Actor 2: ");
        String dest = (reader.nextLine()).toLowerCase();
        if(!graph.adjacencyLists.containsKey(dest))
            return "No such actor";
        reader.close(); 
        
        String result;
        ArrayList<String> arr = BFS(graph,source,dest);
        if(arr == null)
            return "No path exists";
        for(String name : arr){
            result += name+" ";
        }
        
        result = result.substring(0,result.length()); 
        
        return result;
    }
    
    public static void main(String[] args) throws Exception
    {
        //READ IN CSV FILE
        String csvFile = args[0];
        //tmdb_5000_credits.csv
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",\"";
        A3 graph = new A3();
        
        try {
            br = new BufferedReader(new FileReader(csvFile));
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                if(line.contains("[],[]")){ //Movies with no listed cast are ignored
                    line = br.readLine();
                }
                String[] movieInfo = line.split(cvsSplitBy);
                String cast = "";
                if(movieInfo[1].contains("[{")) //Get the cast if it exists
                    cast = movieInfo[1].replace("\"\"","\""); //File is not in JSON format to be parsed. Must remove double qoute ("") and replace with (")
                else if(movieInfo[2].contains("[{"))//Sometimes must check [2] if the Movie title contains quotes (Such as Narnia)
                    cast = movieInfo[2].replace("\"\"","\"");
                JSONParser parser = new JSONParser();
                JSONArray arrayOfCast; // = (JSONArray) parser.parse(cast);
                try{
                    arrayOfCast  = (JSONArray) parser.parse(cast);
                }catch(Exception e){ //If it fails to parse because of bad JSON, Go to next line and do the same. (I Would get EOF error on some movies for unknown reasons)
                    line = br.readLine();
                    movieInfo = line.split(cvsSplitBy);
                    cast = "";
                    if(movieInfo[1].contains("[{"))
                        cast = movieInfo[1].replace("\"\"","\"");
                    else if(movieInfo[2].contains("[{"))
                        cast = movieInfo[2].replace("\"\"","\"");
                    arrayOfCast  = (JSONArray) parser.parse(cast);
                }
               
                for(Object o : arrayOfCast){ //Go through each name in cast
                    JSONObject jsonLineItem = (JSONObject) o;
                    String name = ((String) jsonLineItem.get("name")).toLowerCase();
                    if(!graph.adjacencyLists.containsKey(name)) //If the name is not there already, add it
                        graph.addVertex(name);
                    for(Object j : arrayOfCast){ //Go through rest of cast in that movie. Add the names missing, then create edge to orginal cast member
                        JSONObject jsonLineItem2 = (JSONObject) j;
                        String name2 = ((String) jsonLineItem2.get("name")).toLowerCase();
                        if(!graph.adjacencyLists.containsKey(name2))
                            graph.addVertex(name2);
                        graph.addEdge(name, name2);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
       System.out.println(graph.getPath(graph));
        
    } 
} 
