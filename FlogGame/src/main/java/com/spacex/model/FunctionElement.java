/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spacex.model;

import com.spacex.floggame.FlogElement;
import com.spacex.floggame.database.DBControl;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Tharindu Gihan
 */
public class FunctionElement extends FlogElement {

    //NOTE: Generate random vowel
    public char getRandomVowel() {
        String EVowel = "AEIOU";
        String[] parts = EVowel.split("");
        int rand = (int) (Math.random() * parts.length);
        char randomVowel;
        randomVowel = EVowel.charAt(rand);
        return randomVowel;
    }

    //NOTE: Generate random constant
    public char getRandomConstant() {
        String EConstant = "BCDFGHJKLMNPQRSTVXZWY";
        String[] parts = EConstant.split("");
        int rand = (int) (Math.random() * parts.length);
        char randomConstant;
        randomConstant = EConstant.charAt(rand);

        return randomConstant;
    }

    //NOTE: Generate two default letters by using above methods
    public ArrayList getDefaultLetters() {
        ArrayList<Character> defaultLetters = new ArrayList<>();

        char charone = getRandomConstant();
        char chartwo = getRandomConstant();

        if (charone == chartwo) {
            charone = getRandomConstant();
        }

        defaultLetters.add(charone);
        defaultLetters.add(chartwo);

        return defaultLetters;
    }

    //NOTE: Calculate scrabble point value by using constant element and letter value classes
    public int calculateScrabblePoints(String longestWord) throws URISyntaxException {
        int total = 0;
        boolean verifyWord = checkWord(longestWord);

        if (verifyWord) {
            String upperScrabbleWord = longestWord.toUpperCase();
            LetterValueElement letterValueElement = new LetterValueElement();
            for (int i = 0; i < upperScrabbleWord.length(); i++) {
                char calculatedLetter = upperScrabbleWord.charAt(i);
                int value = letterValueElement.getValue(calculatedLetter);
                total = total + value;
            }

        } else {
            System.out.println("Word is invalid");
        }

        return total;

    }

    //NOTE: Check word spelling validation
    public boolean checkWord(String word) throws URISyntaxException {

        try {
            InputStream ins = getClass().getResourceAsStream("/dictionary/dictionary.dic");
            BufferedReader in = new BufferedReader(new InputStreamReader(ins));
            String str;
            while ((str = in.readLine()) != null) {
                if (str.contains(word)) {
                    return true;
                }
            }
            in.close();
        } catch (IOException e) {

        }

        return false;
    }
    
    //NOTE: Player should not be able to repeat letter
    public boolean Findrepeter(String word) {
        String s = word;
        int distinct = 0;
        boolean found = false;

        for (int i = 0; i < s.length(); i++) {

            for (int j = 0; j < s.length(); j++) {

                if (s.charAt(i) == s.charAt(j)) {
                    distinct++;

                }
            }

            if (distinct > 1) {
                found = true;
            }
            System.out.println(s.charAt(i) + "--" + distinct);
            String d = String.valueOf(s.charAt(i)).trim();
            s = s.replaceAll(d, "");
            distinct = 0;

        }

        return found;

    }
    
    //NOTE: Punishing weakest link
    public int punishing(int winnerScore, int myscore, String player, int gameid) throws SQLException {
//        if (myscore <= 0) {
//            return 0;
//        } else {

            int newScore = 0;
            int diff = winnerScore - myscore;
            System.out.println("punish round winner score " + winnerScore);
            System.out.println(player + " punish score " + myscore);

            int deduction = diff / 2;

            if (deduction > myscore) {
                DBControl dbc = new DBControl();
                int pastScore = dbc.getPastScore(gameid, player);
                newScore = pastScore - deduction;
                System.out.println("hello deduction two");
            } else {
                newScore = myscore - deduction;
                System.out.println("hello deduction one");

            }
            if (newScore < 0) {
                newScore = 0;
            }

            System.out.println("deducted " + deduction + " from " + player);
            return newScore;

        }
//    }

}
