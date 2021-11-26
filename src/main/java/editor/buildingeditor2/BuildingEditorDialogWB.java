/*
 * Created by JFormDesigner on Wed Nov 25 01:11:17 EST 2020
 */

package editor.buildingeditor2;

import editor.buildingeditor2.animations.ModelAnimation;
import editor.buildingeditor2.wb.*;
import editor.handler.MapEditorHandler;
import net.miginfocom.swing.MigLayout;
import nitroreader.nsbca.NSBCA;
import nitroreader.nsbca.NSBCAreader;
import nitroreader.nsbta.NSBTA;
import nitroreader.nsbta.NSBTAreader;
import nitroreader.nsbtp.NSBTP;
import nitroreader.nsbtp.NSBTPreader;
import nitroreader.shared.ByteReader;
import nitroreader.shared.G3Dreader;
import renderer.NitroDisplayGL;
import renderer.ObjectGL;
import utils.Utils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.function.Function;

/**
 * @author PlatinumMaster
 */
public class BuildingEditorDialogWB extends JDialog {
    private MapEditorHandler handler;
    private BuildHandlerWB buildHandler;
    private AB currAB;
    private int currEntry;
    private ArrayList<ModelAnimation> animationList = new ArrayList<>();
    private Utils.MutableBoolean jlBuildModelEnabled = new Utils.MutableBoolean(true);
    private Utils.MutableBoolean buildPropertiesEnabled = new Utils.MutableBoolean(true);
    private Utils.MutableBoolean jlBuildFileEnabled = new Utils.MutableBoolean(true);
    private Utils.MutableBoolean jcbAnimationTypeEnabled = new Utils.MutableBoolean(true);

    public BuildingEditorDialogWB(Window owner) {
        super(owner);
        initComponents();
        nitroDisplayBuildingPosEditor.getObjectsGL().add(new ObjectGL());
        nitroDisplayBuildingEditor.getObjectsGL().add(new ObjectGL());
    }

    public void init(MapEditorHandler handler, BuildHandlerWB buildHandler) {
        this.handler = handler;
        this.buildHandler = buildHandler;
    }

    public void loadGame(String folderPath) {
        buildHandler.setGameFolderPath(folderPath);
        try {
            buildHandler.loadAllFiles();
            SwitchABType();
            nitroDisplayBuildingPosEditor.requestUpdate();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "There was a problem reading some of the files.",
                    "Error opening game files", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void fileOp(boolean saving, Function<File, Void> Actions, String FileType, FileNameExtensionFilter Filter) {
        final JFileChooser fc = new JFileChooser();
        if (handler.getLastMapDirectoryUsed() != null) {
            fc.setCurrentDirectory(new File(handler.getLastMapDirectoryUsed()));
        }
        fc.setFileFilter(Filter);
        fc.setApproveButtonText(saving ? "Save" : "Open");
        fc.setDialogTitle(String.format("%s a %s", saving ? "Save" : "Open", FileType));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                handler.setLastMapDirectoryUsed(fc.getSelectedFile().getParent());
                Actions.apply(fc.getSelectedFile());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, String.format("An error has occured.\n%s", ex.getMessage()),
                        "I/O Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void SwitchABType() {
        jsCurrAB.setValue(0);
        currAB = jcbModelsSelected.getSelectedIndex() == 0 ? buildHandler.getExtAB(0) : buildHandler.getIntAB(0);
        nitroDisplayBuildingEditor.getObjectGL(0).setNsbtxData(jcbModelsSelected.getSelectedIndex() == 0 ? buildHandler.getExtABTextures(0) : buildHandler.getIntABTextures(0));
        RefreshBuildingPack();
    }

    private void LoadAB() {
        if ((int)jsCurrAB.getValue() > -1) {
            if (jcbModelsSelected.getSelectedIndex() == 0 && buildHandler.getExtABCount() > (int)jsCurrAB.getValue()) {
                currAB = buildHandler.getExtAB((int)jsCurrAB.getValue());
                nitroDisplayBuildingEditor.getObjectGL(0).setNsbtxData(buildHandler.getExtABTextures((int)jsCurrAB.getValue()));
                RefreshBuildingPack();
            }
            else if (jcbModelsSelected.getSelectedIndex() == 1 && buildHandler.getIntABCount() > (int)jsCurrAB.getValue()) {
                currAB = buildHandler.getIntAB((int)jsCurrAB.getValue());
                nitroDisplayBuildingEditor.getObjectGL(0).setNsbtxData(buildHandler.getIntABTextures((int)jsCurrAB.getValue()));
                nitroDisplayBuildingPosEditor.getObjectGL(0).setNsbtxData(buildHandler.getIntABTextures((int)jsCurrAB.getValue()));
                RefreshBuildingPack();
            }
        }
    }

    private void OpenFile(String FileType, FileNameExtensionFilter Filter, Function<File, Void> Actions) {
        fileOp(false, Actions, FileType, Filter);
    }

    private void SaveFile(String FileType, FileNameExtensionFilter Filter, Function<File, Void> Actions) {
        fileOp(true, Actions, FileType, Filter);
    }

    private void UpdateModel(AB Data, JComboBox Mdl) {
        DefaultComboBoxModel<String> M = new DefaultComboBoxModel();
        for (int i = 0; i < Data.nModels(); ++i) {
            M.addElement(String.format("%d: %s", Data.getModelToID(i), Data.getModel(i).getName()));
        }
        Mdl.setModel(M);
    }

    private void UpdateModel(AB Data, JList Mdl) {
        DefaultListModel<String> M = new DefaultListModel<>();
        for (int i = 0; i < Data.nModels(); ++i) {
            M.addElement(String.format("%d: %s", Data.getModelToID(i), Data.getModel(i).getName()));
        }
        Mdl.setModel(M);
    }

    private void UpdateModel(WBBuildingList Data, JList Mdl) {
        DefaultListModel<String> M = new DefaultListModel<>();
        for (int i = 0; i < Data.size(); ++i) {
            M.addElement(String.format("%d: %s", Data.get(i).id, currAB.getModel(currAB.getIDToModel(Data.get(i).id)).getName()));
        }
        Mdl.setModel(M);
    }

    private void RefreshBuildingPack() {
        UpdateModel(currAB, jlBuildModel);
        UpdateModel(currAB, jcBuildID);
        UpdateNSBMD();
    }

    private void jbOpenMapActionPerformed(ActionEvent e) {
        OpenFile("Map", new FileNameExtensionFilter("NITRO System Binary Model Data (*.nsbmd)", "nsbmd"), (Data) -> {
            try {
                nitroDisplayBuildingPosEditor.getObjectGL(0).setNsbmdData(Files.readAllBytes(Data.toPath()));
                nitroDisplayBuildingPosEditor.requestUpdate();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return null;
        });
    }

    private void jbOpenBldActionPerformed(ActionEvent e) {
        OpenFile("Building Positions", new FileNameExtensionFilter("Building Positions (*.bld)", "bld"), (Data) -> {
            try {
                buildHandler.loadBuildingData(Data.toPath());
                UpdateBuildingList(0);
                RefreshNitroDisplay();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return null;
        });
    }

    private void UpdateBuildingList(int indexSelected) {
        jlBuildFileEnabled.value = false;
        UpdateModel(buildHandler.getBuildingList(), jlBuildFile);
        jlBuildFile.setSelectedIndex(indexSelected);
        jlBuildFile.ensureIndexIsVisible(indexSelected);
        jlBuildFileEnabled.value = true;
    }

    private void jcBuildIDStateChanged(ActionEvent e) {
        if (buildPropertiesEnabled.value) {
            buildHandler.getBuildingList().get(currEntry).id = currAB.getModelToID(jcBuildID.getSelectedIndex());
            RefreshNitroDisplay();
            UpdateBuildingList(jlBuildFile.getSelectedIndex());
        }
    }

    private void UpdateCoordinates(ChangeEvent e) {
        SpinnerNumberModel X = (SpinnerNumberModel) jsBuildX.getModel(),
                Y = (SpinnerNumberModel) jsBuildY.getModel(),
                Z = (SpinnerNumberModel) jsBuildZ.getModel(),
                Rotation = (SpinnerNumberModel) jsRotation.getModel();
        if (buildPropertiesEnabled.value) {
            buildHandler.getBuildingList().get(currEntry).coords = new FX32[]{
                    FX32.TryParse(X.getNumber().floatValue()),
                    FX32.TryParse(Z.getNumber().floatValue()),
                    FX32.TryParse(-Y.getNumber().floatValue()),
            };
            buildHandler.getBuildingList().get(currEntry).rotation = Rotation.getNumber().shortValue();
        }
        RefreshNitroDisplay();
    }

    private void jbExportBldActionPerformed(ActionEvent e) {
        SaveFile("Building Positions", new FileNameExtensionFilter("Building Positions (*.bld)", "bld"), (Data) -> {
            try {
                buildHandler.getBuildingList().Serialize(Data.toPath().toString());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return null;
        });
    }

    public void setBoundingBoxes() {
        for (ObjectGL object : nitroDisplayBuildingPosEditor.getObjectsGL()) {
            object.setDrawBounds(false);
        }

        try {
            nitroDisplayBuildingPosEditor.getObjectsGL().get(1 + jlBuildFile.getSelectedIndex()).setDrawBounds(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void jbAddBuildBldActionPerformed(ActionEvent e) {
        buildHandler.getBuildingList().add(new WBBuildingEntry(){
            {
                coords = new FX32[]{
                        new FX32(0x0),
                        new FX32(0x0),
                        new FX32(0x0)
                };
                id = 0x0;
                rotation = 0x0;
            }
        });
        UpdateBuildingList(buildHandler.getBuildingList().size() - 1);
        RefreshNitroDisplay();
    }

    private void jbRemoveBldActionPerformed(ActionEvent e) {
        if (buildHandler.getBuildingList().size() == 0) {
            JOptionPane.showMessageDialog(handler.getMainFrame(),
                    "There's nothing to remove.",
                    "You good bro?", JOptionPane.ERROR_MESSAGE);
            return;
        }
        buildHandler.getBuildingList().remove(jlBuildFile.getSelectedIndex());
        DefaultListModel m = (DefaultListModel) jlBuildFile.getModel();
        m.remove(jlBuildFile.getSelectedIndex());
        jlBuildFile.setModel(m);
        RefreshNitroDisplay();
        UpdateBuildingList(0);
    }

    public void RefreshNitroDisplay() {
        for (int i = 1, size = nitroDisplayBuildingPosEditor.getObjectsGL().size(); i < size; i++) {
            nitroDisplayBuildingPosEditor.getObjectsGL().remove(nitroDisplayBuildingPosEditor.getObjectsGL().size() - 1);
        }

        for (int i = 0; i < buildHandler.getBuildingList().size(); i++) {
            WBBuildingEntry e = buildHandler.getBuildingList().get(i);
            if (nitroDisplayBuildingPosEditor.getObjectsGL().size() < buildHandler.getBuildingList().size() + 1) {
                nitroDisplayBuildingPosEditor.getObjectsGL().add(new ObjectGL());
            }
            ObjectGL object = nitroDisplayBuildingPosEditor.getObjectGL(1 + i);
            try {
                object.setNsbmdData(currAB.getModel(currAB.getIDToModel(e.id)).getData());
                object.setNsbtxData(jcbModelsSelected.getSelectedIndex() == 0 ? buildHandler.getExtABTextures((int)jsCurrAB.getValue()) : buildHandler.getIntABTextures((int)jsCurrAB.getValue()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // Technically: X, Z, Y
            object.setX(e.coords[0].GetValueAsFloat() * 16f); // X (Left to Right)
            object.setY(e.coords[1].GetValueAsFloat() * 16f); // Z (Up and Down)
            object.setZ(-e.coords[2].GetValueAsFloat() * 16f); // Y (Forward and Backward)
            // Negate Y value for proper placement.
            nitroDisplayBuildingPosEditor.requestUpdate();
        }
        setBoundingBoxes();
    }

    private void jlBuildFileValueChanged(ListSelectionEvent e) {
        if (jlBuildFile.getSelectedIndex() > -1) {
            buildPropertiesEnabled.value = false;
            currEntry = jlBuildFile.getSelectedIndex();
            WBBuildingEntry entry = buildHandler.getBuildingList().get(currEntry);
            jlBuildFile.setSelectedIndex(currEntry);
            jsBuildX.setValue(entry.coords[0].GetValueAsFloat());
            jsBuildY.setValue(-entry.coords[2].GetValueAsFloat());
            jsBuildZ.setValue(entry.coords[1].GetValueAsFloat());
            jsRotation.setValue(entry.rotation);
            jcBuildID.setSelectedIndex(currAB.getIDToModel(entry.id));
            buildPropertiesEnabled.value = true;
        }
        RefreshNitroDisplay();
    }

    private void jlBuildModelValueChanged(ListSelectionEvent e) {
        if (jlBuildModelEnabled.value) {
            UpdateNSBMD();
            UpdateAnimationList(jlBuildModel.getSelectedIndex());
            nitroDisplayBuildingEditor.fitCameraToModel(0);
            nitroDisplayBuildingEditor.requestUpdate();
            jsControllerFunc.setValue(currAB.getABEntry(jlBuildModel.getSelectedIndex()).ControllerFunc);
            jsAnimPerSet.setValue(currAB.getABEntry(jlBuildModel.getSelectedIndex()).AnimCountPerAnimSet);
            jsAnimType.setValue(currAB.getABEntry(jlBuildModel.getSelectedIndex()).AnimCount);
        }
    }

    public void UpdateNSBMD() {
        if (jlBuildModel.getSelectedIndex() > -1) {
            nitroDisplayBuildingEditor.getObjectGL(0).setNsbmdData(currAB.getModel(jlBuildModel.getSelectedIndex()).getData());
            nitroDisplayBuildingEditor.getObjectGL(0).setNsbca(null);
            nitroDisplayBuildingEditor.getObjectGL(0).setNsbtp(null);
            nitroDisplayBuildingEditor.getObjectGL(0).setNsbta(null);
            nitroDisplayBuildingEditor.getObjectGL(0).setNsbva(null);
        }
    }

    private void UpdateAnimationList(int indexSelected) {
        jcbAnimationTypeEnabled.value = false;
        animationList.clear();
        ABEntry entry = currAB.getABEntryByID(currAB.getModelToID(indexSelected));
        DefaultListModel<String> m = new DefaultListModel<>();
        for (int i = 0; i < entry.numFiles(); ++i) {
            m.addElement(entry.getFile(i).getName());
            animationList.add(entry.getFile(i));
        }
        jlSelectedAnimationsList.setModel(m);
        jcbAnimationTypeEnabled.value = true;
    }

    private void jbAddBuildingActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbReplaceBuildingActionPerformed(ActionEvent e) {
        if (jlBuildModel.getSelectedIndex() > -1) {
            OpenFile("Building Model", new FileNameExtensionFilter("NSBMD (*.nsbmd)", "nsbmd"), (Data) -> {
                try {
                    currAB.replaceModel(jlBuildModel.getSelectedIndex(), new NitroModel(Files.readAllBytes(Data.toPath())));
                    RefreshBuildingPack();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                return null;
            });
        }
    }

    private void jbExportBuildingActionPerformed(ActionEvent e) {
        if (jlBuildModel.getSelectedIndex() > -1) {
            SaveFile("Building Model", new FileNameExtensionFilter("NSBMD (*.nsbmd)", "nsbmd"), (Data) -> {
                try (FileOutputStream fos = new FileOutputStream(Data)) {
                    fos.write(currAB.getModel(jlBuildModel.getSelectedIndex()).getData());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                return null;
            });
        }
    }

    private void jbRemoveBuildingActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbFindBuildingActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbAddMaterialActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbRemoveMaterialActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbImportMaterialsFromNsbmdActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbSetAnimationActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbMoveMaterialUpActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbMoveMaterialDownActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbAddAnimToBuildActionPerformed(ActionEvent e) {
        ABEntry curr;
        if (jlBuildModel.getSelectedIndex() > -1 && (curr = currAB.getABEntry(jlBuildModel.getSelectedIndex())).numFiles() < 4) {
            OpenFile("Animation File", new FileNameExtensionFilter("Animation File (*.nsbta, *.nsbtp, *.nsbca)", "nsbta, nsbtp, nsbca"), (Data) -> {
                try {
                    curr.addFile(new ModelAnimation(Files.readAllBytes(Data.toPath()), jlSelectedAnimationsList.getSelectedIndex()));
                    UpdateNSBMD();
                    UpdateAnimationList(jlBuildModel.getSelectedIndex());
                    RefreshBuildingPack();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return null;
            });
        }
    }

    private void jbReplaceAnimToBuildActionPerformed(ActionEvent e) {
        if (jlSelectedAnimationsList.getSelectedIndex() > -1) {
            OpenFile("Animation File", new FileNameExtensionFilter("Animation File (*.nsbta, *.nsbtp, *.nsbca)", "nsbta, nsbtp, nsbca"), (Data) -> {
                try {
                    currAB.getABEntry(jlBuildModel.getSelectedIndex()).replaceFile(jlSelectedAnimationsList.getSelectedIndex(),
                            new ModelAnimation(Files.readAllBytes(Data.toPath()),
                                    jlSelectedAnimationsList.getSelectedIndex())
                    );
                    UpdateNSBMD();
                    UpdateAnimationList(jlBuildModel.getSelectedIndex());
                    RefreshBuildingPack();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return null;
            });
        }
    }

    private void jbExportAnim(ActionEvent e) {
        if (jlSelectedAnimationsList.getSelectedIndex() > -1) {
            SaveFile("Animation", new FileNameExtensionFilter("Animation File (*.nsbta, *.nsbtp, *.nsbca)", "nsbta, nsbtp, nsbca"), (Data) -> {
                try (FileOutputStream fos = new FileOutputStream(Data)) {
                    fos.write(currAB.getABEntry(jlBuildModel.getSelectedIndex()).getFile(jlSelectedAnimationsList.getSelectedIndex()).getData());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                return null;
            });
        }
    }
    private void jbRemoveAnimToBuildActionPerformed(ActionEvent e) {
        if (jlSelectedAnimationsList.getSelectedIndex() > -1) {
            try {
                currAB.getABEntry(jlBuildModel.getSelectedIndex()).removeFile(jlSelectedAnimationsList.getSelectedIndex());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "There was a problem removing an entry.",
                        "Error writing model.", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportABButtonPressed(ActionEvent e) {
        if (currAB != null) {
            SaveFile("Building Pack", new FileNameExtensionFilter("Building Pack (*.ab)", "ab"), (Data) -> {
                try {
                    currAB.Serialize(Data.toPath().toString());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                return null;
            });
        }
    }

    private void jbPlayActionPerformed(ActionEvent e) {
        if (jlSelectedAnimationsList.getSelectedIndex() != -1) {
            LoadAnimation(nitroDisplayBuildingEditor, 0, animationList.get(jlSelectedAnimationsList.getSelectedIndex()));
        }
    }

    private void LoadAnimation(NitroDisplayGL display, int objectIndex, ModelAnimation anim) {
        System.out.println(anim.getName());
        G3Dreader reader;
        switch (anim.getAnimationType()) {
            case ModelAnimation.TYPE_NSBCA:
                reader = new NSBCAreader(new ByteReader(anim.getData()));
                display.getObjectGL(objectIndex).setNsbca((NSBCA) reader.readFile());
                break;
            case ModelAnimation.TYPE_NSBTA:
                reader = new NSBTAreader(new ByteReader(anim.getData()));
                display.getObjectGL(objectIndex).setNsbta((NSBTA) reader.readFile());
                break;
            case ModelAnimation.TYPE_NSBTP:
                reader = new NSBTPreader(new ByteReader(anim.getData()));
                display.getObjectGL(objectIndex).setNsbtp((NSBTP) reader.readFile());
                break;
        }
        display.requestUpdate();
    }

    private void jcbModelsSelectedActionPerformed(ActionEvent e) {
        SwitchABType();
    }

    private void jlBuildTsetListValueChanged(ListSelectionEvent e) {
        // TODO add your code here
    }

    private void jbAddTsetActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbAddEmptyTilesetActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbReplaceTsetActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbExportTilesetActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbRemoveTsetActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jlAreaBuildListValueChanged(ListSelectionEvent e) {
        // TODO add your code here
    }

    private void jbAddBuildToTsetActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbReplaceBuildToTsetActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbRemoveBuildToTsetActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbAddTexToNsbtxActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbRemoveTexturesActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbRemoveAllUnusedTexPalsActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void button1ActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbSaveAllActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jbCancelActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void jsCurrABStateChanged(ChangeEvent e) {
        LoadAB();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Tungsten
        jTabbedPane1 = new JTabbedPane();
        jPanel3 = new JPanel();
        jPanel1 = new JPanel();
        jLabel4 = new JLabel();
        nitroDisplayBuildingEditor = new NitroDisplayGL();
        jScrollPane1 = new JScrollPane();
        jlBuildModel = new JList<>();
        panel2 = new JPanel();
        jbAddBuilding = new JButton();
        jbReplaceBuilding = new JButton();
        jbExportBuilding = new JButton();
        jbRemoveBuilding = new JButton();
        jbFindBuilding = new JButton();
        button1 = new JButton();
        panel3 = new JPanel();
        jPanel8 = new JPanel();
        jLabel3 = new JLabel();
        jScrollPane3 = new JScrollPane();
        jlSelectedAnimationsList = new JList<>();
        panel5 = new JPanel();
        jbPlay = new JButton();
        jbAddAnimToBuild = new JButton();
        jbRemoveAnimToBuild = new JButton();
        jbReplaceAnimToBuild = new JButton();
        jbPlay2 = new JButton();
        scrollPane1 = new JScrollPane();
        panel6 = new JPanel();
        jLabel12 = new JLabel();
        jsControllerFunc = new JSpinner();
        jLabel25 = new JLabel();
        jsAnimPerSet = new JSpinner();
        jLabel23 = new JLabel();
        jsAnimType = new JSpinner();
        jPanel13 = new JPanel();
        jPanel14 = new JPanel();
        nitroDisplayBuildingPosEditor = new NitroDisplayGL();
        jbOpenMap = new JButton();
        jPanel15 = new JPanel();
        jPanel17 = new JPanel();
        jPanel18 = new JPanel();
        jbImportBld = new JButton();
        jbExportBld = new JButton();
        jPanel19 = new JPanel();
        jbAddBuildBld = new JButton();
        jbRemoveBld = new JButton();
        jScrollPane8 = new JScrollPane();
        jlBuildFile = new JList<>();
        jPanel16 = new JPanel();
        jLabel13 = new JLabel();
        jcBuildID = new JComboBox();
        jLabel14 = new JLabel();
        jsBuildX = new JSpinner();
        jLabel16 = new JLabel();
        jsBuildY = new JSpinner();
        jLabel15 = new JLabel();
        jsBuildZ = new JSpinner();
        jLabel17 = new JLabel();
        jsRotation = new JSpinner();
        jPanel7 = new JPanel();
        jPanel10 = new JPanel();
        jScrollPane6 = new JScrollPane();
        jlBuildTsetList = new JList<>();
        jLabel10 = new JLabel();
        panel8 = new JPanel();
        jbAddTset = new JButton();
        jbAddEmptyTileset = new JButton();
        jbReplaceTset = new JButton();
        jbExportTileset = new JButton();
        jbRemoveTset = new JButton();
        jPanel11 = new JPanel();
        jLabel9 = new JLabel();
        jScrollPane7 = new JScrollPane();
        jlAreaBuildList = new JList<>();
        panel9 = new JPanel();
        jbAddBuildToTset = new JButton();
        jbReplaceBuildToTset = new JButton();
        jbRemoveBuildToTset = new JButton();
        jbAddTexToNsbtx = new JButton();
        jbRemoveTextures = new JButton();
        jbRemoveAllUnusedTexPals = new JButton();
        nitroDisplayAreaData = new NitroDisplayGL();
        panel1 = new JPanel();
        jLabel21 = new JLabel();
        jcbModelsSelected = new JComboBox<>();
        jsCurrAB = new JSpinner();
        jbSaveAll = new JButton();
        jbCancel = new JButton();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Building Editor for Generation V (Experimental)");
        setModal(true);
        var contentPane = getContentPane();
        contentPane.setLayout(new MigLayout(
            "insets 0,hidemode 3,gap 5 5",
            // columns
            "[grow,fill]",
            // rows
            "[grow,fill]" +
            "[]"));

        //======== jTabbedPane1 ========
        {

            //======== jPanel3 ========
            {
                jPanel3.setBorder ( new javax . swing. border .CompoundBorder ( new javax . swing. border .TitledBorder ( new
                javax . swing. border .EmptyBorder ( 0, 0 ,0 , 0) ,  "JF\u006frmDes\u0069gner \u0045valua\u0074ion" , javax
                . swing .border . TitledBorder. CENTER ,javax . swing. border .TitledBorder . BOTTOM, new java
                . awt .Font ( "D\u0069alog", java .awt . Font. BOLD ,12 ) ,java . awt
                . Color .red ) ,jPanel3. getBorder () ) ); jPanel3. addPropertyChangeListener( new java. beans .
                PropertyChangeListener ( ){ @Override public void propertyChange (java . beans. PropertyChangeEvent e) { if( "\u0062order" .
                equals ( e. getPropertyName () ) )throw new RuntimeException( ) ;} } );
                jPanel3.setLayout(new MigLayout(
                    "insets 5,hidemode 3,gap 5 5",
                    // columns
                    "[grow,fill]" +
                    "[grow,fill]",
                    // rows
                    "[grow,fill]"));

                //======== jPanel1 ========
                {
                    jPanel1.setBorder(new TitledBorder("Building Selector (build_model.narc)"));
                    jPanel1.setLayout(new MigLayout(
                        "insets 5,hidemode 3,gap 5 5",
                        // columns
                        "[462,grow,fill]" +
                        "[164,fill]" +
                        "[fill]",
                        // rows
                        "[fill]" +
                        "[grow,fill]"));

                    //---- jLabel4 ----
                    jLabel4.setIcon(new ImageIcon(getClass().getResource("/icons/BuildingIcon.png")));
                    jLabel4.setText("Building List:");
                    jLabel4.setToolTipText("");
                    jPanel1.add(jLabel4, "cell 1 0");

                    //======== nitroDisplayBuildingEditor ========
                    {
                        nitroDisplayBuildingEditor.setBorder(new LineBorder(new Color(102, 102, 102)));

                        GroupLayout nitroDisplayBuildingEditorLayout = new GroupLayout(nitroDisplayBuildingEditor);
                        nitroDisplayBuildingEditor.setLayout(nitroDisplayBuildingEditorLayout);
                        nitroDisplayBuildingEditorLayout.setHorizontalGroup(
                            nitroDisplayBuildingEditorLayout.createParallelGroup()
                                .addGap(0, 656, Short.MAX_VALUE)
                        );
                        nitroDisplayBuildingEditorLayout.setVerticalGroup(
                            nitroDisplayBuildingEditorLayout.createParallelGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                        );
                    }
                    jPanel1.add(nitroDisplayBuildingEditor, "cell 0 0 1 2");

                    //======== jScrollPane1 ========
                    {
                        jScrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                        jScrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

                        //---- jlBuildModel ----
                        jlBuildModel.setModel(new AbstractListModel<String>() {
                            String[] values = {

                            };
                            @Override
                            public int getSize() { return values.length; }
                            @Override
                            public String getElementAt(int i) { return values[i]; }
                        });
                        jlBuildModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        jlBuildModel.addListSelectionListener(e -> jlBuildModelValueChanged(e));
                        jScrollPane1.setViewportView(jlBuildModel);
                    }
                    jPanel1.add(jScrollPane1, "cell 1 1");

                    //======== panel2 ========
                    {
                        panel2.setLayout(new GridBagLayout());
                        ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0};
                        ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
                        ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
                        ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

                        //---- jbAddBuilding ----
                        jbAddBuilding.setIcon(new ImageIcon(getClass().getResource("/icons/AddIcon.png")));
                        jbAddBuilding.setText("Add Building");
                        jbAddBuilding.setHorizontalAlignment(SwingConstants.LEFT);
                        jbAddBuilding.addActionListener(e -> jbAddBuildingActionPerformed(e));
                        panel2.add(jbAddBuilding, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- jbReplaceBuilding ----
                        jbReplaceBuilding.setIcon(new ImageIcon(getClass().getResource("/icons/ReplaceIcon.png")));
                        jbReplaceBuilding.setText("Replace Building");
                        jbReplaceBuilding.setHorizontalAlignment(SwingConstants.LEFT);
                        jbReplaceBuilding.addActionListener(e -> jbReplaceBuildingActionPerformed(e));
                        panel2.add(jbReplaceBuilding, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- jbExportBuilding ----
                        jbExportBuilding.setIcon(new ImageIcon(getClass().getResource("/icons/ExportIcon.png")));
                        jbExportBuilding.setText("Export Building");
                        jbExportBuilding.setHorizontalAlignment(SwingConstants.LEFT);
                        jbExportBuilding.addActionListener(e -> jbExportBuildingActionPerformed(e));
                        panel2.add(jbExportBuilding, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- jbRemoveBuilding ----
                        jbRemoveBuilding.setIcon(new ImageIcon(getClass().getResource("/icons/RemoveIcon.png")));
                        jbRemoveBuilding.setText("Remove Building");
                        jbRemoveBuilding.setEnabled(false);
                        jbRemoveBuilding.setHorizontalAlignment(SwingConstants.LEFT);
                        jbRemoveBuilding.addActionListener(e -> jbRemoveBuildingActionPerformed(e));
                        panel2.add(jbRemoveBuilding, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- jbFindBuilding ----
                        jbFindBuilding.setIcon(new ImageIcon(getClass().getResource("/icons/SearchIcon.png")));
                        jbFindBuilding.setText("Find Usages");
                        jbFindBuilding.setHorizontalAlignment(SwingConstants.LEFT);
                        jbFindBuilding.addActionListener(e -> jbFindBuildingActionPerformed(e));
                        panel2.add(jbFindBuilding, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- button1 ----
                        button1.setText("Export Building Pack");
                        button1.addActionListener(e -> button1ActionPerformed(e));
                        panel2.add(button1, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 0), 0, 0));
                    }
                    jPanel1.add(panel2, "cell 2 1");
                }
                jPanel3.add(jPanel1, "cell 0 0");

                //======== panel3 ========
                {
                    panel3.setLayout(new MigLayout(
                        "hidemode 3",
                        // columns
                        "[grow,fill]",
                        // rows
                        "[grow,fill]" +
                        "[grow,fill]"));

                    //======== jPanel8 ========
                    {
                        jPanel8.setBorder(new TitledBorder("Selected Building Animations (bm_anime_list.narc)"));
                        jPanel8.setLayout(new MigLayout(
                            "insets 05 5 5 5,hidemode 3,gap 5 5",
                            // columns
                            "[156,grow,fill]" +
                            "[fill]",
                            // rows
                            "[fill]" +
                            "[grow,fill]" +
                            "[fill]"));

                        //---- jLabel3 ----
                        jLabel3.setIcon(new ImageIcon(getClass().getResource("/icons/AnimationIcon.png")));
                        jLabel3.setText("Animations:");
                        jLabel3.setToolTipText("");
                        jPanel8.add(jLabel3, "cell 0 0 2 1");

                        //======== jScrollPane3 ========
                        {
                            jScrollPane3.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                            jScrollPane3.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

                            //---- jlSelectedAnimationsList ----
                            jlSelectedAnimationsList.setModel(new AbstractListModel<String>() {
                                String[] values = {

                                };
                                @Override
                                public int getSize() { return values.length; }
                                @Override
                                public String getElementAt(int i) { return values[i]; }
                            });
                            jlSelectedAnimationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                            jScrollPane3.setViewportView(jlSelectedAnimationsList);
                        }
                        jPanel8.add(jScrollPane3, "cell 0 1");

                        //======== panel5 ========
                        {
                            panel5.setLayout(new GridBagLayout());
                            ((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0};
                            ((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                            ((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
                            ((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

                            //---- jbPlay ----
                            jbPlay.setIcon(new ImageIcon(getClass().getResource("/icons/AnimationIcon.png")));
                            jbPlay.setText("Play Animation");
                            jbPlay.setHorizontalAlignment(SwingConstants.LEFT);
                            jbPlay.addActionListener(e -> jbPlayActionPerformed(e));
                            panel5.add(jbPlay, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                new Insets(0, 0, 5, 0), 0, 0));

                            //---- jbAddAnimToBuild ----
                            jbAddAnimToBuild.setIcon(new ImageIcon(getClass().getResource("/icons/AddIcon.png")));
                            jbAddAnimToBuild.setText("Add Animation");
                            jbAddAnimToBuild.setHorizontalAlignment(SwingConstants.LEFT);
                            jbAddAnimToBuild.addActionListener(e -> jbAddAnimToBuildActionPerformed(e));
                            panel5.add(jbAddAnimToBuild, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                new Insets(0, 0, 5, 0), 0, 0));

                            //---- jbRemoveAnimToBuild ----
                            jbRemoveAnimToBuild.setIcon(new ImageIcon(getClass().getResource("/icons/RemoveIcon.png")));
                            jbRemoveAnimToBuild.setText("Remove Animation");
                            jbRemoveAnimToBuild.setHorizontalAlignment(SwingConstants.LEFT);
                            jbRemoveAnimToBuild.addActionListener(e -> jbRemoveAnimToBuildActionPerformed(e));
                            panel5.add(jbRemoveAnimToBuild, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                new Insets(0, 0, 5, 0), 0, 0));

                            //---- jbReplaceAnimToBuild ----
                            jbReplaceAnimToBuild.setIcon(new ImageIcon(getClass().getResource("/icons/ReplaceIcon.png")));
                            jbReplaceAnimToBuild.setText("Replace Animation");
                            jbReplaceAnimToBuild.setHorizontalAlignment(SwingConstants.LEFT);
                            jbReplaceAnimToBuild.addActionListener(e -> jbReplaceAnimToBuildActionPerformed(e));
                            panel5.add(jbReplaceAnimToBuild, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                new Insets(0, 0, 5, 0), 0, 0));

                            //---- jbPlay2 ----
                            jbPlay2.setIcon(new ImageIcon(getClass().getResource("/icons/ExportIcon.png")));
                            jbPlay2.setText("Export Animation");
                            jbPlay2.setHorizontalAlignment(SwingConstants.LEFT);
                            jbPlay2.addActionListener(e -> jbPlayActionPerformed(e));
                            panel5.add(jbPlay2, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                new Insets(0, 0, 5, 0), 0, 0));
                        }
                        jPanel8.add(panel5, "cell 1 1");

                        //======== scrollPane1 ========
                        {

                            //======== panel6 ========
                            {
                                panel6.setLayout(new MigLayout(
                                    "hidemode 3",
                                    // columns
                                    "[fill]" +
                                    "[112,grow,fill]" +
                                    "[fill]" +
                                    "[grow,fill]",
                                    // rows
                                    "[]" +
                                    "[]" +
                                    "[]"));

                                //---- jLabel12 ----
                                jLabel12.setText("Controller Function");
                                panel6.add(jLabel12, "cell 0 0");
                                panel6.add(jsControllerFunc, "cell 1 0");

                                //---- jLabel25 ----
                                jLabel25.setText("Animations per set");
                                panel6.add(jLabel25, "cell 0 1");
                                panel6.add(jsAnimPerSet, "cell 1 1");

                                //---- jLabel23 ----
                                jLabel23.setText("Type");
                                panel6.add(jLabel23, "cell 0 2");
                                panel6.add(jsAnimType, "cell 1 2");
                            }
                            scrollPane1.setViewportView(panel6);
                        }
                        jPanel8.add(scrollPane1, "cell 0 2");
                    }
                    panel3.add(jPanel8, "cell 0 0 1 2");
                }
                jPanel3.add(panel3, "cell 1 0");
            }
            jTabbedPane1.addTab("Building Pack Editor", jPanel3);

            //======== jPanel13 ========
            {
                jPanel13.setLayout(new GridLayout());

                //======== jPanel14 ========
                {
                    jPanel14.setBorder(new TitledBorder("Map Display"));
                    jPanel14.setLayout(new MigLayout(
                        "insets 0,hidemode 3,gap 5 5",
                        // columns
                        "[fill]" +
                        "[grow,fill]",
                        // rows
                        "[fill]" +
                        "[grow,fill]"));

                    //======== nitroDisplayBuildingPosEditor ========
                    {
                        nitroDisplayBuildingPosEditor.setBorder(new LineBorder(new Color(102, 102, 102)));
                        nitroDisplayBuildingPosEditor.setLayout(new BoxLayout(nitroDisplayBuildingPosEditor, BoxLayout.X_AXIS));
                    }
                    jPanel14.add(nitroDisplayBuildingPosEditor, "cell 0 1 2 1");

                    //---- jbOpenMap ----
                    jbOpenMap.setIcon(new ImageIcon(getClass().getResource("/icons/ImportTileIcon.png")));
                    jbOpenMap.setText("Open Map");
                    jbOpenMap.addActionListener(e -> jbOpenMapActionPerformed(e));
                    jPanel14.add(jbOpenMap, "cell 0 0");
                }
                jPanel13.add(jPanel14);

                //======== jPanel15 ========
                {
                    jPanel15.setBorder(new TitledBorder("Building List Editor"));
                    jPanel15.setLayout(new MigLayout(
                        "insets 0,hidemode 3,gap 0 5",
                        // columns
                        "[grow,fill]" +
                        "[fill]",
                        // rows
                        "[fill]" +
                        "[grow,fill]"));

                    //======== jPanel17 ========
                    {
                        jPanel17.setBorder(new TitledBorder("Building File"));
                        jPanel17.setLayout(new GridLayout(2, 2));

                        //======== jPanel18 ========
                        {
                            jPanel18.setLayout(new GridLayout(1, 0, 5, 0));

                            //---- jbImportBld ----
                            jbImportBld.setIcon(new ImageIcon(getClass().getResource("/icons/ImportTileIcon.png")));
                            jbImportBld.setText("Import BLD File");
                            jbImportBld.setToolTipText("");
                            jbImportBld.addActionListener(e -> jbOpenBldActionPerformed(e));
                            jPanel18.add(jbImportBld);

                            //---- jbExportBld ----
                            jbExportBld.setIcon(new ImageIcon(getClass().getResource("/icons/ExportIcon.png")));
                            jbExportBld.setText("Export BLD File");
                            jbExportBld.addActionListener(e -> jbExportBldActionPerformed(e));
                            jPanel18.add(jbExportBld);
                        }
                        jPanel17.add(jPanel18);

                        //======== jPanel19 ========
                        {
                            jPanel19.setLayout(new GridLayout(1, 0, 5, 0));

                            //---- jbAddBuildBld ----
                            jbAddBuildBld.setIcon(new ImageIcon(getClass().getResource("/icons/AddIcon.png")));
                            jbAddBuildBld.setText("Add Building");
                            jbAddBuildBld.setToolTipText("");
                            jbAddBuildBld.addActionListener(e -> jbAddBuildBldActionPerformed(e));
                            jPanel19.add(jbAddBuildBld);

                            //---- jbRemoveBld ----
                            jbRemoveBld.setIcon(new ImageIcon(getClass().getResource("/icons/RemoveIcon.png")));
                            jbRemoveBld.setText("Remove Building");
                            jbRemoveBld.addActionListener(e -> jbRemoveBldActionPerformed(e));
                            jPanel19.add(jbRemoveBld);
                        }
                        jPanel17.add(jPanel19);
                    }
                    jPanel15.add(jPanel17, "cell 0 0 2 1");

                    //======== jScrollPane8 ========
                    {
                        jScrollPane8.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                        jScrollPane8.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                        jScrollPane8.setViewportBorder(new TitledBorder("Building List"));

                        //---- jlBuildFile ----
                        jlBuildFile.setModel(new AbstractListModel<String>() {
                            String[] values = {

                            };
                            @Override
                            public int getSize() { return values.length; }
                            @Override
                            public String getElementAt(int i) { return values[i]; }
                        });
                        jlBuildFile.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        jlBuildFile.setBorder(new TitledBorder("text"));
                        jlBuildFile.addListSelectionListener(e -> jlBuildFileValueChanged(e));
                        jScrollPane8.setViewportView(jlBuildFile);
                    }
                    jPanel15.add(jScrollPane8, "cell 0 0 2 1");

                    //======== jPanel16 ========
                    {
                        jPanel16.setBorder(new TitledBorder("Properties of the Selected Building"));
                        jPanel16.setLayout(new MigLayout(
                            "insets 0,hidemode 3,gap 5 5",
                            // columns
                            "[fill]" +
                            "[grow,fill]" +
                            "[fill]" +
                            "[grow,fill]",
                            // rows
                            "[fill]" +
                            "[fill]" +
                            "[fill]" +
                            "[fill]" +
                            "[fill]" +
                            "[]"));

                        //---- jLabel13 ----
                        jLabel13.setText("Building ID:");
                        jPanel16.add(jLabel13, "cell 0 0");

                        //---- jcBuildID ----
                        jcBuildID.addActionListener(e -> jcBuildIDStateChanged(e));
                        jPanel16.add(jcBuildID, "cell 1 0");

                        //---- jLabel14 ----
                        jLabel14.setForeground(new Color(204, 0, 0));
                        jLabel14.setText("X (Left and Right): ");
                        jPanel16.add(jLabel14, "cell 0 1");

                        //---- jsBuildX ----
                        jsBuildX.setModel(new SpinnerNumberModel(0.0, -16.0, 16.0, 0.5));
                        jsBuildX.addChangeListener(e -> UpdateCoordinates(e));
                        jPanel16.add(jsBuildX, "cell 1 1");

                        //---- jLabel16 ----
                        jLabel16.setForeground(new Color(0, 0, 204));
                        jLabel16.setText("Y (Forwards and Backwards):");
                        jPanel16.add(jLabel16, "cell 0 2");

                        //---- jsBuildY ----
                        jsBuildY.setModel(new SpinnerNumberModel(0.0, -16.0, 16.0, 0.5));
                        jsBuildY.addChangeListener(e -> UpdateCoordinates(e));
                        jPanel16.add(jsBuildY, "cell 1 2");

                        //---- jLabel15 ----
                        jLabel15.setForeground(new Color(51, 153, 0));
                        jLabel15.setText("Z (Up and Down) ");
                        jPanel16.add(jLabel15, "cell 0 3");

                        //---- jsBuildZ ----
                        jsBuildZ.setModel(new SpinnerNumberModel(0.0, -16.0, 16.0, 0.5));
                        jsBuildZ.addChangeListener(e -> UpdateCoordinates(e));
                        jPanel16.add(jsBuildZ, "cell 1 3");

                        //---- jLabel17 ----
                        jLabel17.setForeground(new Color(51, 51, 255));
                        jLabel17.setText("Rotation");
                        jPanel16.add(jLabel17, "cell 0 4");

                        //---- jsRotation ----
                        jsRotation.setModel(new SpinnerNumberModel(0, null, null, 0));
                        jsRotation.addChangeListener(e -> UpdateCoordinates(e));
                        jPanel16.add(jsRotation, "cell 1 4");
                    }
                    jPanel15.add(jPanel16, "cell 0 1");
                }
                jPanel13.add(jPanel15);
            }
            jTabbedPane1.addTab("Map Buildings Editor", jPanel13);

            //======== jPanel7 ========
            {
                jPanel7.setLayout(new MigLayout(
                    "insets 05 5 5 5,hidemode 3,gap 5 5",
                    // columns
                    "[grow,fill]" +
                    "[grow,fill]",
                    // rows
                    "[grow,fill]"));

                //======== jPanel10 ========
                {
                    jPanel10.setBorder(new TitledBorder("Building Tileset Selector (areabm_texset.narc)"));
                    jPanel10.setLayout(new MigLayout(
                        "insets 5,hidemode 3,gap 5 5",
                        // columns
                        "[368,fill]" +
                        "[353,grow,fill]",
                        // rows
                        "[fill]" +
                        "[grow,fill]"));

                    //======== jScrollPane6 ========
                    {
                        jScrollPane6.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                        jScrollPane6.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

                        //---- jlBuildTsetList ----
                        jlBuildTsetList.setModel(new AbstractListModel<String>() {
                            String[] values = {

                            };
                            @Override
                            public int getSize() { return values.length; }
                            @Override
                            public String getElementAt(int i) { return values[i]; }
                        });
                        jlBuildTsetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        jlBuildTsetList.addListSelectionListener(e -> jlBuildTsetListValueChanged(e));
                        jScrollPane6.setViewportView(jlBuildTsetList);
                    }
                    jPanel10.add(jScrollPane6, "cell 1 1");

                    //---- jLabel10 ----
                    jLabel10.setIcon(new ImageIcon(getClass().getResource("/icons/MaterialIcon.png")));
                    jLabel10.setText("Building Tileset List:");
                    jPanel10.add(jLabel10, "cell 1 0");

                    //======== panel8 ========
                    {
                        panel8.setLayout(new GridBagLayout());
                        ((GridBagLayout)panel8.getLayout()).columnWidths = new int[] {0, 0};
                        ((GridBagLayout)panel8.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
                        ((GridBagLayout)panel8.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
                        ((GridBagLayout)panel8.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

                        //---- jbAddTset ----
                        jbAddTset.setIcon(new ImageIcon(getClass().getResource("/icons/AddIcon.png")));
                        jbAddTset.setText("Add Tileset");
                        jbAddTset.setHorizontalAlignment(SwingConstants.LEFT);
                        jbAddTset.addActionListener(e -> jbAddTsetActionPerformed(e));
                        panel8.add(jbAddTset, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- jbAddEmptyTileset ----
                        jbAddEmptyTileset.setIcon(new ImageIcon(getClass().getResource("/icons/AddIcon.png")));
                        jbAddEmptyTileset.setText("Add Empty Tileset");
                        jbAddEmptyTileset.setHorizontalAlignment(SwingConstants.LEFT);
                        jbAddEmptyTileset.addActionListener(e -> jbAddEmptyTilesetActionPerformed(e));
                        panel8.add(jbAddEmptyTileset, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- jbReplaceTset ----
                        jbReplaceTset.setIcon(new ImageIcon(getClass().getResource("/icons/ReplaceIcon.png")));
                        jbReplaceTset.setText("Replace Tileset");
                        jbReplaceTset.setHorizontalAlignment(SwingConstants.LEFT);
                        jbReplaceTset.addActionListener(e -> jbReplaceTsetActionPerformed(e));
                        panel8.add(jbReplaceTset, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- jbExportTileset ----
                        jbExportTileset.setIcon(new ImageIcon(getClass().getResource("/icons/ExportIcon.png")));
                        jbExportTileset.setText("Export Tileset");
                        jbExportTileset.setHorizontalAlignment(SwingConstants.LEFT);
                        jbExportTileset.addActionListener(e -> jbExportTilesetActionPerformed(e));
                        panel8.add(jbExportTileset, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- jbRemoveTset ----
                        jbRemoveTset.setIcon(new ImageIcon(getClass().getResource("/icons/RemoveIcon.png")));
                        jbRemoveTset.setText("Remove Tileset");
                        jbRemoveTset.setEnabled(false);
                        jbRemoveTset.setHorizontalAlignment(SwingConstants.LEFT);
                        jbRemoveTset.addActionListener(e -> jbRemoveTsetActionPerformed(e));
                        panel8.add(jbRemoveTset, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 0), 0, 0));
                    }
                    jPanel10.add(panel8, "cell 1 1");
                }
                jPanel7.add(jPanel10, "cell 0 0");

                //======== jPanel11 ========
                {
                    jPanel11.setBorder(new TitledBorder("Building Tileset Properties (area_build.narc)"));
                    jPanel11.setLayout(new MigLayout(
                        "insets 05 5 5 5,hidemode 3,gap 5 5",
                        // columns
                        "[196,grow,fill]" +
                        "[340,grow,fill]",
                        // rows
                        "[fill]" +
                        "[fill]" +
                        "[grow,fill]"));

                    //---- jLabel9 ----
                    jLabel9.setIcon(new ImageIcon(getClass().getResource("/icons/BuildingIcon.png")));
                    jLabel9.setText("Buildings used by the Tileset:");
                    jPanel11.add(jLabel9, "cell 0 0 2 1");

                    //======== jScrollPane7 ========
                    {
                        jScrollPane7.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                        jScrollPane7.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

                        //---- jlAreaBuildList ----
                        jlAreaBuildList.setModel(new AbstractListModel<String>() {
                            String[] values = {

                            };
                            @Override
                            public int getSize() { return values.length; }
                            @Override
                            public String getElementAt(int i) { return values[i]; }
                        });
                        jlAreaBuildList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        jlAreaBuildList.addListSelectionListener(e -> jlAreaBuildListValueChanged(e));
                        jScrollPane7.setViewportView(jlAreaBuildList);
                    }
                    jPanel11.add(jScrollPane7, "cell 0 1 1 2");

                    //======== panel9 ========
                    {
                        panel9.setLayout(new GridBagLayout());
                        ((GridBagLayout)panel9.getLayout()).columnWidths = new int[] {0, 0};
                        ((GridBagLayout)panel9.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
                        ((GridBagLayout)panel9.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
                        ((GridBagLayout)panel9.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

                        //---- jbAddBuildToTset ----
                        jbAddBuildToTset.setIcon(new ImageIcon(getClass().getResource("/icons/AddIcon.png")));
                        jbAddBuildToTset.setText("Add Building");
                        jbAddBuildToTset.setHorizontalAlignment(SwingConstants.LEFT);
                        jbAddBuildToTset.addActionListener(e -> jbAddBuildToTsetActionPerformed(e));
                        panel9.add(jbAddBuildToTset, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- jbReplaceBuildToTset ----
                        jbReplaceBuildToTset.setIcon(new ImageIcon(getClass().getResource("/icons/ReplaceIcon.png")));
                        jbReplaceBuildToTset.setText("Replace Building");
                        jbReplaceBuildToTset.setHorizontalAlignment(SwingConstants.LEFT);
                        jbReplaceBuildToTset.addActionListener(e -> jbReplaceBuildToTsetActionPerformed(e));
                        panel9.add(jbReplaceBuildToTset, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- jbRemoveBuildToTset ----
                        jbRemoveBuildToTset.setIcon(new ImageIcon(getClass().getResource("/icons/RemoveIcon.png")));
                        jbRemoveBuildToTset.setText("Remove Building");
                        jbRemoveBuildToTset.setHorizontalAlignment(SwingConstants.LEFT);
                        jbRemoveBuildToTset.addActionListener(e -> jbRemoveBuildToTsetActionPerformed(e));
                        panel9.add(jbRemoveBuildToTset, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- jbAddTexToNsbtx ----
                        jbAddTexToNsbtx.setIcon(new ImageIcon(getClass().getResource("/icons/AddIcon.png")));
                        jbAddTexToNsbtx.setText("Add Texs & Pals to NSBTX");
                        jbAddTexToNsbtx.setHorizontalAlignment(SwingConstants.LEFT);
                        jbAddTexToNsbtx.addActionListener(e -> jbAddTexToNsbtxActionPerformed(e));
                        panel9.add(jbAddTexToNsbtx, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- jbRemoveTextures ----
                        jbRemoveTextures.setIcon(new ImageIcon(getClass().getResource("/icons/RemoveIcon.png")));
                        jbRemoveTextures.setText("Remove Tex & Pals from NSBTX");
                        jbRemoveTextures.setToolTipText("");
                        jbRemoveTextures.setHorizontalAlignment(SwingConstants.LEFT);
                        jbRemoveTextures.addActionListener(e -> jbRemoveTexturesActionPerformed(e));
                        panel9.add(jbRemoveTextures, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- jbRemoveAllUnusedTexPals ----
                        jbRemoveAllUnusedTexPals.setIcon(new ImageIcon(getClass().getResource("/icons/RemoveIcon.png")));
                        jbRemoveAllUnusedTexPals.setText("Removed All Unused Tex & Pals");
                        jbRemoveAllUnusedTexPals.setHorizontalAlignment(SwingConstants.LEFT);
                        jbRemoveAllUnusedTexPals.addActionListener(e -> jbRemoveAllUnusedTexPalsActionPerformed(e));
                        panel9.add(jbRemoveAllUnusedTexPals, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 0), 0, 0));
                    }
                    jPanel11.add(panel9, "cell 1 1");

                    //======== nitroDisplayAreaData ========
                    {
                        nitroDisplayAreaData.setBorder(new LineBorder(new Color(102, 102, 102)));

                        GroupLayout nitroDisplayAreaDataLayout = new GroupLayout(nitroDisplayAreaData);
                        nitroDisplayAreaData.setLayout(nitroDisplayAreaDataLayout);
                        nitroDisplayAreaDataLayout.setHorizontalGroup(
                            nitroDisplayAreaDataLayout.createParallelGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                        );
                        nitroDisplayAreaDataLayout.setVerticalGroup(
                            nitroDisplayAreaDataLayout.createParallelGroup()
                                .addGap(0, 322, Short.MAX_VALUE)
                        );
                    }
                    jPanel11.add(nitroDisplayAreaData, "cell 1 2");
                }
                jPanel7.add(jPanel11, "cell 1 0");
            }
            jTabbedPane1.addTab("Building Tileset Editor", jPanel7);
        }
        contentPane.add(jTabbedPane1, "cell 0 0");

        //======== panel1 ========
        {
            panel1.setLayout(new MigLayout(
                "insets 0,hidemode 3,gap 5 5",
                // columns
                "[364:n,grow,fill]" +
                "[fill]" +
                "[600,fill]" +
                "[fill]" +
                "[fill]",
                // rows
                "[fill]"));

            //---- jLabel21 ----
            jLabel21.setText("Models Selected:");
            panel1.add(jLabel21, "");

            //---- jcbModelsSelected ----
            jcbModelsSelected.setModel(new DefaultComboBoxModel<>(new String[] {
                "Outdoor Models",
                "Indoor Models"
            }));
            jcbModelsSelected.addActionListener(e -> jcbModelsSelectedActionPerformed(e));
            panel1.add(jcbModelsSelected, "cell 0 0");

            //---- jsCurrAB ----
            jsCurrAB.setModel(new SpinnerNumberModel(0, 0, null, 1));
            jsCurrAB.addChangeListener(e -> jsCurrABStateChanged(e));
            panel1.add(jsCurrAB, "cell 1 0 2 1");

            //---- jbSaveAll ----
            jbSaveAll.setText("Save All");
            jbSaveAll.setMaximumSize(null);
            jbSaveAll.setMinimumSize(null);
            jbSaveAll.setPreferredSize(new Dimension(100, 30));
            jbSaveAll.setIcon(new ImageIcon(getClass().getResource("/icons/saveMapIconSmall.png")));
            jbSaveAll.addActionListener(e -> jbSaveAllActionPerformed(e));
            panel1.add(jbSaveAll, "cell 3 0");

            //---- jbCancel ----
            jbCancel.setText("Close");
            jbCancel.setPreferredSize(new Dimension(100, 30));
            jbCancel.addActionListener(e -> jbCancelActionPerformed(e));
            panel1.add(jbCancel, "cell 4 0");
        }
        contentPane.add(panel1, "cell 0 1,gapx 5 5,gapy 5 5");
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Tungsten
    private JTabbedPane jTabbedPane1;
    private JPanel jPanel3;
    private JPanel jPanel1;
    private JLabel jLabel4;
    private NitroDisplayGL nitroDisplayBuildingEditor;
    private JScrollPane jScrollPane1;
    private JList<String> jlBuildModel;
    private JPanel panel2;
    private JButton jbAddBuilding;
    private JButton jbReplaceBuilding;
    private JButton jbExportBuilding;
    private JButton jbRemoveBuilding;
    private JButton jbFindBuilding;
    private JButton button1;
    private JPanel panel3;
    private JPanel jPanel8;
    private JLabel jLabel3;
    private JScrollPane jScrollPane3;
    private JList<String> jlSelectedAnimationsList;
    private JPanel panel5;
    private JButton jbPlay;
    private JButton jbAddAnimToBuild;
    private JButton jbRemoveAnimToBuild;
    private JButton jbReplaceAnimToBuild;
    private JButton jbPlay2;
    private JScrollPane scrollPane1;
    private JPanel panel6;
    private JLabel jLabel12;
    private JSpinner jsControllerFunc;
    private JLabel jLabel25;
    private JSpinner jsAnimPerSet;
    private JLabel jLabel23;
    private JSpinner jsAnimType;
    private JPanel jPanel13;
    private JPanel jPanel14;
    private NitroDisplayGL nitroDisplayBuildingPosEditor;
    private JButton jbOpenMap;
    private JPanel jPanel15;
    private JPanel jPanel17;
    private JPanel jPanel18;
    private JButton jbImportBld;
    private JButton jbExportBld;
    private JPanel jPanel19;
    private JButton jbAddBuildBld;
    private JButton jbRemoveBld;
    private JScrollPane jScrollPane8;
    private JList<String> jlBuildFile;
    private JPanel jPanel16;
    private JLabel jLabel13;
    private JComboBox jcBuildID;
    private JLabel jLabel14;
    private JSpinner jsBuildX;
    private JLabel jLabel16;
    private JSpinner jsBuildY;
    private JLabel jLabel15;
    private JSpinner jsBuildZ;
    private JLabel jLabel17;
    private JSpinner jsRotation;
    private JPanel jPanel7;
    private JPanel jPanel10;
    private JScrollPane jScrollPane6;
    private JList<String> jlBuildTsetList;
    private JLabel jLabel10;
    private JPanel panel8;
    private JButton jbAddTset;
    private JButton jbAddEmptyTileset;
    private JButton jbReplaceTset;
    private JButton jbExportTileset;
    private JButton jbRemoveTset;
    private JPanel jPanel11;
    private JLabel jLabel9;
    private JScrollPane jScrollPane7;
    private JList<String> jlAreaBuildList;
    private JPanel panel9;
    private JButton jbAddBuildToTset;
    private JButton jbReplaceBuildToTset;
    private JButton jbRemoveBuildToTset;
    private JButton jbAddTexToNsbtx;
    private JButton jbRemoveTextures;
    private JButton jbRemoveAllUnusedTexPals;
    private NitroDisplayGL nitroDisplayAreaData;
    private JPanel panel1;
    private JLabel jLabel21;
    private JComboBox<String> jcbModelsSelected;
    private JSpinner jsCurrAB;
    private JButton jbSaveAll;
    private JButton jbCancel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
