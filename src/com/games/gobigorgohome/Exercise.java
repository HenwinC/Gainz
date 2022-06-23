package com.games.gobigorgohome;

import com.games.gobigorgohome.parsers.ParseJSON;

public class Exercise {
    ParseJSON jsonParser = new ParseJSON();

    private final Object targetMuscles;
    private final String exerciseStatus;
    private final Long energyCost;
    private final Long MET;



    public Exercise(Object exercises, String exerciseName) {
        Object exercise = jsonParser.getObjectFromJSON(exercises, exerciseName);
        this.targetMuscles = jsonParser.getObjectFromJSONObject(exercise, "target muscles");
        this.exerciseStatus = jsonParser.getObjectStringFromJSONObject(exercise, "status");
        this.energyCost = jsonParser.getLongFromJSONObject(exercise, "energy cost");
        this.MET = jsonParser.getLongFromJSONObject(exercise,"MET");
    }

    public Object getTargetMuscles() {
        return targetMuscles;
    }

    public String getExerciseStatus() {
        return exerciseStatus;
    }

    public Long getEnergyCost() {
        return energyCost;
    }

    public Long getMET() {
        return MET;
    }
}