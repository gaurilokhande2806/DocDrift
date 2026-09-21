package org.docdrift.engine;

import org.springframework.stereotype.Component;

@Component
public class SemanticEngine {

    /**
     * Calculates string similarity score between 0.0 and 1.0 using Jaro-Winkler distance algorithm.
     */
    public double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        if (s1.equalsIgnoreCase(s2)) return 1.0;

        String str1 = s1.toLowerCase().trim();
        String str2 = s2.toLowerCase().trim();

        if (str1.equals(str2)) return 1.0;

        int jaroDistance = getJaroDistance(str1, str2);
        if (jaroDistance == 0) return 0.0;

        // Prefix scale calculation for Winkler
        int prefixLength = 0;
        for (int i = 0; i < Math.min(4, Math.min(str1.length(), str2.length())); i++) {
            if (str1.charAt(i) == str2.charAt(i)) {
                prefixLength++;
            } else {
                break;
            }
        }

        double jaro = (double) jaroDistance / 100.0;
        double winkler = jaro + (prefixLength * 0.1 * (1.0 - jaro));
        return Math.min(1.0, Math.max(0.0, winkler));
    }

    private int getJaroDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        if (len1 == 0 || len2 == 0) return 0;

        int matchDistance = Math.max(len1, len2) / 2 - 1;
        if (matchDistance < 0) matchDistance = 0;

        boolean[] s1Matches = new boolean[len1];
        boolean[] s2Matches = new boolean[len2];

        int matches = 0;
        for (int i = 0; i < len1; i++) {
            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, len2);

            for (int j = start; j < end; j++) {
                if (s2Matches[j]) continue;
                if (s1.charAt(i) != s2.charAt(j)) continue;
                s1Matches[i] = true;
                s2Matches[j] = true;
                matches++;
                break;
            }
        }

        if (matches == 0) return 0;

        int transpositions = 0;
        int k = 0;
        for (int i = 0; i < len1; i++) {
            if (!s1Matches[i]) continue;
            while (!s2Matches[k]) k++;
            if (s1.charAt(i) != s2.charAt(k)) transpositions++;
            k++;
        }

        double jaro = (((double) matches / len1) +
                ((double) matches / len2) +
                (((double) matches - transpositions / 2.0) / matches)) / 3.0;

        return (int) (jaro * 100);
    }
}
