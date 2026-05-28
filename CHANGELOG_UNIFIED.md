# Pokemon-DS-Map-Studio Unified Changelog

**Version: 2.2.2-unified**  
**Date:** May 28, 2026  
**Base:** Trifindo/Pokemon-DS-Map-Studio v2.2 + Community Enhancements

---

## Overview

This unified fork consolidates **94 commits** from two community forks:
- **AdAstra-LD** (68 commits) - Primary enhancement fork
- **PlatinumMaster** (26 commits) - Gen 5 Building Editor

---

## Major Features

### From AdAstra-LD

#### Version 2.2.2 Enhancements
- **Java 21 Support** - Upgraded from Java 8, modernized build configuration
- **Export Groups System** - Complete overhaul with map centering, visualization, and batch operations
- **NSBVA Support** - Animation structure support (playback WIP)
- **BDHCAM Editor Improvements** - Camera settings restoration, plate duplication, widecam mode
- **Material Management** - Move material to top/bottom, Z-offset for tiles
- **Layer Shifting** - Up/down layer manipulation
- **GUI Modernization** - Lower resolution display support, refactored MainFrame delegation

#### Bugfixes (AdAstra-LD)
- Fixed indexOutOfBounds exception in export groups
- Fixed null pointer exceptions when opening BLD editor
- Fixed PDSMAP splitter not working when map at (0,0) is null
- Fixed BDHC plate slope copying and shift-up operations
- Fixed BDHCAM display issues
- Fixed "replace only palette" button in NSBTX editor
- Fixed duplicated dot in exportgroups naming
- Fixed animation editor frame selection UI
- Fixed CopyIcon naming and Soundplates DuplicatePlate code

#### Refactoring (AdAstra-LD)
- `BuildAnimations` → `GlobalAnimationsList` (renamed and optimized)
- MainFrame refactored to delegate actions to modules
- Utils, state handling, and thumbnails refactored
- Export group logic refactored into MapMatrix

---

### From PlatinumMaster

#### Generation V Building Editor (Black/White)
- **AB Parser Implementation** - Full support for Gen 5 building format
- **Building Position Editor** - Functional editor for BW buildings
- **Building Replacement** - Add, remove, and modify buildings
- **Building ID Management** - ComboBox-based ID selection with bounding boxes
- **Animation Export Hooks** - Integration with animation system
- **Controller Function Support** - Full hook-up for Gen 5 features

#### Infrastructure (PlatinumMaster)
- `BuildHandlerWB` - Gen 5 building handler
- `AB.java`, `ABEntry.java` - AB format parsing
- `WBBuildingList.java`, `WBBuildingEntry.java` - Building list management
- `NitroModel.java`, `FX32.java` - Model and fixed-point math utilities
- `AreaData.java` - Area data handling for Gen 5

---

## File Summary

### New Files Added

| Category | Files | Lines Added |
|----------|-------|-------------|
| Gen 5 Building Editor | 7 | ~419 |
| AdAstra-LD Enhancements | 15+ | ~5,000+ |
| **Total** | **22+** | **~5,400+** |

### Key New Files

```
src/main/java/editor/buildingeditor2/wb/
  - AB.java
  - ABEntry.java
  - AreaData.java
  - FX32.java
  - NitroModel.java
  - WBBuildingEntry.java
  - WBBuildingList.java

src/main/java/editor/
  - ExportGroupCenterCheckBox.java
  - MainFrameBusyRunner.java
  - MainFrameContext.java
  - MainFrameViewUpdater.java
  - MapEditActions.java
  - MapExportActions.java
  - MapProjectActions.java
  - RecentMapsMenu.java
  - RecentMapsStore.java
  - ToolDialogLauncher.java

src/main/java/editor/mapgroups/
  - MapGroup.java
  - SavePDSMAPAreasDialog.java
  - VisualizeExportGroupsDialog.java

src/main/java/formats/obj/
  - ExportMapsObjDialog.java
  - ExportSingleMapObjDialog.java
```

---

## Complete Commit List

### Merge Commits
| Hash | Date | Message |
|------|------|---------|
| 968feb1 | 2026-05-28 | Merge PlatinumMaster Gen 5 Building Editor (AB parser, BW building support) |
| 73549a2 | 2026-05-28 | Merge AdAstra-LD enhancements (v2.2.2, Java 21, Export Groups, NSBVA) |

### AdAstra-LD Commits (Selected)
| Hash | Date | Message |
|------|------|---------|
| 8f4b13f | 2026-05-15 | Reworked Export groups and GUI |
| e1b0c6e | 2026-05-15 | Refactor export group logic into MapMatrix |
| 0a9c7cc | 2025-11-09 | Update Java version requirement to 21 |
| debee95 | 2026-04-30 | Version update [2.2.2] |
| 2fd36fd | 2022-08-16 | Added NSBVA support [no playback] |
| b8b148a | 2022-01-16 | Refactored BuildAnimations → GlobalAnimationsList |
| a6c1a3b | 2021-06-04 | Added tile Z offset feature |
| 67f455f | 2021-03-21 | Improved widecam mode choice and OBJ Export |

### PlatinumMaster Commits (Selected)
| Hash | Date | Message |
|------|------|---------|
| 0c7a6c8 | 2022-03-03 | Update README.md |
| c8dea4b | 2021-11-26 | Fix AB property editing (thanks, Hello007!) |
| 7c27daf | 2021-11-26 | Allow AB property editing |
| acb6504 | 2020-11-25 | Begin implementing AB parser, and BuildHandlerWB |
| 3898afb | 2021-01-03 | Functional Generation V building position editor |
| 5f377c6 | 2021-05-30 | Implemented Generation V building replacement, clean up |
| 9132394 | 2020-11-23 | Start work on BW Building Editor |

---

## Build Requirements

- **Java 21** (upgraded from Java 8)
- **Gradle** (wrapper included)
- **JOGL** libraries (included in `libs/`)

---

## Running

```bash
# Windows
gradlew.bat run

# Linux/macOS
./gradlew run
```

---

## Credits

- **Original Project**: Trifindo/Pokemon-DS-Map-Studio
- **AdAstra-LD Enhancements**: AdAstra-LD
- **Gen 5 Building Editor**: PlatinumMaster
- **Pokemon Resort Integration**: pentaenix
- **Unified Fork**: Consolidated by Fleet Admiral Lauren
