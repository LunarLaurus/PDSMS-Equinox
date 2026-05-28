package editor.buildingeditor2;

import editor.buildingeditor2.animations.ModelAnimation;
import editor.buildingeditor2.wb.*;
import editor.game.GameFileSystemB2W2;
import formats.narc2.Narc;
import formats.narc2.NarcFile;
import formats.narc2.NarcIO;
import utils.BinaryArrayReader;
import utils.BinaryReader;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author PlatinumMaster
 **/

public class BuildHandlerWB {
    private String gameFolderPath = "";
    private GameFileSystemB2W2 gameFileSystem;
    private WBBuildingList buildingList;
    private ArrayList<AB> extAB;
    private ArrayList<AB> intAB;
    private ArrayList<byte[]> extABTextures;
    private ArrayList<byte[]> intABTextures;

    public BuildHandlerWB(String gameFolderPath) {
        this.gameFolderPath = gameFolderPath;
        this.gameFileSystem = new GameFileSystemB2W2();
        extAB = new ArrayList<>();
        intAB = new ArrayList<>();
        extABTextures = new ArrayList<>();
        intABTextures = new ArrayList<>();
        buildingList = new WBBuildingList();
    }

    public WBBuildingList getBuildingList()
    {
        return this.buildingList;
    }

    public AB getExtAB(int number)
    {
        return this.extAB.get(number);
    }

    public AB getIntAB(int number)
    {
        return this.intAB.get(number);
    }

    public void loadAllFiles() throws Exception {
        try {
            Narc extBuildingData = NarcIO.loadNarc(getGameFilePath(gameFileSystem.exteriorBuildingPath));
            Narc intBuildingData = NarcIO.loadNarc(getGameFilePath(gameFileSystem.interiorBuildingPath));
            Narc extBuildingTextures = NarcIO.loadNarc(getGameFilePath(gameFileSystem.exteriorBuildingTilesets));
            Narc intBuildingTextures = NarcIO.loadNarc(getGameFilePath(gameFileSystem.interiorBuildingTilesets));
            for (NarcFile n : extBuildingData.getRoot().getFiles())
                extAB.add(new AB(new BinaryArrayReader(n.getData(), 0)));
            for (NarcFile n : intBuildingData.getRoot().getFiles())
                intAB.add(new AB(new BinaryArrayReader(n.getData(), 0)));
            for (NarcFile n : extBuildingTextures.getRoot().getFiles())
                extABTextures.add(n.getData());
            for (NarcFile n : intBuildingTextures.getRoot().getFiles())
                intABTextures.add(n.getData());

        } catch (Exception ex) {
            throw ex;
        }
    }

    public byte[] getExtABTextures(int num) {
        return extABTextures.get(num);
    }

    public byte[] getIntABTextures(int num) {
        return intABTextures.get(num);
    }

    public void loadBuildingData(Path path) throws Exception {
        buildingList = new WBBuildingList(path.toString());
    }

    public void setGameFolderPath(String path) {
        this.gameFolderPath = path;
    }

    public String getGameFolderPath() {
        return gameFolderPath;
    }

    protected String getGameFilePath(String relativePath) {
        return gameFolderPath + File.separator + relativePath;
    }

    public int getExtABCount() {
        return extAB.size();
    }

    public int getIntABCount() {
        return intAB.size();
    }
}