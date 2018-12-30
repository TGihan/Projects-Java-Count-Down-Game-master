package com.spacex.floggame.model;

import java.util.ArrayList;

public class Game {

    public int gameId;
    public boolean winner;

    public ArrayList<String> gameUsers = new ArrayList<>();

    public Game() {
        
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public boolean isWinner() {
        return winner;
    }

    public void setWinner(boolean winner) {
        this.winner = winner;
    }

    public ArrayList<String> getGameUsers() {
        return gameUsers;
    }

    public void setGameUsers(ArrayList<String> gameUsers) {
        this.gameUsers = gameUsers;
    }

    public Game getGameData(int gameid) {
        if (gameid == this.gameId) {
            return this;
        }
        return null;

    }

}
