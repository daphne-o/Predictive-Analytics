import java.io.*;
import java.util.*;

public class MovieRatingProcessor {

    // Mapper Class
    public static class Mapper {
        public List<KeyValue<Integer, Double>> map(String line) {
            List<KeyValue<Integer, Double>> output = new ArrayList<>();
            try {
                // split the line by commas, extracting movie ID and rating
                String[] fields = line.split(",");
                if (fields.length >= 3) {
                    int movieId = Integer.parseInt(fields[1]);
                    double rating = Double.parseDouble(fields[2]);
                    // emit movieId and rating pair
                    output.add(new KeyValue<>(movieId, rating));
                }
            } catch (Exception e) {
                // skip bad lines
            }
            return output;
        }
    }

    // Reducer Class
    public static class Reducer {
        public KeyValue<Integer, Double> reduce(Integer movieId, List<Double> ratings) {
            double totalRating = 0.0;
            for (double rating : ratings) {
                totalRating += rating; // sum all ratings for the movie
            }
            double averageRating = totalRating / ratings.size(); // compute the average rating
            return new KeyValue<>(movieId, averageRating); // return the movieId and average rating pair
        }
    }

    // KeyValue class which is a Helper class that stores a pair of movie_id and rating
    public static class KeyValue<K, V> {
        public K key;
        public V value;
        public KeyValue(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    // Driver Class
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: MovieRatingProcessor ratings.csv output.txt");
            System.exit(1);
        }

        String inputFilePath = args[0];
        String outputFilePath = args[1];

        Mapper movieMapper = new Mapper();
        Reducer movieReducer = new Reducer();

        Map<Integer, List<Double>> movieRatingsData = new HashMap<>();

        // Read the input file and map data to movieId and rating
        processFile(inputFilePath, movieMapper, movieRatingsData);

        // Sort the movie IDs in ascending order before writing the output
        List<Map.Entry<Integer, List<Double>>> sortedMovies = new ArrayList<>(movieRatingsData.entrySet());
        sortedMovies.sort(Map.Entry.comparingByKey());

        // Reduce the ratings and write the output to the file
        writeOutput(outputFilePath, movieReducer, sortedMovies);

        System.out.println("Processing complete. Output written to " + outputFilePath);
    }

    // Helper method to process the input file and map the data
    private static void processFile(String filePath, Mapper mapper, Map<Integer, List<Double>> movieRatingsData) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                List<KeyValue<Integer, Double>> mappedData = mapper.map(line);
                for (KeyValue<Integer, Double> entry : mappedData) {
                    movieRatingsData.putIfAbsent(entry.key, new ArrayList<>());
                    movieRatingsData.get(entry.key).add(entry.value);
                }
            }
        }
    }

    // Helper method to write the output to the output.txt file
    private static void writeOutput(String outputFilePath, Reducer reducer, List<Map.Entry<Integer, List<Double>>> sortedMovies) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            for (Map.Entry<Integer, List<Double>> entry : sortedMovies) {
                KeyValue<Integer, Double> result = reducer.reduce(entry.getKey(), entry.getValue());
                writer.write(result.key + "\t" + result.value);
                writer.newLine();
            }
        }
    }
}