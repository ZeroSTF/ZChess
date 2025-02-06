package tn.zeros.zchess.engine.harness;


import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record TestPosition(
        String fen,
        Set<String> correctMoves,
        String id
) {
    public static TestPosition fromEpd(String epdLine) {
        // Regular expression to extract the best moves (bm)
        Pattern bmPattern = Pattern.compile("bm\\s+([^;]+);");
        Set<String> moves = getStrings(epdLine, bmPattern);

        // Regular expression to extract the id between quotes.
        Pattern idPattern = Pattern.compile("id\\s+\"([^\"]+)\";");
        Matcher idMatcher = idPattern.matcher(epdLine);
        String id = "unnamed";
        if (idMatcher.find()) {
            id = idMatcher.group(1);
        }

        // Extract the FEN by taking the substring from the start until "bm" occurs.
        String fenPart = getFenPart(epdLine);

        return new TestPosition(fenPart, moves, id);
    }

    private static String getFenPart(String epdLine) {
        int bmIndex = epdLine.indexOf(" bm ");
        String fenPart;
        if (bmIndex != -1) {
            fenPart = epdLine.substring(0, bmIndex).trim();
        } else {
            // Fall back: if there is no bm, take everything until the first semicolon.
            int semicolonIndex = epdLine.indexOf(";");
            if (semicolonIndex != -1) {
                fenPart = epdLine.substring(0, semicolonIndex).trim();
            } else {
                fenPart = epdLine.trim();
            }
        }
        return fenPart;
    }

    private static Set<String> getStrings(String epdLine, Pattern bmPattern) {
        Matcher bmMatcher = bmPattern.matcher(epdLine);
        Set<String> moves = new HashSet<>();
        if (bmMatcher.find()) {
            String movesStr = bmMatcher.group(1).trim();
            // Handle multi-move entries by splitting on whitespace.
            String[] moveTokens = movesStr.split("\\s+");
            for (String move : moveTokens) {
                // Clean move: remove any unwanted characters and convert to lowercase.
                String cleanMove = move.replaceAll("[^a-zA-Z0-9+#=]", "").toLowerCase();
                if (!cleanMove.isEmpty()) {
                    moves.add(cleanMove);
                }
            }
        }
        return moves;
    }
}