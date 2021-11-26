package editor.buildingeditor2.wb;
import utils.BinaryReader;
import utils.BinaryWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class WBBuildingList {
    private ArrayList<WBBuildingEntry> buildings;

    public WBBuildingList()
    {
        buildings = new ArrayList<>();
    }

    public WBBuildingList(String path) throws Exception {
        this();
        BinaryReader reader = new BinaryReader(path);
        if (reader.size() == 0)
            throw new Exception("Invalid BLD!");

        int count = (int) reader.readUInt32();
        for (int i = 0; i < count; i++) {
            add(new WBBuildingEntry() {{
                coords = new FX32[] {
                    new FX32((int) reader.readUInt32()),
                    new FX32((int) reader.readUInt32()),
                    new FX32((int) reader.readUInt32())
                };
                rotation = (short) reader.readUInt16();
                id = (short) ((reader.readUInt8() << 0x8) + reader.readUInt8());
            }});
        }
    }

    public void add(WBBuildingEntry newEntry)
    {
        buildings.add(newEntry);
    }

    public void add(int index, WBBuildingEntry newEntry)
    {
        buildings.add(index, newEntry);
    }

    public void remove(int index)
    {
        buildings.remove(index);
    }

    public WBBuildingEntry get(int index)
    {
        return buildings.get(index);
    }

    public int size()
    {
        return this.buildings.size();
    }

    public void Serialize(String Path) throws IOException {
        BinaryWriter Binary = new BinaryWriter(Path);
        Binary.writeUInt32(size()); // Write the number of entries
        for (int i = 0; i < size(); i++)
        {
            Binary.writeUInt32(buildings.get(i).coords[0].GetValue());
            Binary.writeUInt32(buildings.get(i).coords[1].GetValue());
            Binary.writeUInt32(buildings.get(i).coords[2].GetValue());
            Binary.writeUInt16(buildings.get(i).rotation);
            Binary.writeUInt16((buildings.get(i).id << 0x8) | ((buildings.get(i).id >> 0x8) & 0xFF));
        }
    }
}
