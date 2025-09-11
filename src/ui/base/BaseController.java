package ui.base;

import engine.Engine;
import engine.S_Emulator;
import javafx.fxml.FXML;
import ui.header.HeaderController;
import ui.instructionTable.InstructionTableController;

import java.io.File;

public class BaseController {
    @FXML private HeaderController headerComponentController;
    @FXML private InstructionTableController instructionTableComponentController;
    S_Emulator s_emulator;

    @FXML
    public void initialize() {
        if(headerComponentController != null && instructionTableComponentController != null) {
            headerComponentController.setMainController(this);
            instructionTableComponentController.setMainController(this);
        }
        s_emulator = new Engine();
    }


    public void loadFile(File selectedFile) {
        boolean fileLoadedSuccessfully = s_emulator.readProgramFromXml(selectedFile.toString());
        if(fileLoadedSuccessfully){
            instructionTableComponentController.displayInstructions(s_emulator.getCommands());
        } else {
            System.out.println("WRONG FILE");
        }
    }
}
