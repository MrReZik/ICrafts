# 🛠️ iCrafts: In-Game Custom Recipe Manager

**Create, edit, and manage custom crafting, smelting, and anvil recipes with intuitive GUI editors.**

[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![SpigotMC](https://img.shields.io/badge/SpigotMC-1.17+-green.svg)](https://www.spigotmc.org/resources/icrafts)
[![Java](https://img.shields.io/badge/Java-17-red.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

---

## ✨ Core Features

The iCrafts plugin offers a comprehensive, GUI-based system to define and manage custom recipes without ever touching a YAML file for recipe configuration.

* **GUI-Driven Recipe Creation:** Full support for creating and editing recipes directly in-game.
* **Multi-Format Support:** Define recipes for all major crafting stations:
    * **Workbench:** Shaped 3x3 recipes.
    * **Furnace:** Smelting recipes (also works for Smoker and Blast Furnace).
    * **Anvil:** Combination recipes with custom **XP cost control**.
* **Live Recipe Preview:** Use the `VIEW` mode to show players the exact ingredients required for a custom item.
* **Flexible Storage:** All custom recipes are stored in a dedicated `data.yml` file, separate from general configuration and messages.
* **Layout Customization:** Customize the look, size, and decoration of the GUI editors via separate YAML files in the `menus/` folder.

---

## 🛠️ Installation & Usage

### Prerequisites

iCrafts requires **Java 17** or higher to run.

### Installation Steps

1. Download the latest stable version of the plugin (e.g., `iCrafts-1.0-SNAPSHOT.jar`).
2. Place the JAR file into your server's **`/plugins/`** folder.
3. Restart your server.
4. The plugin will automatically generate all necessary configuration files in the `/plugins/iCrafts` folder.

### Recipe Creation Workflow

1. Use the command `/icrafts create <recipe_name> <type>` (e.g., `/icrafts create diamond_apple WORKBENCH`).
2. The GUI editor will open. Place the required ingredients and the final result item.
3. Click the **Save Button** (usually a Green Stained Glass Pane).
4. The recipe is instantly active on the server.

---

## 🚀 Commands & Permissions

All administrative commands require the `icrafts.admin` permission.

| Command | Usage | Permission | Description |
| :--- | :--- | :--- | :--- |
| `/icrafts help` | `/icrafts` | `icrafts.help` | Displays the help menu. |
| `/icrafts create` | `/icrafts create <name> <type>` | `icrafts.admin` | Opens the GUI to create a new custom recipe. |
| `/icrafts edit` | `/icrafts edit <name>` | `icrafts.admin` | Opens the GUI to edit an existing recipe. |
| `/icrafts view` | `/icrafts view <name>` | `icrafts.view` | Opens the GUI to view a recipe's ingredients. |
| `/icrafts delete`| `/icrafts delete <name>` | `icrafts.admin` | Permanently deletes a custom recipe. |
| `/icrafts reload`| `/icrafts reload` | `icrafts.admin` | Reloads all configuration files and recipes. |

**Available Recipe Types (`<type>`):** `WORKBENCH`, `FURNACE`, `ANVIL`.

---

## ⚙️ Configuration (`config.yml` and Menus)

The plugin configuration is split into several files for easy management.

### `config.yml`

Defines global limits and general toggles.

```yaml
# config.yml
# --- Global Plugin Settings ---
enable-recipe-book-display: true # Whether to show custom recipes in the vanilla recipe book (requires future implementation)
enable-console-logs: true        # Show INFO messages in console for recipe loading/saving.
max-custom-recipes: 500          # Maximum number of recipes allowed. Set to -1 for no limit.

menus/ Folder
This folder contains the YAML files that define the slots and decorations for the in-game editors.

Example: anvil_gui.yml structure
# menus/anvil_gui.yml
title: "&8Anvil Customizer"
size: 27

slots:
  # Interactive slots
  ANVIL_LEFT_SLOT: 11
  ANVIL_RIGHT_SLOT: 13
  ANVIL_RESULT_SLOT: 16
  ANVIL_XP_SLOT: 14 # Slot for controlling XP cost (EXPERIENCE_BOTTLE)
  SAVE_SLOT: 22

decorations:
  # Decorative slots (filler items)
  10:
    material: "ORANGE_STAINED_GLASS_PANE"
    name: " "
  12:
    material: "ANVIL"
    name: "&aCombine"
# ... and so on

Created by MrReZik
