package com.games.gobigorgohome;

class Map {
    // read .txt files in resource folder
//    map is displaying well but in the console re factor
    //TO DO: make map to show in the GUI
//    public void dataFromFile(String filePath) throws IOException {
//
//        InputStream stream = ParseTxt.class.getClassLoader().getResourceAsStream(filePath);
//        if (stream == null) {
//            throw new IllegalArgumentException("File Not Found");
//        }
//        List<String> lines = new ArrayList<String>();
//        BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream));
//        String line = null;
//        while ((line = streamReader.readLine()) != null) {
//            lines.add(line);
//            // System.out.println(line);
//        }
//    }

//    public void stringEditor(String currentRoomName) throws IOException {
//        String[] stringArray = currentRoomName.split(" ");
//        StringJoiner joiner = new StringJoiner("_");
//        for (String s : stringArray) {
//            joiner.add(s);
//        }
//        String roomName = joiner.toString();
//        String filePath = "map/" + roomName + "_map.txt";
//
//        dataFromFile(filePath);
//    }

    //Gui version
    public void stringEditor(InputOutput prompter, String currentRoomName) {

        prompter.info("<img src=\"" + currentRoomName + "\" '/>");
    }
}