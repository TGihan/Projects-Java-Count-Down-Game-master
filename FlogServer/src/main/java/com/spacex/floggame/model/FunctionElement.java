/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spacex.floggame.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 *
 * @author Tharindu Gihan
 */
public class FunctionElement {

    // get random vowel
    public char getRandomVowel() {
        String EVowel = "AEIOU";
        String[] parts = EVowel.split("");
        int rand = (int) (Math.random() * parts.length);
        char randomVowel;
        randomVowel = EVowel.charAt(rand);
        return randomVowel;
    }

    public char getRandomConstant() {
        String EConstant = "BCDFGHJKLMNPQRSTVXZWY";
        String[] parts = EConstant.split("");
        int rand = (int) (Math.random() * parts.length);
        char randomConstant;
        randomConstant = EConstant.charAt(rand);

        return randomConstant;
    }

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

    public boolean checkWord(String word) throws URISyntaxException {

        if (word == null) {
            return false;
        } else {

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
    }

    public int punishing(int winnerScore, int myscore, String player) {
        if (myscore <= 0) {
            return 0;
        } else {

            int diff = winnerScore - myscore;
            System.out.println("punish round winner score " + winnerScore);
            System.out.println(player + " punish score " + myscore);

            int deduction = diff / 2;
            int newScore = myscore - deduction;
            if (newScore < 0) {
                newScore = 0;
            }
            System.out.println("deducted " + deduction + " from " + player);
            return newScore;

        }
    }

}
