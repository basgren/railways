package net.bitpot.railways.gui;

import javax.swing.*;

public class ErrorInfoDlg extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel errorTitleLbl;
    private JTextPane errorText;
    private JScrollPane scrollPane;


    public ErrorInfoDlg() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());
    }


    private void onOK() {
        dispose();
    }


    public static void showError(String title, String text) {
        ErrorInfoDlg dialog = new ErrorInfoDlg();

        dialog.setTitle("Routes update error");
        dialog.pack();
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(null);

        dialog.setErrorInfo(title, text);


        dialog.setVisible(true);
    }


    private void setErrorInfo(String title, String text) {
        errorTitleLbl.setText(title);
        errorText.setText(text);
        errorText.setCaretPosition(0);
    }


    public static void main(String[] args) {
        ErrorInfoDlg dialog = new ErrorInfoDlg();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
