/*
 * Copyright 2022 lll69
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lll69.analyze;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import static lll69.analyze.Utils.formatSize;

public class DetailsWindow extends JDialog implements ListSelectionListener, WindowListener {
    final JList<String> itemList;
    final JScrollPane scrollPane;
    final DirItem dirItem;
    final ArrayList<FileItem> items;

    public DetailsWindow(Window window, DirItem it) {
        super(window, it.path + FileItem.SEPARATOR + " Size:" + formatSize(it.getSize(), 3), ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(400, 800);
        dirItem = it;
        items = dirItem.fileItems;
        add(scrollPane = new JScrollPane());
        scrollPane.add(itemList = new JList<>());
        scrollPane.setViewportView(itemList);
        Collections.sort(items, Collections.reverseOrder(new Comparator<FileItem>() {
            @Override
            public int compare(FileItem o1, FileItem o2) {
                return Long.compare(o1.getSize(), o2.getSize());
            }
        }));
        itemList.setListData(toStringVector(items));
        itemList.addListSelectionListener(this);
        addWindowListener(this);
    }

    public static Vector<String> toStringVector(ArrayList<FileItem> items) {
        Vector<String> vector = new Vector<>(items.size());
        for (FileItem item : items) {
            vector.add(item.name + (item.isFile ? "" : FileItem.SEPARATOR) + "    " + formatSize(item.getSize(), 3));
        }
        return vector;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        int index = itemList.getSelectedIndex();
        if (index >= 0 && index < items.size()) {
            FileItem item = items.get(index);
            if (item instanceof DirItem) {
                DetailsWindow window = new DetailsWindow(this, (DirItem) item);
                window.setLocation(getLocation());
                window.show();
            }
        }
        itemList.removeSelectionInterval(index, index);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (dirItem.level == 0) {
            if (JOptionPane.showConfirmDialog(this, "Are you sure to exit?",
                    UIManager.getString("OptionPane.titleText"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                dispose();
            }
        } else {
            dispose();
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
