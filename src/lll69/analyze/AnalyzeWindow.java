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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;

public class AnalyzeWindow extends JFrame {
    AnalyzeThread thread;
    final JPanel root, progressPanel, pathPanel;
    final JTextField pathField;
    final JLabel progressLabel, resultLabel;
    final JProgressBar progressBar;
    final JButton ok;
    long startTime, endTime;

    public AnalyzeWindow() {
        super("Analyze");
        setSize(512, 150);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        add(root = new JPanel());
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.add(pathPanel = new JPanel());
        pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.X_AXIS));
        pathPanel.add(pathField = new JTextField("C:/Windows"));
        root.add(progressBar = new JProgressBar());
        progressBar.setStringPainted(true);
        progressBar.setMaximum(100000);
        root.add(progressPanel = new JPanel());
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.X_AXIS));
        progressPanel.setAlignmentX(LEFT_ALIGNMENT);
        progressPanel.add(ok = new JButton("Analyze"));
        ok.setAlignmentX(LEFT_ALIGNMENT);
        progressPanel.add(progressLabel = new JLabel());
        progressPanel.add(resultLabel = new JLabel());
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = new File(pathField.getText());
                if (!file.exists() || !file.isDirectory()) {
                    JOptionPane.showMessageDialog(AnalyzeWindow.this, "The directory does not exist.");
                } else {
                    ok.setEnabled(false);
                    analyze(pathField.getText());
                }
            }
        });
    }

    public void analyze(String path) {
        startTime = System.currentTimeMillis();
        thread = new AnalyzeThread();
        thread.path = path;
        thread.start();
        new ProgressThread().start();
    }

    void finish() {
        progressLabel.setText("");
        resultLabel.setText(String.format("Size:%d,Time:%fs", thread.root.calcSize, (endTime - startTime) / 1000.));
        ok.setEnabled(true);
        DetailsWindow window = new DetailsWindow(this, thread.root);
        window.setLocationRelativeTo(null);
        window.show();
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        new AnalyzeWindow().show();
    }

    private class ProgressThread extends Thread {
        public ProgressThread() {
            super("ProgressThread");
        }

        @Override
        public void run() {
            double progress = 0;
            while (!(progress >= 1 || (thread != null && thread.size >= 0))) {
                try {
                    Thread.sleep(16);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (thread.root != null) {
                    progress = thread.root.getProgress();
                    progressBar.setValue((int) (progress * 100000));
                    progressLabel.setText(String.format("%.8f%% ", progress * 100));
                    resultLabel.setText(thread.currentFile + thread.currentType);
                }
            }
            endTime = System.currentTimeMillis();
            SwingUtilities.invokeLater(AnalyzeWindow.this::finish);
        }

    }
}
