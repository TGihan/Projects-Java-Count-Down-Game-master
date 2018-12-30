/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spacex.model;

import com.spacex.floggame.FlogElement;

/**
 *
 * @author Tharindu Gihan
 */
public class LetterValueElement extends FlogElement{
    
    //NOTE: Get scrabble value for a letter by using constant element class constants
    public int getValue(char calculatedLetter) {
		//get letter values by using constant arrays which are in constant element class
		int[] alphabetScore=ConstantElement.alphabetScore;
		char[] possibleChars=ConstantElement.possibleChars;
		
		int value=0;
		for (int j = 0; j < possibleChars.length; j++) {
            if (possibleChars[j] == calculatedLetter) {
                for (int k = 0; k < alphabetScore.length; k++) {
                    if (k == j) {                           
                       value= alphabetScore[k];
                    }
                }
            }
        }
		return value;
	}
    
}
