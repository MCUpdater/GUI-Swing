package org.mcupdater.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;

public class TextContextMenu extends JPopupMenu implements ActionListener {
    public static final TextContextMenu INSTANCE = new TextContextMenu();
    private final JMenuItem itemCut;
    private final JMenuItem itemCopy;
    private final JMenuItem itemPaste;
    private final JMenuItem itemDelete;
    private final JMenuItem itemSelectAll;

    private TextContextMenu() {
        itemCut = newItem("Cut", 'T');
        itemCopy = newItem("Copy", 'C');
        itemPaste = newItem("Paste", 'P');
        itemDelete = newItem("Delete", 'D');
        addSeparator();
        itemSelectAll = newItem("Select All", 'A');
    }

    private JMenuItem newItem(String text, char mnemonic) {
        JMenuItem item = new JMenuItem(text, mnemonic);
        item.addActionListener(this);
        return add(item);
    }

    @Override
    public void show(Component invoker, int x, int y) {
        JTextComponent tc = (JTextComponent)invoker;
        boolean changeable = tc.isEditable() && tc.isEnabled();
        itemCut.setVisible(changeable);
        itemPaste.setVisible(changeable);
        itemDelete.setVisible(changeable);
        super.show(invoker, x, y);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JTextComponent tc = (JTextComponent)getInvoker();
        tc.requestFocus();

        boolean haveSelection = tc.getSelectionStart() != tc.getSelectionEnd();
        if (e.getSource() == itemCut) {
            if (!haveSelection) tc.selectAll();
            tc.cut();
        } else if (e.getSource() == itemCopy) {
            if (!haveSelection) tc.selectAll();
            tc.copy();
        } else if (e.getSource() == itemPaste) {
            tc.paste();
        } else if (e.getSource() == itemDelete) {
            if (!haveSelection) tc.selectAll();
            tc.replaceSelection("");
        } else if (e.getSource() == itemSelectAll) {
            tc.selectAll();
        }
    }
}