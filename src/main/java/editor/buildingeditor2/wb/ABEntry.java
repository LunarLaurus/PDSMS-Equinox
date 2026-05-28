package editor.buildingeditor2.wb;

import editor.buildingeditor2.animations.ModelAnimation;

import java.util.ArrayList;
import java.util.List;

public class ABEntry {
    public short ID;
    public short Type;
    public short DoorID;
    public short X;
    public short Y;
    public short Z;
    public short Unused;
    public short Unused2;
    public short ControllerFunc;
    public byte AnimCountPerAnimSet;
    public byte ItemCount;
    private List<ModelAnimation> files = new ArrayList<>();

    public long size() {
        int fileSize = 0;
        for (ModelAnimation ma : files)
            fileSize += ma.getData().length;
        return 0x24 + fileSize;
    }

    public int numFiles() {
        return files.size();
    }

    public ModelAnimation getFile(int index) {
        return files.get(index);
    }

    public void addFile(ModelAnimation file) throws Exception {
        if (files.size() == 4)
            throw new Exception("The maximum number of files is 4.");
        files.add(file);
    }

    public void removeFile(int index) throws Exception {
        if (files.size() == 0)
            throw new Exception("There are no files in this entry.");
        if (index > 4 || index < 0)
            throw new Exception("Not a valid index.");
        files.remove(index);
    }

    public void replaceFile(int selectedIndex, ModelAnimation modelAnimation) throws Exception {
        if (selectedIndex > numFiles()) {
            throw new Exception("Invalid index.");
        }
        files.set(selectedIndex, modelAnimation);
    }
}
