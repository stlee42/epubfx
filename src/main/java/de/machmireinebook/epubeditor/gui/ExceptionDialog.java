package de.machmireinebook.epubeditor.gui;

import javafx.stage.Window;

/**
 * User: mjungierek
 * Date: 26.09.2017
 * Time: 21:03
 */
public class ExceptionDialog
{
    public static void showAndWait(Throwable t, Window stage, String title, String text)
    {
        org.controlsfx.dialog.ExceptionDialog exceptionDialog = new org.controlsfx.dialog.ExceptionDialog(t);
        exceptionDialog.setTitle(title);
        exceptionDialog.setHeaderText(null);
        exceptionDialog.setContentText(text);
        exceptionDialog.initOwner(stage);
        exceptionDialog.showAndWait();
    }
}
