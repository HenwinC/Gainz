package com.games.gobigorgohome;

import com.games.gobigorgohome.characters.NPC;
import com.games.gobigorgohome.parsers.ParseJSON;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class Room {
    private final ParseJSON jsonParser = new ParseJSON();

    private final String roomName;
    private final Object items;
    private final Object exercises;
    private final String map;

    private final String npc_type;
    private final List requiredItems;
    public NPC npc;
    private final Map roomMap = new Map();


    public Room(Object room) {
        this.roomName = jsonParser.getObjectStringFromJSONObject(room, "name");
        this.map = jsonParser.getObjectStringFromJSONObject(room, "map");
        this.items = jsonParser.getObjectFromJSONObject(room, "items");

        this.exercises = jsonParser.getObjectFromJSON(room, "exercises");

        Object npcTypeObject = jsonParser.getObjectFromJSONObject(room, "NPCS");
        this.npc_type = jsonParser.getStringValueFromIndexInJSONArray(npcTypeObject, 0);

        Object requiredItemsObject = jsonParser.getObjectFromJSONObject(room, "required items");
        this.requiredItems = jsonParser.getKeySetFromJSONArray(requiredItemsObject);
//        // System.out.println(this.requiredItems);

        if (!"none".equals(npc_type)) {
            try {
                this.npc = new NPC(npc_type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void getRoomMap(InputOutput prompter) throws IOException {

        roomMap.stringEditor(prompter, map);
    }

    public Object getItems() {
        return items;
    }

    public String getRoomName() {
        return roomName;
    }

    public Object getExercises() {
        return exercises;
    }

    //    TODO: determine the Set value types
    public Set getExerciseList() {
        return jsonParser.getKeySetFromJSONObject(getExercises());
    }

    public List getRequiredItems() {
        return requiredItems;
    }

    public String getNpc_type() {
        return npc_type;
    }

    public NPC getNpc() {
        return npc;
    }

    private String getValidNpc() {
        return getNpc() == null ? "No one" : getNpc().getNpcName();
    }

    @Override
    public String toString() {

        return Colors.YELLOW + "THIS IS WHAT YOU CAN SEE HERE: \n You are in " + Colors.RESET + getRoomName() + "\n" +
                "Exercises available are: " + getExerciseList() + "\n" +
                "You see: " + getItems() + "\n" +
                getValidNpc() + " is standing there with you.\n";
    }
}

