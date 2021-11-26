package editor.buildingeditor2.wb;

import editor.buildingeditor2.animations.ModelAnimation;
import utils.BinaryArrayReader;
import utils.BinaryWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AB {
    public short magic;
    public int fileSize;
    private ArrayList<ABEntry> ABEntries;
    private ArrayList<NitroModel> Models;
    private Map<Short, Integer> IDLookupTable;
    private Map<Integer, Short> ModelToID;

    public AB()
    {
        ABEntries = new ArrayList<>();
        Models = new ArrayList<>();
        IDLookupTable = new HashMap<>();
    }

    public AB(BinaryArrayReader reader) throws Exception {
        this();
        Map<Short, Integer> ModelLookupTable = new HashMap<>();
        ArrayList<Short> IDs = new ArrayList<>();

        magic = (short) reader.readUInt16();
        if (magic != 0x4241)
            throw new Exception("Invalid AB file!");

        short nFiles = (short) reader.readUInt16();
        ArrayList<Integer> offsets = new ArrayList<>();

        for (int i = 0; i < nFiles; i++)
            offsets.add((int) reader.readUInt32());

        int fileSize = (int) reader.readUInt32();

        for (int i = 0; i < nFiles / 2; i++) {
            ArrayList<Integer> fileOffsets = new ArrayList<>();
            int startABEntrySection = offsets.get(i) + 0x14;
            reader.jumpAbs(offsets.get(i));
            ABEntry e = new ABEntry() {{
                ID = (short) reader.readUInt16();
                Type = (short) reader.readUInt16();
                DoorID = (short) reader.readUInt16();
                X = (short) reader.readUInt16();
                Y = (short) reader.readUInt16();
                Z = (short) reader.readUInt16();
                Unused = (short) reader.readUInt16();
                Unused2 = (short) reader.readUInt16();
                ControllerFunc = (short) reader.readUInt16();
                AnimCountPerAnimSet = (byte) reader.readUInt8();
                AnimCount = (byte) reader.readUInt8();
            }};
            IDs.add(e.ID);

            while (reader.peekUInt32() != -1 && fileOffsets.size() < 4)
                fileOffsets.add(startABEntrySection + (int) reader.readUInt32());

            for (int j = 0; j < fileOffsets.size(); j++)
            {
                reader.jumpAbs(fileOffsets.get(j) + 0x4);
                int subFileSize = (int) reader.peekUInt32();
                reader.jumpAbs(fileOffsets.get(j) - 0x4);
                e.addFile(new ModelAnimation(reader.readBytes(subFileSize), 0));
            }
            ABEntries.add(e);
        }

        for (int i = nFiles / 2; i < nFiles; i++)
        {
            ModelLookupTable.put(IDs.get(nFiles - i - 1), nFiles - i - 1);
            reader.jumpAbs(offsets.get(i) + 0x8);
            int subFileSize = (int) reader.peekUInt32();
            reader.jumpAbs(offsets.get(i));
            addModel(new NitroModel(reader.readBytes(subFileSize)));
        }
        setIDLookupTable(ModelLookupTable);
    }

    public void add(ABEntry newEntry)
    {
        ABEntries.add(newEntry);
    }

    public void remove(int index)
    {
        ABEntries.remove(index);
    }

    public void addModel(NitroModel model)
    {
        Models.add(model);
    }

    public void replaceModel(int index, NitroModel model) {
        Models.set(index, model);
    }

    public void setIDLookupTable(Map<Short, Integer> IDLookupTable)
    {
        this.IDLookupTable = IDLookupTable;
        ModelToID = InvertKV(IDLookupTable);
    }

    public static <V, K> Map<V, K> InvertKV(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    public ABEntry getABEntry(int id) {
        return ABEntries.get(id);
    }

    public ABEntry getABEntryByID(int id) {
        for (ABEntry e : ABEntries)
            if (e.ID == id)
                return e;
        return ABEntries.get(0);
    }

    public int getIDToModel(short ID)
    {
        return IDLookupTable.get(ID) == null ? -1 : IDLookupTable.get(ID);
    }

    public int nModels()
    {
        return this.Models.size();
    }

    public short getModelToID(int ID)
    {
        return ModelToID.get(ID);
    }

    public NitroModel getModel(int index)
    {
        return this.Models.get(index);
    }

    public void Serialize(String output) throws IOException {
        BinaryWriter b = new BinaryWriter(output);
        int base = 0x8 + 0x4 * (ABEntries.size() + Models.size());
        b.writeUInt16(0x4241); // Magic
        b.writeUInt16(ABEntries.size() + Models.size());

        // Determine the offsets.
        for (int i = 0; i < ABEntries.size(); ++i) {
            b.writeUInt32(base);
            base += ABEntries.get(i).size();
        }

        for (int i = 0; i < Models.size(); ++i) {
            b.writeUInt32(base);
            base += Models.get(i).getData().length;
        }

        // Write fileSize
        b.writeUInt32(base);

        // Write AB entries.
        for (int i = 0; i < ABEntries.size(); ++i) {
            // Write the header.
            ABEntry entry = ABEntries.get(i);
            b.writeUInt16(entry.ID);
            b.writeUInt16(entry.Type);
            b.writeUInt16(entry.DoorID);
            b.writeUInt16(entry.X);
            b.writeUInt16(entry.Y);
            b.writeUInt16(entry.Z);
            b.writeUInt16(entry.Unused);
            b.writeUInt16(entry.Unused2);
            b.writeUInt16(entry.ControllerFunc);
            b.writeUInt8(entry.AnimCountPerAnimSet);
            b.writeUInt8(entry.AnimCount);

            // Write the file header.
            int ABFileBase = 0x14;
            for (int j = 0; j < entry.numFiles(); ++j) {
                b.writeUInt32(ABFileBase);
                ABFileBase += entry.getFile(j).getData().length;
            }
            for (int j = 4 - entry.numFiles(); j > 0; j--)
                b.writeUInt32(-1);

            // Write the files.
            for (int j = 0; j < entry.numFiles(); j++) {
                b.writeBytes(entry.getFile(j).getData());
            }
        }

        // Write the models.
        for (NitroModel nsbmd : Models) {
            b.writeBytes(nsbmd.getData());
        }
        b.close();
        // Done.
    }
}
