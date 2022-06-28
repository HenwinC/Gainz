package com.games.gobigorgohome.app;

import com.games.gobigorgohome.characters.Player;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameTest {
    List requiredItems = new ArrayList();
    List noRequiredItems = new ArrayList();
    Game game = new Game();


    @Before
    public void createRequiredItemList() {
        requiredItems.add("wrench");
    }

    @Before
    public void createNoRequiredItemList() {
        noRequiredItems.add("none");
    }

//    @Test
//    public void isItemRequiredShouldReturnFalseIfNoItemsAreRequired() {
//        assertFalse(Game.isItemRequired(noRequiredItems));
//    }


//    @Test
//    public void isItemRequiredShouldReturnTrueIfItemsAreRequired() {
//        assertTrue(Game.isItemRequired(requiredItems));
//    }
    @Test


    public void checkGameStatus_shouldReturnFalse_whenPlayerDoesNotHaveWiningCondition(){
        assertFalse(game.checkGameStatus());
    }
//    @Test
//    public void checkGameStatus_shouldReturnTrue_whenPlayerHasWiningCondition(){
//
//        assertTrue(game.checkGameStatus());
//    }
}